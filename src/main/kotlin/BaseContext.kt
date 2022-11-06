import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import models.TestDesc
import java.io.File
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

abstract class BaseContext {
    /**
     * Access_token для доступа к списку форков репозиториев
     * */
    val token by lazy {
        System.getenv().getOrDefault("TOKEN", "")
    }

    /**
     * Токен для телеграм-бота
     * */
    val telegramToken by lazy {
        System.getenv().getOrDefault("TELEGRAM_TOKEN", "")
    }

    /**
     * Экземпляр телеграм-бота
     * */
    val telegramBot by lazy {
        bot {
            token = telegramToken
        }
    }

    /**
     * Идентификаторы чатов - куда отправлять сообщения об ошибке
     * */
    val telegramChatIds by lazy {
        System.getenv().get("TELEGRAM_TOKEN")?.split(";")?.map { it.toLong() } ?: emptyList()
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
                telegramBot.sendMessage(
                    ChatId.fromId(chatId),
                    message
                )
            }
        } catch (t: Throwable) {
            println("При попытке отправки сообщения в телеграм бот - возникла ошибка. Сообщение $message")
        }
    }
}

/**
 * Контекст объединения файлов с описаниями тестов
 * */
class ConcatContext : BaseContext() {
    /**
     * Директория для сохранения копии файлов с локальными тестами
     * */
    override val dirToSave by lazy {
        File(projectDir, "locals").also { it.mkdir() }
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
    val createdAt = Instant.now()

    /**
     * Название файла, содержащего локальные тесты (именно сбор этих файлов будет происходить)
     * */
    val localFileName by lazy {
        System.getenv().getOrDefault("LOCAL_FILE_NAME", "local.csv")
    }

    /**
     * Заголовок файла с описаниями тестов - будет добавлен при создании
     * */
    val mainHeader by lazy {
        System.getenv().getOrDefault("MAIN_HEADER", "")
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
        System.getenv().getOrDefault("START_TIME", "").let { Instant.parse(it) }
    }

    /**
     * Набор описаний тестов - полученный из общего файла main.csv
     * */
    var testDescs = listOf<TestDesc>()
}