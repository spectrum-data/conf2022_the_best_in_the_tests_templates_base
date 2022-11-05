import java.io.File
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
    val createdAt = LocalTime.now(ZoneId.of("Asia/Yekaterinburg"))

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

    /**
     * Символ-разделитель, который используется в локальных файлах (будет также использован в общем)
     * */
    val delimiter by lazy {
        System.getenv().getOrDefault("DELIMITER", "|")
    }
}

class RunTestsContext : BaseContext() {
    /**
     *
     * */
    override val dirToSave: File by lazy {
        File(projectDir, "result").also { it.mkdir() }
    }

    /**
     * Директория с файлами отчетов по выполненным тестам
     * */
    val reportsDir by lazy {
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
}