package com.example.util

import com.example.model.CasteCategory
import com.example.model.UserEligibilityProfile
import java.util.Locale

enum class PhysicalEligibilityStatus(val label: String, val colorHex: Long) {
    PASS("Eligible (Pass)", 0xFF059669),
    BORDERLINE("Borderline (Marginal)", 0xFFD97706),
    BELOW_CUTOFF("Below Cutoff", 0xFFDC2626)
}

data class PhysicalBenchmarkResult(
    val forceName: String,
    val postTitle: String,
    val requiredHeightCm: Float,
    val candidateHeightCm: Float,
    val heightStatus: PhysicalEligibilityStatus,
    val heightDifferenceCm: Float,
    val requiredChest: String?, // e.g. "79 - 84 cm (+5 cm expansion)" or null for females
    val chestStatus: PhysicalEligibilityStatus?,
    val runningTest: String,
    val overallStatus: PhysicalEligibilityStatus,
    val tips: String
)

object PhysicalStandardsChecker {

    fun evaluateAllForces(profile: UserEligibilityProfile): List<PhysicalBenchmarkResult> {
        val isFemale = profile.gender.equals("FEMALE", ignoreCase = true) || profile.isWomenCandidate
        val isST = profile.category == CasteCategory.ST

        val results = mutableListOf<PhysicalBenchmarkResult>()

        // 1. UP Police Constable / Sub-Inspector
        val upPoliceHeightReq = when {
            isFemale && isST -> 147f
            isFemale -> 152f
            isST -> 160f
            else -> 168f
        }
        val upPoliceDiff = profile.heightCm - upPoliceHeightReq
        val upPoliceHeightStatus = when {
            upPoliceDiff >= 0f -> PhysicalEligibilityStatus.PASS
            upPoliceDiff >= -1.0f -> PhysicalEligibilityStatus.BORDERLINE
            else -> PhysicalEligibilityStatus.BELOW_CUTOFF
        }
        val upPoliceChestReq = if (!isFemale) (if (isST) "77 - 82 cm" else "79 - 84 cm (+5 cm)") else null
        val upPoliceChestStatus = if (!isFemale) {
            val unexpandedReq = if (isST) 77f else 79f
            val expansion = profile.chestExpandedCm - profile.chestUnexpandedCm
            if (profile.chestUnexpandedCm >= unexpandedReq && expansion >= 5f) PhysicalEligibilityStatus.PASS
            else if (profile.chestUnexpandedCm >= unexpandedReq - 1f) PhysicalEligibilityStatus.BORDERLINE
            else PhysicalEligibilityStatus.BELOW_CUTOFF
        } else null

        val upPoliceOverall = if (isFemale) upPoliceHeightStatus else {
            if (upPoliceHeightStatus == PhysicalEligibilityStatus.PASS && upPoliceChestStatus == PhysicalEligibilityStatus.PASS) PhysicalEligibilityStatus.PASS
            else if (upPoliceHeightStatus == PhysicalEligibilityStatus.BELOW_CUTOFF || upPoliceChestStatus == PhysicalEligibilityStatus.BELOW_CUTOFF) PhysicalEligibilityStatus.BELOW_CUTOFF
            else PhysicalEligibilityStatus.BORDERLINE
        }

        results.add(
            PhysicalBenchmarkResult(
                forceName = "UP Police",
                postTitle = "Constable & Sub-Inspector (Civil)",
                requiredHeightCm = upPoliceHeightReq,
                candidateHeightCm = profile.heightCm,
                heightStatus = upPoliceHeightStatus,
                heightDifferenceCm = upPoliceDiff,
                requiredChest = upPoliceChestReq,
                chestStatus = upPoliceChestStatus,
                runningTest = if (isFemale) "2.4 km run in 14 minutes" else "4.8 km run in 25 minutes",
                overallStatus = upPoliceOverall,
                tips = if (upPoliceDiff >= 0) "Safe height clearance of +${String.format(Locale.US, "%.1f", upPoliceDiff)} cm." else "Short by ${String.format(Locale.US, "%.1f", -upPoliceDiff)} cm for UP Police standard."
            )
        )

        // 2. SSC GD Constable (CRPF, BSF, CISF, ITBP, SSB, SSF)
        val sscGdHeightReq = when {
            isFemale && isST -> 150f
            isFemale -> 157f
            isST -> 162.5f
            else -> 170f
        }
        val sscGdDiff = profile.heightCm - sscGdHeightReq
        val sscGdHeightStatus = when {
            sscGdDiff >= 0f -> PhysicalEligibilityStatus.PASS
            sscGdDiff >= -1.0f -> PhysicalEligibilityStatus.BORDERLINE
            else -> PhysicalEligibilityStatus.BELOW_CUTOFF
        }
        val sscGdChestReq = if (!isFemale) (if (isST) "76 - 81 cm" else "80 - 85 cm (+5 cm)") else null
        val sscGdChestStatus = if (!isFemale) {
            val unexpandedReq = if (isST) 76f else 80f
            val expansion = profile.chestExpandedCm - profile.chestUnexpandedCm
            if (profile.chestUnexpandedCm >= unexpandedReq && expansion >= 5f) PhysicalEligibilityStatus.PASS
            else if (profile.chestUnexpandedCm >= unexpandedReq - 1f) PhysicalEligibilityStatus.BORDERLINE
            else PhysicalEligibilityStatus.BELOW_CUTOFF
        } else null

        val sscGdOverall = if (isFemale) sscGdHeightStatus else {
            if (sscGdHeightStatus == PhysicalEligibilityStatus.PASS && sscGdChestStatus == PhysicalEligibilityStatus.PASS) PhysicalEligibilityStatus.PASS
            else if (sscGdHeightStatus == PhysicalEligibilityStatus.BELOW_CUTOFF || sscGdChestStatus == PhysicalEligibilityStatus.BELOW_CUTOFF) PhysicalEligibilityStatus.BELOW_CUTOFF
            else PhysicalEligibilityStatus.BORDERLINE
        }

        results.add(
            PhysicalBenchmarkResult(
                forceName = "SSC GD Constable",
                postTitle = "Central Armed Police Forces (BSF, CISF, CRPF)",
                requiredHeightCm = sscGdHeightReq,
                candidateHeightCm = profile.heightCm,
                heightStatus = sscGdHeightStatus,
                heightDifferenceCm = sscGdDiff,
                requiredChest = sscGdChestReq,
                chestStatus = sscGdChestStatus,
                runningTest = if (isFemale) "1.6 km run in 8.5 minutes" else "5.0 km run in 24 minutes",
                overallStatus = sscGdOverall,
                tips = if (sscGdDiff >= 0) "Fully compliant with CAPF Central standards (+${String.format(Locale.US, "%.1f", sscGdDiff)} cm)." else "Requires minimum 170 cm for General/OBC male candidates."
            )
        )

        // 3. Indian Army Agniveer GD / Clerk
        val armyHeightReq = if (isFemale) 152f else 169f
        val armyDiff = profile.heightCm - armyHeightReq
        val armyHeightStatus = when {
            armyDiff >= 0f -> PhysicalEligibilityStatus.PASS
            armyDiff >= -1.0f -> PhysicalEligibilityStatus.BORDERLINE
            else -> PhysicalEligibilityStatus.BELOW_CUTOFF
        }
        val armyChestReq = if (!isFemale) "77 - 82 cm (+5 cm)" else null
        val armyChestStatus = if (!isFemale) {
            val expansion = profile.chestExpandedCm - profile.chestUnexpandedCm
            if (profile.chestUnexpandedCm >= 77f && expansion >= 5f) PhysicalEligibilityStatus.PASS
            else PhysicalEligibilityStatus.BELOW_CUTOFF
        } else null

        val armyOverall = if (isFemale) armyHeightStatus else {
            if (armyHeightStatus == PhysicalEligibilityStatus.PASS && armyChestStatus == PhysicalEligibilityStatus.PASS) PhysicalEligibilityStatus.PASS
            else PhysicalEligibilityStatus.BELOW_CUTOFF
        }

        results.add(
            PhysicalBenchmarkResult(
                forceName = "Indian Army",
                postTitle = "Agniveer General Duty & Technical",
                requiredHeightCm = armyHeightReq,
                candidateHeightCm = profile.heightCm,
                heightStatus = armyHeightStatus,
                heightDifferenceCm = armyDiff,
                requiredChest = armyChestReq,
                chestStatus = armyChestStatus,
                runningTest = "1.6 km run (Group 1: <= 5m 30s | Group 2: <= 5m 45s) + 10 Pull-ups",
                overallStatus = armyOverall,
                tips = if (armyDiff >= 0) "Height qualified for Indian Army ground rally (+${String.format(Locale.US, "%.1f", armyDiff)} cm)." else "Below standard Army rally requirement."
            )
        )

        // 4. NDA & CDS Defence Services (Army / Navy / Airforce)
        val ndaHeightReq = if (isFemale) 152f else 157f
        val ndaDiff = profile.heightCm - ndaHeightReq
        val ndaHeightStatus = when {
            ndaDiff >= 0f -> PhysicalEligibilityStatus.PASS
            else -> PhysicalEligibilityStatus.BELOW_CUTOFF
        }

        results.add(
            PhysicalBenchmarkResult(
                forceName = "UPSC NDA & CDS",
                postTitle = "Commissioned Officers (Army Wing)",
                requiredHeightCm = ndaHeightReq,
                candidateHeightCm = profile.heightCm,
                heightStatus = ndaHeightStatus,
                heightDifferenceCm = ndaDiff,
                requiredChest = if (!isFemale) "Well-developed (+5 cm expansion)" else null,
                chestStatus = null,
                runningTest = "2.4 km run in 15 minutes, skipping, push-ups",
                overallStatus = ndaHeightStatus,
                tips = if (ndaDiff >= 0) "Clearance achieved for NDA/CDS officer cadre (+${String.format(Locale.US, "%.1f", ndaDiff)} cm)." else "Below minimum medical board cutoff."
            )
        )

        // 5. Railway Protection Force (RPF) Constable & SI
        val rpfHeightReq = when {
            isFemale && (isST || profile.category == CasteCategory.SC) -> 152f
            isFemale -> 157f
            isST || profile.category == CasteCategory.SC -> 160f
            else -> 165f
        }
        val rpfDiff = profile.heightCm - rpfHeightReq
        val rpfHeightStatus = when {
            rpfDiff >= 0f -> PhysicalEligibilityStatus.PASS
            rpfDiff >= -1.0f -> PhysicalEligibilityStatus.BORDERLINE
            else -> PhysicalEligibilityStatus.BELOW_CUTOFF
        }

        results.add(
            PhysicalBenchmarkResult(
                forceName = "RPF (Railways)",
                postTitle = "Sub-Inspector & Constable",
                requiredHeightCm = rpfHeightReq,
                candidateHeightCm = profile.heightCm,
                heightStatus = rpfHeightStatus,
                heightDifferenceCm = rpfDiff,
                requiredChest = if (!isFemale) "80 - 85 cm (+5 cm)" else null,
                chestStatus = null,
                runningTest = if (isFemale) "800m run in 3m 40s + Long Jump 9 ft" else "1600m run in 5m 45s + Long Jump 14 ft",
                overallStatus = rpfHeightStatus,
                tips = if (rpfDiff >= 0) "Comfortable clearance for Railway Police (+${String.format(Locale.US, "%.1f", rpfDiff)} cm)." else "Short for RPF open recruitment."
            )
        )

        return results
    }
}
