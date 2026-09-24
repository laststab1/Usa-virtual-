package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationRecord
import com.example.data.db.CalculationRepository
import com.example.logic.CalculatorEngine
import com.example.logic.RamCalculator
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository
    private val calcEngine = CalculatorEngine()
    private val ramCalculator = RamCalculator()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalculationRepository(db.calculationDao())
    }

    val historyList: StateFlow<List<CalculationRecord>> = repository.allCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calculator State
    private val _expression = MutableStateFlow("0")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _isScientific = MutableStateFlow(false)
    val isScientific: StateFlow<Boolean> = _isScientific.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    // 8GB RAM Hub State
    private val _deviceRamStats = MutableStateFlow<DeviceRamStats?>(null)
    val deviceRamStats: StateFlow<DeviceRamStats?> = _deviceRamStats.asStateFlow()

    private val _budgetConfig = MutableStateFlow(RamBudgetConfig())
    val budgetConfig: StateFlow<RamBudgetConfig> = _budgetConfig.asStateFlow()

    private val _budgetResult = MutableStateFlow(ramCalculator.calculate8GbBudget(RamBudgetConfig()))
    val budgetResult: StateFlow<RamBudgetResult> = _budgetResult.asStateFlow()

    // RAM Unit Converter State
    private val _convertInput = MutableStateFlow("8")
    val convertInput: StateFlow<String> = _convertInput.asStateFlow()

    private val _convertUnit = MutableStateFlow("GB")
    val convertUnit: StateFlow<String> = _convertUnit.asStateFlow()

    private val _isBinaryUnit = MutableStateFlow(true)
    val isBinaryUnit: StateFlow<Boolean> = _isBinaryUnit.asStateFlow()

    private val _convertedResults = MutableStateFlow<Map<String, String>>(emptyMap())
    val convertedResults: StateFlow<Map<String, String>> = _convertedResults.asStateFlow()

    // RAM Standards & Benchmark
    private val _selectedStandard = MutableStateFlow(RamStandard.LPDDR4X)
    val selectedStandard: StateFlow<RamStandard> = _selectedStandard.asStateFlow()

    private val _benchmarkResult = MutableStateFlow<MemoryBenchmarkResult?>(null)
    val benchmarkResult: StateFlow<MemoryBenchmarkResult?> = _benchmarkResult.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    init {
        refreshRamStats()
        updateConversion("8", "GB", true)
    }

    // --- Calculator Actions ---

    fun onInput(char: String) {
        val current = _expression.value
        val updated = if (current == "0" && char !in "+-×÷.^%") {
            char
        } else if (current == "Error" || current == "Cannot divide by 0") {
            char
        } else {
            current + char
        }
        _expression.value = updated
        updatePreview(updated)
    }

    fun onClear() {
        _expression.value = "0"
        _previewResult.value = ""
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.length <= 1 || current == "Error" || current == "Cannot divide by 0") {
            _expression.value = "0"
            _previewResult.value = ""
        } else {
            val updated = current.substring(0, current.length - 1)
            _expression.value = updated
            updatePreview(updated)
        }
    }

    fun onTogglePlusMinus() {
        val current = _expression.value
        if (current == "0" || current == "Error") return
        if (current.startsWith("-")) {
            _expression.value = current.substring(1)
        } else {
            _expression.value = "-$current"
        }
        updatePreview(_expression.value)
    }

    fun onToggleScientific() {
        _isScientific.value = !_isScientific.value
    }

    fun onEquals() {
        val current = _expression.value
        if (current.isBlank() || current == "0") return

        val (res, success) = calcEngine.evaluate(current)
        if (success) {
            viewModelScope.launch {
                repository.saveCalculation(current, res, "CALC")
            }
            _expression.value = res
            _previewResult.value = ""
        } else {
            _previewResult.value = res
        }
    }

    fun onScientificFunction(func: String) {
        val current = _expression.value
        val updated = when (func) {
            "x²" -> "($current)^2"
            "√x", "sqrt" -> "sqrt($current)"
            "1/x" -> "1/($current)"
            "sin", "cos", "tan", "log", "ln" -> "$func($current)"
            "π" -> if (current == "0") "π" else "$current*π"
            "e" -> if (current == "0") "e" else "$current*e"
            else -> current
        }
        _expression.value = updated
        updatePreview(updated)
    }

    private fun updatePreview(expr: String) {
        if (expr.isEmpty() || expr == "0") {
            _previewResult.value = ""
            return
        }
        val (res, success) = calcEngine.evaluate(expr)
        if (success && res != expr) {
            _previewResult.value = "= $res"
        } else {
            _previewResult.value = ""
        }
    }

    // Memory operations
    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    fun memoryRecall() {
        val formatted = calcEngine.formatNumber(_memoryValue.value)
        _expression.value = formatted
        updatePreview(formatted)
    }

    fun memoryAdd() {
        val (res, success) = calcEngine.evaluate(_expression.value)
        if (success) {
            val num = res.replace(",", "").toDoubleOrNull() ?: 0.0
            _memoryValue.value += num
        }
    }

    fun memorySubtract() {
        val (res, success) = calcEngine.evaluate(_expression.value)
        if (success) {
            val num = res.replace(",", "").toDoubleOrNull() ?: 0.0
            _memoryValue.value -= num
        }
    }

    fun memoryStore() {
        val (res, success) = calcEngine.evaluate(_expression.value)
        if (success) {
            val num = res.replace(",", "").toDoubleOrNull() ?: 0.0
            _memoryValue.value = num
        }
    }

    fun setExpressionFromHistory(expr: String, res: String) {
        _expression.value = res
        _previewResult.value = ""
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- 8GB RAM Hub Actions ---

    fun refreshRamStats() {
        try {
            val stats = ramCalculator.readDeviceRam(getApplication())
            _deviceRamStats.value = stats
        } catch (_: Exception) {}
    }

    fun updateBudgetConfig(newConfig: RamBudgetConfig) {
        _budgetConfig.value = newConfig
        _budgetResult.value = ramCalculator.calculate8GbBudget(newConfig)
    }

    fun resetBudgetToDefault() {
        updateBudgetConfig(RamBudgetConfig())
    }

    fun updateConversion(input: String, unit: String, isBinary: Boolean) {
        _convertInput.value = input
        _convertUnit.value = unit
        _isBinaryUnit.value = isBinary

        val num = input.toDoubleOrNull() ?: 0.0
        val results = ramCalculator.convertMemoryUnit(num, unit, isBinary)
        _convertedResults.value = results
    }

    fun selectRamStandard(standard: RamStandard) {
        _selectedStandard.value = standard
    }

    fun runRamBenchmark() {
        if (_isBenchmarking.value) return
        viewModelScope.launch {
            _isBenchmarking.value = true
            try {
                val res = ramCalculator.runSafeMemoryBenchmark(40)
                _benchmarkResult.value = res
            } catch (_: Exception) {
            } finally {
                _isBenchmarking.value = false
            }
        }
    }

    fun copyToClipboard(label: String, text: String): Boolean {
        return try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (_: Exception) {
            false
        }
    }
}
