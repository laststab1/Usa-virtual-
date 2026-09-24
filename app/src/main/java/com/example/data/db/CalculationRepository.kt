package com.example.data.db

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val allCalculations: Flow<List<CalculationRecord>> = dao.getAllCalculations()

    suspend fun saveCalculation(expression: String, result: String, category: String = "CALC") {
        if (expression.isNotBlank() && result.isNotBlank() && result != "Error") {
            dao.insertCalculation(
                CalculationRecord(
                    expression = expression,
                    result = result,
                    category = category
                )
            )
        }
    }

    suspend fun delete(id: Long) {
        dao.deleteCalculation(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
