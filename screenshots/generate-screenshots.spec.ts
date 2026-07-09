import { test } from "@playwright/test";
import * as path from "path";
import * as fs from "fs";

// ─── Device Definitions ─────────────────────────────────────────────────────
interface DeviceProfile {
  name: string;
  width: number;
  height: number;
  deviceScaleFactor: number;
  platform: string;
  category: "phone" | "tablet" | "desktop";
}

const devices: DeviceProfile[] = [
  // iOS - iPhone 6.5" (1242×2688 required by App Store)
  { name: "iPhone-6.5-inch", width: 414, height: 896, deviceScaleFactor: 3, platform: "ios", category: "phone" },
  // iOS - iPhone 6.7" (1284×2778 required by App Store)
  { name: "iPhone-6.7-inch", width: 428, height: 926, deviceScaleFactor: 3, platform: "ios", category: "phone" },
  // iOS - iPad 12.9" (2048×2732 required by App Store)
  { name: "iPad-12.9-inch", width: 1024, height: 1366, deviceScaleFactor: 2, platform: "ios", category: "tablet" },
  // iOS - iPad 13" (2064×2752 required by App Store)
  { name: "iPad-13-inch", width: 1032, height: 1376, deviceScaleFactor: 2, platform: "ios", category: "tablet" },
  // Android Phone
  { name: "Pixel-8-Pro", width: 412, height: 915, deviceScaleFactor: 2.625, platform: "android", category: "phone" },
  // Android Tablet
  { name: "Galaxy-Tab-S9", width: 800, height: 1280, deviceScaleFactor: 2, platform: "android", category: "tablet" },
  // Desktops
  // macOS App Store: 2880×1800 (highest supported retina resolution)
  { name: "MacBook-Pro-16", width: 1440, height: 900, deviceScaleFactor: 2, platform: "macos", category: "desktop" },
  { name: "Windows-Surface-Laptop", width: 1536, height: 1024, deviceScaleFactor: 1.5, platform: "windows", category: "desktop" },
  { name: "Linux-Desktop", width: 1920, height: 1080, deviceScaleFactor: 1, platform: "linux", category: "desktop" },
];

// ─── Marketing Slide Definitions ────────────────────────────────────────────
interface MarketingSlide {
  screenFile: string;
  headline: string;
  subheadline: string;
  badge: string;
  accentColor: string;
  bgGradient: string; // Rich vibrant gradient background
}

const slides: MarketingSlide[] = [
  {
    screenFile: "live_ai_translator/code.html",
    headline: "Real-Time AI\nInterpreter",
    subheadline: "Speak naturally. Lexi translates instantly — 100% on-device.",
    badge: "🔒 OFFLINE • PRIVATE • OPEN SOURCE",
    accentColor: "#0891b2",
    bgGradient: "linear-gradient(145deg, #e0f7fa 0%, #b2ebf2 20%, #ddd6fe 50%, #c4b5fd 75%, #e0f2fe 100%)",
  },
  {
    screenFile: "text_translation/code.html",
    headline: "Neural Text\nTranslation",
    subheadline: "10,000+ characters in milliseconds. No internet. Ever.",
    badge: "⚡ POWERED BY ON-DEVICE OPUS-MT",
    accentColor: "#7c3aed",
    bgGradient: "linear-gradient(145deg, #ede9fe 0%, #ddd6fe 20%, #e0e7ff 50%, #c7d2fe 75%, #f0e6ff 100%)",
  },
  {
    screenFile: "neural_speak_tts/code.html",
    headline: "Neural Speak\nTTS Studio",
    subheadline: "Natural AI voices. Multiple engines. Works completely offline.",
    badge: "🎙️ KOKORO • PIPER • VITS ENGINES",
    accentColor: "#059669",
    bgGradient: "linear-gradient(145deg, #d1fae5 0%, #a7f3d0 20%, #ccfbf1 50%, #99f6e4 75%, #d1fae5 100%)",
  },
  {
    screenFile: "library_history/code.html",
    headline: "Encrypted\nLibrary",
    subheadline: "AES-256 encrypted. Biometric lock. Your data stays yours.",
    badge: "🛡️ ZERO DATA COLLECTION",
    accentColor: "#0284c7",
    bgGradient: "linear-gradient(145deg, #e0f2fe 0%, #bae6fd 20%, #dbeafe 50%, #bfdbfe 75%, #e0f2fe 100%)",
  },
  {
    screenFile: "settings_models/code.html",
    headline: "AI Model\nStore",
    subheadline: "Download what you need. SHA-256 verified. Full control.",
    badge: "📦 AGPL-3.0 • OPEN SOURCE",
    accentColor: "#7c3aed",
    bgGradient: "linear-gradient(145deg, #f3e8ff 0%, #e9d5ff 20%, #ede9fe 50%, #ddd6fe 75%, #fae8ff 100%)",
  },
];

