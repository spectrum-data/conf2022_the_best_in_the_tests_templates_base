import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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
    val owner: Owner = Owner()
)

@Serializable
data class Owner(
    @SerialName("login")
    val login: String = ""
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Получение информации о форк-репозиториях
 * */
fun getForksInfo(baseRepo: String, token: String): List<ForkInfo> {
    val request = HttpRequest
        .newBuilder()
        .GET()
        .uri(URI.create("https://api.github.com/repos/$baseRepo/forks"))
        .header("Accept", "application/vnd.github+json")
        .header("Authorization", "Bearer $token")
        .build()

    val response = HttpClient.newHttpClient()
        .send(
            request, HttpResponse.BodyHandlers.ofString()
        )

    println("******GETTING FORKS RESPONSE:${response.body()}")

    return json.decodeFromString(response.body())
}

/**
 * Клонирует проект указанного форка во временную директорию
 * */
fun gitCloneToTemp(forkInfo: ForkInfo, token: String): File {
    val tempDir = createTempDirectory().toFile().also { it.mkdirs() }

    val processStarted = ProcessBuilder("git", "clone", forkInfo.url.pasteToken(token), tempDir.absolutePath)
        .redirectErrorStream(true)
        .start().also { processStarted ->
            println(processStarted.inputStream.reader().use { it.readText() })
        }

    processStarted.waitFor(3, TimeUnit.SECONDS)

    return tempDir
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
