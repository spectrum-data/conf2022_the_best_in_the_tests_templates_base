import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Запуск тестов и сбор отчетов
 * */
fun runAndCollect() {
    val context = RunAndCalculateContext()

    val kotlinForks = getForksInfo(baseRepo = context.kotlinRepo, token = context.token)
    val goForks = getForksInfo(baseRepo = context.goRepo, token = context.token)

    kotlinForks.forEach { kotlinFork ->
        val projectDir = runCatching { gitCloneToTemp(forkInfo = kotlinFork, token = context.token) }

        if (projectDir.isFailure) {
            context.sendToTelegramBot("Kotlin: Не удалось склонировать репозиторий участника ${kotlinFork.owner}")
        } else {
            val projectDirResult = projectDir.getOrThrow()

            runCatching { runTestsKotlin(projectDirResult) }
                .getOrElse { context.sendToTelegramBot("Kotlin: Не удалось выполнить тесты участника ${kotlinFork.owner}. Ошибка: ${it.message}") }

            runCatching { collectReport(kotlinFork, projectDirResult, context) }
                .getOrElse { context.sendToTelegramBot("Kotlin: Не собрать отчет о выполнении тестов участника ${kotlinFork.owner}. Ошибка: ${it.message}") }
        }
    }

    goForks.forEach { goFork ->
        val projectDir = runCatching { gitCloneToTemp(forkInfo = goFork, token = context.token) }

        if (projectDir.isFailure) {
            context.sendToTelegramBot("GO: Не удалось склонировать репозиторий участника ${goFork.owner}")
        } else {
            val projectDirResult = projectDir.getOrThrow()

            runCatching { runTestsGo(projectDirResult) }
                .getOrElse { context.sendToTelegramBot("GO: Не удалось выполнить тесты участника ${goFork.owner}. Ошибка: ${it.message}") }

            runCatching { collectReport(goFork, projectDirResult, context) }
                .getOrElse { context.sendToTelegramBot("GO: Не собрать отчет о выполнении тестов участника ${goFork.owner}. Ошибка: ${it.message}") }
        }
    }
}

private fun runTestsKotlin(projectDir: File) {
    val processStarted = ProcessBuilder("./gradlew", "test", "--tests", "*MainTest")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start().also { processStarted ->
            println(processStarted.inputStream.reader().use { it.readText() })
        }

    processStarted.waitFor(20, TimeUnit.SECONDS)
}

private fun runTestsGo(projectDir: File) {
    val processStarted = ProcessBuilder("go", "test", "Main_test.go")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start().also { processStarted ->
            println(processStarted.inputStream.reader().use { it.readText() })
        }

    processStarted.waitFor(20, TimeUnit.SECONDS)
}

private fun collectReport(forkInfo: ForkInfo, projectDir: File, context: RunAndCalculateContext) {
    val ownerDir = File(context.reportsDir, forkInfo.owner.login).also { it.mkdir() }

    File(projectDir, context.reportFileName).also {
        if (it.exists()) {
            it.copyTo(File(ownerDir, context.reportFileName), true)
        } else {
            error("Для форки Owner: ${forkInfo.owner} Url: ${forkInfo.url} не удалось получить файл с отчетом. Проверить выполнение тестов локально.")
        }
    }
}