// ─── Frame Generator with Vibrant Background ────────────────────────────────

function generateStoreScreenshot(
  device: DeviceProfile,
  slide: MarketingSlide,
  screenshotBase64: string
): string {
  const isDesktop = device.category === "desktop";
  const isTablet = device.category === "tablet";

  // Output canvas — exact store pixel dimensions
  // iOS iPhone 6.5": 1242×2688, iPhone 6.7": 1284×2778
  // iOS iPad 12.9": 2048×2732, iPad 13": 2064×2752
  // Android/Desktop: use standard marketing sizes
  let canvasWidth: number, canvasHeight: number;
  if (device.platform === "ios" && device.category === "phone") {
    if (device.name === "iPhone-6.5-inch") {
      canvasWidth = 1242; canvasHeight = 2688;
    } else {
      canvasWidth = 1284; canvasHeight = 2778;
    }
  } else if (device.platform === "ios" && device.category === "tablet") {
    if (device.name === "iPad-12.9-inch") {
      canvasWidth = 2048; canvasHeight = 2732;
    } else {
      canvasWidth = 2064; canvasHeight = 2752;
    }
  } else if (isDesktop) {
    if (device.platform === "macos") {
      canvasWidth = 2880; canvasHeight = 1800;
    } else {
      canvasWidth = 1920; canvasHeight = 1080;
    }
  } else if (isTablet) {
    canvasWidth = 1200; canvasHeight = 1600;
  } else {
    // Android phone
    canvasWidth = 1080; canvasHeight = 1920;
  }

  // Device mockup sizing — fill more of the canvas
  let mockupWidth: number, mockupHeight: number;
  if (isDesktop) {
    mockupWidth = Math.round(canvasWidth * 0.55);
    mockupHeight = Math.round(mockupWidth * (device.height / device.width));
  } else if (isTablet) {
    mockupWidth = Math.round(canvasWidth * 0.72);
    mockupHeight = Math.round(mockupWidth * (device.height / device.width));
  } else {
    // Phone — make it big, let it overflow the bottom edge
    mockupHeight = Math.round(canvasHeight * 0.78);
    mockupWidth = Math.round(mockupHeight * (device.width / device.height));
  }

  const deviceHtml = isDesktop
    ? generateDesktopMockup(device, mockupWidth, mockupHeight, screenshotBase64, slide.accentColor)
    : generateMobileMockup(device, mockupWidth, mockupHeight, screenshotBase64, slide.accentColor);

  const headlineSize = isDesktop ? "56px" : isTablet ? "48px" : "44px";
  const subSize = isDesktop ? "19px" : isTablet ? "17px" : "16px";

  // Escape newlines in headline for HTML
  const headlineHtml = slide.headline.replace(/\n/g, "<br/>");

  if (isDesktop) {
    // ─── DESKTOP: Side-by-side layout ────────────────────────────────
    return `<!DOCTYPE html><html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{width:${canvasWidth}px;height:${canvasHeight}px;overflow:hidden;font-family:'Inter',sans-serif}
.material-symbols-outlined{font-variation-settings:'FILL' 1,'wght' 400,'GRAD' 0,'opsz' 24}
.canvas{
  width:100%;height:100%;
  background:${slide.bgGradient};
  display:flex;align-items:center;padding:50px 70px;gap:50px;
  position:relative;overflow:hidden;
}
/* Decorative glows */
.glow-1{position:absolute;width:500px;height:500px;border-radius:50%;background:${slide.accentColor};opacity:0.15;filter:blur(120px);top:-100px;left:-100px;}
.glow-2{position:absolute;width:400px;height:400px;border-radius:50%;background:${slide.accentColor};opacity:0.1;filter:blur(100px);bottom:-50px;right:10%;}
.glow-3{position:absolute;width:300px;height:300px;border-radius:50%;background:#7c3aed;opacity:0.08;filter:blur(80px);top:40%;left:30%;}
/* Subtle grid pattern */
.grid-overlay{position:absolute;inset:0;background-image:radial-gradient(rgba(0,0,0,0.04) 1px, transparent 1px);background-size:32px 32px;}

.text-side{flex:1;z-index:2;display:flex;flex-direction:column;justify-content:center;}
.device-side{flex-shrink:0;z-index:2;display:flex;align-items:center;justify-content:center;}
.badge{display:inline-flex;align-items:center;padding:10px 18px;border-radius:100px;background:rgba(0,0,0,0.06);border:1px solid rgba(0,0,0,0.1);font-size:11px;color:${slide.accentColor};letter-spacing:0.1em;font-weight:700;backdrop-filter:blur(12px);margin-bottom:24px;width:fit-content;}
.headline{font-size:${headlineSize};font-weight:900;line-height:1.05;color:#1e1b4b;letter-spacing:-0.03em;margin-bottom:18px;}
.subheadline{font-size:${subSize};color:rgba(30,27,75,0.6);line-height:1.6;max-width:460px;margin-bottom:32px;}
.trust-row{display:flex;gap:20px;flex-wrap:wrap;}
.chip{display:flex;align-items:center;gap:6px;padding:8px 14px;border-radius:8px;background:rgba(0,0,0,0.05);border:1px solid rgba(0,0,0,0.08);font-size:12px;color:rgba(30,27,75,0.7);font-weight:500;}
.chip .material-symbols-outlined{font-size:16px;color:${slide.accentColor};}
</style></head><body>
<div class="canvas">
  <div class="glow-1"></div><div class="glow-2"></div><div class="glow-3"></div>
  <div class="grid-overlay"></div>
  <div class="text-side">
    <div class="badge">${slide.badge}</div>
    <h1 class="headline">${headlineHtml}</h1>
    <p class="subheadline">${slide.subheadline}</p>
    <div class="trust-row">
      <div class="chip"><span class="material-symbols-outlined">wifi_off</span>Offline</div>
      <div class="chip"><span class="material-symbols-outlined">shield</span>Private</div>
      <div class="chip"><span class="material-symbols-outlined">code</span>Open Source</div>
      <div class="chip"><span class="material-symbols-outlined">verified_user</span>Trusted</div>
    </div>
  </div>
  <div class="device-side">${deviceHtml}</div>
</div>
</body></html>`;
  } else {
    // ─── PHONE/TABLET: Text top, large device below, device overflows bottom ─
    const textPadTop = isTablet ? "50px" : "48px";
    const textPadSide = isTablet ? "50px" : "48px";
    return `<!DOCTYPE html><html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{width:${canvasWidth}px;height:${canvasHeight}px;overflow:hidden;font-family:'Inter',sans-serif}
.material-symbols-outlined{font-variation-settings:'FILL' 1,'wght' 400,'GRAD' 0,'opsz' 24}
.canvas{
  width:100%;height:100%;
  background:${slide.bgGradient};
  display:flex;flex-direction:column;align-items:center;
  padding:${textPadTop} ${textPadSide} 0;
  position:relative;overflow:hidden;
}
.glow-1{position:absolute;width:600px;height:600px;border-radius:50%;background:${slide.accentColor};opacity:0.18;filter:blur(150px);top:-200px;left:50%;transform:translateX(-50%);}
.glow-2{position:absolute;width:400px;height:400px;border-radius:50%;background:#7c3aed;opacity:0.08;filter:blur(100px);bottom:20%;right:-10%;}
.glow-3{position:absolute;width:350px;height:350px;border-radius:50%;background:${slide.accentColor};opacity:0.08;filter:blur(90px);bottom:10%;left:-5%;}
.grid-overlay{position:absolute;inset:0;background-image:radial-gradient(rgba(0,0,0,0.03) 1px, transparent 1px);background-size:28px 28px;}

.text-area{text-align:center;z-index:2;margin-bottom:${isTablet ? '36px' : '28px'};}
.badge{display:inline-flex;align-items:center;padding:10px 18px;border-radius:100px;background:rgba(0,0,0,0.06);border:1px solid rgba(0,0,0,0.1);font-size:11px;color:${slide.accentColor};letter-spacing:0.1em;font-weight:700;backdrop-filter:blur(12px);margin-bottom:20px;}
.headline{font-size:${headlineSize};font-weight:900;line-height:1.05;color:#1e1b4b;letter-spacing:-0.03em;margin-bottom:14px;}
.subheadline{font-size:${subSize};color:rgba(30,27,75,0.6);line-height:1.55;max-width:${isTablet ? '550px' : '380px'};margin:0 auto 22px;}
.trust-row{display:flex;justify-content:center;gap:12px;flex-wrap:wrap;}
.chip{display:flex;align-items:center;gap:5px;padding:7px 12px;border-radius:8px;background:rgba(0,0,0,0.05);border:1px solid rgba(0,0,0,0.08);font-size:11px;color:rgba(30,27,75,0.7);font-weight:500;}
.chip .material-symbols-outlined{font-size:14px;color:${slide.accentColor};}
.device-area{z-index:2;position:relative;flex:1;display:flex;align-items:flex-start;justify-content:center;min-height:0;}
/* Device shadow/glow underneath */
.device-reflection{position:absolute;bottom:-20px;width:${mockupWidth * 0.8}px;height:80px;background:rgba(0,0,0,0.15);filter:blur(30px);border-radius:50%;z-index:0;}
</style></head><body>
<div class="canvas">
  <div class="glow-1"></div><div class="glow-2"></div><div class="glow-3"></div>
  <div class="grid-overlay"></div>
  <div class="text-area">
    <div class="badge">${slide.badge}</div>
    <h1 class="headline">${headlineHtml}</h1>
    <p class="subheadline">${slide.subheadline}</p>
    <div class="trust-row">
      <div class="chip"><span class="material-symbols-outlined">wifi_off</span>Offline</div>
      <div class="chip"><span class="material-symbols-outlined">shield</span>Private</div>
      <div class="chip"><span class="material-symbols-outlined">code</span>Open Source</div>
      <div class="chip"><span class="material-symbols-outlined">verified_user</span>Trusted</div>
    </div>
  </div>
  <div class="device-area">
    <div class="device-reflection"></div>
    ${deviceHtml}
  </div>
</div>
</body></html>`;
  }
}

