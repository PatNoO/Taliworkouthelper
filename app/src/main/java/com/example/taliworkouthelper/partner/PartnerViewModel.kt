package com.example.taliworkouthelper.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PartnerState(val partners: List<Partner> = emptyList(), val connected: Partner? = null)

class PartnerViewModel(private val repo: PartnerRepository) : ViewModel() {
    private val partnersState: StateFlow<List<Partner>> = repo.availablePartners().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val state: StateFlow<PartnerState> = partnersState
        .map { list -> PartnerState(partners = list) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PartnerState())

    fun connect(partnerId: String, onResult: (Result<Partner>) -> Unit) {
        viewModelScope.launch {
            val res = repo.connect(partnerId)
            onResult(res)
        }
    }

    fun disconnect() {
        viewModelScope.launch { repo.disconnect() }
    }
}

// no helper needed; mapping is done in-place above

