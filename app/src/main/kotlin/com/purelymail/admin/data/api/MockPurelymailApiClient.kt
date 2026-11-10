package com.purelymail.admin.data.api

import com.purelymail.admin.data.model.*
import kotlinx.coroutines.delay

class MockPurelymailApiClient : PurelymailApiClient {

    private var creditAmount = "14.85"

    private val usersList = mutableListOf(
        "alex@coolbrand.dev",
        "sarah@coolbrand.dev",
        "support@coolbrand.dev",
        "billing@coolbrand.dev",
        "founder@stealthai.io"
    )

    private val userSettingsMap = mutableMapOf(
        "alex@coolbrand.dev" to GetUserResponse(
            enableSearchIndexing = true,
            recoveryEnabled = true,
            requireTwoFactorAuthentication = true,
            enableSpamFiltering = true,
            resetMethods = listOf(
                GetUserPasswordResetMethod(type = "email", target = "alex.personal@gmail.com", description = "Personal Gmail", allowMfaReset = true),
                GetUserPasswordResetMethod(type = "phone", target = "+15550192834", description = "Mobile SMS", allowMfaReset = false)
            )
        ),
        "sarah@coolbrand.dev" to GetUserResponse(
            enableSearchIndexing = true,
            recoveryEnabled = false,
            requireTwoFactorAuthentication = false,
            enableSpamFiltering = true,
            resetMethods = emptyList()
        ),
        "support@coolbrand.dev" to GetUserResponse(
            enableSearchIndexing = true,
            recoveryEnabled = true,
            requireTwoFactorAuthentication = false,
            enableSpamFiltering = true,
            resetMethods = listOf(
                GetUserPasswordResetMethod(type = "email", target = "ops@coolbrand.dev", description = "Ops Team Escalation", allowMfaReset = true)
            )
        ),
        "billing@coolbrand.dev" to GetUserResponse(
            enableSearchIndexing = false,
            recoveryEnabled = false,
            requireTwoFactorAuthentication = true,
            enableSpamFiltering = true,
            resetMethods = emptyList()
        ),
        "founder@stealthai.io" to GetUserResponse(
            enableSearchIndexing = true,
            recoveryEnabled = true,
            requireTwoFactorAuthentication = true,
            enableSpamFiltering = true,
            resetMethods = listOf(
                GetUserPasswordResetMethod(type = "email", target = "founder.backup@proton.me", description = "Encrypted Backup", allowMfaReset = true)
            )
        )
    )

    private val domainsList = mutableListOf(
        ApiDomainInfo(
            name = "coolbrand.dev",
            allowAccountReset = true,
            symbolicSubaddressing = true,
            isShared = false,
            dnsSummary = ApiDomainDnsSummary(
                passesMx = true,
                passesSpf = true,
                passesDkim = true,
                passesDmarc = true
            )
        ),
        ApiDomainInfo(
            name = "stealthai.io",
            allowAccountReset = false,
            symbolicSubaddressing = true,
            isShared = false,
            dnsSummary = ApiDomainDnsSummary(
                passesMx = true,
                passesSpf = true,
                passesDkim = false,
                passesDmarc = false
            )
        ),
        ApiDomainInfo(
            name = "purelymail.com",
            allowAccountReset = false,
            symbolicSubaddressing = true,
            isShared = true,
            dnsSummary = ApiDomainDnsSummary(
                passesMx = true,
                passesSpf = true,
                passesDkim = true,
                passesDmarc = true
            )
        )
    )

    private val routingRulesList = mutableListOf(
        RoutingRule(
            id = 101L,
            domainName = "coolbrand.dev",
            prefix = false,
            matchUser = "press",
            targetAddresses = listOf("alex@coolbrand.dev", "sarah@coolbrand.dev"),
            catchall = false
        ),
        RoutingRule(
            id = 102L,
            domainName = "coolbrand.dev",
            prefix = true,
            matchUser = "newsletter-",
            targetAddresses = listOf("marketing-archive@coolbrand.dev"),
            catchall = false
        ),
        RoutingRule(
            id = 103L,
            domainName = "stealthai.io",
            prefix = false,
            matchUser = "contact",
            targetAddresses = listOf("founder@stealthai.io"),
            catchall = false
        ),
        RoutingRule(
            id = 104L,
            domainName = "coolbrand.dev",
            prefix = false,
            matchUser = "*",
            targetAddresses = listOf("alex@coolbrand.dev"),
            catchall = true
        )
    )

