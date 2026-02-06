# Assets Report: Loan-App Android Migration

## Icon Assets

### Current Location
All icons are defined as **inline SVG components** in `src/constants.tsx` (9KB file).

### Icon Inventory

| Icon | Usage | Android Equivalent |
|------|-------|-------------------|
| `EyeIcon` | Password visibility toggle | `ic_visibility_24dp` (Material) |
| `EyeOffIcon` | Password visibility toggle | `ic_visibility_off_24dp` (Material) |
| `HomeIcon` | Navigation | `ic_home_24dp` |
| `UserIcon` | Customer/Profile | `ic_person_24dp` |
| `MoneyIcon` | Loans/Finance | `ic_payments_24dp` |
| `ListIcon` | Lists | `ic_list_24dp` |
| `ChartIcon` | Summary/Analytics | `ic_analytics_24dp` |
| `TrashIcon` | Delete actions | `ic_delete_24dp` |
| `EditIcon` | Edit actions | `ic_edit_24dp` |
| `PlusIcon` | Add actions | `ic_add_24dp` |
| `CheckIcon` | Success/Confirm | `ic_check_24dp` |
| `CloseIcon` | Close/Cancel | `ic_close_24dp` |
| `SearchIcon` | Search | `ic_search_24dp` |
| `FilterIcon` | Filter | `ic_filter_list_24dp` |
| `DownloadIcon` | PDF download | `ic_download_24dp` |
| `MenuIcon` | Hamburger menu | `ic_menu_24dp` |
| `ArrowBackIcon` | Back navigation | `ic_arrow_back_24dp` |
| `ExpandIcon` | Expand/Collapse | `ic_expand_more_24dp` |

### Recommendation
**Use Material Icons** from `androidx.compose.material:material-icons-extended` instead of custom SVGs.

---

## Splash & App Icons

### Current Assets (from Expo wrapper)

| Asset | Path | Size | Format |
|-------|------|------|--------|
| App Icon | `loan-app-mobile/assets/icon.png` | 1024x1024 | PNG |
| Splash | `loan-app-mobile/assets/splash.png` | 1284x2778 | PNG |
| Adaptive Icon FG | `loan-app-mobile/assets/adaptive-icon.png` | 1024x1024 | PNG |
| Notification Icon | `loan-app-mobile/assets/notification-icon.png` | 96x96 | PNG |
| Favicon | `loan-app-mobile/assets/favicon.png` | 48x48 | PNG |

### Brand Colors
- Primary: `#4F46E5` (Indigo)
- Background: `#4F46E5` (for splash)

### Android Required Sizes

#### Launcher Icons (mipmap-*)
```
mipmap-mdpi/ic_launcher.png      48x48
mipmap-hdpi/ic_launcher.png      72x72
mipmap-xhdpi/ic_launcher.png     96x96
mipmap-xxhdpi/ic_launcher.png    144x144
mipmap-xxxhdpi/ic_launcher.png   192x192
```

#### Adaptive Icon (Android 8.0+)
```
mipmap-mdpi/ic_launcher_foreground.png      108x108
mipmap-hdpi/ic_launcher_foreground.png      162x162
mipmap-xhdpi/ic_launcher_foreground.png     216x216
mipmap-xxhdpi/ic_launcher_foreground.png    324x324
mipmap-xxxhdpi/ic_launcher_foreground.png   432x432
```

### Conversion Commands

```bash
# Install ImageMagick if not present
# Windows: choco install imagemagick
# macOS: brew install imagemagick

# Generate launcher icons from 1024x1024 source
convert icon.png -resize 48x48 mipmap-mdpi/ic_launcher.png
convert icon.png -resize 72x72 mipmap-hdpi/ic_launcher.png
convert icon.png -resize 96x96 mipmap-xhdpi/ic_launcher.png
convert icon.png -resize 144x144 mipmap-xxhdpi/ic_launcher.png
convert icon.png -resize 192x192 mipmap-xxxhdpi/ic_launcher.png

# Generate adaptive icon foreground (with safe zone padding)
convert adaptive-icon.png -resize 108x108 mipmap-mdpi/ic_launcher_foreground.png
convert adaptive-icon.png -resize 162x162 mipmap-hdpi/ic_launcher_foreground.png
convert adaptive-icon.png -resize 216x216 mipmap-xhdpi/ic_launcher_foreground.png
convert adaptive-icon.png -resize 324x324 mipmap-xxhdpi/ic_launcher_foreground.png
convert adaptive-icon.png -resize 432x432 mipmap-xxxhdpi/ic_launcher_foreground.png
```

---

## Fonts

### Current Usage
- TailwindCSS defaults (system fonts)
- No custom fonts loaded

### Android Recommendation
Use **Material 3 Typography** with system fonts:

```kotlin
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp
    ),
    // ... other styles
)
```

---

## Dynamic Media (Documents)

### Storage Location
- **Supabase Storage** bucket (name TBD from env)
- Referenced in `documents` table

### Download Pattern
```kotlin
// Using Supabase Storage
val url = supabase.storage
    .from("documents")
    .publicUrl(document.file_path)

// Download with OkHttp/Ktor
```

---

## Missing Assets to Create

| Asset | Priority | Notes |
|-------|----------|-------|
| Notification icon (monochrome) | High | Needs white-on-transparent version |
| Play Store feature graphic | Medium | 1024x500 promotional graphic |
| Play Store screenshots | Medium | Phone and tablet mockups |

---

## File Structure for Android

```
app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png
│   ├── ic_launcher_foreground.png
│   └── ic_launcher_round.png
├── mipmap-hdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
├── mipmap-xxxhdpi/
├── drawable/
│   ├── ic_launcher_background.xml (vector, #4F46E5)
│   └── splash_background.xml
├── values/
│   └── ic_launcher_background.xml (<color>#4F46E5</color>)
└── xml/
    └── ic_launcher.xml (adaptive icon config)
```

---

## Summary

| Category | Count | Action |
|----------|-------|--------|
| SVG Icons | ~20 | Replace with Material Icons |
| App Icons | 5 | Resize existing assets |
| Splash | 1 | Create Android splash with existing colors |
| Custom Fonts | 0 | Use system fonts |
| Dynamic Media | PDF downloads | Implement via Supabase Storage |
