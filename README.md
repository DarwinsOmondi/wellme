# WellMe 🌿

**WellMe** is a modern Android application designed as a smart campus digital wallet and merchant marketplace ecosystem. It bridges the gap between university students and campus merchants by offering seamless contactless payments, wallet top-ups via M-Pesa, merchant discovery, point-of-sale (POS) processing, and inventory management.

---

## 🌟 Key Features

### 🎓 Student Portal
- **Secure Authentication & Onboarding**: Email/OTP login with campus email verification and KYC identity confirmation.
- **Digital Student Wallet**: Real-time balance tracking, daily allowance management, and transaction history.
- **M-Pesa STK Push Integration**: Direct, instant wallet funding and payment initiation powered by M-Pesa.
- **Contactless QR Payments**: Generate dynamic QR codes or use the camera scanner to complete fast payments at campus merchants.
- **Campus Discovery**: Browse nearby student-friendly merchants, cafes, and campus stores, and view item catalogs.
- **Account Settings**: Manage personal details, profile picture, security preferences, and notification options.

### 🏪 Merchant Portal
- **Streamlined Merchant Onboarding**: Simple registration for campus businesses with legal documentation and financial settlement configuration (M-Pesa Till / Paybill / Bank account).
- **Point of Sale (POS)**: Accept payments instantly via dynamic QR code scanning and direct student wallet transfers.
- **Inventory Management**: Add and manage store items with product titles, pricing, categories, stock levels, and image uploads.
- **Capital & Loans**: Request working capital loans and monitor pool progress directly from the dashboard.

---

## 🏗 Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) 2.0+
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (`androidx.compose.material3`)
- **Navigation**: Modern AndroidX Navigation 3 (`androidx.navigation3`)
- **Architecture**: Clean Architecture with MVVM pattern (Model-View-ViewModel) and Use Cases
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) (Dagger Hilt)
- **Backend Services**: [Supabase](https://supabase.com/) (Auth, Postgrest Database, Storage, and Realtime)
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) for offline-first capabilities
- **Networking**: [Ktor Client](https://ktor.io/) & [Coil 3](https://coil-kt.github.io/coil/) for image loading
- **Payments**: Safaricom M-Pesa Daraja API (STK Push)

---

## 📁 Project Structure

```
app/src/main/java/com/example/wellme/
├── data/                  # Remote & Local data sources, Repositories implementations, Room DAOs & Entities
│   ├── local/             # Room Database, Entities, and DAOs
│   ├── remote/            # Supabase & M-Pesa API services and DTOs
│   └── repository/        # Repository implementations
├── di/                    # Dependency Injection modules (AppModule, DatabaseModule, NetworkModule, SupabaseModule)
├── domain/                # Business logic layer
│   ├── model/             # Domain Models
│   ├── repository/        # Repository Interfaces
│   └── usecase/           # Domain Use Cases (Payment, Loans, M-Pesa STK Push, etc.)
├── presentation/          # Jetpack Compose UI Screens & ViewModels
│   ├── auth/              # Sign In, Sign Up, OTP Verification
│   ├── onboarding/        # Student & Merchant Onboarding flows
│   ├── student/           # Student Dashboard, Wallet, Merchant Discovery, QR Scanner
│   ├── merchant/          # Merchant Dashboard, POS, Inventory Management, Capital Requests
│   ├── profile/           # Profile & Settings screens
│   └── common/            # Reusable Composables (QR Code, Bottom Sheets, SwipeToConfirm)
├── theme/                 # App Color schemes, Typography, and Material 3 Themes
└── util/                  # Helper utilities and Error mappers
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java 21 Toolchain
- **Android SDK**: Minimum SDK 24 (Android 7.0), Target SDK 36

### Configuration

Create a `local.properties` file in the project root directory (if not present) and add your API credentials:

```properties
# Supabase Configuration
supabase.url=https://YOUR_SUPABASE_PROJECT.supabase.co
supabase.key=YOUR_SUPABASE_ANON_KEY

# M-Pesa Daraja API Credentials
mpesa.consumer.key=YOUR_MPESA_CONSUMER_KEY
mpesa.consumer.secret=YOUR_MPESA_CONSUMER_SECRET
mpesa.passkey=YOUR_MPESA_PASSKEY
mpesa.callback.url=https://YOUR_CALLBACK_URL
```

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/DarwinsOmondi/wellme.git
   cd wellme
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle and run the app on an Android Emulator or connected physical device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Testing

To execute unit tests:
```bash
./gradlew test
```

To run instrumented UI tests:
```bash
./gradlew connectedAndroidTest
```

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
