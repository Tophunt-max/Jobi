package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiMentorRepository
import com.example.data.repository.AiResponseState
import com.example.model.JobEntity
import com.example.model.UserEligibilityProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiMentorViewModel(
    private val aiRepository: AiMentorRepository = AiMentorRepository()
) : ViewModel() {

    private val _responseState = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val responseState: StateFlow<AiResponseState> = _responseState.asStateFlow()

    private val _currentQueryTitle = MutableStateFlow<String>("")
    val currentQueryTitle: StateFlow<String> = _currentQueryTitle.asStateFlow()

    fun evaluateJobEligibility(profile: UserEligibilityProfile, job: JobEntity) {
        _currentQueryTitle.value = "Eligibility Analysis: ${job.organization} (${job.advtNumber})"
        _responseState.value = AiResponseState.Loading
        viewModelScope.launch {
            try {
                val result = aiRepository.analyzeJobEligibility(profile, job)
                _responseState.value = AiResponseState.Success(result)
            } catch (e: Exception) {
                _responseState.value = AiResponseState.Error("Evaluation failed: ${e.message}")
            }
        }
    }

    fun getExamPreparationStrategy(examName: String, job: JobEntity?) {
        _currentQueryTitle.value = "Preparation Strategy: $examName"
        _responseState.value = AiResponseState.Loading
        viewModelScope.launch {
            try {
                val result = aiRepository.getExamStrategyAndSyllabusGuidance(examName, job)
                _responseState.value = AiResponseState.Success(result)
            } catch (e: Exception) {
                _responseState.value = AiResponseState.Error("Strategy generation failed: ${e.message}")
            }
        }
    }

    fun askCustomQuestion(query: String, profile: UserEligibilityProfile) {
        if (query.isBlank()) return
        _currentQueryTitle.value = query
        _responseState.value = AiResponseState.Loading
        viewModelScope.launch {
            try {
                val result = aiRepository.askCustomQuery(query, profile)
                _responseState.value = AiResponseState.Success(result)
            } catch (e: Exception) {
                _responseState.value = AiResponseState.Error("Query failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _responseState.value = AiResponseState.Idle
        _currentQueryTitle.value = ""
    }
}
