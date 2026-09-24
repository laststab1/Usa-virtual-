package com.example.model

data class DeviceRamStats(
    val totalBytes: Long,
    val availBytes: Long,
    val thresholdBytes: Long,
    val isLowMemory: Boolean,
    val usedBytes: Long,
    val usedPercentage: Float
) {
    val totalGbFormatted: String
        get() = String.format("%.2f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))

    val availGbFormatted: String
        get() = String.format("%.2f GB", availBytes / (1024.0 * 1024.0 * 1024.0))

    val usedGbFormatted: String
        get() = String.format("%.2f GB", usedBytes / (1024.0 * 1024.0 * 1024.0))
}

data class RamBudgetConfig(
    val games: Int = 1,
    val socialApps: Int = 4,
    val browserTabs: Int = 6,
    val bgServices: Int = 3
)

data class RamBudgetResult(
    val systemOsMb: Int = 1350,
    val hardwareReservedMb: Int = 1850,
    val gamesMb: Int,
    val socialMb: Int,
    val browserMb: Int,
    val bgServicesMb: Int,
    val totalRequiredMb: Int,
    val availableAppSpaceMb: Int = 4992, // 8192 - (1350 + 1850)
    val totalDeviceRamMb: Int = 8192,
    val memoryPressureState: MemoryPressureState,
    val remainingMb: Int
)

enum class MemoryPressureState(val label: String, val description: String) {
    COMFORTABLE("Comfortable", "Plenty of RAM left. Apps remain in background cache instantly."),
    MODERATE("Moderate", "Smooth multi-tasking. Background apps resume quickly without reload."),
    HIGH("High Memory Pressure", "ZRAM compression is active. Some older background apps may reload."),
    CRITICAL("LMK Killing Active", "Exceeds 8 GB! Android Low Memory Killer will force-close background apps.")
}

enum class RamStandard(
    val title: String,
    val speedMt: Int,
    val bandwidthGbps: Double,
    val typicalVoltage: String,
    val commonIn: String
) {
    LPDDR4X("LPDDR4X", 4266, 34.1, "0.6V / 1.1V", "Budget to Mid-range 8GB phones"),
    LPDDR5("LPDDR5", 6400, 51.2, "0.5V / 1.05V", "Upper Mid-range & Flagships"),
    LPDDR5X("LPDDR5X", 8533, 68.2, "0.5V / 1.05V", "Premium 8GB/12GB/16GB Flagships")
}

data class MemoryBenchmarkResult(
    val allocatedMb: Int,
    val durationMs: Long,
    val speedMbPerSec: Double,
    val timestamp: Long = System.currentTimeMillis()
)
