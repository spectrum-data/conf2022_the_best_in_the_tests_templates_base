import models.TestDesc
import models.TestDescParser
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
    val mainFileParseResult = TestDescParser.parse(mainFile)
    if(mainFileParseResult.isOk) {
        context.testDescs = mainFileParseResult.data
    }else{
        throw Exception("Error parse main data: ${mainFileParseResult.error}")
    }
}

fun collectUserResults(context: RunAndCalculateContext): List<UserResult> {
    return context.reportsDir.listFiles()
        .flatMap { userDir -> userDir.listFiles().toList() }
        .map { fileReport -> UserResult.parseFromReport(context, fileReport.parentFile.name, fileReport) }
}
