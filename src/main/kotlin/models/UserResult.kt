package models

import RunAndCalculateContext
import java.io.File

/**
 * Результаты выполнения тестов участником
 * */
data class UserResult(
    /**
     * Логин участника
     * */
    val login: String,

    /**
     * Пройдены ли базовые тесты
     * */
    val isPassBase: Boolean,

    /**
     * Результаты запуска собственных тестов
     * */
    val ownTests: List<TestRunResult>,

    /**
     * Результаты запуска тестов других участников
     * */
    val competitorsTests: List<TestRunResult>,
) {
    /**
     * Полученные выстрелы
     * */
    val receivedShots: MutableList<TestShot> = mutableListOf()

    /**
     * Выстрелы по соперникам
     * */
    val firedShots: MutableList<TestShot> = mutableListOf()

    /**
     * Кол-во заработанных очков
     * */
    val score by lazy {
        firedShots.filter { it.isSuccessful }.sumOf { it.getScore() }
            .minus(receivedShots.filter { it.isSuccessful }.sumOf { it.getScore() })
    }

    /**
     * Заполнить информацию о полученных/выпущенных тестов-снарядов
     * */
    fun fillShotInfo(context: RunAndCalculateContext, otherUser: UserResult) {
        ownTests.filter { it.isTestPass }.forEach { ownTest ->
            otherUser.competitorsTests.singleOrNull() {
                it.authorOfTest.lowercase().trim() == login.lowercase().trim()  &&
                        it.input.lowercase() == ownTest.input.lowercase() &&
                        it.expected.lowercase() == ownTest.expected.lowercase()
            }?.also {
                firedShots.add(
                    TestShot(
                        from = login,
                        to = otherUser.login,
                        timeToPublishMinutes = ownTest.testDesc.getTestPublishMinutes(context),
                        isSuccessful = !it.isTestPass,
                    )
                )
            }
        }

        competitorsTests
            .filter { it.authorOfTest.lowercase().trim() == otherUser.login.lowercase().trim() }
            .filter { competitorsTest -> otherUser.ownTests.any { it.fullStringToProcessed.trim() == competitorsTest.fullStringToProcessed.trim() && it.isTestPass } }
            .forEach { testRun ->
                receivedShots.add(
                    TestShot(
                        from = otherUser.login,
                        to = login,
                        timeToPublishMinutes = testRun.testDesc.getTestPublishMinutes(context),
                        isSuccessful = !testRun.isTestPass,
                    )
                )
            }
    }

    companion object {
        /**
         * Получает описание результата участника, по файлу отчета
         * */
        fun parseFromReport(context: RunAndCalculateContext, login: String, reportFile: File): UserResult {
            var isBasePass = false
            val testResultLines = mutableListOf<String>()

            reportFile.bufferedReader().use { reader ->
                var isNeedCollectLine = false
                var line = reader.readLine()

                while (line != null) {
                    if (line == "##### All basic tests were passed") {
                        isBasePass = true
                    }

                    if (isNeedCollectLine && line.isNotBlank()) {
                        testResultLines.add(line)
                    }

                    if (line.startsWith("|-----")) {
                        isNeedCollectLine = true
                    }

                    line = reader.readLine()
                }
            }

            val testResults =
                testResultLines.filter { it.isNotBlank() }.mapNotNull { TestRunResult.parseFromString(context, it) }

            return UserResult(
                login = login,
                isPassBase = isBasePass,
                ownTests = testResults.filter { it.authorOfTest.lowercase() == login.lowercase() },
                competitorsTests = testResults.filter { it.authorOfTest.lowercase() != login.lowercase() },
            )
        }
    }
}

/**
 * Единица обмена участниками - тестовый снаряд
 * */
data class TestShot(
    /**
     * От кого
     * */
    val from: String,

    /**
     * По кому
     * */
    val to: String,

    /**
     * Вес снаряда в минутах
     * */
    val timeToPublishMinutes: Int,

    /**
     * Успешен ли выстрел
     * */
    val isSuccessful: Boolean
) {
    /**
     * Получить балл в зависимости от времени публикации теста
     * */
    fun getScore(): Double {
        return when {
            timeToPublishMinutes <= 15 -> 2.0
            timeToPublishMinutes <= 30 -> 1.8
            timeToPublishMinutes <= 45 -> 1.6
            timeToPublishMinutes <= 60 -> 1.4
            timeToPublishMinutes <= 75 -> 1.0
            timeToPublishMinutes <= 90 -> 0.6
            timeToPublishMinutes <= 105 -> 0.4
            timeToPublishMinutes <= 120 -> 0.2
            else -> 0.0
        }
    }
}
