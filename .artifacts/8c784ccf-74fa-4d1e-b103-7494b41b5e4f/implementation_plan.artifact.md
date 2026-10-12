# Implementation Plan: Backend-Ledger-First FinTech Refactoring

Refactor the Android application to remove all direct client-side financial writes, eliminate item-purchase STK pushes, and route all wallet deposits and wallet-balance checkouts exclusively through secure backend RPC functions.

## User Review Required

> [!IMPORTANT]
> - **No Item Checkout STK Push**: Item purchases will now be deducted directly and atomically from the student's in-app wallet balance via a backend RPC (`process_merchant_payment`).
> - **No Client-Side Financial Writes**: The app will no longer insert directly into `transactions`, `pending_payments`, or `student_wallets`.

## Proposed Changes

### Data & Repository Layer
#### [MODIFY] [WalletRepositoryImpl.kt](file:///C:/Users/darwi/.gemini/antigravity/scratch/WellMe/app/src/main/java/com/example/wellme/data/repository/WalletRepositoryImpl.kt)
- Remove direct `.insert` and `.upsert` calls on financial tables.
- Route deposit logging and checkout payments through Supabase RPC functions (`process_student_deposit`, `process_merchant_payment`).

### Presentation & ViewModel Layer
#### [MODIFY] [StudentDiscoverViewModel.kt](file:///C:/Users/darwi/.gemini/antigravity/scratch/WellMe/app/src/main/java/com/example/wellme/presentation/student/StudentDiscoverViewModel.kt)
- Replace item checkout STK Push with wallet-balance debiting routed via `process_merchant_payment` RPC.
- Display backend-computed discount amounts (linked to merchant loan request discount tiers).

#### [MODIFY] [StudentViewModel.kt](file:///C:/Users/darwi/.gemini/antigravity/scratch/WellMe/app/src/main/java/com/example/wellme/presentation/student/StudentViewModel.kt)
- Retain STK Push *only* for wallet top-ups, keeping pending state observation until webhook confirmation.

## Verification Plan

### Automated Tests
- Run unit tests (`app:testDebugUnitTest`) to verify repository and ViewModel logic.

### Manual Verification
- Verify student wallet deposit flow with pending webhook observation.
- Verify student wallet-balance checkout debiting against merchant inventory and pool discount tiers.
