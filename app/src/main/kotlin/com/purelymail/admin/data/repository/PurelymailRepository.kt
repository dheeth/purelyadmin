package com.purelymail.admin.data.repository

import com.purelymail.admin.data.api.MockPurelymailApiClient
import com.purelymail.admin.data.api.PurelymailApiClient
import com.purelymail.admin.data.api.RealPurelymailApiClient
import com.purelymail.admin.data.model.*
import kotlinx.coroutines.flow.StateFlow

data class DashboardSummary(
    val credit: CheckCreditResponse? = null,
    val totalUsers: Int = 0,
    val totalDomains: Int = 0,
    val failingDnsDomains: List<ApiDomainInfo> = emptyList(),
    val totalRoutingRules: Int = 0
)

data class EmailClientConfig(
    val imapHost: String = "mail.purelymail.com",
    val imapPort: Int = 993,
    val imapSecurity: String = "SSL / TLS",
    val smtpHost: String = "mail.purelymail.com",
    val smtpPort: Int = 465,
    val smtpSecurity: String = "SSL / TLS (or 587 STARTTLS)",
    val webmailUrl: String = "https://mail.purelymail.com"
)

class PurelymailRepository(
    val tokenManager: TokenManager
) {
    private val realClient = RealPurelymailApiClient(tokenManager)
    private val mockClient = MockPurelymailApiClient()

    val client: PurelymailApiClient
        get() = if (tokenManager.isDemoMode.value || tokenManager.apiToken.value.isBlank()) {
            mockClient
        } else {
            realClient
        }

    val isDemoMode: StateFlow<Boolean> = tokenManager.isDemoMode
    val hasValidToken: Boolean get() = tokenManager.hasValidToken

    suspend fun getDashboardSummary(): Result<DashboardSummary> {
        val activeClient = client
        val creditRes = activeClient.checkAccountCredit()
        val usersRes = activeClient.listUsers()
        val domainsRes = activeClient.listDomains(includeShared = false)
        val rulesRes = activeClient.listRoutingRules()

        val credit = creditRes.getOrNull()
        val users = usersRes.getOrNull() ?: emptyList()
        val domains = domainsRes.getOrNull() ?: emptyList()
        val rules = rulesRes.getOrNull() ?: emptyList()

        val failingDomains = domains.filter { !it.dnsSummary.isFullyPassing }

        return Result.success(
            DashboardSummary(
                credit = credit,
                totalUsers = users.size,
                totalDomains = domains.size,
                failingDnsDomains = failingDomains,
                totalRoutingRules = rules.size
            )
        )
    }

    val clientConfig = EmailClientConfig()

    // Pass-through helpers for convenience
    suspend fun checkCredit(): Result<CheckCreditResponse> = client.checkAccountCredit()
    suspend fun listUsers(): Result<List<String>> = client.listUsers()
    suspend fun getUser(userName: String): Result<GetUserResponse> = client.getUser(userName)
    suspend fun createUser(request: CreateUserRequest): Result<Unit> = client.createUser(request)
    suspend fun modifyUser(request: ModifyUserRequest): Result<Unit> = client.modifyUser(request)
    suspend fun deleteUser(userName: String): Result<Unit> = client.deleteUser(userName)
    suspend fun createAppPassword(userHandle: String, name: String = ""): Result<String> = client.createAppPassword(userHandle, name)
    suspend fun deleteAppPassword(userName: String, appPassword: String): Result<Unit> = client.deleteAppPassword(userName, appPassword)
    suspend fun listPasswordReset(userName: String): Result<List<ListPasswordResetResponseItem>> = client.listPasswordReset(userName)
    suspend fun upsertPasswordReset(request: UpsertPasswordResetRequest): Result<Unit> = client.upsertPasswordReset(request)
    suspend fun deletePasswordReset(userName: String, target: String?): Result<Unit> = client.deletePasswordReset(DeletePasswordResetRequest(userName, target))
    suspend fun listDomains(includeShared: Boolean = false): Result<List<ApiDomainInfo>> = client.listDomains(includeShared)
    suspend fun addDomain(domainName: String): Result<Unit> = client.addDomain(domainName)
    suspend fun getOwnershipCode(): Result<String> = client.getOwnershipCode()
    suspend fun updateDomainSettings(request: UpdateDomainSettingsRequest): Result<Unit> = client.updateDomainSettings(request)
    suspend fun deleteDomain(name: String): Result<Unit> = client.deleteDomain(name)
    suspend fun listRoutingRules(): Result<List<RoutingRule>> = client.listRoutingRules()
    suspend fun createRoutingRule(request: CreateRoutingRequest): Result<Unit> = client.createRoutingRule(request)
    suspend fun deleteRoutingRule(ruleId: Long): Result<Unit> = client.deleteRoutingRule(ruleId)
}
