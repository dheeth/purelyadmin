package com.purelymail.admin

import com.purelymail.admin.data.api.MockPurelymailApiClient
import com.purelymail.admin.data.model.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PurelymailUnitTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private lateinit var mockClient: MockPurelymailApiClient

    @Before
    fun setUp() {
        mockClient = MockPurelymailApiClient()
    }

    @Test
    fun testParseSuccessCreditResponse() {
        val rawJson = """{"result": {"credit": "14.85"}}"""
        val response = json.decodeFromString<PurelymailResponse<CheckCreditResponse>>(rawJson)

        assertTrue(response.isSuccess)
        assertNotNull(response.result)
        assertEquals("14.85", response.result?.credit)
        assertEquals(14.85, response.result?.numericCredit ?: 0.0, 0.001)
        assertEquals("$14.85", response.result?.formattedCredit)
        assertFalse(response.result?.isLowBalance == true)
    }

    @Test
    fun testParseErrorResponse() {
        val rawJson = """{"type": "error", "code": "invalidToken", "message": "Token not valid."}"""
        val response = json.decodeFromString<PurelymailResponse<CheckCreditResponse>>(rawJson)

        assertFalse(response.isSuccess)
        assertNull(response.result)
        assertEquals("invalidToken", response.code)
        assertEquals("Token not valid.", response.errorMessage)
    }

    @Test
    fun testParseListDomainsResponse() {
        val rawJson = """
            {
              "result": {
                "domains": [
                  {
                    "name": "example.com",
                    "allowAccountReset": true,
                    "symbolicSubaddressing": true,
                    "isShared": false,
                    "dnsSummary": {
                      "passesMx": true,
                      "passesSpf": true,
                      "passesDkim": true,
                      "passesDmarc": true
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<PurelymailResponse<ListDomainsResponse>>(rawJson)
        assertTrue(response.isSuccess)
        val domain = response.result?.domains?.firstOrNull()
        assertNotNull(domain)
        assertEquals("example.com", domain?.name)
        assertTrue(domain?.dnsSummary?.isFullyPassing == true)
        assertEquals(4, domain?.dnsSummary?.passingCount)
    }

    @Test
    fun testCreateUserRequestSerialization() {
        val request = CreateUserRequest(
            userName = "testuser",
            domainName = "example.com",
            password = "SecurePassword123!",
            recoveryEmail = "recovery@test.com"
        )
        val encoded = json.encodeToString(request)
        assertTrue(encoded.contains("\"userName\":\"testuser\""))
        assertTrue(encoded.contains("\"domainName\":\"example.com\""))
        assertTrue(encoded.contains("\"recoveryEmail\":\"recovery@test.com\""))
    }

    @Test
    fun testMockClientUserLifecycle() = runTest {
        // 1. Initial users
        val initialUsers = mockClient.listUsers().getOrThrow()
        assertTrue(initialUsers.isNotEmpty())
        val initialCount = initialUsers.size

        // 2. Create user
        val createReq = CreateUserRequest(
            userName = "newunituser",
            domainName = "coolbrand.dev",
            password = "TestPassword456!",
            recoveryEmail = "recovery@gmail.com"
        )
        val createResult = mockClient.createUser(createReq)
        assertTrue(createResult.isSuccess)

        // 3. Verify user in list
        val updatedUsers = mockClient.listUsers().getOrThrow()
        assertEquals(initialCount + 1, updatedUsers.size)
        assertTrue(updatedUsers.contains("newunituser@coolbrand.dev"))

        // 4. Get user details
        val userDetails = mockClient.getUser("newunituser@coolbrand.dev").getOrThrow()
        assertTrue(userDetails.enableSearchIndexing)
        assertEquals(1, userDetails.resetMethods.size)
        assertEquals("recovery@gmail.com", userDetails.resetMethods[0].target)

        // 5. Generate app password
        val appPass = mockClient.createAppPassword("newunituser@coolbrand.dev", "Test App").getOrThrow()
        assertTrue(appPass.startsWith("pm_"))

        // 6. Delete user
        val deleteResult = mockClient.deleteUser("newunituser@coolbrand.dev")
        assertTrue(deleteResult.isSuccess)

        val finalUsers = mockClient.listUsers().getOrThrow()
        assertEquals(initialCount, finalUsers.size)
        assertFalse(finalUsers.contains("newunituser@coolbrand.dev"))
    }

    @Test
    fun testMockClientDomainAndDns() = runTest {
        // 1. Add domain
        val addResult = mockClient.addDomain("newstartup.org")
        assertTrue(addResult.isSuccess)

        val domains = mockClient.listDomains().getOrThrow()
        val addedDomain = domains.find { it.name == "newstartup.org" }
        assertNotNull(addedDomain)
        assertFalse(addedDomain!!.dnsSummary.isFullyPassing)

        // 2. Recheck DNS -> should update to passing in mock
        val updateResult = mockClient.updateDomainSettings(
            UpdateDomainSettingsRequest(name = "newstartup.org", recheckDns = true)
        )
        assertTrue(updateResult.isSuccess)

        val domainsAfter = mockClient.listDomains().getOrThrow()
        val verifiedDomain = domainsAfter.find { it.name == "newstartup.org" }
        assertTrue(verifiedDomain!!.dnsSummary.isFullyPassing)

        // 3. Delete domain
        val deleteResult = mockClient.deleteDomain("newstartup.org")
        assertTrue(deleteResult.isSuccess)
        assertFalse(mockClient.listDomains().getOrThrow().any { it.name == "newstartup.org" })
    }

    @Test
    fun testMockClientRoutingRules() = runTest {
        val initialRules = mockClient.listRoutingRules().getOrThrow()
        val initialCount = initialRules.size

        // Create rule
        val createRuleReq = CreateRoutingRequest(
            domainName = "coolbrand.dev",
            prefix = false,
            matchUser = "devops",
            targetAddresses = listOf("alex@coolbrand.dev", "sarah@coolbrand.dev"),
            catchall = false
        )
        val result = mockClient.createRoutingRule(createRuleReq)
        assertTrue(result.isSuccess)

        val rulesAfter = mockClient.listRoutingRules().getOrThrow()
        assertEquals(initialCount + 1, rulesAfter.size)
        val devopsRule = rulesAfter.find { it.matchUser == "devops" }
        assertNotNull(devopsRule)
        assertEquals("devops@coolbrand.dev", devopsRule?.displayMatch)
        assertEquals(2, devopsRule?.targetAddresses?.size)

        // Delete rule
        mockClient.deleteRoutingRule(devopsRule!!.id)
        val finalRules = mockClient.listRoutingRules().getOrThrow()
        assertEquals(initialCount, finalRules.size)
    }
}
