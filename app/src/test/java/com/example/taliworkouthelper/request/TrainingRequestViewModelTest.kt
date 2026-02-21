package com.example.taliworkouthelper.request

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingRequestViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `user can send request`() = runTest {
        val vm = TrainingRequestViewModel(FakeTrainingRequestRepository())

        vm.onPartnerUidChanged("partner-1")
        vm.onStartChanged("2026-02-21T18:00")
        vm.onEndChanged("2026-02-21T19:00")
        vm.sendRequest()
        delay(20)

        assertEquals(1, vm.state.value.outgoing.size)
        assertEquals(TrainingRequestStatus.PENDING, vm.state.value.outgoing.first().status)
    }

    @Test
    fun `partner can accept and synced booking is created`() = runTest {
        val repo = FakeTrainingRequestRepository()
        val sender = TrainingRequestViewModel(repo)

        sender.onPartnerUidChanged("partner-2")
        sender.onStartChanged("2026-02-22T10:00")
        sender.onEndChanged("2026-02-22T11:00")
        sender.sendRequest()
        delay(20)

        val requestId = sender.state.value.outgoing.first().id
        sender.acceptRequest(requestId)
        delay(20)

        assertTrue(sender.state.value.bookings.isNotEmpty())
        assertEquals(TrainingRequestStatus.ACCEPTED, sender.state.value.outgoing.first().status)
    }

    @Test
    fun `decline updates request status correctly`() = runTest {
        val vm = TrainingRequestViewModel(FakeTrainingRequestRepository())

        vm.onPartnerUidChanged("partner-3")
        vm.onStartChanged("2026-02-23T09:00")
        vm.onEndChanged("2026-02-23T10:00")
        vm.sendRequest()
        delay(20)

        val requestId = vm.state.value.outgoing.first().id
        vm.declineRequest(requestId)
        delay(20)

        assertEquals(TrainingRequestStatus.DECLINED, vm.state.value.outgoing.first().status)
    }

    @Test
    fun `double booking conflict is handled`() = runTest {
        val vm = TrainingRequestViewModel(FakeTrainingRequestRepository())

        vm.onPartnerUidChanged("partner-4")
        vm.onStartChanged("2026-02-24T12:00")
        vm.onEndChanged("2026-02-24T13:00")
        vm.sendRequest()
        delay(20)

        val firstRequestId = vm.state.value.outgoing.first().id
        vm.acceptRequest(firstRequestId)
        delay(20)

        vm.onPartnerUidChanged("partner-5")
        vm.onStartChanged("2026-02-24T12:30")
        vm.onEndChanged("2026-02-24T13:30")
        vm.sendRequest()
        delay(20)

        assertEquals("Booking conflict detected", vm.state.value.errorMessage)
    }
}
