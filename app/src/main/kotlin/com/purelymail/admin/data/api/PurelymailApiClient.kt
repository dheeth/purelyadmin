package com.purelymail.admin.data.api

import com.purelymail.admin.data.model.*
import com.purelymail.admin.data.repository.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

interface PurelymailApiClient {
    suspend fun checkAccountCredit(): Result<CheckCreditResponse>
    suspend fun listUsers(): Result<List<String>>
    suspend fun getUser(userName: String): Result<GetUserResponse>
    suspend fun createUser(request: CreateUserRequest): Result<Unit>
    suspend fun modifyUser(request: ModifyUserRequest): Result<Unit>
    suspend fun deleteUser(userName: String): Result<Unit>
    suspend fun createAppPassword(userHandle: String, name: String = ""): Result<String>
    suspend fun deleteAppPassword(userName: String, appPassword: String): Result<Unit>
    suspend fun listPasswordReset(userName: String): Result<List<ListPasswordResetResponseItem>>
    suspend fun upsertPasswordReset(request: UpsertPasswordResetRequest): Result<Unit>
    suspend fun deletePasswordReset(request: DeletePasswordResetRequest): Result<Unit>
    suspend fun listDomains(includeShared: Boolean = false): Result<List<ApiDomainInfo>>
    suspend fun addDomain(domainName: String): Result<Unit>
    suspend fun getOwnershipCode(): Result<String>
    suspend fun updateDomainSettings(request: UpdateDomainSettingsRequest): Result<Unit>
    suspend fun deleteDomain(name: String): Result<Unit>
    suspend fun listRoutingRules(): Result<List<RoutingRule>>
    suspend fun createRoutingRule(request: CreateRoutingRequest): Result<Unit>
    suspend fun deleteRoutingRule(routingRuleId: Long): Result<Unit>
}

