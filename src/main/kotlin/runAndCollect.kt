import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Запуск тестов и сбор отчетов
 * */
fun runAndCollect() {
    val context = RunAndCalculateContext()

    val kotlinForks = emptyList<ForkInfo>()//getForksInfo(baseRepo = context.kotlinRepo, token = context.token)
    val goForks = getForksInfo(baseRepo = context.goRepo, token = context.token)

    kotlinForks.forEach { kotlinFork ->
        val projectDir = gitCloneToTemp(forkInfo = kotlinFork, token = context.token)

        runTestsKotlin(projectDir)

        collectReport(kotlinFork, projectDir, context)
    }

    goForks.forEach { goFork ->
        val projectDir = gitCloneToTemp(forkInfo = goFork, token = context.token)

        runTestsGo(projectDir)

        collectReport(goFork, projectDir, context)
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
        }
    }
}

