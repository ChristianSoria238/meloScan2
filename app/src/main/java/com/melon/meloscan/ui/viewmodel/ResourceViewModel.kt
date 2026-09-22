package com.melon.meloscan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melon.meloscan.data.repository.ResourceRepository
import com.melon.meloscan.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResourceViewModel : ViewModel() {

    private val repository = ResourceRepository()

    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> =
        _resources.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()

    fun loadResources() {

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val result = repository.getActiveResources()

                _resources.value = result

            } catch (e: Exception) {

                e.printStackTrace()

                _resources.value = emptyList()

                _error.value = buildString {

                    append("Error type: ")
                    append(e::class.simpleName ?: "Unknown")
                    append("\n\n")

                    append("Message:\n")
                    append(e.message ?: "No error message available.")

                    append("\n\n")

                    append("Details:\n")
                    append(
                        e.stackTraceToString()
                            .take(4000)
                    )
                }

            } finally {

                _isLoading.value = false
            }
        }
    }
}