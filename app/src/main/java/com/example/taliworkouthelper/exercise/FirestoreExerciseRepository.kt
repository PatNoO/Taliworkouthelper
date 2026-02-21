package com.example.taliworkouthelper.exercise

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Reads exercise library data and ensures initial seed content exists.
 *
 * Schema:
 * exercises/{exerciseId}
 * - name: String
 * - description: String
 * - muscleGroup: String
 * - equipment: String
 * - imageUrl: String
 * - updatedAt: server timestamp
 */
class FirestoreExerciseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> = flow {
        seedDefaultsIfNeeded()
        emitAll(observeExerciseSnapshots())
    }

    private fun observeExerciseSnapshots(): Flow<List<Exercise>> = callbackFlow {
        val listener = firestore.collection(EXERCISES_COLLECTION)
            .orderBy(NAME_FIELD, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val exercises = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val name = doc.getString(NAME_FIELD)?.trim().orEmpty()
                    val description = doc.getString(DESCRIPTION_FIELD)?.trim().orEmpty()
                    val muscleGroup = doc.getString(MUSCLE_GROUP_FIELD)?.trim().orEmpty()
                    val equipment = doc.getString(EQUIPMENT_FIELD)?.trim().orEmpty()
                    val imageUrl = doc.getString(IMAGE_URL_FIELD)?.trim().orEmpty()

                    if (name.isBlank() || description.isBlank() || muscleGroup.isBlank()) {
                        return@mapNotNull null
                    }

                    Exercise(
                        id = doc.id,
                        name = name,
                        description = description,
                        muscleGroup = muscleGroup,
                        equipment = equipment,
                        imageUrl = imageUrl
                    )
                }

                trySend(exercises)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Ensures US7 minimum inventory exists on first load.
     *
     * Why:
     * The acceptance criteria requires at least 10 available exercises.
     */
    private suspend fun seedDefaultsIfNeeded() {
        val collection = firestore.collection(EXERCISES_COLLECTION)
        val existingCount = collection.get().await().size()
        if (existingCount >= MINIMUM_REQUIRED_EXERCISES) {
            return
        }

        val batch = firestore.batch()
        defaultExercises().forEach { exercise ->
            val payload = mapOf(
                NAME_FIELD to exercise.name,
                DESCRIPTION_FIELD to exercise.description,
                MUSCLE_GROUP_FIELD to exercise.muscleGroup,
                EQUIPMENT_FIELD to exercise.equipment,
                IMAGE_URL_FIELD to exercise.imageUrl,
                UPDATED_AT_FIELD to FieldValue.serverTimestamp()
            )
            batch.set(collection.document(exercise.id), payload)
        }
        batch.commit().await()
    }

    private fun defaultExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = "squat",
                name = "Back Squat",
                description = "Barbell squat emphasizing quads and glutes with controlled depth.",
                muscleGroup = "Legs",
                equipment = "Barbell",
                imageUrl = "https://images.unsplash.com/photo-1574680178050-55c6a6a96e0a?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "bench_press",
                name = "Bench Press",
                description = "Horizontal pressing movement focused on chest, shoulders, and triceps.",
                muscleGroup = "Chest",
                equipment = "Barbell",
                imageUrl = "https://images.unsplash.com/photo-1534367507873-d2d7e24c797f?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "deadlift",
                name = "Deadlift",
                description = "Hip hinge lift developing posterior chain strength.",
                muscleGroup = "Back",
                equipment = "Barbell",
                imageUrl = "https://images.unsplash.com/photo-1434682881908-b43d0467b798?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "overhead_press",
                name = "Overhead Press",
                description = "Standing vertical press that builds shoulder and triceps strength.",
                muscleGroup = "Shoulders",
                equipment = "Barbell",
                imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "pull_up",
                name = "Pull-Up",
                description = "Bodyweight pulling exercise targeting lats and upper back.",
                muscleGroup = "Back",
                equipment = "Pull-up Bar",
                imageUrl = "https://images.unsplash.com/photo-1599058917212-d750089bc07e?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "lunge",
                name = "Walking Lunge",
                description = "Unilateral leg exercise that trains balance and lower-body control.",
                muscleGroup = "Legs",
                equipment = "Dumbbells",
                imageUrl = "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "plank",
                name = "Plank",
                description = "Core stability hold focusing on trunk stiffness and posture.",
                muscleGroup = "Core",
                equipment = "Bodyweight",
                imageUrl = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "bicep_curl",
                name = "Bicep Curl",
                description = "Elbow flexion movement to isolate and strengthen biceps.",
                muscleGroup = "Arms",
                equipment = "Dumbbells",
                imageUrl = "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "tricep_pushdown",
                name = "Tricep Pushdown",
                description = "Cable exercise for triceps extension and lockout strength.",
                muscleGroup = "Arms",
                equipment = "Cable Machine",
                imageUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=1200&q=80"
            ),
            Exercise(
                id = "glute_bridge",
                name = "Glute Bridge",
                description = "Hip extension movement emphasizing glutes and hamstrings.",
                muscleGroup = "Legs",
                equipment = "Bodyweight",
                imageUrl = ""
            )
        )
    }

    private companion object {
        const val EXERCISES_COLLECTION = "exercises"
        const val NAME_FIELD = "name"
        const val DESCRIPTION_FIELD = "description"
        const val MUSCLE_GROUP_FIELD = "muscleGroup"
        const val EQUIPMENT_FIELD = "equipment"
        const val IMAGE_URL_FIELD = "imageUrl"
        const val UPDATED_AT_FIELD = "updatedAt"
        const val MINIMUM_REQUIRED_EXERCISES = 10
    }
}
