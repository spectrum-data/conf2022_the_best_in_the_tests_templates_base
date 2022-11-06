import com.github.kotlintelegrambot.entities.ChatId
import java.io.File
import java.io.OutputStreamWriter

/**
 * Запускает объединение файлов с описаниями тестов
 * */
fun concat() {
    val context = ConcatContext()

    clearLocals(context)

    context.repos.forEach { repo ->
        getForksInfo(repo, context.token).forEach { fork ->
            try {
                downloadLocal(fork, context)
            } catch (t: Throwable) {
                context.sendToTelegramBot("При попытке скачивания локальных файлов с репозитория ${fork.url} произошла ошибка. ${t.message} ${t.stackTrace}")
            }
        }
    }

    makeMain(context)
}

/**
 * Очищает папку с копиями локальных файлов
 * */
private fun clearLocals(context: ConcatContext) {
    context.dirToSave.listFiles().forEach { localDir ->
        localDir.deleteRecursively()
    }
}

/**
 * Собственно объединение скаченных локальных файлов в общий файл
 * Также добавляет время создания записи о тесте
 * */
private fun makeMain(context: ConcatContext): File {
    val existedMain = File(context.projectDir, context.mainFileName)

    val testHashToTimeMap = existedMain.takeIf { it.exists() }
        ?.let { mainFile -> mainFile.useLines { it.drop(1).toList() } }
        ?.map { line -> line.split(context.delimiter).map { it.trim() } }
        ?.associate { splitLine -> "${splitLine[0]}${splitLine[2]}".hashCode() to splitLine.getOrNull(5) }

    fun OutputStreamWriter.addTimeAndAppendLine(line: String) {
        val currentLineHash = line.split(context.delimiter).map { it.trim() }
            .let { splitLine -> "${splitLine[0]}${splitLine[2]}".hashCode() }

        val createdAt = testHashToTimeMap?.get(currentLineHash)?.takeIf { it.isNotBlank() }
            ?: context.createdAt.toString()

        val lineWithAddedTime = buildList {
            add(line)

            if (!line.trim().endsWith(context.delimiter)) {
                add(context.delimiter)
            }

            add(createdAt)
        }.joinToString("")

        appendLine(lineWithAddedTime)
    }

    return File(context.projectDir, context.mainFileName).also {
        it.createNewFile()

        it.writer().use { writer ->
            writer.write("${context.mainHeader}\n")

            context.dirToSave.listFiles().forEach { dirWithLocal ->
                dirWithLocal.listFiles().forEach { localFile ->
                    localFile.useLines { lines -> lines.drop(1).forEach { line -> writer.addTimeAndAppendLine(line) } }
                }
            }
        }
    }
}

private fun downloadLocal(info: ForkInfo, context: ConcatContext): File? {
    val ownerDir = File(context.dirToSave, info.owner.login).also { it.mkdir() }

    val dirWithProject = gitCloneToTemp(info, context.token)

    File(dirWithProject, "${context.localFileName}").also {
        if (it.exists()) {
            it.copyTo(File(ownerDir, "${context.localFileName}"), true)
        }
    }

    return null
}

