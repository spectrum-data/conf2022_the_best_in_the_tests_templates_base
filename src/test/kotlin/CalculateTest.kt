import io.kotest.core.spec.style.FunSpec
import models.TestDesc
import java.io.File
import java.time.LocalTime

class CalculateTest : FunSpec() {
    init {
        context("парсинг") {
            test("парсит корректные файлы") {
                val context = object : RunAndCalculateContext() {
                    override val startAt: LocalTime = LocalTime.parse("00:00:00")

                    override val reportsDir: File
                        get() = File(projectDir, "src/test/resources/reports")

                    override val delimiter: String = "|"
                }
                context.testDescs = File(context.projectDir, "src/test/resources/main.csv").useLines { seq ->
                    seq.drop(1).map { TestDesc.parse(it, context) }.toList()
                }

                val parseResult = collectUserResults(context)

                println(parseResult)
            }
        }
    }
}
