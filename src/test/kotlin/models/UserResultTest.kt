package models

import RunAndCalculateContext
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.time.LocalTime
import kotlin.random.Random

class UserResultTest : FunSpec() {
    val _startAt = LocalTime.parse("00:00:00")
    val context = object : RunAndCalculateContext() {
        override val startAt: LocalTime = _startAt
    }

    fun getTestRunResult(author: String, isTestPass: Boolean, publishTime: LocalTime): TestRunResult {
        val input = Random.nextInt().toString()
        val expected = Random.nextInt().toString()

        return TestRunResult(
            authorOfTest = author,
            input = input,
            expected = expected,
            isTestPass = isTestPass,
            testDesc = TestDesc(
                author = author,
                input = input,
                expected = expected,
                isDisabled = false,
                comment = "someComment",
                publishTime = publishTime
            )
        )
    }

    val firstUserLogin = "First"
    val secondUserLogin = "Second"

    init {
        context("Свои тесты") {
            test("Сам выполнил - противник выполнил - записывает не удачный выстрел") {
                var firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(firstUserLogin, true, _startAt.plusMinutes(15)),
                    ),
                    competitorsTests = listOf()
                )

                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(),
                    competitorsTests = listOf(
                        firstUser.ownTests.first().copy(isTestPass = true)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.firedShots.single() shouldBe TestShot(firstUserLogin, secondUserLogin, 15, false)
                }
            }

            test("Сам выполнил - противник не выполнил - записывает удачный выстрел") {
                var firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(firstUserLogin, true, _startAt.plusMinutes(15)),
                    ),
                    competitorsTests = listOf()
                )

                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(),
                    competitorsTests = listOf(
                        firstUser.ownTests.first().copy(isTestPass = false)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.firedShots.single() shouldBe TestShot(firstUserLogin, secondUserLogin, 15, true)
                }
            }

            test("Сам не выполнил - противник не выполнил - не записывает выстрел") {
                var firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(firstUserLogin, false, _startAt.plusMinutes(15)),
                    ),
                    competitorsTests = listOf()
                )

                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(),
                    competitorsTests = listOf(
                        firstUser.ownTests.first().copy(isTestPass = false)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.firedShots.shouldBeEmpty()
                }
            }
        }

        context("Чужие выстрелы") {
            test("Противник выполнил - я не выполнил - записывает удачное попадание по мне") {
                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(secondUserLogin, true, _startAt.plusMinutes(20)),
                    ),
                    competitorsTests = listOf(
                    )
                )

                val firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                    ),
                    competitorsTests = listOf(
                        secondUserResult.ownTests.first().copy(isTestPass = false)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.receivedShots.single() shouldBe TestShot(secondUserLogin, firstUserLogin, 20, true)
                }
            }

            test("Противник выполнил - я выполнил - записывает неудачный выстрел по мне") {
                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(secondUserLogin, true, _startAt.plusMinutes(20)),
                    ),
                    competitorsTests = listOf(
                    )
                )

                val firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                    ),
                    competitorsTests = listOf(
                        secondUserResult.ownTests.first().copy(isTestPass = true)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.receivedShots.single() shouldBe TestShot(secondUserLogin, firstUserLogin, 20, false)
                }
            }

            test("Противник не выполнил - я не выполнил - не записывает выстрел") {
                val secondUserResult = UserResult(
                    login = secondUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                        getTestRunResult(firstUserLogin, false, _startAt.plusMinutes(20)),
                    ),
                    competitorsTests = listOf(
                    )
                )

                val firstUser = UserResult(
                    login = firstUserLogin,
                    isPassBase = false,
                    ownTests = listOf(
                    ),
                    competitorsTests = listOf(
                        secondUserResult.ownTests.first().copy(isTestPass = false)
                    )
                )

                firstUser.fillShotInfo(context, secondUserResult)

                assertSoftly {
                    firstUser.receivedShots.shouldBeEmpty()
                }
            }
        }
    }
}
