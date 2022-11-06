import models.TestDesc
import models.UserResult
import java.io.File

/**
 * Подсчет результатов
 * */
fun calculate() {
    val context = RunAndCalculateContext()

    parseMain(context)

    val collectUserResults = collectUserResults(context)
}

fun parseMain(context: RunAndCalculateContext) {
    val mainFile = File(context.projectDir, context.mainFileName)

    context.testDescs = TestDesc.parseFile(mainFile, context)
}

fun collectUserResults(context: RunAndCalculateContext): List<UserResult> {
    return context.reportsDir.listFiles()
        .flatMap { userDir -> userDir.listFiles().toList() }
        .map { fileReport -> UserResult.parseFromReport(context, fileReport.parentFile.name, fileReport) }
}
