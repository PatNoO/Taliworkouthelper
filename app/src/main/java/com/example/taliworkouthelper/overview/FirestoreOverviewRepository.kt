package com.example.taliworkouthelper.overview

import com.example.taliworkouthelper.session.FirestoreWorkoutSessionRepository
import com.example.taliworkouthelper.session.WorkoutSession
import com.example.taliworkouthelper.session.WorkoutSessionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Reads overview data from Firestore without introducing business logic in UI.
 *
 * Bookings schema (read):
 * bookings/{bookingId}
 * - userIds: [uid]
 * - startAt: Number|Timestamp
 * - endAt: Number|Timestamp|null
 * - partnerName: String
 * - title: String
 */
class FirestoreOverviewRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val sessionRepository: WorkoutSessionRepository = FirestoreWorkoutSessionRepository()
) : OverviewRepository {

    override fun observeUpcomingBookings(limit: Int): Flow<List<UpcomingBooking>> = callbackFlow {
        val uid = requireUserId()
        val listener = firestore.collection(BOOKINGS_COLLECTION)
            .whereArrayContains(USER_IDS_FIELD, uid)
            .whereGreaterThanOrEqualTo(START_AT_FIELD, System.currentTimeMillis())
            .orderBy(START_AT_FIELD, Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val bookings = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val startAt = document.getMillis(START_AT_FIELD) ?: return@mapNotNull null
                    val endAt = document.getMillis(END_AT_FIELD)
                    UpcomingBooking(
                        id = document.id,
                        partnerName = document.getString(PARTNER_NAME_FIELD).orEmpty().ifBlank { "Partner" },
                        startAtMillis = startAt,
                        endAtMillis = endAt,
                        title = document.getString(TITLE_FIELD).orEmpty()
                    )
                }

                trySend(bookings)
            }

        awaitClose { listener.remove() }
    }

    override fun observeWorkoutHistory(limit: Int): Flow<List<WorkoutSession>> {
        return sessionRepository.observeCompletedSessions(limit)
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getMillis(field: String): Long? {
        val value = get(field)
        return when (value) {
            is Number -> value.toLong()
            is Timestamp -> value.toDate().time
            else -> null
        }
    }

    private companion object {
        const val BOOKINGS_COLLECTION = "bookings"
        const val USER_IDS_FIELD = "userIds"
        const val START_AT_FIELD = "startAt"
        const val END_AT_FIELD = "endAt"
        const val PARTNER_NAME_FIELD = "partnerName"
        const val TITLE_FIELD = "title"
    }
}
