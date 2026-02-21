package com.example.taliworkouthelper.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Persists active and completed workout sessions for a user.
 *
 * Schema:
 * users/{uid}/workoutSessions/{sessionId}
 * - status: in_progress | completed
 * - generalNote: String
 * - exerciseLogs: [{exerciseName: String, note: String, sets: [{reps: Number, weightKg: Number|null}]}]
 * - updatedAt: server timestamp
 */
class FirestoreWorkoutSessionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : WorkoutSessionRepository {

    override fun observeActiveSession(): Flow<WorkoutSession?> = callbackFlow {
        val uid = requireUserId()
        val listener = sessionsCollection(uid)
            .whereEqualTo(STATUS_FIELD, STATUS_IN_PROGRESS)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val document = snapshot?.documents?.firstOrNull()
                trySend(document?.toWorkoutSession())
            }

        awaitClose { listener.remove() }
    }

    override fun observeCompletedSessions(limit: Int): Flow<List<WorkoutSession>> = callbackFlow {
        val uid = requireUserId()
        val listener = sessionsCollection(uid)
            .whereEqualTo(STATUS_FIELD, STATUS_COMPLETED)
            .orderBy(UPDATED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents.orEmpty().mapNotNull { it.toWorkoutSession() }
                trySend(sessions)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveActiveSession(session: WorkoutSession): Result<WorkoutSession> {
        return runCatching {
            val uid = requireUserId()
            val inProgress = session.copy(status = WorkoutSessionStatus.IN_PROGRESS)
            sessionsCollection(uid)
                .document(inProgress.id)
                .set(inProgress.toFirestoreMap(), SetOptions.merge())
                .await()
            inProgress
        }
    }

    override suspend fun completeSession(session: WorkoutSession): Result<WorkoutSession> {
        return runCatching {
            val uid = requireUserId()
            val completed = session.copy(status = WorkoutSessionStatus.COMPLETED)
            sessionsCollection(uid)
                .document(completed.id)
                .set(completed.toFirestoreMap(), SetOptions.merge())
                .await()
            completed
        }
    }

    private fun sessionsCollection(uid: String) =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUT_SESSIONS_COLLECTION)

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toWorkoutSession(): WorkoutSession? {
        val statusValue = getString(STATUS_FIELD) ?: return null
        val status = when (statusValue) {
            STATUS_IN_PROGRESS -> WorkoutSessionStatus.IN_PROGRESS
            STATUS_COMPLETED -> WorkoutSessionStatus.COMPLETED
            else -> return null
        }

        val generalNote = getString(GENERAL_NOTE_FIELD).orEmpty()
        val rawLogs = (get(EXERCISE_LOGS_FIELD) as? List<*>)
            ?.filterIsInstance<Map<String, Any?>>()
            .orEmpty()

        val logs = rawLogs.mapNotNull { log ->
            val exerciseName = log[EXERCISE_NAME_FIELD] as? String ?: return@mapNotNull null
            val note = log[EXERCISE_NOTE_FIELD] as? String ?: ""
            val rawSets = (log[SETS_FIELD] as? List<*>)
                ?.filterIsInstance<Map<String, Any?>>()
                .orEmpty()
            val sets = rawSets.mapNotNull { set ->
                val reps = (set[REPS_FIELD] as? Number)?.toInt() ?: return@mapNotNull null
                val weight = (set[WEIGHT_FIELD] as? Number)?.toDouble()
                LoggedSet(reps = reps, weightKg = weight)
            }
            ExerciseSessionLog(exerciseName = exerciseName, sets = sets, note = note)
        }

        return WorkoutSession(
            id = id,
            status = status,
            exerciseLogs = logs,
            generalNote = generalNote
        )
    }

    private fun WorkoutSession.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            STATUS_FIELD to when (status) {
                WorkoutSessionStatus.IN_PROGRESS -> STATUS_IN_PROGRESS
                WorkoutSessionStatus.COMPLETED -> STATUS_COMPLETED
            },
            GENERAL_NOTE_FIELD to generalNote,
            EXERCISE_LOGS_FIELD to exerciseLogs.map { log ->
                mapOf(
                    EXERCISE_NAME_FIELD to log.exerciseName,
                    EXERCISE_NOTE_FIELD to log.note,
                    SETS_FIELD to log.sets.map { set ->
                        mapOf(REPS_FIELD to set.reps, WEIGHT_FIELD to set.weightKg)
                    }
                )
            },
            UPDATED_AT_FIELD to FieldValue.serverTimestamp()
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val WORKOUT_SESSIONS_COLLECTION = "workoutSessions"
        const val STATUS_FIELD = "status"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val GENERAL_NOTE_FIELD = "generalNote"
        const val EXERCISE_LOGS_FIELD = "exerciseLogs"
        const val EXERCISE_NAME_FIELD = "exerciseName"
        const val EXERCISE_NOTE_FIELD = "note"
        const val SETS_FIELD = "sets"
        const val REPS_FIELD = "reps"
        const val WEIGHT_FIELD = "weightKg"
        const val UPDATED_AT_FIELD = "updatedAt"
    }
}