    override suspend fun checkAccountCredit(): Result<CheckCreditResponse> {
        delay(300)
        return Result.success(CheckCreditResponse(credit = creditAmount))
    }

    override suspend fun listUsers(): Result<List<String>> {
        delay(350)
        return Result.success(usersList.toList())
    }

    override suspend fun getUser(userName: String): Result<GetUserResponse> {
        delay(250)
        val settings = userSettingsMap[userName] ?: GetUserResponse(
            enableSearchIndexing = true,
            recoveryEnabled = false,
            requireTwoFactorAuthentication = false,
            enableSpamFiltering = true,
            resetMethods = emptyList()
        )
        return Result.success(settings)
    }

    override suspend fun createUser(request: CreateUserRequest): Result<Unit> {
        delay(400)
        val fullUser = "${request.userName}@${request.domainName}"
        if (!usersList.contains(fullUser)) {
            usersList.add(fullUser)
            val resetMethods = mutableListOf<GetUserPasswordResetMethod>()
            if (!request.recoveryEmail.isNullOrBlank()) {
                resetMethods.add(
                    GetUserPasswordResetMethod(
                        type = "email",
                        target = request.recoveryEmail,
                        description = request.recoveryEmailDescription ?: "Initial recovery email",
                        allowMfaReset = true
                    )
                )
            }
            if (!request.recoveryPhone.isNullOrBlank()) {
                resetMethods.add(
                    GetUserPasswordResetMethod(
                        type = "phone",
                        target = request.recoveryPhone,
                        description = request.recoveryPhoneDescription ?: "Initial recovery phone",
                        allowMfaReset = false
                    )
                )
            }
            userSettingsMap[fullUser] = GetUserResponse(
                enableSearchIndexing = request.enableSearchIndexing,
                recoveryEnabled = request.enablePasswordReset,
                requireTwoFactorAuthentication = false,
                enableSpamFiltering = true,
                resetMethods = resetMethods
            )
        }
        return Result.success(Unit)
    }

    override suspend fun modifyUser(request: ModifyUserRequest): Result<Unit> {
        delay(350)
        val existing = userSettingsMap[request.userName] ?: GetUserResponse()
        val updatedUser = request.newUserName ?: request.userName

        if (request.newUserName != null && request.newUserName != request.userName) {
            usersList.remove(request.userName)
            usersList.add(request.newUserName)
            userSettingsMap.remove(request.userName)
        }

        userSettingsMap[updatedUser] = existing.copy(
            enableSearchIndexing = request.enableSearchIndexing ?: existing.enableSearchIndexing,
            recoveryEnabled = request.enablePasswordReset ?: existing.recoveryEnabled,
            requireTwoFactorAuthentication = request.requireTwoFactorAuthentication ?: existing.requireTwoFactorAuthentication
        )
        return Result.success(Unit)
    }

    override suspend fun deleteUser(userName: String): Result<Unit> {
        delay(350)
        usersList.remove(userName)
        userSettingsMap.remove(userName)
        return Result.success(Unit)
    }

    override suspend fun createAppPassword(userHandle: String, name: String): Result<String> {
        delay(300)
        val randomChars = (1..16).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
        val password = "pm_${randomChars.substring(0, 4)}_${randomChars.substring(4, 8)}_${randomChars.substring(8, 12)}_${randomChars.substring(12)}"
        return Result.success(password)
    }

    override suspend fun deleteAppPassword(userName: String, appPassword: String): Result<Unit> {
        delay(250)
        return Result.success(Unit)
    }

    override suspend fun listPasswordReset(userName: String): Result<List<ListPasswordResetResponseItem>> {
        delay(250)
        val user = userSettingsMap[userName]
        val items = user?.resetMethods?.map {
            ListPasswordResetResponseItem(
                type = it.type,
                target = it.target,
                description = it.description,
                allowMfaReset = it.allowMfaReset
            )
        } ?: emptyList()
        return Result.success(items)
    }

