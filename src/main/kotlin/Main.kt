import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStreamWriter
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

/**
 * Класс для описания форков репозиториев-шаблонов
 * */
@Serializable
data class ForkInfo(
    /**
     * URL форк-репозитория
     * */
    @SerialName("html_url")
    val url: String = "",

    @SerialName("owner")
    val owner: Owner? = null
)

@Serializable
data class Owner(
    @SerialName("login")
    val login: String = ""
)

/**
 * Контекст объединения файлов с описаниями тестов
 * */
class ConcaterContext {

    /**
     * Access_token для доступа к списку форков репозиториев
     * */
    val token by lazy {
        System.getenv().getOrDefault("TOKEN", "")
    }

    /**
     * Список репозиториев-шаблонов - форки данных репозиториев и будут обходиться
     * */
    val repos by lazy {
        System.getenv().getOrDefault("REPOS", "spectrum-data/conf2022_the_best_in_the_tests_templates_kotlin")
            .takeIf { it.isNotBlank() }?.split(";")?.map { it.trim() }
            ?: emptyList()
    }

    /**
     * Время запуска скрипта по объединению файлов с описанями тестов
     * */
    val createdAt = LocalTime.now(ZoneId.of("Asia/Yekaterinburg"))

    /**
     * Название файла, содержащего локальные тесты (именно сбор этих файлов будет происходить)
     * */
    val localFileName by lazy {
        System.getenv().getOrDefault("LOCAL_FILE_NAME", "local.csv")
    }

    /**
     * Название результирующего/общего файла
     * */
    val mainFileName by lazy {
        System.getenv().getOrDefault("MAIN_FILE_NAME", "main.csv")
    }

    /**
     * Корневая директория проекта
     * */
    val projectDir by lazy {
        File(System.getProperty("user.dir"))
    }

    /**
     * Директория для сохранения копии файлов с локальными тестами
     * */
    val dirToSave by lazy {
        File(projectDir, "locals").also { it.mkdir() }
    }

    /**
     * Заголовок файла с описаниями тестов - будет добавлен при создании
     * */
    val mainHeader by lazy {
        System.getenv().getOrDefault("MAIN_HEADER", "")
    }

    /**
     * Символ-разделитель, который используется в локальных файлах (будет также использован в общем)
     * */
    val delimiter by lazy {
        System.getenv().getOrDefault("DELIMITER", "|")
    }
}

fun main(args: Array<String>) {
    val context = ConcaterContext()

    clearLocals(context)

    context.repos.forEach { repo ->
        getForksInfo(repo, context.token).forEach { info ->
            downloadLocal(info, context)
        }
    }

    makeMain(context)
}

/**
 * Очищает папку с копиями локальных файлов
 * */
private fun clearLocals(context: ConcaterContext) {
    context.dirToSave.listFiles().forEach { localDir ->
        localDir.deleteRecursively()
    }
}

/**
 * Собственно объединение скаченных локальных файлов в общий файл
 * Также добавляет время создания записи о тесте
 * */
private fun makeMain(context: ConcaterContext): File {
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

private fun downloadLocal(info: ForkInfo, context: ConcaterContext): File? {
    if (info.owner != null) {
        val ownerDir = File(context.dirToSave, info.owner.login).also { it.mkdir() }

        val tempDir = createTempDirectory().toFile().also { it.mkdirs() }

        val processStarted = ProcessBuilder("git", "clone", info.url.pasteToken(context.token), tempDir.absolutePath)
            .redirectErrorStream(true)
            .start().also { processStarted ->
                println(processStarted.inputStream.reader().use { it.readText() })
            }

        processStarted.waitFor(3, TimeUnit.SECONDS)

        File(tempDir, "${context.localFileName}").also {
            if (it.exists()) {
                it.copyTo(File(context.dirToSave, "${info.owner!!.login}/${context.localFileName}"), true)
            }
        }
    }

    return null
}

private fun getForksInfo(baseRepo: String, token: String): List<ForkInfo> {
    val request = HttpRequest
        .newBuilder()
        .GET()
        .uri(URI.create("https://api.github.com/repos/$baseRepo/forks"))
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .build()

    println("******GETTING FORKS REQUEST:${request.uri().toASCIIString()}")

    val response = HttpClient.newHttpClient()
        .send(
            request, HttpResponse.BodyHandlers.ofString()
        )

    println("******GETTING FORKS RESPONSE:${response.body()}")

    return Json { ignoreUnknownKeys = true }.decodeFromString<List<ForkInfo>>(response.body())
}

private fun String.pasteToken(token: String): String {
    require(this.startsWith("https://"))

    val afterHttps = this.substringAfter("https://", "")

    return buildString {
        append("https://")
        append("$token@")
        append(afterHttps)
    }
}