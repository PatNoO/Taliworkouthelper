package com.example.taliworkouthelper.partner

import kotlinx.coroutines.flow.Flow

data class Partner(val id: String, val name: String)

interface PartnerRepository {
    fun availablePartners(): Flow<List<Partner>>
    suspend fun connect(partnerId: String): Result<Partner>
    suspend fun disconnect()
}