// ─── Device Mockup Generators ───────────────────────────────────────────────

function generateDesktopMockup(
  device: DeviceProfile,
  mockupWidth: number,
  mockupHeight: number,
  screenshotBase64: string,
  accentColor: string
): string {
  const titleBarH = device.platform === "macos" ? 28 : 32;
  const borderRad = device.platform === "macos" ? 12 : device.platform === "linux" ? 10 : 2;
  const screenH = mockupHeight - titleBarH;

  const controls = device.platform === "macos"
    ? `<div style="display:flex;gap:7px;padding-left:13px;align-items:center;">
        <div style="width:12px;height:12px;border-radius:50%;background:#FF5F57;box-shadow:inset 0 -1px 2px rgba(0,0,0,0.2);"></div>
        <div style="width:12px;height:12px;border-radius:50%;background:#FEBC2E;box-shadow:inset 0 -1px 2px rgba(0,0,0,0.2);"></div>
        <div style="width:12px;height:12px;border-radius:50%;background:#27C840;box-shadow:inset 0 -1px 2px rgba(0,0,0,0.2);"></div>
      </div>
      <div style="position:absolute;left:50%;transform:translateX(-50%);font-size:12px;color:rgba(255,255,255,0.5);font-weight:500;">Lexi Translator</div>`
    : device.platform === "windows"
    ? `<div style="padding-left:12px;font-size:11px;color:rgba(255,255,255,0.5);font-weight:500;">Lexi Translator</div>
       <div style="display:flex;margin-left:auto;">
         <div style="width:42px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:10px;">─</div>
         <div style="width:42px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:10px;">□</div>
         <div style="width:42px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:10px;">✕</div>
       </div>`
    : `<div style="padding-left:12px;font-size:11px;color:rgba(255,255,255,0.5);font-weight:500;">Lexi Translator</div>
       <div style="display:flex;margin-left:auto;">
         <div style="width:34px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:12px;">─</div>
         <div style="width:34px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:10px;">□</div>
         <div style="width:34px;height:${titleBarH}px;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,0.4);font-size:12px;">✕</div>
       </div>`;

  return `<div style="width:${mockupWidth}px;border-radius:${borderRad}px;overflow:hidden;box-shadow:0 30px 60px -10px rgba(0,0,0,0.3),0 0 0 1px rgba(0,0,0,0.1),0 10px 30px -5px rgba(0,0,0,0.2);position:relative;">
    <div style="height:${titleBarH}px;background:rgba(20,20,25,0.95);display:flex;align-items:center;position:relative;border-bottom:1px solid rgba(255,255,255,0.06);">
      ${controls}
    </div>
    <div style="width:${mockupWidth}px;height:${screenH}px;overflow:hidden;">
      <img src="data:image/png;base64,${screenshotBase64}" style="width:100%;height:100%;object-fit:cover;object-position:top;" />
    </div>
  </div>`;
}

