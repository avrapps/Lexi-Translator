# Lexi Translator - Marketing Screenshots

Auto-generated device-framed screenshots for all platforms using Playwright.

## Output Structure

```
output/
├── ios/
│   ├── iPhone-15-Pro/         (393×852 @3x, Dynamic Island)
│   └── iPad-Pro-12.9/         (1024×1366 @2x)
├── android/
│   ├── Pixel-8-Pro/           (412×915 @2.625x, Punch-hole camera)
│   └── Galaxy-Tab-S9/         (800×1280 @2x)
├── macos/
│   └── MacBook-Pro-16/        (1728×1117 @2x, macOS window chrome)
├── windows/
│   └── Windows-Surface-Laptop/ (1536×1024 @1.5x, Windows title bar)
└── linux/
    └── Linux-Desktop/          (1920×1080 @1x, GNOME-style title bar)
```

## Screens Captured

| Screen | Description |
|--------|-------------|
| `live-interpreter` | Real-time AI Orb interpreter with waveform |
| `text-translate` | Text translation with language swap |
| `neural-speak` | Offline TTS studio with voice profiles |
| `library-history` | Encrypted history with search & filters |
| `settings-models` | Model store with download progress |

## Regenerate Screenshots

```bash
cd screenshots
npx playwright test --config=playwright.config.ts
```

## Device Frame Features

- **iPhone 15 Pro**: Titanium frame + Dynamic Island cutout + side buttons + home indicator
- **iPad Pro 12.9"**: Slim bezels + home indicator
- **Pixel 8 Pro**: Dark frame + punch-hole camera cutout + side buttons
- **Galaxy Tab S9**: Slim bezel tablet frame
- **MacBook Pro 16"**: macOS window chrome with traffic lights
- **Windows Surface**: Windows 11 title bar with min/max/close buttons
- **Linux Desktop**: GNOME-style window decoration

## Customization

Edit `generate-screenshots.spec.ts` to:
- Add new device profiles (modify the `devices` array)
- Add new screens (add entries to `screens` array pointing at your HTML mockups in `requirements/`)
- Adjust frame styling (colors, radii, button positions)
- Change the background gradient behind device frames
