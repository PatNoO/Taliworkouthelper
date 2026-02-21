package com.example.taliworkouthelper.schedule

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Persists work shifts per authenticated user in Firestore.
 *
 * Schema:
 * users/{uid}/workShifts/{shiftId} with fields startHour and endHour.
 */
class FirestoreWorkScheduleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : WorkScheduleRepository {

    override fun getShifts(): Flow<List<WorkShift>> = callbackFlow {
        val uid = requireUserId()
        val listener = shiftsCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val shifts = snapshot?.documents.orEmpty().mapNotNull { document ->
                val startHour = document.getLong(START_HOUR_FIELD)?.toInt() ?: return@mapNotNull null
                val endHour = document.getLong(END_HOUR_FIELD)?.toInt() ?: return@mapNotNull null
                WorkShift(
                    id = document.id,
                    startHour = startHour,
                    endHour = endHour
                )
            }.sortedBy { it.startHour }

            trySend(shifts)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addShift(shift: WorkShift): Result<WorkShift> {
        return runCatching {
            val uid = requireUserId()
            shiftsCollection(uid)
                .document(shift.id)
                .set(shift.toFirestoreMap(), SetOptions.merge())
                .await()
            shift
        }
    }

    override suspend fun updateShift(shift: WorkShift): Result<WorkShift> {
        return runCatching {
            val uid = requireUserId()
            shiftsCollection(uid)
                .document(shift.id)
                .set(shift.toFirestoreMap(), SetOptions.merge())
                .await()
            shift
        }
    }

    override suspend fun removeShift(id: String) {
        val uid = requireUserId()
        shiftsCollection(uid).document(id).delete().await()
    }

    private fun shiftsCollection(uid: String) =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORK_SHIFTS_COLLECTION)

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
    }

    private fun WorkShift.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            START_HOUR_FIELD to startHour,
            END_HOUR_FIELD to endHour
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val WORK_SHIFTS_COLLECTION = "workShifts"
        const val START_HOUR_FIELD = "startHour"
        const val END_HOUR_FIELD = "endHour"
    }
}
