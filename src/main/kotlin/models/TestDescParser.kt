package models

import ExpectedResult
import java.io.File
import java.io.Reader
import java.time.Instant

/**
 * Парсер исходных файлов тестов в полном или сокращенном
 * формате, для парсинга в коротком формате в опциях необходимо
 * передать название форки (автора теста)
 *
 * Поддерживает короткий формат (локльный)
 * Полный формат (CSV)
 * а также ранний вариант полного (в которому условие и результат склеены)
 *
 * Разрешает в любом формате пустые строки, а также строки комментариев на
 * `\s*#`
 */
object TestDescParser {
    /**
     * Опции парсинга
     */
    class Options(
        /**
         * Автор - для полного формата игнорируется
         * а для локального обязателен
         */
        val author: String = "",
        /**
         * Время публикации, по умолчанию - текущее,
         * но может быть и явно выставлено
         */
        val publishTime: Instant = Instant.now()
    ) {
        companion object {
            val Empty = Options()
        }
    }

    /**
     * Ошибки парсера
     */
    sealed class Error(val message: String) {
        override fun toString(): String {
            return "${this::class.simpleName} : $message"
        }
        /**
         * Синглтон успешной обработки
         */
        object NoError : Error("Нет ошибок")

        /**
         * Невозможность определения формата
         */
        object CannotDetektFormat : Error("Не могу определить формат файла")

        /**
         * Ошибка в структуре файла
         */
        class InvalidLineStruct(val line: Int) : Error("Не правильная структура строки $line") {
            override fun equals(other: Any?): Boolean {
                return (other as? InvalidLineStruct)?.line == this.line
            }
            override fun hashCode(): Int {
                return line.hashCode()
            }
        }

        /**
         * Ошибка в синтаксисе ожидаемого результата
         */
        class InvalidExpectedSyntax(val line: Int, comment: String = "") : Error("Не правильный синтаксис проверки $line $comment".trim())

        /**
         * Ошибка неправильный опций
         */
        object NoAuthorInfoForLocal : Error("Не укзано имя автора при парсинге из локального файла")

        /**
         * Все тесты должны быть уникальны
         */
        class DuplicateBizKeys(val keys: List<String>) : Error("Обнаружены тесты дубли: [${keys.joinToString()}]")
    }

    /**
     * Результат работы парсера
     */
    class Result(
        /**
         * Собственно прочитанные данные
         */
        val data: List<TestDesc> = emptyList(),
        /**
         * Ошибка в обработке
         */
        val error: Error = Error.NoError,
    ) {
        val isOk: Boolean = error == Error.NoError

        fun unwrap() : List<TestDesc> {
            if(!isOk) {
                error(error.toString())
            }
            return data
        }
    }

    /**
     * Вариант входного формата
     */
    enum class Format(val regex: Regex) {
        /**
         * Неопределенный формат
         */
        UNDEFINED("".toRegex()),

        /**
         * Может быть опущено `==` потом если нет ни одного оператора, он будет дописан
         */
        LOCAL("""^([\s\S]*?[^~=?]+)->(==|~=|=\?|~\?)?([^~=?]+[\s\S]*?)$""".toRegex()) {
            override fun parseLineOrError(lineNumber: Int, line: String, options: Options): Pair<TestDesc, Error> {
                val beforeCommentAndComment = line.split("#")
                if (beforeCommentAndComment.size > 2) {
                    return TestDesc.None to Error.InvalidLineStruct(lineNumber)
                }
                val commentOnFailure = beforeCommentAndComment.elementAtOrNull(1) ?: ""
                val inputAndExpected = beforeCommentAndComment.first().split("->")
                if (inputAndExpected.size != 2 || inputAndExpected.any { it.isBlank() }) {
                    return TestDesc.None to Error.InvalidLineStruct(lineNumber)
                }
                val (inputCandidate, expectedCandidate) = inputAndExpected.map { it.trim() }
                // дополнение ==
                val expected = if (expectedCandidate.contains(FULL_EXPECTATION_REGEX)) {
                    expectedCandidate
                } else {
                    "==$expectedCandidate"
                }
                val isDisabled = inputCandidate.startsWith("!")
                val input = inputCandidate.trimStart('!')
                val testDesc = TestDesc(
                    author = options.author,
                    input = input,
                    expected = expected,
                    isDisabled = isDisabled,
                    commentOnFailure = commentOnFailure.trim(),
                    publishTime = options.publishTime
                )
                val error = checkExpectedResult(input+expected, lineNumber)

                return testDesc to error
            }
        },
        MAIN("""^\s*author\s*\|\s*input""".toRegex()) {
            override fun parseLineOrError(lineNumber: Int, line: String, options: Options): Pair<TestDesc, Error> {
                val fields = line.split("|")
                if (fields.size != FIELDS_SIZE) {
                    return TestDesc.None to Error.InvalidLineStruct(lineNumber)
                }
                val author = fields[0].trim()
                val input = fields[1].trim()
                val expected = fields[2].trim()
                val isDisabled = fields[3].trim().toBooleanStrict()
                val commentOnFailure = fields[4].trim()
                val publishTime: Instant = Instant.parse(fields[5].trim())
                val testDesc = TestDesc(
                    author = author,
                    input = input,
                    expected = expected,
                    isDisabled = isDisabled,
                    commentOnFailure = commentOnFailure,
                    publishTime = publishTime
                )
                val error = checkExpectedResult(input+expected, lineNumber)
                return testDesc to error
            }
        };

