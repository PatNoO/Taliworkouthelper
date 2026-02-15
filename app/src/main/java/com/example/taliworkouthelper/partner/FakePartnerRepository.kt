package com.example.taliworkouthelper.partner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePartnerRepository : PartnerRepository {
    private val _available = MutableStateFlow(
        listOf(Partner("p1", "Alice"), Partner("p2", "Bob"))
    )
    private var _connected: Partner? = null

    override fun availablePartners(): Flow<List<Partner>> = _available.asStateFlow()

    override suspend fun connect(partnerId: String): Result<Partner> {
        val p = _available.value.find { it.id == partnerId }
        return if (p != null) {
            _connected = p
            Result.success(p)
        } else {
            Result.failure(Exception("Partner not found"))
        }
    }

    override suspend fun disconnect() {
        _connected = null
    }
}
