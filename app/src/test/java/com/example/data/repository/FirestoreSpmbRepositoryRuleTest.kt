package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirestoreApplicant
import com.example.data.firebase.FirestoreSpmbRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FirestoreSpmbRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createApplicant_authenticatedUser_success() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreSpmbRepository(firestore, auth)

        val applicant = FirestoreApplicant(
            id = "app_${UUID.randomUUID()}",
            userId = uid,
            registrationNumber = "SPMB-2027-101",
            fullName = "Budi Hartono",
            gender = "Laki-laki",
            schoolOrigin = "SMP N 1",
            phone = "08123456789",
            majorCode = "TKJ",
            verificationStatus = "Menunggu verifikasi",
            admissionStatus = "Menunggu keputusan"
        )

        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addApplicant(applicant)
        }
        assertTrue(docId.isNotEmpty())

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeApplicants().first()
        }
        assertTrue(list.any { it.registrationNumber == "SPMB-2027-101" })
    }

    @Test
    fun observeApplicants_unauthenticated_fails() = runBlocking {
        auth.signOut()
        val repository = FirestoreSpmbRepository(firestore, auth)

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.observeApplicants().first()
            }
            fail("Expected IllegalStateException for unauthenticated user")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Google") == true)
        }
    }

    @Test
    fun crossUserAccess_bobCannotReadAliceDirectly() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreSpmbRepository(firestore, auth)
        val uniqueAppId = "alice_app_${UUID.randomUUID()}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addApplicant(
                FirestoreApplicant(
                    id = uniqueAppId,
                    userId = aliceUid,
                    registrationNumber = "SPMB-2027-102",
                    fullName = "Siti Alice",
                    gender = "Perempuan",
                    schoolOrigin = "SMP N 2",
                    phone = "08123456780",
                    majorCode = "DKV",
                    verificationStatus = "Terverifikasi",
                    admissionStatus = "Diterima"
                )
            )
        }

        // Now Bob signs in
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("applicants").document(uniqueAppId).get().await()
            }
            fail("Expected PERMISSION_DENIED when Bob tries to read Alice's applicant")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@spmb.test"
        const val BOB_EMAIL = "bob@spmb.test"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
