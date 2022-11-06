import models.TestDesc
import models.TestDescParser
import java.io.File

/**
 * Запускает объединение файлов с описаниями тестов
 * */
fun concat(context: ConcatContext = ConcatContext()) {
    if(context.doDownloadLocal) {
        clearLocals(context)
        context.repos.forEach { repo ->
            getForksInfo(repo, context.token).forEach { fork ->
                try {
                    downloadLocal(fork, context)
                } catch (t: Throwable) {
                    context.reportError( "При попытке скачивания локальных файлов с репозитория ${fork.url} произошла ошибка. ${t.message} ${t.stackTrace}")
                }
            }
        }
    }

    val currentMainParseResult = TestDescParser.parse(context.currentMainFile)
    if (!currentMainParseResult.isOk) {
        context.reportError("Проблема при чтении локального main, новый main не выпускается. ${currentMainParseResult.error}")
        return // дальше нельзя собирать
    }
    val userTests = mutableListOf<TestDesc>()
    val dirs = context.dirToSave.listFiles().filter { it.isDirectory }
    for (forkDir in dirs) {
        val author = forkDir.name
        val file = File(forkDir, context.localFileName)
        val time = context.createdAt
        val testParseResult = TestDescParser.parse(file, TestDescParser.Options(author, time))
        if (testParseResult.isOk) {
            userTests.addAll(testParseResult.data)
        } else {
            context.reportError("При парсинге тестов ${author} произошла ошибка. ${testParseResult.error}")
        }
    }

    val currentMainMap = currentMainParseResult.data.associateBy { it.bizKey }.toMutableMap()
    val currentUserMap = userTests.associateBy { it.bizKey }.toMutableMap()
    val resolvedTests = mutableListOf<TestDesc>()
    // обрабатываем имеющиеся тесты
    currentMainMap.entries.forEach { existedEntry ->
        val existed = existedEntry.value
        // чтобы в карте пользовательских остались только новые
        val userLocal = currentUserMap.get(existed.bizKey)
        currentMainMap.remove(existed.bizKey)
        resolvedTests.add(existed.merge(userLocal))
    }
    // все тут остались только новые, добавляем как есть
    resolvedTests.addAll(currentUserMap.values)
    writeNewMainFile(context, resolvedTests)

    context.reportError("Обновленный файл с меткой времени ${context.createdAt} успешно сохранен!")
}

fun writeNewMainFile(context: ConcatContext, tests: List<TestDesc>) {
    val file = File(context.projectDir, context.mainFileName)
    file.writer().use { w ->
        w.appendLine(TestDesc.csvHeader)
        tests.sortedWith(compareBy<TestDesc> { it.author }.thenBy { it.publishTime }).forEach { t ->
            w.appendLine(t.toCsvString())
        }
    }

}

/**
 * Очищает папку с копиями локальных файлов
 * */
private fun clearLocals(context: ConcatContext) {
    context.dirToSave.listFiles().forEach { localDir ->
        localDir.deleteRecursively()
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

