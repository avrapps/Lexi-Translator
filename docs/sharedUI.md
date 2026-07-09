# sharedUI Module

Architecture and component documentation for the `sharedUI` Kotlin Multiplatform module.

---

## Widgets (`ui/widgets/`)

### StorageUsageCard

`StorageUsageCard.kt` — Reusable composable card displaying device storage usage for AI models.

- **Visual progress bar** showing used vs. available space (red when < 500 MB free).
- **Used / Available** text labels with human-readable formatting (MB/GB).
- **Per-model breakdown** listing up to 5 models sorted by size.
- **Low storage warning** banner when available space drops below 500 MB.
- **MockStorageData** object provides sample data for previews and UI development.
- Accepts `totalUsedBytes`, `availableBytes`, and `perModelUsage` as parameters.
- Uses Material 3 theming (`Card`, `LinearProgressIndicator`, `MaterialTheme`).

---
