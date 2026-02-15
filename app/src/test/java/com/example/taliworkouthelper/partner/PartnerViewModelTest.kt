package com.example.taliworkouthelper.partner

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PartnerViewModelTest {
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
    fun `available partners and connect`() = runBlocking {
        val repo = FakePartnerRepository()
        val vm = PartnerViewModel(repo)

        // initial partners should be present
        val partners = repo.availablePartners().first()
        assertEquals(2, partners.size)

        var res: Result<Partner>? = null
        vm.connect("p1") { res = it }
        kotlinx.coroutines.delay(10)
        assertNotNull(res)
        assertTrue(res!!.isSuccess)
        assertEquals("Alice", res!!.getOrNull()!!.name)
    }
}
