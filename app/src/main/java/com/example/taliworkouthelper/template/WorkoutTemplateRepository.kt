package com.example.taliworkouthelper.template

import kotlinx.coroutines.flow.Flow

interface WorkoutTemplateRepository {
    fun getTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun addTemplate(template: WorkoutTemplate): Result<WorkoutTemplate>
    suspend fun updateTemplate(template: WorkoutTemplate): Result<WorkoutTemplate>
}
