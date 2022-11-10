fun main(args: Array<String>) {
    /**
     * Выбор режими работы - объединение файлов с описаниями тестов/подведение результатов
     * */
    val mode by lazy {
        System.getenv().getOrDefault("MODE", "")
    }

    calculate()
    /*
    when (mode.uppercase()) {
        "CONCAT" -> concat()
        "RUN_AND_COLLECT" -> runAndCollect()
        "CALCULATE" -> calculate()
    }*/
}
