import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
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
    val localFileName by lazy {
        System.getenv().getOrDefault("LOCAL_FILE_NANE", "local.csv")
    }

    val dirToSave by lazy {
        File(System.getProperty("user.dir"), "locals").also { it.mkdir() }
    }
}

fun main(args: Array<String>) {
    val token = System.getenv().getOrDefault("GITHUB_TOKEN", "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")
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
    return File(context.dirToSave.parent, "main.csv").also {
        it.createNewFile()

        it.writer().use { writer ->
            context.dirToSave.listFiles().forEach { dirWithLocal ->
                dirWithLocal.listFiles().forEach { localFile ->
                    localFile.useLines { lines -> lines.drop(1).forEach { line -> writer.appendLine(line) } }
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

        processStarted.waitFor(5, TimeUnit.SECONDS)

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