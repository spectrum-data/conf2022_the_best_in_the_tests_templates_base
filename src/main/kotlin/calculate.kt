import models.TestDescParser
import models.UserResult
import java.io.File
import java.io.OutputStreamWriter

/**
 * Подсчет результатов
 * */
fun calculate() {
    val context = RunAndCalculateContext()

    parseMain(context)

    val userResults = getUserResult(context)

    userResults.forEach { userResult ->
        userResults.minus(userResult).forEach { otherUserResult ->
            userResult.fillShotInfo(context, otherUserResult)
        }
    }

    buildReport(context, userResults)
}

/**
 * Парсинг общего файла с описаниями тестов
 * */
fun parseMain(context: RunAndCalculateContext) {
    val mainFile = File(context.projectDir, context.mainFileName)

    val mainFileParseResult = TestDescParser.parse(mainFile)
    if (mainFileParseResult.isOk) {
        context.testDescs = mainFileParseResult.data
    } else {
        throw Exception("Error parse main data: ${mainFileParseResult.error}")
    }
}

/**
 * Получение описаний результатов тестов участников
 * */
fun getUserResult(context: RunAndCalculateContext): List<UserResult> {
    return context.reportsDir.listFiles()
        .flatMap { userDir -> userDir.listFiles().toList() }
        .map { fileReport -> UserResult.parseFromReport(context, fileReport.parentFile.name, fileReport) }
}

/**
 * Построение финального отчета с результатами
 * */
fun buildReport(context: RunAndCalculateContext, userResults: List<UserResult>) {
    fun OutputStreamWriter.appendUserResult(userResult: UserResult) {
        val allUserTests = context.testDescs.filter { it.author.lowercase() == userResult.login.lowercase() }

        val averageTestPublishTime = allUserTests.map { it.getTestPublishMinutes(context) }.average()

        appendLine(
            buildString {
                append("|")
                append(
                    listOf(
                        userResult.login,
                        userResult.isPassBase.toString(),
                        allUserTests.count().toString(),
                        userResult.ownTests.count().toString(),
                        userResult.ownTests.count { it.isTestPass }.toString(),
                        userResult.firedShots.count { it.isSuccessful }.toString(),
                        userResult.receivedShots.count{it.isSuccessful}.toString(),
                        userResult.score.toString(),
                        averageTestPublishTime.toString(),
                    ).joinToString("|")
                )
                append("|")
            }
        )
    }

    File(context.projectDir, "final_report.md")
        .also { it.createNewFile() }
        .writer().use { writer ->
            writer.appendLine("|Участник|Прошел ли базовые тесты|Всего тестов|Активных|Выполнено своих тестов|Попадания|Пропущенных|Балл|Ср. время от начала соревнований на публикацию теста (мин)|")
            writer.appendLine("|--------|--------|--------|--------|--------|--------|--------|--------|--------|")

            userResults.sortedWith(compareBy<UserResult> { it.isPassBase }.thenByDescending { it.score })
                .forEach { userResult ->
                    writer.appendUserResult(userResult)
                }
        }

}