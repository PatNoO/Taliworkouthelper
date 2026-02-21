package com.example.taliworkouthelper.request

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Persists training requests and synced bookings.
 *
 * Schema:
 * trainingRequests/{requestId}
 * - fromUid, toUid, startEpochMillis, endEpochMillis, status, updatedAt
 *
 * bookings/{bookingId}
 * - ownerUid, partnerUid, requestId, startEpochMillis, endEpochMillis, createdAt
 */
class FirestoreTrainingRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TrainingRequestRepository {

    override fun observeIncomingRequests(): Flow<List<TrainingRequest>> = callbackFlow {
        val currentUid = requireCurrentUid()
        val listener = requestsCollection()
            .whereEqualTo(TO_UID_FIELD, currentUid)
            .whereEqualTo(STATUS_FIELD, STATUS_PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents.orEmpty().mapNotNull { it.toTrainingRequest() }
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    override fun observeOutgoingRequests(): Flow<List<TrainingRequest>> = callbackFlow {
        val currentUid = requireCurrentUid()
        val listener = requestsCollection()
            .whereEqualTo(FROM_UID_FIELD, currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents.orEmpty().mapNotNull { it.toTrainingRequest() }
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    override fun observeBookings(): Flow<List<Booking>> = callbackFlow {
        val currentUid = requireCurrentUid()
        val listener = bookingsCollection()
            .whereEqualTo(OWNER_UID_FIELD, currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents.orEmpty().mapNotNull { it.toBooking() }
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendRequest(
        toUid: String,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Result<TrainingRequest> {
        return runCatching {
            val fromUid = requireCurrentUid()
            require(endEpochMillis > startEpochMillis) { "End time must be after start time" }
            ensureNoConflict(fromUid, startEpochMillis, endEpochMillis)
            ensureNoConflict(toUid, startEpochMillis, endEpochMillis)

            val id = requestsCollection().document().id
            val request = TrainingRequest(
                id = id,
                fromUid = fromUid,
                toUid = toUid,
                startEpochMillis = startEpochMillis,
                endEpochMillis = endEpochMillis,
                status = TrainingRequestStatus.PENDING
            )

            requestsCollection()
                .document(id)
                .set(request.toFirestoreMap(), SetOptions.merge())
                .await()

            request
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<TrainingRequest> {
        return runCatching {
            val request = loadRequest(requestId)
            ensureNoConflict(request.fromUid, request.startEpochMillis, request.endEpochMillis)
            ensureNoConflict(request.toUid, request.startEpochMillis, request.endEpochMillis)

            val accepted = request.copy(status = TrainingRequestStatus.ACCEPTED)
            requestsCollection()
                .document(requestId)
                .set(accepted.toFirestoreMap(), SetOptions.merge())
                .await()

            val bookingA = Booking(
                id = bookingsCollection().document().id,
                ownerUid = request.fromUid,
                partnerUid = request.toUid,
                startEpochMillis = request.startEpochMillis,
                endEpochMillis = request.endEpochMillis,
                requestId = request.id
            )
            val bookingB = bookingA.copy(
                id = bookingsCollection().document().id,
                ownerUid = request.toUid,
                partnerUid = request.fromUid
            )

            bookingsCollection().document(bookingA.id).set(bookingA.toFirestoreMap(), SetOptions.merge()).await()
            bookingsCollection().document(bookingB.id).set(bookingB.toFirestoreMap(), SetOptions.merge()).await()

            accepted
        }
    }

    override suspend fun declineRequest(requestId: String): Result<TrainingRequest> {
        return runCatching {
            val request = loadRequest(requestId)
            val declined = request.copy(status = TrainingRequestStatus.DECLINED)
            requestsCollection()
                .document(requestId)
                .set(declined.toFirestoreMap(), SetOptions.merge())
                .await()
            declined
        }
    }

    private suspend fun ensureNoConflict(userId: String, startEpochMillis: Long, endEpochMillis: Long) {
        val bookings = bookingsCollection().whereEqualTo(OWNER_UID_FIELD, userId).get().await()
        val hasConflict = bookings.documents.any { doc ->
            val existingStart = doc.getLong(START_EPOCH_FIELD) ?: return@any false
            val existingEnd = doc.getLong(END_EPOCH_FIELD) ?: return@any false
            startEpochMillis < existingEnd && existingStart < endEpochMillis
        }
        check(!hasConflict) { "Booking conflict detected" }
    }

    private suspend fun loadRequest(requestId: String): TrainingRequest {
        val snapshot = requestsCollection().document(requestId).get().await()
        return snapshot.toTrainingRequest() ?: throw IllegalArgumentException("Request not found")
    }

    private fun requestsCollection() = firestore.collection(REQUESTS_COLLECTION)

    private fun bookingsCollection() = firestore.collection(BOOKINGS_COLLECTION)

    private fun requireCurrentUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTrainingRequest(): TrainingRequest? {
        val fromUid = getString(FROM_UID_FIELD) ?: return null
        val toUid = getString(TO_UID_FIELD) ?: return null
        val startEpochMillis = getLong(START_EPOCH_FIELD) ?: return null
        val endEpochMillis = getLong(END_EPOCH_FIELD) ?: return null
        val status = when (getString(STATUS_FIELD)) {
            STATUS_PENDING -> TrainingRequestStatus.PENDING
            STATUS_ACCEPTED -> TrainingRequestStatus.ACCEPTED
            STATUS_DECLINED -> TrainingRequestStatus.DECLINED
            else -> return null
        }

        return TrainingRequest(
            id = id,
            fromUid = fromUid,
            toUid = toUid,
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis,
            status = status
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toBooking(): Booking? {
        val ownerUid = getString(OWNER_UID_FIELD) ?: return null
        val partnerUid = getString(PARTNER_UID_FIELD) ?: return null
        val requestId = getString(REQUEST_ID_FIELD) ?: return null
        val startEpochMillis = getLong(START_EPOCH_FIELD) ?: return null
        val endEpochMillis = getLong(END_EPOCH_FIELD) ?: return null

        return Booking(
            id = id,
            ownerUid = ownerUid,
            partnerUid = partnerUid,
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis,
            requestId = requestId
        )
    }

    private fun TrainingRequest.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            FROM_UID_FIELD to fromUid,
            TO_UID_FIELD to toUid,
            START_EPOCH_FIELD to startEpochMillis,
            END_EPOCH_FIELD to endEpochMillis,
            STATUS_FIELD to when (status) {
                TrainingRequestStatus.PENDING -> STATUS_PENDING
                TrainingRequestStatus.ACCEPTED -> STATUS_ACCEPTED
                TrainingRequestStatus.DECLINED -> STATUS_DECLINED
            },
            UPDATED_AT_FIELD to FieldValue.serverTimestamp()
        )
    }

    private fun Booking.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            OWNER_UID_FIELD to ownerUid,
            PARTNER_UID_FIELD to partnerUid,
            REQUEST_ID_FIELD to requestId,
            START_EPOCH_FIELD to startEpochMillis,
            END_EPOCH_FIELD to endEpochMillis,
            CREATED_AT_FIELD to FieldValue.serverTimestamp()
        )
    }

    private companion object {
        const val REQUESTS_COLLECTION = "trainingRequests"
        const val BOOKINGS_COLLECTION = "bookings"

        const val FROM_UID_FIELD = "fromUid"
        const val TO_UID_FIELD = "toUid"
        const val OWNER_UID_FIELD = "ownerUid"
        const val PARTNER_UID_FIELD = "partnerUid"
        const val REQUEST_ID_FIELD = "requestId"

        const val START_EPOCH_FIELD = "startEpochMillis"
        const val END_EPOCH_FIELD = "endEpochMillis"
        const val STATUS_FIELD = "status"

        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"

        const val UPDATED_AT_FIELD = "updatedAt"
        const val CREATED_AT_FIELD = "createdAt"
    }
}
