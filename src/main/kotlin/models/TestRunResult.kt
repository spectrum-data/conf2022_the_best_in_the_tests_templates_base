package models

import RunAndCalculateContext

/**
 * Результат запуска теста участником
 * */
data class TestRunResult(
    /**
     * Логин автора теста
     * */
    val authorOfTest: String,

    /**
     * Входная строка
     * */
    val input: String,

    /**
     * Ожидаемый результат
     * */
    val expected: String,

    /**
     * Пройден ли тест
     * */
    val isTestPass: Boolean,

    /**
     * Связанное описание теста
     * */
    val testDesc: TestDesc
) {
    /**
     * Полная строка на обработку - вход + ожидаемый результат
     * */
    val fullStringToProcessed: String
        get() {
            return "$input$expected"
        }

    companion object {
        /**
         * Получается описание запуска теста из строки отчета
         * */
        fun parseFromString(context: RunAndCalculateContext, lineFromReport: String): TestRunResult? {

            return lineFromReport.trim('|').trim().let { line ->
                line.split("|")
                    .map { it.trim() }
                    .let { split ->
                        val testDesc = context.testDescs.firstOrNull() {
                            it.author.lowercase() == split[0].lowercase() && "${it.input.trim()}${it.expected.trim()}" == "${split[1]}${split[2]}"
                        } ?: return null

                        if (testDesc.isDisabled) return null


                        TestRunResult(
                            authorOfTest = split[0],
                            input = split[1],
                            expected = split[2],
                            isTestPass = split[3].lowercase() == "true",
                            testDesc = testDesc
                        )
                    }
            }
        }
    }
}
