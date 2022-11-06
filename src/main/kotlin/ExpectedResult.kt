import kotlinx.serialization.Serializable

/**
 * Описание ожидаемого результата :
 *
 * вход:
 * Паспорт РФ 01 23 456789 == PASSPORT_RF:01234567890
 *
 * выход:
 * isExactly = true, isOrderRequired = true,
 * result = listOf(Document(PASSPORT_RF, "01234567890"))
 */

/**
 * Описание ожидаемого результата парсинга входной строки
 * */
@Serializable
data class ExpectedResult(
    /**
     * Ограничение на вхождение результатов в итоговую выборку
     * true - исключительно ожидаемый набор и ничего кроме
     * false - ожидаемый набор содержатся в итоговой выборке, но могут быть и другие
     * */
    val isExactly: Boolean = false,

    /**
     * Ограничение на порядок расположения результатов в выборке
     * true - ожидаемый набор в указанном порядке
     * false - ожидаемый набор в любом порядке
     * */
    val isOrderRequired: Boolean = false,

    /**
     * Набор ожидаемых извлеченных документов
     * */
    val expected: List<ExtractedDocument> = emptyList()
) {

    /**
     * Проверяет набор документов на совпадение с ожидаемым результатом.
     * */
    fun match(actual: List<ExtractedDocument>): Boolean {
        val doCountsEqual = actual.count() == expected.count()

        return when {
            isExactly && isOrderRequired -> {
                doCountsEqual && actual.zip(expected).all { (a, b) -> a == b }
            }

            isExactly && !isOrderRequired -> {
                doCountsEqual && actual.containsAll(expected)
            }

            /**
             * Должно ли быть эквивалетно?
             * Expected: PASSPORT_RF, INN_FL, INN_UL
             * Actual: PASSPORT_RF, INN_UL
             * */
            !isExactly && isOrderRequired -> {
                var subsequenceIndex = 0
                val actualIterator = actual.iterator()

                while (actualIterator.hasNext() && subsequenceIndex < expected.size) {
                    if (actualIterator.next() == expected[subsequenceIndex]) subsequenceIndex += 1
                }

                return subsequenceIndex == expected.size
            }

            !isExactly && !isOrderRequired -> actual.containsAll(expected)
            else -> error("Некорректной сконфигурирован ожидаемый результат: $this")
        }
    }

    companion object {
        /**
         * Разделитель при указании нескольких документов
         *
         * 1234567890 =? PASSPORT_RF:1234567890[EXPECTED_DOCUMENTS_SEPARATOR]INN_UL:1234567890
         * */
        const val EXPECTED_DOCUMENTS_SEPARATOR = ","

        fun parse(input: String): ExpectedResult {
            val splitByRegex = INPUT_STRUCTURE_REGEX.toRegex().matchEntire(input)

            check(splitByRegex != null && splitByRegex.groupValues.count() == 4) {
                "Входная строка '$input' не соответствует структуре '$INPUT_STRUCTURE_REGEX'"
            }

            val filledConstraints = createAndFillConstraints(splitByRegex.groupValues[2])
            val parseExpectedDocs = parseExpectedDocs(splitByRegex.groupValues[3])

            return filledConstraints.copy(expected = parseExpectedDocs)
        }

        private fun parseExpectedDocs(input: String): List<ExtractedDocument> {
            return input.split(EXPECTED_DOCUMENTS_SEPARATOR).map { expectedDocDesc ->
                expectedDocDesc.split(":")
                    .let {
                        val value = it.getOrElse(1) { "" }.trim().replace(Regex("\\s"), "")

                        if (it[0].startsWith(INVALID_DOC_PREFIX)) {
                            ExtractedDocument(
                                docType = DocType.valueOf(it[0].drop(1).uppercase().trim()),
                                isValid = false,

                                value = value,
                            )
                        } else {
                            ExtractedDocument(
                                docType = DocType.valueOf(it[0].uppercase().trim()),
                                isValid = true,

                                value = value,
                            )
                        }
                    }
            }
        }

        private fun createAndFillConstraints(constraints: String): ExpectedResult {
            var isExactly: Boolean? = null
            var isOrderRequired: Boolean? = null

            when (constraints) {
                "==" -> {
                    isExactly = true; isOrderRequired = true
                }

                "~=" -> {
                    isExactly = false; isOrderRequired = true
                }

                "=?" -> {
                    isExactly = true; isOrderRequired = false
                }

                "~?" -> {
                    isExactly = false; isOrderRequired = false
                }

                else -> error("неожиданное обозначение ограничений в описании теста - $constraints")
            }

            return ExpectedResult(isExactly = isExactly, isOrderRequired = isOrderRequired)
        }

        /**
         * Префикс документа - номер документа не валиден
         * */
        const val INVALID_DOC_PREFIX = "!"

        /**
         * Регулярное выражение структуры описания теста
         * */
        const val INPUT_STRUCTURE_REGEX = "^([\\s\\S]+?[^~=?]+)(==|~=|=\\?|~\\?)([^~=?]+[\\s\\S]+?)\$"
    }
}