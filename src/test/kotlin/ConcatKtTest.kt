import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.TestDesc
import models.TestDescParser
import java.io.File
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

internal class ConcatKtTest : FunSpec() {
    val time = Instant.parse("2022-11-09T10:00:00.000Z")
    val ctx = ConcatContext(
        _overrideMainFile = File("tmp/main.csv").also { it.parentFile.mkdir() },
        _overrideDirToSave = File("tmp/local").also { it.mkdir() },
        _overrideStartAt = time,
        doDownloadLocal = false,
        doSendToTelegram = false
    )

    // контекст в будущем, но такой же
    val newCtx = ConcatContext(
        _overrideMainFile = File("tmp/main.csv").also { it.parentFile.mkdir() },
        _overrideDirToSave = File("tmp/local").also { it.mkdir() },
        _overrideStartAt = time.plus(20.minutes.toJavaDuration()),
        doDownloadLocal = false,
        doSendToTelegram = false
    )

    /**
     * Чтобы тесты друг другу не мешали
     */
    override suspend fun beforeEach(testCase: TestCase) {
        ctx.currentMainFile.delete()
        ctx.dirToSave.deleteRecursively()
        ctx.currentMainFile.writeText("")
        ctx.dirToSave.mkdir()
    }

    init {

        val u1dir = File(ctx.dirToSave, "user1").also { it.mkdir() }
        val u2dir = File(ctx.dirToSave, "user2").also { it.mkdir() }
        val u1file = File(u1dir, ctx.localFileName)
        val u2file = File(u2dir, ctx.localFileName)
        fun createFirstLocalCommit() {
            ctx.dirToSave.also {
                u1file.also { it.parentFile.mkdirs() }.writeText(
                    "1 -> PASSPORT_RF:1 # comment 1\n"
                )
                u2file.also { it.parentFile.mkdirs() }.writeText(
                    "2 -> PASSPORT_RF:2 # comment 2\n"
                )
            }
        }

        context("базовые сценарии") {
            test("main пустой, локали пустые, на выходе main с хидером") {
                concat(ctx)
                ctx.currentMainFile.readText() shouldBe TestDesc.csvHeader + "\n"
            }

            test("main пустой, есть 2 локали с тестами, слили, получили") {
                createFirstLocalCommit()
                concat(ctx)
                val result = TestDescParser.parse(ctx.currentMainFile).unwrap()
                result.shouldHaveSize(2)
                //упорядочен по алфавиту авторов
                result[0].author.shouldBe("user1")
                result[1].author.shouldBe("user2")
                result.forAll { it.publishTime shouldBe time }
            }
            test("main не пустой и в локалях ничего не поменялось") {
                createFirstLocalCommit()
                concat(ctx) // получили main
                val main = TestDescParser.parse(ctx.currentMainFile).unwrap()

                // прошло 20 минут и мы снова сводим, но ничего не должно поменяться
                concat(newCtx)

                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()

                updated shouldBe main

            }

            test("появился новый тест") {
                createFirstLocalCommit()
                concat(ctx)
                val main = TestDescParser.parse(ctx.currentMainFile).unwrap()
                // и тут user1 добавляет еще один тест
                u1file.appendText("3 -> INN_FL:3")
                concat(newCtx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                // старые тесты не должны пропасть
                updated.shouldContainAll(main)
                // но второй тест это наш добавленный и время более позднее
                updated[1].also {
                    it.input shouldBe "3"
                    it.publishTime shouldBe newCtx.createdAt
                }
            }
            test("тест удален") {
                createFirstLocalCommit()
                u1file.appendText("3 -> INN_FL:3")
                // итак было 2 теста

                concat(ctx)
                val main = TestDescParser.parse(ctx.currentMainFile).unwrap()
                // и тут user1 по факту откатывает изменения и тест на 3 ушел
                u1file.writeText("1 -> PASSPORT_RF:1 # comment 1\n")
                concat(newCtx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                // старые тесты не должны пропасть
                updated.single { it.input == "3" }.isDisabled.shouldBeTrue()
            }

            test("тест удален а потом вернулся") {
                createFirstLocalCommit()
                u1file.appendText("3 -> INN_FL:3")
                concat(ctx)
                // и тут user1 по факту откатывает изменения и тест на 3 ушел
                u1file.writeText("1 -> PASSPORT_RF:1 # comment 1\n")
                concat(newCtx)
                // и снова вернулся - должен сняться с disable
                u1file.appendText("3 -> INN_FL:3")
                concat(newCtx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                updated[1].isDisabled.shouldBeFalse()
            }

            test("пользователь сам за дизаблил тест") {
                createFirstLocalCommit()
                concat(ctx)
                u1file.writeText("!1 -> PASSPORT_RF:1 # comment 1\n")
                concat(ctx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                updated.shouldHaveSize(2) // новых тестов не должно было породиться
                updated[0].isDisabled.shouldBeTrue() // явно отключен пользователем
            }

            test("тест изменен - то есть по факту это удаление старого и добавление нового") {
                createFirstLocalCommit()
                concat(ctx)
                // и тут user1 меняет свой тест
                u1file.writeText(u1file.readText().replace("1", "3"))
                concat(newCtx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                // старые тесты не должны пропасть
                updated[0].isDisabled.shouldBeTrue() // тест на 1 отключен
                updated[1].also { // тройка добавлена как новый тест
                    it.input shouldBe "3"
                    it.publishTime shouldBe newCtx.createdAt
                }
            }

        }

        context("ошибки"){
            test("завал одной форки просто отключит его тесты"){
                createFirstLocalCommit()
                concat(ctx) // слияние по идее пройдет успешно
                u1file.writeText("не пойми чо\n")
                concat(ctx)
                val updated = TestDescParser.parse(ctx.currentMainFile).unwrap()
                updated.shouldHaveSize(2) // новых тестов не должно было породиться
                updated[0].isDisabled.shouldBeTrue() // но у первого пользователя тесты времено
                // будут отключены
            }
            test("при каком-то грубом завале чтения main - он НЕ будет обновлен"){
                createFirstLocalCommit()
                concat(ctx)
                // порча
                ctx.currentMainFile.writeText(ctx.currentMainFile.readText().replaceFirst("|","~"))
                val badFile = ctx.currentMainFile.readText()
                // обновить пытаемся, но не получится
                concat(ctx)
                ctx.currentMainFile.readText() shouldBe badFile
            }
        }

    }
}