package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.RequestUrgency
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BloodCareRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBloodRequest_validPayload_createsDocumentAndReturnsId() = runBlocking {
        val uid = signInTestUser("patient_${UUID.randomUUID()}@test.com")
        val repository = BloodCareRepository(firestore)

        val result = withTimeout(5000L) {
            repository.createBloodRequest(
                patientName = "Test Patient",
                patientPhone = "03001234567",
                bloodGroup = "A+",
                city = "Nawabshah",
                hospitalName = "Civil Hospital Nawabshah",
                hospitalAddress = "Hospital Road",
                unitsRequired = 2L,
                urgency = RequestUrgency.CRITICAL,
                requiredDate = "Immediate",
                additionalNotes = "Emergency surgery"
            )
        }

        assertTrue("Expected request creation to succeed", result.isSuccess)
        val requestId = result.getOrThrow()
        assertTrue(requestId.isNotBlank())
    }

    @Test
    fun observeCityBloodRequests_inMatchingCity_emitsOpenRequests() = runBlocking {
        val uid = signInTestUser("patient_${UUID.randomUUID()}@test.com")
        val repository = BloodCareRepository(firestore)

        val createResult = withTimeout(5000L) {
            repository.createBloodRequest(
                patientName = "Patient in Nawabshah",
                patientPhone = "03001234567",
                bloodGroup = "B+",
                city = "Nawabshah",
                hospitalName = "Peoples Hospital",
                hospitalAddress = "Main Road",
                unitsRequired = 1L,
                urgency = RequestUrgency.URGENT,
                requiredDate = "Today",
                additionalNotes = "Thalassemia patient"
            )
        }
        val requestId = createResult.getOrThrow()

        // Observe matching requests in Nawabshah
        val requests = withTimeout(5000L) {
            repository.observeCityBloodRequests("nawabshah").first { list ->
                list.any { it.id == requestId }
            }
        }

        assertTrue(requests.any { it.id == requestId })
    }

    @Test
    fun unauthenticatedCaller_createBloodRequest_failsFast() = runBlocking {
        auth.signOut()
        val repository = BloodCareRepository(firestore)

        var failed = false
        try {
            repository.createBloodRequest(
                patientName = "Unauth Patient",
                patientPhone = "03000000000",
                bloodGroup = "O+",
                city = "Karachi",
                hospitalName = "JPMC",
                hospitalAddress = "Rafiqui Shaheed Rd",
                unitsRequired = 2L,
                urgency = RequestUrgency.NORMAL,
                requiredDate = "Tomorrow",
                additionalNotes = ""
            )
        } catch (e: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected IllegalStateException for unauthenticated caller", failed)
    }
}
