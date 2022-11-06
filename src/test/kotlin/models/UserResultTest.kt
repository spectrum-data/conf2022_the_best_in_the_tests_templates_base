package models

import RunAndCalculateContext
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class UserResultTest : FunSpec() {
    val _startAt = Instant.parse("2022-01-01T01:01:01.000Z")
    val context = object : RunAndCalculateContext() {
        override val startAt: Instant = _startAt
    }

    fun getTestRunResult(author: String, isTestPass: Boolean, publishTime: Instant): TestRunResult {
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
                commentOnFailure = "someComment",
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
                        getTestRunResult(firstUserLogin, true, _startAt.plus(15.minutes.toJavaDuration())),
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
                        getTestRunResult(firstUserLogin, true, _startAt.plus(15.minutes.toJavaDuration())),
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
                        getTestRunResult(firstUserLogin, false, _startAt.plus(15.minutes.toJavaDuration())),
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
                        getTestRunResult(secondUserLogin, true, _startAt.plus(20.minutes.toJavaDuration())),
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
                        getTestRunResult(secondUserLogin, true, _startAt.plus(20.minutes.toJavaDuration())),
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
                        getTestRunResult(firstUserLogin, false, _startAt.plus(20.minutes.toJavaDuration())),
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
