LOAN-APP ANDROID MIGRATION - QUICK START
=========================================

This package contains artifacts for migrating the Loan-App
from React+Supabase web to native Android (Kotlin+Compose).

ARTIFACTS GENERATED:
--------------------
1. repo_index.json      - Full file inventory (78 files)
2. sql_extraction.json  - Database schema (10 tables, 12+ queries)
3. api_contracts.json   - API endpoints (auth + data)
4. ui_components.json   - UI inventory (16 screens, 11 modals)
5. migration_plan.md    - 12-section implementation guide
6. assets_report.md     - Icons, splash, fonts analysis

NEXT THREE DEVELOPER ACTIONS:
-----------------------------

ACTION 1: Initialize Android Project
  > Create new Android Studio project with:
    - Kotlin + Jetpack Compose
    - Min SDK 26 (Android 8.0)
    - Dependencies: supabase-kt, Room, Navigation Compose
  > See: migration_plan.md Section 11

ACTION 2: Implement Login Screen
  > Port LoginPage.tsx to Kotlin Compose
  > Implement EncryptedSharedPreferences for token storage
  > Handle phone->email conversion: {phone}@loanapp.local
  > See: api_contracts.json (auth endpoints)

ACTION 3: Create Room Database Entities
  > Define 8 entities matching sql_extraction.json schema:
    - CustomerEntity, LoanEntity, InstallmentEntity
    - SubscriptionEntity, DataEntryEntity
    - SeniorityEntity, InterestEntity, DocumentEntity
  > See: sql_extraction.json (tables section)

BLOCKING ITEMS:
---------------
- Login flow MUST work before any other feature
- Supabase project URL and anon key required (.env)

TECH STACK MAPPING:
-------------------
Web                    -> Android
React Context          -> ViewModel + StateFlow
react-router-dom       -> Navigation Compose
Framer Motion          -> Compose Animations
react-hook-form        -> ViewModel state
Chart.js               -> MPAndroidChart
TailwindCSS            -> Material3 Theme

For full details, see migration_plan.md