        open fun parseLineOrError(lineNumber: Int, line: String, options: Options): Pair<TestDesc, Error> {
            error("UNDEFINED no implements parsing")
        }

        protected fun checkExpectedResult(fullexpectation: String, lineNumber: Int): Error {
            val expectedValidationResult = kotlin.runCatching {
                ExpectedResult.parse(fullexpectation)
            }
            return if (expectedValidationResult.isSuccess) {
                Error.NoError
            } else {
                val exception = expectedValidationResult.exceptionOrNull()!!
                Error.InvalidExpectedSyntax(lineNumber, "${exception::class.simpleName}: ${exception.message}")
            }
        }


        companion object {

            const val FIELDS_SIZE = 6

            /**
             * Регекс условия в полном файле, нет никаких послаблений на структуру
             */
            val FULL_EXPECTATION_REGEX = "^(==|~=|=\\?|~\\?)([^~=?]+[\\s\\S]*?)$".toRegex()

            /**
             * Определяет формат
             */
            fun detektOrError(line: String, options: Options): Pair<Format, Error> {
                val format = values().firstOrNull {
                    it != UNDEFINED && line.contains(it.regex)
                } ?: UNDEFINED
                val error = when {
                    format == UNDEFINED -> {
                        Error.CannotDetektFormat
                    }

                    format == LOCAL && options.author.isBlank() -> Error.NoAuthorInfoForLocal
                    else -> Error.NoError
                }
                return format to error
            }
        }
    }

    private val commentRegex = """^\s*#""".toRegex()

    /**
     * Собственно метод парсинга
     */
    fun parse(reader: Reader, options: Options = Options.Empty): Result {
        val data = mutableListOf<TestDesc>()
        var error: Error = Error.NoError
        var format = Format.UNDEFINED
        fun String.isComment() = contains(commentRegex)
        reader.useLines { lines ->
            for ((i, line) in lines.withIndex()) {
                if (line.isBlank() || line.isComment()) {
                    continue
                }
                if (format == Format.UNDEFINED) {
                    val (f, e) = Format.detektOrError(line, options)
                    if (e != Error.NoError) {
                        error = e
                        break
                    }
                    format = f
                    if (format == Format.MAIN) {
                        // пропуск хидера
                        continue
                    }
                }
                val (t, e) = format.parseLineOrError(i, line, options)
                if (e != Error.NoError) {
                    error = e
                    break
                }
                data.add(t)
            }
        }

        val grouppedByBizKey = data.groupBy { it.bizKey }
        val doubles = grouppedByBizKey.filter { it.value.size > 1 }
        if(doubles.any()) {
            error = Error.DuplicateBizKeys(doubles.keys.toList())
        }
        return Result(data, error)
    }

    /**
     * Для форк ридера
     */
    fun parse(file: File, options: Options = Options.Empty): Result {
        return file.reader().use {
            parse(it, options)
        }
    }

    /**
     * Для тестов
     */
    fun parse(content: String, options: Options = Options.Empty): Result {
        return content.reader().let { parse(it, options) }
    }

}