function generateMobileMockup(
  device: DeviceProfile,
  mockupWidth: number,
  mockupHeight: number,
  screenshotBase64: string,
  accentColor: string
): string {
  const isIphone = device.platform === "ios" && device.name.includes("iPhone");
  const isTablet = device.category === "tablet";
  const frameThickness = isTablet ? 10 : 12;
  const frameRadius = isTablet
    ? (device.platform === "ios" ? 22 : 16)
    : (device.platform === "ios" ? 48 : 40);
  const screenRadius = Math.max(frameRadius - frameThickness, 6);

  const innerW = mockupWidth - frameThickness * 2;
  const innerH = mockupHeight - frameThickness * 2;

  // Dynamic Island / punch-hole
  let notchHtml = "";
  if (isIphone && !isTablet) {
    const islandW = Math.round(innerW * 0.32);
    const islandH = Math.round(islandW * 0.28);
    notchHtml = `<div style="position:absolute;top:${frameThickness + Math.round(innerH * 0.015)}px;left:50%;transform:translateX(-50%);width:${islandW}px;height:${islandH}px;background:#000;border-radius:${islandH}px;z-index:10;"></div>`;
  } else if (device.platform === "android" && !isTablet) {
    const holeSize = Math.round(innerW * 0.04);
    notchHtml = `<div style="position:absolute;top:${frameThickness + Math.round(innerH * 0.017)}px;left:50%;transform:translateX(-50%);width:${holeSize}px;height:${holeSize}px;background:#000;border-radius:50%;z-index:10;"></div>`;
  }

  // Home indicator
  let homeIndicator = "";
  if (device.platform === "ios" && !isTablet) {
    homeIndicator = `<div style="position:absolute;bottom:${frameThickness + 6}px;left:50%;transform:translateX(-50%);width:${Math.round(innerW * 0.35)}px;height:4px;background:rgba(255,255,255,0.25);border-radius:3px;z-index:10;"></div>`;
  }

  // Side buttons (phone only)
  let sideButtons = "";
  if (!isTablet) {
    const btnTop = Math.round(mockupHeight * 0.2);
    sideButtons = `
      <div style="position:absolute;right:-2px;top:${btnTop}px;width:3px;height:${Math.round(mockupHeight * 0.08)}px;background:rgba(60,60,60,0.9);border-radius:2px 0 0 2px;"></div>
      <div style="position:absolute;left:-2px;top:${Math.round(btnTop * 0.85)}px;width:3px;height:${Math.round(mockupHeight * 0.035)}px;background:rgba(60,60,60,0.9);border-radius:0 2px 2px 0;"></div>
      <div style="position:absolute;left:-2px;top:${Math.round(btnTop * 1.05)}px;width:3px;height:${Math.round(mockupHeight * 0.055)}px;background:rgba(60,60,60,0.9);border-radius:0 2px 2px 0;"></div>`;
  }

  return `<div style="position:relative;width:${mockupWidth}px;height:${mockupHeight}px;background:linear-gradient(160deg,#1a1a1e,#0f0f12);border-radius:${frameRadius}px;padding:${frameThickness}px;box-shadow:0 30px 60px -10px rgba(0,0,0,0.35),0 0 0 1px rgba(0,0,0,0.15),0 15px 35px -5px rgba(0,0,0,0.25);">
    ${notchHtml}${homeIndicator}${sideButtons}
    <div style="width:${innerW}px;height:${innerH}px;border-radius:${screenRadius}px;overflow:hidden;">
      <img src="data:image/png;base64,${screenshotBase64}" style="width:100%;height:100%;object-fit:cover;object-position:top;" />
    </div>
  </div>`;
}

