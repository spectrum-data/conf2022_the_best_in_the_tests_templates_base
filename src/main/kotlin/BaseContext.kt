import models.TestDesc
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant

abstract class BaseContext {
    /**
     * Access_token для доступа к списку форков репозиториев
     * */
    val token by lazy {
        "ghp_" + System.getenv().getOrDefault("TOKEN", "")
    }

    /**
     * Токен для телеграм-бота
     * */
    val telegramToken by lazy {
        System.getenv().getOrDefault("TELEGRAM_TOKEN", "")
    }

    /**
     * Идентификаторы чатов - куда отправлять сообщения об ошибке
     * */
    val telegramChatIds by lazy {
        System.getenv().get("TELEGRAM_CHAT_IDS")?.split(";")?.map { it.toLong() } ?: emptyList()
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
     * Директория для сохранения
     * */
    abstract val dirToSave: File

    /**
     * Символ-разделитель, который используется в локальных файлах (будет также использован в общем)
     * */
    open val delimiter by lazy {
        System.getenv().getOrDefault("DELIMITER", "|")
    }

    /**
     * Отправка сообщения в телеграм бот
     * */
    fun sendToTelegramBot(message: String) {
        try {
            telegramChatIds.forEach { chatId ->
                val request = HttpRequest
                    .newBuilder()
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"chat_id\": \"${chatId}\", \"text\": \"${message}\", \"disable_notification\": true}"))
                    .uri(
                        URI("https://api.telegram.org/bot$telegramToken/sendMessage")
                    )
                    .build()

                val response = HttpClient.newHttpClient()
                    .send(
                        request, HttpResponse.BodyHandlers.ofString()
                    )

                println("${response.body()}")
            }
        } catch (t: Throwable) {
            println("При попытке отправки сообщения в телеграм бот - возникла ошибка. Сообщение $message")
        }
    }
}

/**
 * Контекст объединения файлов с описаниями тестов
 * */
class ConcatContext(
    _overrideMainFile: File? = null,
    _overrideDirToSave: File? = null,
    _overrideRepos: List<String>? = null,
    _overrideStartAt: Instant? = null,
    _overrideLocalFileName: String? = null,
    /**
     * Признак того, что требуется загрузкаы
     */
    val doDownloadLocal: Boolean = true,
    val doSendToTelegram: Boolean = true,
) : BaseContext() {

    val currentMainFile: File by lazy {
        _overrideMainFile ?: File(projectDir, mainFileName)
    }

    fun reportError(message: String) {
        println("!! ${message}")
        if (doSendToTelegram) {
            sendToTelegramBot(message)
        }
    }

    /**
     * Директория для сохранения копии файлов с локальными тестами
     * */
    override val dirToSave: File by lazy {
        _overrideDirToSave ?: File(projectDir, "locals").also { it.mkdir() }
    }

    /**
     * Список репозиториев-шаблонов - форки данных репозиториев и будут обходиться
     * */
    val repos: List<String> by lazy {
        _overrideRepos ?: System.getenv()
            .getOrDefault("REPOS", "spectrum-data/conf2022_the_best_in_the_tests_templates_kotlin")
            .takeIf { it.isNotBlank() }?.split(";")?.map { it.trim() }
        ?: emptyList()
    }

    /**
     * Время запуска скрипта по объединению файлов с описанями тестов
     * */
    val createdAt = _overrideStartAt ?: Instant.now()

    /**
     * Название файла, содержащего локальные тесты (именно сбор этих файлов будет происходить)
     * */
    val localFileName: String by lazy {
        _overrideLocalFileName ?: System.getenv().getOrDefault("LOCAL_FILE_NAME", "local.csv")
    }
}

open class RunAndCalculateContext : BaseContext() {
    /**
     *
     * */
    override val dirToSave: File by lazy {
        File(projectDir, "result").also { it.mkdir() }
    }

    /**
     * Директория с файлами отчетов по выполненным тестам
     * */
    open val reportsDir by lazy {
        File(projectDir, "reports").also { it.mkdirs() }
    }

    /**
     * Название файла отчета
     * */
    val reportFileName by lazy {
        System.getenv().getOrDefault("REPORT_FILE_NAME", "report.md")
    }

    /**
     * Название репозитория-шаблона на Kotlin
     * */
    val kotlinRepo by lazy {
        System.getenv().getOrDefault("KOTLIN_REPO", "spectrum-data/conf2022_the_best_in_the_tests_templates_kotlin")
    }

    /**
     * Название репозитория-шаблона на GO
     * */
    val goRepo by lazy {
        System.getenv().getOrDefault("GO_REPO", "spectrum-data/conf2022_the_best_in_the_tests_templates_go")
    }

    /**
     * Время начала соревнований
     * */
    open val startAt: Instant by lazy {
        System.getenv().getOrDefault("START_TIME", "2022-11-10T10:25:43.456891Z").let { Instant.parse(it) }
    }

    /**
     * Набор описаний тестов - полученный из общего файла main.csv
     * */
    var testDescs = listOf<TestDesc>()
}