package com.ditech.cloudphone.Utils
import java.text.Normalizer
class CONFIG {
    companion object {
        fun normalizeText(input: String): String {
            return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }
    }

}