class RealPurelymailApiClient(
    private val tokenManager: TokenManager
) : PurelymailApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Request pacing mutex to avoid flooding or disrupting Purelymail servers
    private val requestMutex = Mutex()
    private var lastRequestTimestamp = 0L
    private val minRequestSpacingMs = 250L

    private suspend inline fun <reified REQ, reified RESP> post(
        endpoint: String,
        body: REQ
    ): Result<RESP> = withContext(Dispatchers.IO) {
        val token = tokenManager.apiToken.value
        if (token.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Purelymail API token configured."))
        }

        // Direct client-to-server request: No intermediate proxy server is ever used
        val baseUrl = tokenManager.serverUrl.value
        val url = "$baseUrl/api/v0/$endpoint"
        val jsonBody = json.encodeToString(body)

        val maxAttempts = 3
        var attempt = 0
        var backoffMs = 800L

        while (attempt < maxAttempts) {
            attempt++

            // Throttling: Enforce minimum inter-request delay
            requestMutex.withLock {
                val now = System.currentTimeMillis()
                val elapsed = now - lastRequestTimestamp
                if (elapsed < minRequestSpacingMs) {
                    delay(minRequestSpacingMs - elapsed)
                }
                lastRequestTimestamp = System.currentTimeMillis()
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Purelymail-Api-Token", token)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    // Handle HTTP 429 Too Many Requests or 503 Throttling
                    if (response.code == 429 || response.code == 503) {
                        val retryAfterSec = response.header("Retry-After")?.toLongOrNull() ?: (backoffMs / 1000L).coerceAtLeast(1L)
                        if (attempt < maxAttempts) {
                            val waitDuration = (retryAfterSec * 1000L).coerceAtLeast(backoffMs) + Random.nextLong(100, 350)
                            delay(waitDuration)
                            backoffMs *= 2
                            return@use // continue retry loop
                        } else {
                            return@withContext Result.failure(
                                PurelymailRateLimitException(
                                    retryAfterSeconds = retryAfterSec,
                                    message = "Purelymail rate limit encountered (HTTP ${response.code}). Please wait $retryAfterSec seconds before retrying."
                                )
                            )
                        }
                    }

                    val responseString = response.body?.string().orEmpty()
                    if (responseString.isBlank()) {
                        return@withContext Result.failure(IOException("Empty response from Purelymail server (HTTP ${response.code})"))
                    }

                    try {
                        val parsed = json.decodeFromString<PurelymailResponse<RESP>>(responseString)
                        if (parsed.isSuccess && parsed.result != null) {
                            return@withContext Result.success(parsed.result)
                        } else {
                            val isThrottled = parsed.code?.contains("rate", ignoreCase = true) == true ||
                                    parsed.code?.contains("throttle", ignoreCase = true) == true ||
                                    parsed.errorMessage.contains("rate limit", ignoreCase = true)

                            if (isThrottled) {
                                return@withContext Result.failure(PurelymailRateLimitException(message = parsed.errorMessage))
                            } else {
                                return@withContext Result.failure(PurelymailApiException(code = parsed.code, message = parsed.errorMessage))
                            }
                        }
                    } catch (e: Exception) {
                        return@withContext Result.failure(IOException("Failed to parse Purelymail response: ${e.message}\nRaw: $responseString", e))
                    }
                }
            } catch (e: Exception) {
                if (attempt >= maxAttempts) {
                    return@withContext Result.failure(e)
                }
                delay(backoffMs + Random.nextLong(100, 250))
                backoffMs *= 2
            }
        }

        Result.failure(IOException("Request to Purelymail failed after $maxAttempts attempts."))
    }

    override suspend fun checkAccountCredit(): Result<CheckCreditResponse> =
        post("checkAccountCredit", EmptyRequest())

    override suspend fun listUsers(): Result<List<String>> =
        post<EmptyRequest, ListUserResponse>("listUser", EmptyRequest()).map { it.users }

    override suspend fun getUser(userName: String): Result<GetUserResponse> =
        post("getUser", GetUserRequest(userName = userName))

    override suspend fun createUser(request: CreateUserRequest): Result<Unit> =
        post<CreateUserRequest, EmptyResponse>("createUser", request).map { }

    override suspend fun modifyUser(request: ModifyUserRequest): Result<Unit> =
        post<ModifyUserRequest, EmptyResponse>("modifyUser", request).map { }

    override suspend fun deleteUser(userName: String): Result<Unit> =
        post<DeleteUserRequest, EmptyResponse>("deleteUser", DeleteUserRequest(userName = userName)).map { }

    override suspend fun createAppPassword(userHandle: String, name: String): Result<String> =
        post<CreateAppPasswordRequest, CreateAppPasswordResponse>(
            "createAppPassword",
            CreateAppPasswordRequest(userHandle = userHandle, name = name)
        ).map { it.appPassword }

    override suspend fun deleteAppPassword(userName: String, appPassword: String): Result<Unit> =
        post<DeleteAppPasswordRequest, EmptyResponse>(
            "deleteAppPassword",
            DeleteAppPasswordRequest(userName = userName, appPassword = appPassword)
        ).map { }

    override suspend fun listPasswordReset(userName: String): Result<List<ListPasswordResetResponseItem>> =
        post<ListPasswordResetRequest, ListPasswordResetResponse>(
            "listPasswordReset",
            ListPasswordResetRequest(userName = userName)
        ).map { it.users }

    override suspend fun upsertPasswordReset(request: UpsertPasswordResetRequest): Result<Unit> =
        post<UpsertPasswordResetRequest, EmptyResponse>("upsertPasswordReset", request).map { }

    override suspend fun deletePasswordReset(request: DeletePasswordResetRequest): Result<Unit> =
        post<DeletePasswordResetRequest, EmptyResponse>("deletePasswordReset", request).map { }

    override suspend fun listDomains(includeShared: Boolean): Result<List<ApiDomainInfo>> =
        post<ListDomainsRequest, ListDomainsResponse>("listDomains", ListDomainsRequest(includeShared = includeShared)).map { it.domains }

    override suspend fun addDomain(domainName: String): Result<Unit> =
        post<AddDomainRequest, EmptyResponse>("addDomain", AddDomainRequest(domainName = domainName)).map { }

    override suspend fun getOwnershipCode(): Result<String> =
        post<EmptyRequest, GetOwnershipCodeResponse>("getOwnershipCode", EmptyRequest()).map { it.code }

    override suspend fun updateDomainSettings(request: UpdateDomainSettingsRequest): Result<Unit> =
        post<UpdateDomainSettingsRequest, EmptyResponse>("updateDomainSettings", request).map { }

    override suspend fun deleteDomain(name: String): Result<Unit> =
        post<DeleteDomainRequest, EmptyResponse>("deleteDomain", DeleteDomainRequest(name = name)).map { }

    override suspend fun listRoutingRules(): Result<List<RoutingRule>> =
        post<EmptyRequest, ListRoutingResponse>("listRoutingRules", EmptyRequest()).map { it.rules }

    override suspend fun createRoutingRule(request: CreateRoutingRequest): Result<Unit> =
        post<CreateRoutingRequest, EmptyResponse>("createRoutingRule", request).map { }

    override suspend fun deleteRoutingRule(routingRuleId: Long): Result<Unit> =
        post<DeleteRoutingRequest, EmptyResponse>("deleteRoutingRule", DeleteRoutingRequest(routingRuleId = routingRuleId)).map { }
}
