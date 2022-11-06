import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import models.TestDesc
import models.TestDescParser
import java.time.Instant

internal class TestDescParserTest : FunSpec() {
    init {
        val time =  Instant.parse("2022-11-09T11:01:22.123567Z")
        val test1 = TestDesc(
            "some_author",
            "1234567890",
            "==PASSPORT_RF:1234567890",
            false,
            "some comment 1",
            time,
        )
        val test2 = TestDesc(
            "some_author",
            "6511111111",
            "==INN_FL:1111111111",
            false,
            "some comment 2",
            time,
        )

        context("обычные кейсы") {
            test("чтение полного и нормального файла") {
                val content = listOf(
                    TestDesc.csvHeader,
                    test1.toCsvString(),
                    test2.toCsvString()
                ).joinToString("\n").also { println(it) }
                TestDescParser.parse(content).also {
                    it.error.shouldBe(TestDescParser.Error.NoError)
                    it.isOk.shouldBeTrue()
                    it.data.shouldBe(listOf(test1, test2))
                }
            }
            test("чтение локального и нормального файла") {
                val content = listOf(
                    test1.toLocalString(),
                    test2.toLocalString()
                ).joinToString("\n").also { println(it) }
                TestDescParser.parse(
                    content, options = TestDescParser.Options(
                        "some_author",
                        time
                    )
                ).also {
                    it.error.shouldBe(TestDescParser.Error.NoError)
                    it.isOk.shouldBeTrue()
                    it.data.shouldBe(listOf(test1, test2))
                }
            }
        }

        context("relax кейсы"){
            test("разрешены пустые строки в перемешку с комментариями "){
                val content = listOf(
                    "# а вот и наш main",
                    "",
                    "",
                    TestDesc.csvHeader,
                    "",
                    " # а вот и тесты нашего молодца",
                    test1.toCsvString(),
                    "","",
                    test2.toCsvString(),
                    ""
                ).joinToString("\n").also { println(it) }
                TestDescParser.parse(content).also {
                    it.error.shouldBe(TestDescParser.Error.NoError)
                    it.isOk.shouldBeTrue()
                    it.data.shouldBe(listOf(test1, test2))
                }
            }
            test("в локальных тестах можно пропускать `==`"){
                val content = listOf(
                    "1111111111 -> PASSPORT_RF:1111111111",
                    "2222222222 -> ==PASSPORT_RF:2222222222",
                    "3333333333 -> ~=PASSPORT_RF:3333333333",
                ).joinToString("\n").also { println(it) }
                TestDescParser.parse(content, options = TestDescParser.Options(author = "x")).also {
                    it.error.shouldBe(TestDescParser.Error.NoError)
                    it.data[0].expected shouldBe "==PASSPORT_RF:1111111111"
                    it.data[1].expected shouldBe "==PASSPORT_RF:2222222222"
                    it.data[2].expected shouldBe "~=PASSPORT_RF:3333333333"
                }
            }
        }

        context("ошибки"){
            test("неведомый локальный формат"){
                TestDescParser.parse("""
                    я хочу == так писать тесты
                """.trimIndent()).also {
                    it.isOk.shouldBeFalse()
                    it.error.shouldBe(TestDescParser.Error.CannotDetektFormat)
                }
            }
            test("неведомый полный формат, сбитый CSV"){
                TestDescParser.parse(TestDesc.csvHeader.replace("author","shmauthor")).also {
                    it.isOk.shouldBeFalse()
                    it.error.shouldBe(TestDescParser.Error.CannotDetektFormat)
                }
            }
            test("завалена одна из строк") {
                TestDescParser.parse(listOf(
                    TestDesc.csvHeader,
                    test1.toCsvString(),
                    // вот тут полом
                    test2.toCsvString().replaceFirst("|","~"),
                    test1.copy(author = "y").toCsvString()
                ).joinToString("\n")).also {
                    it.isOk.shouldBeFalse()
                    it.error.shouldBe(TestDescParser.Error.InvalidLineStruct(2))
                    // тем не менее что-то было прочитано
                    it.data.shouldContain(test1)
                }
            }

            test("неправильное условие") {
                TestDescParser.parse(listOf(
                    TestDesc.csvHeader,
                    test1.toCsvString(),
                    // вот тут полом
                    test2.copy(expected = "==труляля:1111").toCsvString(),
                    test1.copy(author = "y").toCsvString()
                ).joinToString("\n")).also {
                    it.isOk.shouldBeFalse()
                    (it.error as TestDescParser.Error.InvalidExpectedSyntax).also {
                        it.line shouldBe 2
                        it.message shouldBe "Не правильный синтаксис проверки 2 IllegalArgumentException: No enum constant DocType.ТРУЛЯЛЯ"
                    }
                    // тем не менее что-то было прочитано
                    it.data.shouldContain(test1)
                }
            }
        }
    }
}