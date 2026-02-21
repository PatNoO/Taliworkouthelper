package com.example.taliworkouthelper.template

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkoutTemplateRepository : WorkoutTemplateRepository {
    private val templatesState = MutableStateFlow<List<WorkoutTemplate>>(emptyList())

    override fun getTemplates(): Flow<List<WorkoutTemplate>> = templatesState

    override suspend fun addTemplate(template: WorkoutTemplate): Result<WorkoutTemplate> {
        templatesState.value = templatesState.value + template
        return Result.success(template)
    }

    override suspend fun updateTemplate(template: WorkoutTemplate): Result<WorkoutTemplate> {
        templatesState.value = templatesState.value.map { existing ->
            if (existing.id == template.id) template else existing
        }
        return Result.success(template)
    }
}
