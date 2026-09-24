package com.example

import com.example.logic.CalculatorEngine
import com.example.logic.RamCalculator
import com.example.model.MemoryPressureState
import com.example.model.RamBudgetConfig
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    private val engine = CalculatorEngine()
    private val ramCalc = RamCalculator()

    @Test
    fun testBasicCalculations() {
        val (res1, s1) = engine.evaluate("2+2")
        assertTrue(s1)
        assertEquals("4", res1)

        val (res2, s2) = engine.evaluate("15×3-5")
        assertTrue(s2)
        assertEquals("40", res2)

        val (res3, s3) = engine.evaluate("100÷4")
        assertTrue(s3)
        assertEquals("25", res3)
    }

    @Test
    fun testParenthesesAndPrecedence() {
        val (res, ok) = engine.evaluate("2*(3+4)")
        assertTrue(ok)
        assertEquals("14", res)
    }

    @Test
    fun testScientificFunctions() {
        val (res1, ok1) = engine.evaluate("sqrt(16)")
        assertTrue(ok1)
        assertEquals("4", res1)

        val (res2, ok2) = engine.evaluate("2^3")
        assertTrue(ok2)
        assertEquals("8", res2)
    }

    @Test
    fun testDivisionByZero() {
        val (res, ok) = engine.evaluate("10/0")
        assertFalse(ok)
        assertEquals("Cannot divide by 0", res)
    }

    @Test
    fun test8GbRamBudget() {
        val config = RamBudgetConfig(games = 1, socialApps = 3, browserTabs = 5, bgServices = 2)
        val result = ramCalc.calculate8GbBudget(config)

        assertEquals(8192, result.totalDeviceRamMb)
        assertTrue(result.totalRequiredMb > 3000)
        assertTrue(result.remainingMb > 0)
        assertNotEquals(MemoryPressureState.CRITICAL, result.memoryPressureState)
    }

    @Test
    fun test8GbOverBudget() {
        val heavyConfig = RamBudgetConfig(games = 4, socialApps = 8, browserTabs = 15, bgServices = 6)
        val result = ramCalc.calculate8GbBudget(heavyConfig)

        assertTrue(result.totalRequiredMb > 8192)
        assertEquals(MemoryPressureState.CRITICAL, result.memoryPressureState)
        assertTrue(result.remainingMb < 0)
    }

    @Test
    fun testMemoryUnitConversion() {
        val binaryResults = ramCalc.convertMemoryUnit(8.0, "GB", true)
        assertEquals("8,192", binaryResults["MiB"])
        assertEquals("8,589,934,592", binaryResults["Bytes"])

        val decimalResults = ramCalc.convertMemoryUnit(8.0, "GB", false)
        assertEquals("8,000", decimalResults["MB"])
        assertEquals("8,000,000,000", decimalResults["Bytes"])
    }
}