    override suspend fun upsertPasswordReset(request: UpsertPasswordResetRequest): Result<Unit> {
        delay(300)
        val current = userSettingsMap[request.userName] ?: GetUserResponse()
        val currentMethods = current.resetMethods.toMutableList()

        if (request.existingTarget != null) {
            currentMethods.removeAll { it.target == request.existingTarget }
        }
        currentMethods.add(
            GetUserPasswordResetMethod(
                type = request.type,
                target = request.target,
                description = request.description,
                allowMfaReset = request.allowMfaReset
            )
        )
        userSettingsMap[request.userName] = current.copy(
            recoveryEnabled = true,
            resetMethods = currentMethods
        )
        return Result.success(Unit)
    }

    override suspend fun deletePasswordReset(request: DeletePasswordResetRequest): Result<Unit> {
        delay(300)
        val current = userSettingsMap[request.userName] ?: return Result.success(Unit)
        val currentMethods = current.resetMethods.toMutableList()
        if (request.target != null) {
            currentMethods.removeAll { it.target == request.target }
        } else {
            currentMethods.clear()
        }
        userSettingsMap[request.userName] = current.copy(resetMethods = currentMethods)
        return Result.success(Unit)
    }

    override suspend fun listDomains(includeShared: Boolean): Result<List<ApiDomainInfo>> {
        delay(350)
        val filtered = if (includeShared) domainsList else domainsList.filter { !it.isShared }
        return Result.success(filtered)
    }

    override suspend fun addDomain(domainName: String): Result<Unit> {
        delay(400)
        if (domainsList.none { it.name.equals(domainName, ignoreCase = true) }) {
            domainsList.add(
                ApiDomainInfo(
                    name = domainName,
                    allowAccountReset = true,
                    symbolicSubaddressing = true,
                    isShared = false,
                    dnsSummary = ApiDomainDnsSummary(
                        passesMx = false,
                        passesSpf = false,
                        passesDkim = false,
                        passesDmarc = false
                    )
                )
            )
        }
        return Result.success(Unit)
    }

    override suspend fun getOwnershipCode(): Result<String> {
        delay(200)
        return Result.success("purelymail-ownership-proof=a8f93c01b9e2467d")
    }

    override suspend fun updateDomainSettings(request: UpdateDomainSettingsRequest): Result<Unit> {
        delay(350)
        val index = domainsList.indexOfFirst { it.name.equals(request.name, ignoreCase = true) }
        if (index != -1) {
            val old = domainsList[index]
            // If recheckDns is requested in mock, simulate passing DNS
            val newDns = if (request.recheckDns) {
                ApiDomainDnsSummary(passesMx = true, passesSpf = true, passesDkim = true, passesDmarc = true)
            } else {
                old.dnsSummary
            }
            domainsList[index] = old.copy(
                allowAccountReset = request.allowAccountReset ?: old.allowAccountReset,
                symbolicSubaddressing = request.symbolicSubaddressing ?: old.symbolicSubaddressing,
                dnsSummary = newDns
            )
        }
        return Result.success(Unit)
    }

    override suspend fun deleteDomain(name: String): Result<Unit> {
        delay(350)
        domainsList.removeAll { it.name.equals(name, ignoreCase = true) }
        return Result.success(Unit)
    }

    override suspend fun listRoutingRules(): Result<List<RoutingRule>> {
        delay(300)
        return Result.success(routingRulesList.toList())
    }

    override suspend fun createRoutingRule(request: CreateRoutingRequest): Result<Unit> {
        delay(350)
        val newId = (routingRulesList.maxOfOrNull { it.id } ?: 100L) + 1L
        routingRulesList.add(
            RoutingRule(
                id = newId,
                domainName = request.domainName,
                prefix = request.prefix,
                matchUser = request.matchUser,
                targetAddresses = request.targetAddresses,
                catchall = request.catchall
            )
        )
        return Result.success(Unit)
    }

    override suspend fun deleteRoutingRule(routingRuleId: Long): Result<Unit> {
        delay(300)
        routingRulesList.removeAll { it.id == routingRuleId }
        return Result.success(Unit)
    }
}
