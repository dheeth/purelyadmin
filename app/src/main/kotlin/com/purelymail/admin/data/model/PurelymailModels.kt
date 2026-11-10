package com.purelymail.admin.data.model

import kotlinx.serialization.Serializable
import java.io.IOException

/**
 * Custom exceptions for Purelymail API interactions.
 */
open class PurelymailApiException(
    val code: String?,
    override val message: String
) : IOException("Purelymail Error${if (!code.isNullOrBlank()) " [$code]" else ""}: $message")

class PurelymailRateLimitException(
    val retryAfterSeconds: Long? = null,
    message: String = "Purelymail rate limit reached. Please wait ${retryAfterSeconds ?: 5}s before retrying."
) : PurelymailApiException("RATE_LIMIT_EXCEEDED", message)

/**
 * Generic response wrapper returned by Purelymail API.
 * Successful responses have `result` field.
 * Error responses have `type = "error"`, `code`, and `message`.
 */
@Serializable
data class PurelymailResponse<T>(
    val result: T? = null,
    val type: String? = null,
    val code: String? = null,
    val message: String? = null
) {
    val isSuccess: Boolean
        get() = result != null && (type == null || type == "success")

    val errorMessage: String
        get() = message ?: code ?: "Unknown Purelymail API error"
}

@Serializable
class EmptyResponse

@Serializable
class EmptyRequest

@Serializable
data class PurelymailError(
    val code: String,
    val message: String
)

// ==================== USER MANAGEMENT ====================

@Serializable
data class CreateUserRequest(
    val userName: String,
    val domainName: String,
    val password: String,
    val enablePasswordReset: Boolean = true,
    val recoveryEmail: String? = null,
    val recoveryEmailDescription: String? = null,
    val recoveryPhone: String? = null,
    val recoveryPhoneDescription: String? = null,
    val enableSearchIndexing: Boolean = true,
    val sendWelcomeEmail: Boolean = true
)

@Serializable
data class DeleteUserRequest(
    val userName: String
)

@Serializable
data class GetUserRequest(
    val userName: String
)

@Serializable
data class ModifyUserRequest(
    val userName: String,
    val newUserName: String? = null,
    val newPassword: String? = null,
    val enableSearchIndexing: Boolean? = null,
    val enablePasswordReset: Boolean? = null,
    val requireTwoFactorAuthentication: Boolean? = null
)

@Serializable
data class ListUserResponse(
    val users: List<String> = emptyList()
)

@Serializable
data class GetUserPasswordResetMethod(
    val type: String = "email",
    val target: String = "",
    val description: String = "",
    val allowMfaReset: Boolean = true
)

@Serializable
data class GetUserResponse(
    val enableSearchIndexing: Boolean = true,
    val recoveryEnabled: Boolean = false,
    val requireTwoFactorAuthentication: Boolean = false,
    val enableSpamFiltering: Boolean = true,
    val resetMethods: List<GetUserPasswordResetMethod> = emptyList()
)

@Serializable
data class CreateAppPasswordRequest(
    val userHandle: String,
    val name: String = ""
)

@Serializable
data class CreateAppPasswordResponse(
    val appPassword: String
)

@Serializable
data class DeleteAppPasswordRequest(
    val userName: String,
    val appPassword: String
)

// ==================== PASSWORD RESET & RECOVERY ====================

@Serializable
data class UpsertPasswordResetRequest(
    val userName: String,
    val existingTarget: String? = null,
    val type: String, // "email" or "phone"
    val target: String,
    val description: String = "",
    val allowMfaReset: Boolean = true
)

@Serializable
data class DeletePasswordResetRequest(
    val userName: String,
    val target: String? = null
)

@Serializable
data class ListPasswordResetRequest(
    val userName: String
)

@Serializable
data class ListPasswordResetResponseItem(
    val type: String,
    val target: String,
    val description: String = "",
    val allowMfaReset: Boolean = true
)

@Serializable
data class ListPasswordResetResponse(
    val users: List<ListPasswordResetResponseItem> = emptyList()
)

// ==================== ROUTING & ALIASES ====================

@Serializable
data class RoutingRule(
    val id: Long,
    val domainName: String,
    val prefix: Boolean,
    val matchUser: String,
    val targetAddresses: List<String>,
    val catchall: Boolean = false
) {
    val displayMatch: String
        get() = when {
            catchall -> "*@$domainName (Catch-all)"
            prefix -> "$matchUser*@$domainName"
            else -> "$matchUser@$domainName"
        }
}

@Serializable
data class ListRoutingResponse(
    val rules: List<RoutingRule> = emptyList()
)

@Serializable
data class CreateRoutingRequest(
    val domainName: String,
    val prefix: Boolean,
    val matchUser: String,
    val targetAddresses: List<String>,
    val catchall: Boolean = false
)

@Serializable
data class DeleteRoutingRequest(
    val routingRuleId: Long
)

// ==================== DOMAINS ====================

@Serializable
data class ApiDomainDnsSummary(
    val passesMx: Boolean = false,
    val passesSpf: Boolean = false,
    val passesDkim: Boolean = false,
    val passesDmarc: Boolean = false
) {
    val isFullyPassing: Boolean
        get() = passesMx && passesSpf && passesDkim && passesDmarc

    val passingCount: Int
        get() = (if (passesMx) 1 else 0) + (if (passesSpf) 1 else 0) +
                (if (passesDkim) 1 else 0) + (if (passesDmarc) 1 else 0)
}

@Serializable
data class ApiDomainInfo(
    val name: String,
    val allowAccountReset: Boolean = false,
    val symbolicSubaddressing: Boolean = true,
    val isShared: Boolean = false,
    val dnsSummary: ApiDomainDnsSummary = ApiDomainDnsSummary()
)

@Serializable
data class ListDomainsRequest(
    val includeShared: Boolean = false
)

@Serializable
data class ListDomainsResponse(
    val domains: List<ApiDomainInfo> = emptyList()
)

@Serializable
data class AddDomainRequest(
    val domainName: String
)

@Serializable
data class GetOwnershipCodeResponse(
    val code: String
)

@Serializable
data class UpdateDomainSettingsRequest(
    val name: String,
    val allowAccountReset: Boolean? = null,
    val symbolicSubaddressing: Boolean? = null,
    val recheckDns: Boolean = false
)

@Serializable
data class DeleteDomainRequest(
    val name: String
)

// ==================== BILLING ====================

@Serializable
data class CheckCreditResponse(
    val credit: String
) {
    val numericCredit: Double
        get() = credit.toDoubleOrNull() ?: 0.0

    val formattedCredit: String
        get() = String.format("$%.2f", numericCredit)

    val isLowBalance: Boolean
        get() = numericCredit < 2.0
}
