#!/bin/bash
# Generate platform-specific icons from the SVG source
# Requires: rsvg-convert (from librsvg), icnsutil (pip), and ImageMagick
#
# Usage: ./scripts/generate-icons.sh
#
# Input: desktopApp/src/main/resources/icons/app-icon.svg
# Outputs:
#   - desktopApp/src/main/resources/icons/app-icon.png (512x512, Linux)
#   - desktopApp/src/main/resources/icons/app-icon.ico (Windows, multi-size)
#   - desktopApp/src/main/resources/icons/app-icon.icns (macOS)
#   - iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/ (iOS icon set)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SVG="$PROJECT_DIR/desktopApp/src/main/resources/icons/app-icon.svg"
ICONS_DIR="$PROJECT_DIR/desktopApp/src/main/resources/icons"
IOS_ICONSET="$PROJECT_DIR/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

echo "📐 Generating platform icons from SVG..."

# Create temp directory
TMPDIR=$(mktemp -d)
trap "rm -rf $TMPDIR" EXIT

# Generate PNGs at various sizes
for SIZE in 16 32 48 64 128 256 512 1024; do
  rsvg-convert -w $SIZE -h $SIZE "$SVG" > "$TMPDIR/icon-${SIZE}.png"
  echo "  ✓ ${SIZE}x${SIZE}"
done

# --- Linux (PNG 512x512) ---
cp "$TMPDIR/icon-512.png" "$ICONS_DIR/app-icon.png"
echo "✅ Linux PNG: $ICONS_DIR/app-icon.png"

# --- Windows (ICO multi-size) ---
convert "$TMPDIR/icon-16.png" "$TMPDIR/icon-32.png" "$TMPDIR/icon-48.png" "$TMPDIR/icon-64.png" "$TMPDIR/icon-128.png" "$TMPDIR/icon-256.png" "$ICONS_DIR/app-icon.ico"
echo "✅ Windows ICO: $ICONS_DIR/app-icon.ico"

# --- macOS (ICNS) ---
ICONSET_DIR="$TMPDIR/AppIcon.iconset"
mkdir -p "$ICONSET_DIR"
cp "$TMPDIR/icon-16.png" "$ICONSET_DIR/icon_16x16.png"
cp "$TMPDIR/icon-32.png" "$ICONSET_DIR/icon_16x16@2x.png"
cp "$TMPDIR/icon-32.png" "$ICONSET_DIR/icon_32x32.png"
cp "$TMPDIR/icon-64.png" "$ICONSET_DIR/icon_32x32@2x.png"
cp "$TMPDIR/icon-128.png" "$ICONSET_DIR/icon_128x128.png"
cp "$TMPDIR/icon-256.png" "$ICONSET_DIR/icon_128x128@2x.png"
cp "$TMPDIR/icon-256.png" "$ICONSET_DIR/icon_256x256.png"
cp "$TMPDIR/icon-512.png" "$ICONSET_DIR/icon_256x256@2x.png"
cp "$TMPDIR/icon-512.png" "$ICONSET_DIR/icon_512x512.png"
cp "$TMPDIR/icon-1024.png" "$ICONSET_DIR/icon_512x512@2x.png"
iconutil -c icns "$ICONSET_DIR" -o "$ICONS_DIR/app-icon.icns"
echo "✅ macOS ICNS: $ICONS_DIR/app-icon.icns"

# --- iOS (AppIcon set) ---
mkdir -p "$IOS_ICONSET"
cp "$TMPDIR/icon-1024.png" "$IOS_ICONSET/app-icon-1024.png"

cat > "$IOS_ICONSET/Contents.json" << 'EOF'
{
  "images": [
    {
      "filename": "app-icon-1024.png",
      "idiom": "universal",
      "platform": "ios",
      "size": "1024x1024"
    }
  ],
  "info": {
    "author": "xcode",
    "version": 1
  }
}
EOF
echo "✅ iOS AppIcon: $IOS_ICONSET/"

echo ""
echo "🎉 All platform icons generated successfully!"
echo ""
echo "NOTE: You still need to create Android adaptive icons manually."
echo "      Place them in androidApp/src/main/res/mipmap-*/"
