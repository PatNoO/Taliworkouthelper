package com.example.taliworkouthelper.template

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Persists workout templates for the authenticated user.
 *
 * Schema:
 * users/{uid}/workoutTemplates/{templateId}
 * - title: String
 * - exercises: [{name: String, sets: Number, reps: Number}]
 * - updatedAt: server timestamp
 */
class FirestoreWorkoutTemplateRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : WorkoutTemplateRepository {

    override fun getTemplates(): Flow<List<WorkoutTemplate>> = callbackFlow {
        val uid = requireUserId()
        val listener = templatesCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val templates = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val title = doc.getString(TITLE_FIELD) ?: return@mapNotNull null
                val rawExercises = (doc.get(EXERCISES_FIELD) as? List<*>)?.filterIsInstance<Map<String, Any?>>().orEmpty()

                val exercises = rawExercises.mapNotNull { raw ->
                    val name = raw[EXERCISE_NAME_FIELD] as? String ?: return@mapNotNull null
                    val sets = (raw[EXERCISE_SETS_FIELD] as? Number)?.toInt() ?: return@mapNotNull null
                    val reps = (raw[EXERCISE_REPS_FIELD] as? Number)?.toInt() ?: return@mapNotNull null
                    TemplateExercise(name = name, sets = sets, reps = reps)
                }

                WorkoutTemplate(id = doc.id, title = title, exercises = exercises)
            }

            trySend(templates)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addTemplate(template: WorkoutTemplate): Result<WorkoutTemplate> {
        return runCatching {
            val uid = requireUserId()
            templatesCollection(uid)
                .document(template.id)
                .set(template.toFirestoreMap(), SetOptions.merge())
                .await()
            template
        }
    }

    override suspend fun updateTemplate(template: WorkoutTemplate): Result<WorkoutTemplate> {
        return runCatching {
            val uid = requireUserId()
            templatesCollection(uid)
                .document(template.id)
                .set(template.toFirestoreMap(), SetOptions.merge())
                .await()
            template
        }
    }

    private fun templatesCollection(uid: String) =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUT_TEMPLATES_COLLECTION)

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
    }

    private fun WorkoutTemplate.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            TITLE_FIELD to title,
            EXERCISES_FIELD to exercises.map { exercise ->
                mapOf(
                    EXERCISE_NAME_FIELD to exercise.name,
                    EXERCISE_SETS_FIELD to exercise.sets,
                    EXERCISE_REPS_FIELD to exercise.reps
                )
            },
            UPDATED_AT_FIELD to FieldValue.serverTimestamp()
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val WORKOUT_TEMPLATES_COLLECTION = "workoutTemplates"
        const val TITLE_FIELD = "title"
        const val EXERCISES_FIELD = "exercises"
        const val EXERCISE_NAME_FIELD = "name"
        const val EXERCISE_SETS_FIELD = "sets"
        const val EXERCISE_REPS_FIELD = "reps"
        const val UPDATED_AT_FIELD = "updatedAt"
    }
}