// ─── Test Suite ──────────────────────────────────────────────────────────────
const requirementsDir = path.resolve(__dirname, "../requirements");
const outputDir = path.resolve(__dirname, "output");

test.describe("Lexi Translator - Store Screenshots", () => {
  test.beforeAll(async () => {
    for (const device of devices) {
      const dir = path.join(outputDir, device.platform, device.name);
      fs.mkdirSync(dir, { recursive: true });
    }
  });

  for (const slide of slides) {
    for (const device of devices) {
      const slideName = path.basename(path.dirname(slide.screenFile));
      test(`${slideName} → ${device.name}`, async ({ browser }) => {
        // Step 1: Render app screen at device resolution
        const appContext = await browser.newContext({
          viewport: { width: device.width, height: device.height },
          deviceScaleFactor: Math.min(device.deviceScaleFactor, 2),
          colorScheme: "dark",
        });
        const appPage = await appContext.newPage();
        await appPage.goto(`file://${path.join(requirementsDir, slide.screenFile)}`);
        await appPage.waitForLoadState("networkidle");
        await appPage.waitForTimeout(2000);
        const rawScreenshot = await appPage.screenshot({ type: "png", fullPage: false });
        await appContext.close();

        // Step 2: Compose the marketing store image
        const screenshotBase64 = rawScreenshot.toString("base64");
        const storeHtml = generateStoreScreenshot(device, slide, screenshotBase64);

        // Determine exact output pixel dimensions
        let outWidth: number, outHeight: number;
        if (device.platform === "ios" && device.category === "phone") {
          if (device.name === "iPhone-6.5-inch") { outWidth = 1242; outHeight = 2688; }
          else { outWidth = 1284; outHeight = 2778; }
        } else if (device.platform === "ios" && device.category === "tablet") {
          if (device.name === "iPad-12.9-inch") { outWidth = 2048; outHeight = 2732; }
          else { outWidth = 2064; outHeight = 2752; }
        } else if (device.platform === "macos") {
          // Mac App Store: 2880×1800
          outWidth = 2880; outHeight = 1800;
        } else if (device.category === "desktop") {
          outWidth = 1920; outHeight = 1080;
        } else if (device.category === "tablet") {
          outWidth = 1200; outHeight = 1600;
        } else {
          outWidth = 1080; outHeight = 1920;
        }

        // Render at 1x — canvas dimensions ARE the final pixel output
        const storeContext = await browser.newContext({
          viewport: { width: outWidth, height: outHeight },
          deviceScaleFactor: 1,
        });
        const storePage = await storeContext.newPage();
        await storePage.setContent(storeHtml);
        await storePage.waitForLoadState("networkidle");
        await storePage.waitForTimeout(1000);

        const finalScreenshot = await storePage.screenshot({ type: "png", fullPage: false });
        const outPath = path.join(outputDir, device.platform, device.name, `${slideName}.png`);
        fs.writeFileSync(outPath, finalScreenshot);
        await storeContext.close();
      });
    }
  }
});
