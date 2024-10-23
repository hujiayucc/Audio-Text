package io.github.datch666.audio_text

import io.github.datch666.core.CharToFrequencyMapper.getFrequency
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val text = "Hello 123, 世界!"
        for (c in text) {
            val frequency = getFrequency(c)
            System.out.printf("字符: %c, 频率: %.2f Hz\n", c, frequency)
        }
    }
}