import kotlinx.serialization.Serializable

/**
 * Описание извлеченного документа
 * */
@Serializable
data class ExtractedDocument(
    val docType: DocType = DocType.UNDEFINED,
    val isValid: Boolean = true,
    val value: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other is ExtractedDocument) {
            return internalEqual(other as ExtractedDocument)
        }

        return super.equals(other)
    }

    private fun internalEqual(comparedAnswer: ExtractedDocument): Boolean {
        return comparedAnswer.docType == docType
                && comparedAnswer.value == value
    }
}
