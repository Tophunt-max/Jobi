package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GeminiThinkingConfig
import com.example.model.JobEntity
import com.example.model.UserEligibilityProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AiResponseState {
    data object Idle : AiResponseState()
    data object Loading : AiResponseState()
    data class Success(val responseText: String, val isFromLiveModel: Boolean = true) : AiResponseState()
    data class Error(val errorMessage: String, val fallbackAnswer: String? = null) : AiResponseState()
}

class AiMentorRepository {

    suspend fun analyzeJobEligibility(
        userProfile: UserEligibilityProfile,
        targetJob: JobEntity
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("You are an expert Indian Government Recruitment & Reservation Policy Mentor.")
            appendLine("Carefully evaluate this candidate's exact eligibility with thorough legal reasoning.")
            appendLine()
            appendLine("CANDIDATE PROFILE:")
            appendLine("- Age: ${userProfile.age} Years")
            appendLine("- Category: ${userProfile.category.displayName} (Standard Age Relaxation: ${userProfile.category.standardRelaxationYears} yrs)")
            appendLine("- Educational Qualification: ${userProfile.qualification.displayName}")
            appendLine("- Degree: ${userProfile.specificDegree}")
            appendLine("- Home State: ${userProfile.homeState}")
            appendLine("- PwD Status: ${if (userProfile.isPwD) "Yes" else "No"}")
            appendLine("- Ex-Servicemen: ${if (userProfile.isExServicemen) "Yes" else "No"}")
            appendLine()
            appendLine("JOB VACANCY DETAILS:")
            appendLine("- Title: ${targetJob.title}")
            appendLine("- Recruiting Organization: ${targetJob.organization}")
            appendLine("- Advt No: ${targetJob.advtNumber}")
            appendLine("- State / Scope: ${targetJob.stateOrAllIndia}")
            appendLine("- Total Vacancies: ${targetJob.totalVacancies}")
            appendLine("- Min Age: ${targetJob.minAge}, Max Age (UR): ${targetJob.maxAge}")
            appendLine("- Age Cutoff Date: ${targetJob.ageCutoffDate}")
            appendLine("- Official Qualification: ${targetJob.qualification}")
            appendLine("- Category Age Relaxation Matrix: ${targetJob.categoryAgeRelaxationJson}")
            appendLine("- Selection Process: ${targetJob.selectionProcess}")
            appendLine("- Application Fee: General: ${targetJob.feeGeneral}, Reserved: ${targetJob.feeReserved}")
            appendLine()
            appendLine("TASK:")
            appendLine("1. ELIGIBILITY VERDICT: State clearly whether the candidate is ELIGIBLE, INELIGIBLE, or CONDITIONALLY ELIGIBLE.")
            appendLine("2. AGE ANALYSIS: Calculate candidate's effective age on cutoff date with category-specific relaxation. Explain the exact math.")
            appendLine("3. EDUCATIONAL CRITERIA: Check if degree/level matches post requirements.")
            appendLine("4. STATE DOMICILE & RESERVATION: Explain whether candidate receives home-state reservation benefits or will be treated as Unreserved/General.")
            appendLine("5. FEE & APPLICATION TIPS: Advise on application fee exemption and crucial documents required (e.g., OBC-NCL certificate crucial date, EWS validity, PwD disability percentage).")
            appendLine("Format with clean headers and bullet points for mobile readability.")
        }

