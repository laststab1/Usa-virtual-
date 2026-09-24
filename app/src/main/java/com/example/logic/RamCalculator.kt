package com.example.logic

import android.app.ActivityManager
import android.content.Context
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import kotlin.system.measureTimeMillis

class RamCalculator {

    /**
     * Reads current hardware & OS memory state via Android ActivityManager
     */
    fun readDeviceRam(context: Context): DeviceRamStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)

        val total = memoryInfo.totalMem
        val avail = memoryInfo.availMem
        val threshold = memoryInfo.threshold
        val isLow = memoryInfo.lowMemory
        val used = (total - avail).coerceAtLeast(0L)
        val percentage = if (total > 0) (used.toFloat() / total.toFloat()) * 100f else 0f

        return DeviceRamStats(
            totalBytes = total,
            availBytes = avail,
            thresholdBytes = threshold,
            isLowMemory = isLow,
            usedBytes = used,
            usedPercentage = percentage
        )
    }

    /**
     * Calculates RAM multitasking budget specifically for an 8 GB (8,192 MB) Mobile Device
     */
    fun calculate8GbBudget(config: RamBudgetConfig): RamBudgetResult {
        val systemOsMb = 1350
        val hwReservedMb = 1850

        // Average realistic memory footprints on modern Android:
        val gamesMb = config.games * 1650 // 3D Heavy game (e.g. Genshin, CoD, PUBG)
        val socialMb = config.socialApps * 380 // Instagram, TikTok, Facebook, Twitter/X
        val browserMb = config.browserTabs * 110 // Chrome/Firefox tabs with dynamic DOM
        val bgServicesMb = config.bgServices * 160 // Spotify, WhatsApp, VPN, Smartwatch sync

        val totalAppsMb = gamesMb + socialMb + browserMb + bgServicesMb
        val totalRequiredMb = systemOsMb + hwReservedMb + totalAppsMb
        val totalDeviceRamMb = 8192
        val remainingMb = totalDeviceRamMb - totalRequiredMb

        val pressure = when {
            totalRequiredMb <= 5800 -> MemoryPressureState.COMFORTABLE
            totalRequiredMb <= 7200 -> MemoryPressureState.MODERATE
            totalRequiredMb <= 8192 -> MemoryPressureState.HIGH
            else -> MemoryPressureState.CRITICAL
        }

        return RamBudgetResult(
            systemOsMb = systemOsMb,
            hardwareReservedMb = hwReservedMb,
            gamesMb = gamesMb,
            socialMb = socialMb,
            browserMb = browserMb,
            bgServicesMb = bgServicesMb,
            totalRequiredMb = totalRequiredMb,
            availableAppSpaceMb = totalDeviceRamMb - (systemOsMb + hwReservedMb),
            totalDeviceRamMb = totalDeviceRamMb,
            memoryPressureState = pressure,
            remainingMb = remainingMb
        )
    }

    /**
     * Unit conversion between Bytes, KB, MB, GB, TB
     */
    fun convertMemoryUnit(
        value: Double,
        fromUnit: String,
        isBinary: Boolean // true: 1024, false: 1000
    ): Map<String, String> {
        val base: Double = if (isBinary) 1024.0 else 1000.0

        // Normalize to bytes first
        val bytes = when (fromUnit.uppercase()) {
            "BYTES", "B" -> value
            "KB", "KIB" -> value * base
            "MB", "MIB" -> value * base * base
            "GB", "GIB" -> value * base * base * base
            "TB", "TIB" -> value * base * base * base * base
            else -> value
        }

        val df = DecimalFormat("#,##0.####")
        val dfLarge = DecimalFormat("#,##0")

        val b = bytes
        val kb = bytes / base
        val mb = bytes / (base * base)
        val gb = bytes / (base * base * base)
        val tb = bytes / (base * base * base * base)

        return mapOf(
            "Bytes" to dfLarge.format(b.toLong()),
            (if (isBinary) "KiB" else "KB") to df.format(kb),
            (if (isBinary) "MiB" else "MB") to df.format(mb),
            (if (isBinary) "GiB" else "GB") to df.format(gb),
            (if (isBinary) "TiB" else "TB") to df.format(tb)
        )
    }

    /**
     * Safe controlled memory allocation benchmark:
     * Allocates small byte arrays up to testMb (e.g. 40MB) and measures read/write speed
     */
    suspend fun runSafeMemoryBenchmark(targetMb: Int = 40): MemoryBenchmarkResult = withContext(Dispatchers.Default) {
        val safeMb = targetMb.coerceIn(10, 60)
        val chunkSize = 1024 * 1024 // 1 MB chunks
        val buffers = ArrayList<ByteArray>(safeMb)

        val duration = measureTimeMillis {
            // Allocate and write
            for (i in 0 until safeMb) {
                val block = ByteArray(chunkSize)
                // Touch memory pages to force real physical RAM mapping
                for (j in 0 until chunkSize step 128) {
                    block[j] = (i + j).toByte()
                }
                buffers.add(block)
            }
            // Read verification pass
            var checksum = 0L
            for (block in buffers) {
                for (j in 0 until chunkSize step 256) {
                    checksum += block[j]
                }
            }
            if (checksum == 42L) {
                // dummy check to avoid JIT optimization elimination
                buffers.clear()
            }
        }
        buffers.clear()
        System.gc()

        val speed = if (duration > 0) (safeMb.toDouble() / (duration.toDouble() / 1000.0)) else 0.0

        MemoryBenchmarkResult(
            allocatedMb = safeMb,
            durationMs = duration,
            speedMbPerSec = speed
        )
    }
}
