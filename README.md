# PureAdmin for Purelymail

[![Release](https://img.shields.io/badge/Release-v1.0.0-5E35B1.svg?style=flat-square)](https://github.com/dheeth/purelyadmin/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-00E5FF.svg?style=flat-square)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF.svg?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Direct Connection](https://img.shields.io/badge/Architecture-100%25%20Direct%20TLS-00C853.svg?style=flat-square)](#-privacy--security-architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

A fast, modern, privacy-first native Android management client for [Purelymail](https://purelymail.com). Built with Kotlin, Jetpack Compose, and Material 3, PureAdmin brings full administrative control of your email infrastructure to your phone.

---

## 📥 Download & Install

### GitHub Releases
Download the latest signed production APK directly from the [Releases](https://github.com/dheeth/purelyadmin/releases) page:
* **[Download PureAdmin v1.0.0 APK](https://github.com/dheeth/purelyadmin/releases/download/v1.0.0/pureadmin-v1.0.0.apk)**

### F-Droid
PureAdmin is fully open source (Apache-2.0) and has zero proprietary dependencies or telemetry. Inclusion recipes and Fastlane metadata are configured in the repository:
* Track official inclusion status on [F-Droid RFP / GitLab](https://gitlab.com/fdroid/rfp).
* Pre-compiled builds are available via the [Releases](https://github.com/dheeth/purelyadmin/releases) tab.

> **Android Installation Note**: Since PureAdmin is distributed independently outside the Google Play Store, Android will display a standard prompt asking to allow installations from your browser or file manager. The release APK is signed with a dedicated production RSA certificate using APK Signature Scheme v2 and v3.

---

## ✨ Features

### 📊 Executive Overview & Balance
* **Real-Time Credit Monitor**: Track prepaid Purelymail balance with automated low-credit alert thresholds.
* **Instant Shortcuts**: One-tap access to Purelymail Webmail (`https://inbox.purelymail.com/`) and the Billing portal.
* **DNS Alert Banner**: Immediate proactive notification whenever any custom domain has missing or failing DNS records.
* **Email Client Cheat Sheet**: Copy pre-formatted IMAP (`mail.purelymail.com:993`) and SMTP (`mail.purelymail.com:465 / 587`) hostnames and port configs in a single tap.

### 👥 Mailbox (User) Management
* **Fast Mailbox Search**: Real-time filtering across users and domain handles.
* **Mailbox Creation**: Provision new users with custom domain selection, built-in strong password generation, recovery contact setup, and welcome email options.
* **Dedicated App Passwords**: Generate and label application-specific passwords for third-party email clients (Thunderbird, Apple Mail, K-9 Mail, Outlook) without exposing your master account password.
* **Recovery & Security Controls**: View, assign, or revoke recovery email addresses and phone numbers with MFA authorization options.
* **Anti-Abuse Safeguards**: In-app guidance protecting against accidental policy violations (such as prohibited domain warming or bulk spam).

### 🌐 Custom Domains & DNS Health Center
* **Live DNS Health Matrix**: Real-time validation chips for `[MX]`, `[SPF]`, `[DKIM]`, and `[DMARC]`.
* **One-Tap TXT Ownership Proof**: Retrieve and copy your domain's verification string (`purelymail-ownership-proof=...`) with one click.
* **DNS Recheck Action**: Trigger an immediate server-side DNS re-scan after updating DNS records at your registrar.
* **Domain Feature Toggles**: Control plus-addressing (symbolic subaddressing: `user+tag@domain`) and account password reset permissions.
* **Shared Domain Filter**: Cleanly filter or include Purelymail's default shared domain (`@purelymail.com`).

### 🔀 Smart Routing & Instant Service Aliases
* **Instant Disposable Aliases**: Generate private, disposable forwarding addresses (e.g. `netflix.x8b2@yourdomain.com`) on the fly when signing up for web services.
* **One-Click Service Presets**: Quick templates for Netflix, Amazon, Banking, Newsletters, GitHub, Travel, and Shopping.
* **Advanced Rule Routing**:
  * Exact address matches (`press@yourdomain.com`)
  * Prefix wildcard rules (`orders-*@yourdomain.com`)
  * Domain-wide catch-all rules (`*@yourdomain.com`)
  * Multiple forwarding target recipients

### ⚙️ Streamlined Settings
* **Encrypted API Token Management**: View or update your token at any time with instant on-device encryption.
* **Automatic Global Refresh**: Re-saving your token immediately refreshes live data across all dashboard screens.
* **Safe Session Resets**: Disconnect or wipe local application data with safety confirmation dialogs.

---

## 🛡️ Privacy & Security Architecture

PureAdmin was built from the ground up with a strict zero-knowledge security model:

1. **100% Direct Client-to-Server**: Every network request originates directly on your Android device and connects strictly to `https://purelymail.com/api/v0/` over TLS 1.3.
2. **Zero Intermediary Servers**: There are no proxy servers, no middleware relays, no external cloud databases, and no third-party telemetry frameworks. We cannot see, log, or access your credentials or email metadata.
3. **Hardware-Backed Encryption**: Your Purelymail API token is stored on-device using Android's `EncryptedSharedPreferences` backed by the hardware **Android Keystore** with AES-256 GCM encryption.
4. **Minimal App Permissions**: The app requires only standard `INTERNET` and `ACCESS_NETWORK_STATE` permissions. It does not request access to contacts, storage, camera, location, or SMS.
5. **Polite Request Pacing & Backoff**: Includes automated client-side request throttling and exponential backoff handling for HTTP 429 rate-limiting responses to ensure server stability.

---

## 🔑 Getting Started

1. Log in to your Purelymail account at **[purelymail.com/manage/account](https://purelymail.com/manage/account)**.
2. Navigate to **API Keys** and generate a new token.
3. Open **PureAdmin** on your phone.
4. Paste your token and tap **Connect**.

---

## 🏗️ Building from Source

### Prerequisites
* JDK 17 or JDK 21
* Android SDK (API 36)

### Clone & Compile
```bash
git clone https://github.com/dheeth/purelyadmin.git
cd purelyadmin

# Build debug APK
./gradlew assembleDebug

# Build production signed release APK
./gradlew assembleRelease
```

The compiled release APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## ⚖️ Legal & Disclaimer

PureAdmin is an independent, open-source project developed for Purelymail users. 

* PureAdmin is **not** affiliated with, authorized, maintained, sponsored, or endorsed by **Add Rabbit LLC** or **Purelymail**.
* All product names, logos, and brands are property of their respective owners.
* Mailboxes and routing rules created through PureAdmin remain subject to Purelymail's Terms of Service and Anti-Abuse Policies (domain warming, bulk unsolicited marketing, and deceptive automation are strictly prohibited by Purelymail).

---

## 📄 License

Licensed under the [Apache License, Version 2.0](LICENSE).
