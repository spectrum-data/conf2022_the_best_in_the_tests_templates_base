package models

import RunAndCalculateContext
import java.io.File
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Описания тестов
 * */
data class TestDesc(
    /**
     * Автор
     * */
    val author: String,

    /**
     * Входная строка
     * */
    val input: String,

    /**
     * Ожидаемый результат
     * */
    val expected: String,

    /**
     * Отключен ли тест
     * */
    val isDisabled: Boolean,

    /**
     * Комментарий
     * */
    val comment: String,

    /**
     * Время публикации
     * */
    val publishTime: LocalTime,
) {
    /**
     * Получает время, которое прошло после старта, до публикации теста
     * */
    fun getTestPublishMinutes(context: RunAndCalculateContext): Int =
        context.startAt.until(publishTime, ChronoUnit.MINUTES).toInt()


    companion object {
        /**
         * Парсит файл с описаниями тестов в список описаний
         * */
        fun parseFile(mainFile: File, context: RunAndCalculateContext): List<TestDesc> {
            return mainFile.useLines { seq -> seq.drop(1).map { it.toTestDesc(context) }.toList() }
        }

        /**
         * Парсит из строки с описанием теста
         * */
        fun parse(line: String, context: RunAndCalculateContext): TestDesc {
            return line.trim()
                .split(context.delimiter)
                .map { it.trim() }
                .let {
                    TestDesc(
                        author = it[0],
                        input = it[1],
                        expected = it[2],
                        isDisabled = it[3].lowercase() == "true",
                        comment = it[4],
                        publishTime = LocalTime.parse(it[5])
                    )
                }
        }

        fun String.toTestDesc(context: RunAndCalculateContext) = parse(this, context)
    }
}