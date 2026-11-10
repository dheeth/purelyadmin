# PurelyAdmin ✉️

An Android management application for **Purelymail** built with **Kotlin**, **Jetpack Compose**, and **Material 3 Expressive Design**. 

PurelyAdmin implements **100% of Purelymail's 19 administrative API endpoints** into a clean mobile experience.

---

## 🌟 Key Features & UX Innovations

### 1. 📊 Executive Dashboard
* **Real-Time Credit Balance**: Live account credit with visual threshold alerts (alerts when credit is low, e.g. < $2.00).
* **One-Tap Webmail & Billing Shortcuts**: Quick access to Purelymail Webmail (`mail.purelymail.com`) and Billing Portal.
* **DNS Alert Banner**: Highlights any custom domain with missing or unverified MX, SPF, DKIM, or DMARC records.
* **Metric Cards**: Total mailboxes, connected domains, and active forwarding rules at a glance.
* **Email Client Cheat Sheet**: Pre-filled IMAP/SMTP hostnames, ports, and SSL security settings with one-tap copy buttons.

### 2. 👥 Mailbox (User) Management
* **Instant Search & Filtering**: Live search by mailbox name or domain.
* **New Mailbox Creator**: Full support for local parts, custom domain picker, built-in strong password generator, optional recovery email & phone, search indexing toggle, and welcome email switch.
* **User Settings Sheet**: Modify password, enable/disable search indexing, allow/disallow self-service password reset, and enforce two-factor authentication (2FA).
* **App Password Generator**: Generate application-specific passwords for email clients (Thunderbird, Apple Mail, K-9 Mail, Outlook) with custom friendly labels and instant copy.
* **Password Reset & Recovery**: View, add, and delete recovery email addresses and phone numbers with MFA reset authority flags.
* **Safety Safeguards**: Confirmation dialogs for destructive actions.

### 3. 🌐 Custom Domains & DNS Health Center
* **Live DNS Matrix**: Visual breakdown chips for `[MX]`, `[SPF]`, `[DKIM]`, and `[DMARC]`.
* **TXT Ownership Code**: Automatic retrieval and one-click copy of the required DNS ownership verification string (`purelymail-ownership-proof=...`).
* **Required DNS Cheat Sheet**: Pre-formatted MX records (priority 10), SPF TXT records, and DMARC policies.
* **One-Tap "Recheck DNS"**: Query Purelymail to immediately re-verify DNS changes.
* **Domain Feature Switches**: Toggle symbolic subaddressing (plus-tagging: `user+tag@domain`) and account reset permissions.
* **Filter Shared Domains**: Easily toggle visibility for Purelymail's default shared domain (`@purelymail.com`).

### 4. 🔀 Smart Routing & Instant Alias Generator
* **One-Tap "Instant Service Alias"**: Create a disposable, private forwarding address for signups (e.g. `amazon.7a2f@yourdomain.com`) that delivers directly to your inbox.
* **Preset Suggestions**: One-click presets for Amazon, Netflix, Banking, Newsletters, GitHub, Shopping, and Travel.
* **Advanced Rule Creator**:
  * Exact address matches (`press@domain.com`)
  * Prefix wildcard matches (`newsletter-*@domain.com`)
  * Catch-all rules (`*@domain.com`)
  * Multiple forwarding target recipients

### 5. 🛡️ Security, Settings & Demo Mode
* **Encrypted Token Storage**: Saved securely using `EncryptedSharedPreferences` with AES-256 GCM encryption.
* **Interactive Demo / Mock Mode**: Full mock implementation allowing complete interactive exploration of all app features without needing an API token.
* **Connection Tester**: Immediate test of token validity with clear feedback.
* **Custom Server URL**: Configurable base URL for testing or proxy setups.

---

## 🛠️ Complete Purelymail API Coverage

PurelyAdmin maps all **19 official Purelymail API endpoints**:

| API Category | Endpoint | PurelyAdmin Feature / UI Action |
|:---|:---|:---|
| **Billing** | `POST /api/v0/checkAccountCredit` | Live Account Balance card on Dashboard & Low Credit warning |
| **Users** | `POST /api/v0/listUser` | Mailbox list with live search and domain badge |
| **Users** | `POST /api/v0/getUser` | User detail sheet (2FA, search indexing, recovery status) |
| **Users** | `POST /api/v0/createUser` | "New Mailbox" dialog with password generator |
| **Users** | `POST /api/v0/modifyUser` | "Edit User" dialog (password, 2FA, indexing, reset permissions) |
| **Users** | `POST /api/v0/deleteUser` | Safe delete mailbox confirmation dialog |
| **App Passwords** | `POST /api/v0/createAppPassword` | App Password generator with client setup instructions |
| **App Passwords** | `POST /api/v0/deleteAppPassword` | Revoke app password functionality |
| **Password Reset** | `POST /api/v0/listPasswordReset` | Recovery methods list in user detail sheet |
| **Password Reset** | `POST /api/v0/upsertPasswordReset` | "Add Recovery Method" dialog (Email / Phone + MFA reset) |
| **Password Reset** | `POST /api/v0/deletePasswordReset` | Remove individual or all recovery methods |
| **Domains** | `POST /api/v0/listDomains` | Domain cards with DNS health badges & shared domain filter |
| **Domains** | `POST /api/v0/addDomain` | "Add Custom Domain" dialog with DNS ownership guidance |
| **Domains** | `POST /api/v0/getOwnershipCode` | Copies DNS TXT ownership proof token in one click |
| **Domains** | `POST /api/v0/updateDomainSettings` | Toggle plus-addressing, account reset, and trigger "Recheck DNS" |
| **Domains** | `POST /api/v0/deleteDomain` | Delete domain with confirmation dialog |
| **Routing** | `POST /api/v0/listRoutingRules` | Routing rules list with visual arrow flow and destination chips |
| **Routing** | `POST /api/v0/createRoutingRule` | "New Rule" dialog & "Instant Service Alias" generator |
| **Routing** | `POST /api/v0/deleteRoutingRule` | Delete routing rule with confirmation |

---

## 🏗️ Architecture & Tech Stack

* **UI Framework**: Jetpack Compose with Material Design 3 (M3)
* **Architecture**: Clean Architecture with MVVM / MVI pattern using Kotlin Coroutines & `StateFlow`
* **Networking**: OkHttp 4 + Kotlinx Serialization JSON
* **Storage & Security**: `androidx.security.crypto` (`EncryptedSharedPreferences` + Android Keystore)
* **Dynamic Theming**: Purelymail royal purple brand colors (`#5E35B1`), light/dark adaptive color schemes, and Android 12+ Material You Dynamic Color support.

---

## 🚀 Building & Running

### Prerequisites
* JDK 17 or JDK 21
* Android SDK (API 35/36)

### Build the APK
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Install onto an Android Device or Emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Or open the project in **Android Studio** and click **Run 'app'**.
