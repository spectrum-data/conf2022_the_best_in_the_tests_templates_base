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

@Serializable
data class Owner(
    @SerialName("login")
    val login: String = ""
)

@Serializable
data class ForkInfo(
    @SerialName("html_url")
    val url: String = "",

    @SerialName("name")
    val projectName: String = "",

    @SerialName("owner")
    val owner: Owner? = null
)

data class ConcaterContext(
    val token: String,

    ) {

    val createdAt = LocalTime.now(ZoneId.of("Asia/Yekaterinburg"))

    val localFileName by lazy {
        System.getenv().getOrDefault("LOCAL_FILE_NAME", "local.csv")
    }

    val mainFileName by lazy {
        System.getenv().getOrDefault("MAIN_FILE_NAME", "main.csv")
    }

    val projectDir by lazy {
        File(System.getProperty("user.dir"))
    }

    val dirToSave by lazy {
        File(projectDir, "locals").also { it.mkdir() }
    }

    val mainHeader by lazy {
        System.getenv().getOrDefault("MAIN_HEADER", "")
    }

    val delimiter by lazy {
        System.getenv().getOrDefault("DELIMITER", "|")
    }
}

fun main(args: Array<String>) {
    val token = System.getenv().getOrDefault("TOKEN", "")
    val repos = System.getenv().getOrDefault("REPOS", "spectrum-data/conf2022_the_best_in_the_tests_templates_kotlin")
        .takeIf { it.isNotBlank() }?.split(";")?.map { it.trim() }
        ?: emptyList()

    val context = ConcaterContext(token = token)

    clearLocals(context)

    repos.forEach { repo ->
        println("********CURRENT REPOSITORY: $repo")

        getForksInfo(repo, token).forEach { info ->
            downloadLocal(info, context)
        }
    }

    makeMain(context)
}

private fun clearLocals(context: ConcaterContext) {
    context.dirToSave.listFiles().forEach { localDir ->
        localDir.deleteRecursively()
    }
}

private fun makeMain(context: ConcaterContext): File {
    val existedMain = File(context.projectDir, context.mainFileName)

    val testToTimeMap = existedMain.takeIf { it.exists() }
        ?.let { mainFile -> mainFile.useLines { it.drop(1).toList() } }
        ?.map { line -> line.split(context.delimiter) }
        ?.associate { splitLine -> "${splitLine[0]}${splitLine[2]}".hashCode() to splitLine.getOrNull(5) }

    fun OutputStreamWriter.addTimeAndAppendLine(line: String) {
        val hash = line.split(context.delimiter).let { splitLine -> "${splitLine[0]}${splitLine[2]}".hashCode() }

        val time = testToTimeMap?.getOrDefault(hash, null)?.takeIf { it.isNotBlank() }
            ?: context.createdAt.toString()

        this.appendLine(listOf(line, context.delimiter, time).joinToString(""))
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