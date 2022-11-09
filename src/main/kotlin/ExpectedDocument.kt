import kotlinx.serialization.Serializable

/**
 * Описание извлеченного документа
 * */
@Serializable
data class ExtractedDocument(
    /** Тип документа */
    val docType: DocType = DocType.UNDEFINED,

    /** Значение документа (номер) */
    val value: String = "",

    /** Установлена ли валидация */
    val isValidSetup: Boolean = false,

    /** Является ли документ валидным
     * !! устанавливается только в том случае, если действительно проверяется ВАЛИДНОСТЬ нормализованного номера документа
     * Например - валидный документ - у которого сходится контрольная сумма, не валидный - у которого не сходится
     * */
    val isValid: Boolean = false,
) {
    /**
     * Проверяет, что переданный документ подходит под данный паттерн
     * */
    fun match(comparedAnswer: ExtractedDocument): Boolean {
        val doTypesEqual = docType == comparedAnswer.docType

        val isNeedToCompareNumber = value.isNotBlank()
        val isNeedToCompareValidation = isValidSetup

        return doTypesEqual
                && (!isNeedToCompareNumber || value == comparedAnswer.value)
                && (!isNeedToCompareValidation || isValid == comparedAnswer.isValid)
    }

    /**
     * Проверяет, что если проверяется значение - оно должно быть нормализовано
     * */
    fun isNormal(): Boolean = value.isBlank() || docType.normaliseRegex.matches(value)
}
