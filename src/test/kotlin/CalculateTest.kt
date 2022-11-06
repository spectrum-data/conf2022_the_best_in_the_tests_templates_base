import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import models.TestDescParser
import java.io.File
import java.time.Instant
import java.time.LocalTime

class CalculateTest : FunSpec() {
    init {
        context("парсинг") {
            test("парсит корректные файлы") {
                val context = object : RunAndCalculateContext() {
                    override val startAt: Instant = Instant.parse("2022-01-01T00:00:00.000Z")

                    override val reportsDir: File
                        get() = File(projectDir, "src/test/resources/reports")

                    override val delimiter: String = "|"
                }

                val file = File(context.projectDir, "src/test/resources/main.csv")
                val parseMainResult = TestDescParser.parse(file)
                parseMainResult.isOk.shouldBeTrue()


                val parseUserResult = collectUserResults(context)

                println(parseUserResult)
            }
        }
    }
}
