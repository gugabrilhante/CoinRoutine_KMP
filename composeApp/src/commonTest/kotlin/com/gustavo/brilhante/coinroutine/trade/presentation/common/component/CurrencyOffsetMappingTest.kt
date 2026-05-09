package com.gustavo.brilhante.coinroutine.trade.presentation.common.component

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [CurrencyOffsetMapping].
 *
 * Mappings are verified against concrete original/formatted string pairs without
 * calling [formatFiat] (an `expect` function), so these tests run on any platform.
 *
 * Example: "1000" → "$1,000"
 *   Digit indexes: '1'→1, '0'→3, '0'→4, '0'→5
 */
class CurrencyOffsetMappingTest {

    // "$1,000": original = "1000", formatted = "$1,000"
    // digit indexes in formatted: [1, 3, 4, 5]
    private val mapping = CurrencyOffsetMapping("1000", "\$1,000")

    // ── originalToTransformed ────────────────────────────────────────────────

    @Test
    fun `originalToTransformed maps first digit to its formatted position`() {
        // '1' is at index 1 in "$1,000"
        assertEquals(1, mapping.originalToTransformed(0))
    }

    @Test
    fun `originalToTransformed skips formatting characters`() {
        // First '0' is at index 3 (skips ',')
        assertEquals(3, mapping.originalToTransformed(1))
    }

    @Test
    fun `originalToTransformed maps consecutive digits`() {
        assertEquals(4, mapping.originalToTransformed(2))
        assertEquals(5, mapping.originalToTransformed(3))
    }

    @Test
    fun `originalToTransformed at end of original returns position after last digit`() {
        // offset >= originalLength (4) → indexes.last() + 1 = 5 + 1 = 6
        assertEquals(6, mapping.originalToTransformed(4))
    }

    // ── transformedToOriginal ────────────────────────────────────────────────

    @Test
    fun `transformedToOriginal maps position before currency symbol to start`() {
        // offset 0 → first index >= 0 is indexes[0]=1 → original index 0
        assertEquals(0, mapping.transformedToOriginal(0))
    }

    @Test
    fun `transformedToOriginal maps position of first digit to original index 0`() {
        // offset 1 → first index >= 1 is indexes[0]=1 → original index 0
        assertEquals(0, mapping.transformedToOriginal(1))
    }

    @Test
    fun `transformedToOriginal maps position of comma separator to original index 1`() {
        // offset 2 → first index >= 2 is indexes[1]=3 → original index 1
        assertEquals(1, mapping.transformedToOriginal(2))
    }

    @Test
    fun `transformedToOriginal maps position past last digit to original length`() {
        // offset 6 → no index >= 6 → originalLength = 4
        assertEquals(4, mapping.transformedToOriginal(6))
    }

    // ── edge cases ───────────────────────────────────────────────────────────

    @Test
    fun `single digit mapping works correctly`() {
        // "5" → "$5": digit index [1]
        val single = CurrencyOffsetMapping("5", "\$5")
        assertEquals(1, single.originalToTransformed(0))
        assertEquals(2, single.originalToTransformed(1))
        assertEquals(0, single.transformedToOriginal(0))
        assertEquals(0, single.transformedToOriginal(1))
        assertEquals(1, single.transformedToOriginal(2))
    }
}
