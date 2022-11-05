fun main(args: Array<String>) {
    /**
     * Выбор режими работы - объединение файлов с описаниями тестов/подведение результатов
     * */
    val mode by lazy {
        System.getenv().getOrDefault("MODE", "")
    }

    when (mode.uppercase()) {
        "CONCAT" -> concat()
        "CALCULATE" -> calculate()
    }
}
