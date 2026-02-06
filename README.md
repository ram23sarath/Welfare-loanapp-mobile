# Loanapp-mobile

Native Android app for the Loan Management System.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository Pattern
- **Backend**: Supabase (PostgreSQL)
- **Local Database**: Room
- **Dependency Injection**: Hilt
- **Networking**: Ktor Client

## Project Structure

```
app/
├── src/main/
│   ├── java/com/ijreddy/loanapp/
│   │   ├── data/           # Repository, Room entities, DAOs
│   │   ├── di/             # Hilt modules
│   │   ├── domain/         # Use cases, business logic
│   │   ├── ui/             # Composables, ViewModels
│   │   │   ├── auth/       # Login screens
│   │   │   ├── dashboard/  # Customer dashboard
│   │   │   ├── loans/      # Loan list & detail
│   │   │   ├── subscriptions/
│   │   │   ├── data/       # Data entries
│   │   │   └── common/     # Shared UI components
│   │   └── util/           # Extensions, helpers
│   └── res/
└── build.gradle.kts
```

## Getting Started

1. Clone this repository
2. Open in Android Studio (Hedgehog or later)
3. Copy `.env.example` to `local.properties` and add your Supabase credentials
4. Build and run on emulator or device

## Related Documentation

See `docs/` folder for:
- `migration_plan.md` - Full migration guide from web app
- `sql_extraction.json` - Database schema
- `api_contracts.json` - API endpoints
- `ui_components.json` - Screen mappings

## Moving to Separate Repository

This project is currently inside the Loan-App repo. To move to a separate repo:

```powershell
# From parent directory
mv Loan-App/Loanapp-mobile ../Loanapp-mobile
cd ../Loanapp-mobile
git remote add origin https://github.com/YOUR_USERNAME/Loanapp-mobile.git
git push -u origin main
```
