package models

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.TestDesc.Companion.toTestDesc

class TestDescTest : FunSpec() {
    init {
//        test("Корректно парсит строку") {
//            val testDesc =
//                "Lokbugs|паспортРФ|==PASSPORT_RF|false|Не удалось определить корректный паспорт РФ ФЛ|18:12:28.642595".toTestDesc()
//
//            assertSoftly {
//                testDesc.author shouldBe "Lokbugs"
//                testDesc.input shouldBe "паспортРФ"
//                testDesc.expected shouldBe "==PASSPORT_RF"
//                testDesc.isDisabled shouldBe false
//            }
//        }
    }
}