        callGeminiWithHighThinking(prompt) ?: generateOfflineEligibilityAssessment(userProfile, targetJob)
    }

    suspend fun getExamStrategyAndSyllabusGuidance(
        examName: String,
        targetJob: JobEntity?
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("You are an expert Indian Government Examination Coach (UPSC, SSC, Banking, Railways).")
            appendLine("Provide a high-yield, step-by-step preparation strategy for: $examName.")
            if (targetJob != null) {
                appendLine("Recruiting Body: ${targetJob.organization}")
                appendLine("Selection Stages: ${targetJob.selectionProcess}")
                appendLine("Exam Timeline: ${targetJob.examDate}")
            }
            appendLine()
            appendLine("TASK:")
            appendLine("1. EXAM PATTERN & TIME MANAGEMENT: Breakdown of sections, marks, negative marking discipline.")
            appendLine("2. HIGH-WEIGHTAGE TOPICS: Top 5 essential scoring areas in General Awareness, Quantitative Aptitude, Reasoning, and English/Language.")
            appendLine("3. 90-DAY STUDY ROADMAP: Phase 1 (Foundation), Phase 2 (Sectional Tests), Phase 3 (Full-length Mocks & Error Analysis).")
            appendLine("4. OFFICIAL RESOURCES & PYQs: Recommendations for standard reference books & previous year papers.")
            appendLine("Structure with clear, actionable advice.")
        }

        callGeminiWithHighThinking(prompt) ?: generateOfflineStrategyGuidance(examName)
    }

    suspend fun askCustomQuery(
        query: String,
        userProfile: UserEligibilityProfile
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("You are the official AI Sarkari Job & Examination Advisor for Indian aspirants.")
            appendLine("Candidate Profile: Age: ${userProfile.age}, Category: ${userProfile.category.displayName}, Qualification: ${userProfile.qualification.displayName}, State: ${userProfile.homeState}.")
            appendLine("USER QUESTION:")
            appendLine(query)
            appendLine()
            appendLine("Provide a verified, factual, and encouraging response based on official Indian government recruitment rules (DoPT, UPSC, SSC, RRB, IBPS).")
        }

        callGeminiWithHighThinking(prompt) ?: generateOfflineQueryResponse(query, userProfile)
    }

    private suspend fun callGeminiWithHighThinking(promptText: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = promptText)),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    thinkingConfig = GeminiThinkingConfig(thinkingLevel = "HIGH"),
                    temperature = 0.4f
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = "You are the AI Sarkari Job & Eligibility Mentor. You provide rigorous, accurate, and structured analysis of Indian government recruitments, reservation policies, age relaxations, and exam strategies. Always cite official gazette criteria and clarify that candidates should confirm with the original advertisement."
                        )
                    )
                )
            )

            val response = GeminiApiClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            null
        }
    }

    // High quality offline heuristic fallback
    private fun generateOfflineEligibilityAssessment(
        profile: UserEligibilityProfile,
        job: JobEntity
    ): String {
        val maxAgeWithRelaxation = job.maxAge + profile.category.standardRelaxationYears
        val isAgeEligible = profile.age in job.minAge..maxAgeWithRelaxation
        val isFeeExempted = profile.category in listOf(
            com.example.model.CasteCategory.SC,
            com.example.model.CasteCategory.ST,
            com.example.model.CasteCategory.PWD_GENERAL,
            com.example.model.CasteCategory.PWD_OBC,
            com.example.model.CasteCategory.PWD_SC_ST
        ) || profile.isWomenCandidate

        return buildString {
            appendLine("📋 ELIGIBILITY ASSESSMENT REPORT")
            appendLine("══════════════════════════════════════")
            appendLine("Job: ${job.title}")
            appendLine("Recruiting Body: ${job.organization} (Advt No: ${job.advtNumber})")
            appendLine()
            if (isAgeEligible) {
                appendLine("✅ VERDICT: ELIGIBLE")
                appendLine("You satisfy the age and category reservation criteria for this recruitment.")
            } else {
                appendLine("⚠️ VERDICT: AGE CRITERIA CHECK NEEDED")
                appendLine("Your age (${profile.age} years) exceeds the maximum age limit including category relaxation (${maxAgeWithRelaxation} years).")
            }
            appendLine()
            appendLine("1. AGE RELAXATION CALCULATION:")
            appendLine("• Unreserved (UR) Age Limit: ${job.minAge} to ${job.maxAge} Years")
            appendLine("• Candidate Category: ${profile.category.displayName}")
            appendLine("• Applicable Category Relaxation: +${profile.category.standardRelaxationYears} Years")
            appendLine("• Effective Upper Age Limit: $maxAgeWithRelaxation Years (Cutoff Date: ${job.ageCutoffDate})")
            appendLine("• Candidate Current Age: ${profile.age} Years")
            appendLine()
            appendLine("2. EDUCATIONAL QUALIFICATION:")
            appendLine("• Official Requirement: ${job.qualification}")
            appendLine("• Candidate Qualification: ${profile.qualification.displayName}")
            appendLine("• Specific Degree: ${profile.specificDegree}")
            appendLine()
            appendLine("3. APPLICATION FEE STATUS:")
            if (isFeeExempted) {
                appendLine("• Fee: EXEMPTED (Nil fee for ${profile.category.displayName} candidates)")
            } else {
                appendLine("• General/OBC Fee: ${job.feeGeneral} (${job.feeMode})")
            }
            appendLine()
            appendLine("4. CRITICAL DOCUMENT CHECKLIST:")
            appendLine("• Valid Category/Caste Certificate (issued before closing date)")
            appendLine("• Non-Creamy Layer (OBC-NCL) certificate for current financial year")
            appendLine("• Matriculation (10th) Certificate for Date of Birth verification")
            appendLine("• Degree Marksheets / Provisional Certificate")
            appendLine()
            appendLine("📌 Note: Sourced from official notification: ${job.verifiedSourceDomain}")
        }
    }

    private fun generateOfflineStrategyGuidance(examName: String): String {
        return """
🎯 EXAM PREPARATION BLUEPRINT: $examName
══════════════════════════════════════

1. SELECTION STAGES & EXAM STRUCTURE:
• Phase 1 (Screening / Prelims): Speed & Accuracy (Objective CBT)
• Phase 2 (Mains / Technical): Depth of Knowledge & Analytical Skills
• Phase 3: Document Verification & Medical / Skill Test

2. HIGH-YIELD TOPICS TO MASTER:
• Quantitative Aptitude: Arithmetic (Percentages, Ratio, SI/CI, Time & Work), Number Systems, Data Interpretation
• Reasoning: Syllogism, Blood Relations, Coding-Decoding, Seating Arrangements, Non-Verbal
• General Awareness: Indian Polity (Articles & Amendments), Modern History, Current Affairs (Last 6 Months), Science & Tech
• English Comprehension: Reading Comprehension, Cloze Test, Error Detection, Active/Passive Voice

3. 90-DAY PREPARATION SCHEDULE:
• Month 1: Complete 100% syllabus coverage and build chapter-wise short notes.
• Month 2: Solve 10 years Chapter-wise Previous Year Questions (PYQs).
• Month 3: Attempt 2 Full-Length Mocks every week in exact examination time-slot with rigorous error logging.

💡 Golden Rule: Accuracy is paramount due to negative marking (typically 0.33 to 0.50 marks per wrong answer).
        """.trimIndent()
    }

    private fun generateOfflineQueryResponse(query: String, profile: UserEligibilityProfile): String {
        return """
📌 ADVISORY RESPONSE FOR ${profile.category.displayName.uppercase()} CANDIDATE
══════════════════════════════════════

Regarding your query: "$query"

• Official Rule Reference:
Central and State Government recruitments strictly adhere to reservation guidelines issued by DoPT and respective State Public Service Commissions.

• Crucial Dates:
Ensure your certificates (Caste, EWS income certificate, PwD benchmark disability certificate) are issued within the crucial validity period specified in the official advertisement.

• Verification Recommendation:
Always cross-verify on the official recruiting portal and download the original gazette PDF notice to verify post-specific clauses.
        """.trimIndent()
    }
}
