package com.example.model

enum class AlertFrequency(val displayName: String, val subtitle: String) {
    INSTANT("Instant Alerts", "Notify immediately when matching jobs are posted"),
    DAILY_DIGEST("Daily Digest (8:00 AM)", "Consolidated morning summary of new vacancies"),
    WEEKLY("Weekly Round-up", "Weekly digest sent every Monday morning")
}

data class JobAlertPreferences(
    val isEnabled: Boolean = true,
    val preferredStates: Set<String> = setOf("All India", "Uttar Pradesh", "Bihar", "Rajasthan", "Delhi"),
    val preferredCategories: Set<String> = setOf("SSC", "Banking", "Railways", "UPSC", "Police", "Defence", "Teaching"),
    val preferredQualifications: Set<String> = setOf("Graduate", "12th Pass", "10th Pass"),
    val minVacancies: Int = 0, // 0 = Any, 100, 500, 1000, 5000
    val alertOnClosingSoon: Boolean = true,
    val alertOnNewOpenings: Boolean = true,
    val alertFrequency: AlertFrequency = AlertFrequency.DAILY_DIGEST,
    val quietHoursEnabled: Boolean = true
)

data class MatchedJob(
    val job: JobEntity,
    val matchScorePercent: Int,
    val matchReasons: List<String>,
    val isPerfectMatch: Boolean
)

object JobAlertMatcher {

    val AvailableCategories = listOf(
        "SSC" to "📝",
        "Railways" to "🚆",
        "Banking" to "🏦",
        "Police" to "👮",
        "Defence" to "🛡️",
        "UPSC" to "🏛️",
        "Teaching" to "📚",
        "Engineering" to "⚙️",
        "Medical" to "🩺",
        "State PSC" to "🏛️",
        "PSU" to "🏢"
    )

    val AvailableQualifications = listOf(
        "10th Pass",
        "12th Pass",
        "ITI / Diploma",
        "Graduate",
        "B.Tech / BE",
        "Post Graduate",
        "LLB",
        "Medical / Nursing"
    )

    val AvailableStates = listOf(
        "All India",
        "Uttar Pradesh",
        "Bihar",
        "Rajasthan",
        "Madhya Pradesh",
        "Delhi (NCT)",
        "West Bengal",
        "Maharashtra",
        "Odisha",
        "Haryana",
        "Punjab",
        "Jharkhand",
        "Chhattisgarh",
        "Gujarat",
        "Uttarakhand",
        "Tamil Nadu",
        "Karnataka",
        "Andhra Pradesh",
        "Telangana",
        "Kerala",
        "Assam"
    )

    val VacancyThresholds = listOf(
        0 to "Any Number",
        100 to "100+ Posts",
        500 to "500+ Posts",
        1000 to "1,000+ Posts",
        5000 to "5,000+ Mega"
    )

    fun evaluateMatch(job: JobEntity, prefs: JobAlertPreferences): MatchedJob {
        var score = 0
        val reasons = mutableListOf<String>()

        // 1. Category / Sector Match (40 pts)
        val matchesCategory = prefs.preferredCategories.isEmpty() || prefs.preferredCategories.any { cat ->
            job.category.contains(cat, ignoreCase = true) ||
                job.title.contains(cat, ignoreCase = true) ||
                job.organization.contains(cat, ignoreCase = true)
        }
        if (matchesCategory) {
            score += 40
            val matchingCat = prefs.preferredCategories.firstOrNull { cat ->
                job.category.contains(cat, ignoreCase = true) || job.title.contains(cat, ignoreCase = true)
            } ?: job.category
            reasons.add("Sector: $matchingCat")
        }

        // 2. Education Qualification Match (30 pts)
        val matchesQual = prefs.preferredQualifications.isEmpty() || prefs.preferredQualifications.any { qual ->
            job.qualification.contains(qual, ignoreCase = true) ||
                job.detailedEligibility.contains(qual, ignoreCase = true)
        }
        if (matchesQual) {
            score += 30
            val matchingQual = prefs.preferredQualifications.firstOrNull { qual ->
                job.qualification.contains(qual, ignoreCase = true)
            } ?: job.qualification
            reasons.add("Eligible: $matchingQual")
        }

        // 3. State Match (20 pts)
        val isAllIndia = job.stateOrAllIndia.equals("All India", ignoreCase = true)
        val matchesState = isAllIndia ||
            prefs.preferredStates.contains("All India") ||
            prefs.preferredStates.any { state ->
                job.stateOrAllIndia.contains(state, ignoreCase = true) ||
                    state.contains(job.stateOrAllIndia, ignoreCase = true)
            }
        if (matchesState) {
            score += 20
            reasons.add(if (isAllIndia) "Pan-India Open" else "State: ${job.stateOrAllIndia}")
        }

        // 4. Vacancy Threshold (10 pts)
        if (job.totalVacancies >= prefs.minVacancies) {
            score += 10
            if (prefs.minVacancies > 0) {
                reasons.add("${job.totalVacancies} Vacancies")
            }
        }

        val finalScore = score.coerceIn(0, 100)
        return MatchedJob(
            job = job,
            matchScorePercent = finalScore,
            matchReasons = reasons,
            isPerfectMatch = finalScore >= 90
        )
    }

    fun matchJobs(jobs: List<JobEntity>, prefs: JobAlertPreferences): List<MatchedJob> {
        if (!prefs.isEnabled) return emptyList()

        return jobs.map { evaluateMatch(it, prefs) }
            .filter { it.matchScorePercent >= 60 } // Candidate matches at least 60% of their criteria
            .sortedByDescending { it.matchScorePercent }
    }
}
