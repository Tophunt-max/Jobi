package com.example.util

import androidx.compose.ui.graphics.Color
import java.util.concurrent.ConcurrentHashMap

data class BrandInfo(
    val monogram: String,
    val gradientColors: List<Color>
)

object JobBrandHelper {
    private val upscColors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
    private val sscColors = listOf(Color(0xFF0F766E), Color(0xFF0D9488))
    private val rrbColors = listOf(Color(0xFFB45309), Color(0xFFD97706))
    private val bankColors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))
    private val defColors = listOf(Color(0xFF065F46), Color(0xFF059669))
    private val eduColors = listOf(Color(0xFF7C2D12), Color(0xFFEA580C))
    private val policeColors = listOf(Color(0xFF312E81), Color(0xFF4338CA))
    private val pscColors = listOf(Color(0xFF6B21A8), Color(0xFF9333EA))
    private val medColors = listOf(Color(0xFF047857), Color(0xFF10B981))
    private val defaultColors = listOf(Color(0xFF1E293B), Color(0xFF475569))

    private val brandCache = ConcurrentHashMap<String, BrandInfo>(128)

    fun getBrand(org: String, category: String = "", title: String = ""): BrandInfo {
        val cacheKey = "$org|$category|$title"
        val cached = brandCache[cacheKey]
        if (cached != null) return cached

        val lowerOrg = org.lowercase()
        val lowerCat = category.lowercase()
        val lowerTitle = title.lowercase()
        val combined = "$lowerOrg $lowerCat $lowerTitle"

        val info = when {
            combined.contains("upsc") || combined.contains("union public") -> {
                BrandInfo("UPSC", upscColors)
            }
            combined.contains("ssc") || combined.contains("staff selection") -> {
                BrandInfo("SSC", sscColors)
            }
            combined.contains("railway") || combined.contains("rrb") || combined.contains("rrc") -> {
                BrandInfo("RRB", rrbColors)
            }
            combined.contains("bank") || combined.contains("ibps") || combined.contains("sbi") || combined.contains("rbi") -> {
                BrandInfo("BANK", bankColors)
            }
            combined.contains("defence") || combined.contains("defense") || combined.contains("army") ||
                combined.contains("navy") || combined.contains("air force") || combined.contains("nda") || combined.contains("cds") -> {
                BrandInfo("DEF", defColors)
            }
            combined.contains("police") || combined.contains("constable") || combined.contains("sub inspector") -> {
                BrandInfo("POLICE", policeColors)
            }
            combined.contains("teach") || combined.contains("tet") || combined.contains("ctet") ||
                combined.contains("kvs") || combined.contains("nvs") || combined.contains("professor") -> {
                BrandInfo("EDU", eduColors)
            }
            combined.contains("psc") || combined.contains("bpsc") || combined.contains("uppsc") ||
                combined.contains("mppsc") || combined.contains("wbpsc") || combined.contains("rpsc") -> {
                BrandInfo("PSC", pscColors)
            }
            combined.contains("medical") || combined.contains("health") || combined.contains("aiims") || combined.contains("nurse") -> {
                BrandInfo("MED", medColors)
            }
            else -> {
                val monogram = when {
                    org.isNotBlank() -> org.take(4).uppercase()
                    category.isNotBlank() -> category.take(4).uppercase()
                    else -> "GOVT"
                }
                BrandInfo(monogram, defaultColors)
            }
        }

        brandCache[cacheKey] = info
        return info
    }
}
