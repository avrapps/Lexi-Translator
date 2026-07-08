import { test } from "@playwright/test";
import * as path from "path";
import * as fs from "fs";

const outputDir = path.resolve(__dirname, "output");

test("Generate Feature Graphic 1024x500", async ({ browser }) => {
  fs.mkdirSync(outputDir, { recursive: true });

  const html = `<!DOCTYPE html><html><head><meta charset="utf-8">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{width:1024px;height:500px;overflow:hidden;font-family:'Inter',sans-serif}
.material-symbols-outlined{font-variation-settings:'FILL' 1,'wght' 400,'GRAD' 0,'opsz' 24}
.canvas{
  width:100%;height:100%;
  background:linear-gradient(135deg, #ede9fe 0%, #ddd6fe 15%, #e0f2fe 35%, #bae6fd 55%, #d1fae5 75%, #ccfbf1 90%, #ede9fe 100%);
  display:flex;align-items:center;justify-content:center;
  padding:40px 60px;gap:50px;
  position:relative;overflow:hidden;
}
/* Decorative blurs */
.glow-1{position:absolute;width:400px;height:400px;border-radius:50%;background:#7c3aed;opacity:0.12;filter:blur(100px);top:-120px;left:-80px;}
.glow-2{position:absolute;width:350px;height:350px;border-radius:50%;background:#0891b2;opacity:0.10;filter:blur(90px);bottom:-100px;right:5%;}
.glow-3{position:absolute;width:250px;height:250px;border-radius:50%;background:#059669;opacity:0.08;filter:blur(70px);top:50%;right:35%;}
.grid-overlay{position:absolute;inset:0;background-image:radial-gradient(rgba(0,0,0,0.03) 1px, transparent 1px);background-size:24px 24px;}

/* Left: Brand + Text */
.left{flex:1;z-index:2;display:flex;flex-direction:column;justify-content:center;}
.logo-row{display:flex;align-items:center;gap:12px;margin-bottom:20px;}
.logo-icon{width:48px;height:48px;}
.logo-text{font-size:32px;font-weight:900;color:#1e1b4b;letter-spacing:-0.03em;}
.headline{font-size:36px;font-weight:800;line-height:1.15;color:#1e1b4b;letter-spacing:-0.02em;margin-bottom:12px;}
.headline span{color:#7c3aed;}
.subheadline{font-size:15px;color:rgba(30,27,75,0.6);line-height:1.5;max-width:380px;margin-bottom:20px;}
.chips{display:flex;gap:10px;flex-wrap:wrap;}
.chip{display:flex;align-items:center;gap:5px;padding:6px 12px;border-radius:8px;background:rgba(0,0,0,0.05);border:1px solid rgba(0,0,0,0.08);font-size:11px;color:rgba(30,27,75,0.7);font-weight:600;}
.chip .material-symbols-outlined{font-size:14px;color:#7c3aed;}
.platforms{display:flex;gap:8px;margin-top:16px;}
.platform-badge{padding:5px 10px;border-radius:6px;background:rgba(30,27,75,0.08);font-size:10px;font-weight:600;color:rgba(30,27,75,0.6);letter-spacing:0.05em;}

/* Right: Device stack */
.right{flex-shrink:0;z-index:2;position:relative;width:380px;height:420px;display:flex;align-items:center;justify-content:center;}
.phone{position:absolute;width:180px;height:380px;background:linear-gradient(160deg,#1a1a1e,#0f0f12);border-radius:28px;padding:6px;box-shadow:0 20px 50px -10px rgba(0,0,0,0.35),0 0 0 1px rgba(0,0,0,0.12);}
.phone-screen{width:100%;height:100%;border-radius:22px;overflow:hidden;background:#0b1326;}
.phone-screen img{width:100%;height:100%;object-fit:cover;object-position:top;}
.phone-1{transform:translateX(-80px) rotate(-5deg);z-index:1;}
.phone-2{transform:translateX(80px) rotate(5deg);z-index:2;}
.phone-3{transform:translateY(0) rotate(0deg);z-index:3;}
/* Dynamic island on center phone */
.island{position:absolute;top:12px;left:50%;transform:translateX(-50%);width:56px;height:18px;background:#000;border-radius:12px;z-index:10;}
</style></head><body>
<div class="canvas">
  <div class="glow-1"></div><div class="glow-2"></div><div class="glow-3"></div>
  <div class="grid-overlay"></div>

  <div class="left">
    <div class="logo-row">
      <svg class="logo-icon" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="lg" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#7c3aed"/>
            <stop offset="100%" stop-color="#0891b2"/>
          </linearGradient>
        </defs>
        <circle cx="100" cy="100" r="80" stroke="url(#lg)" stroke-width="3" stroke-dasharray="12 8" opacity="0.4"/>
        <path d="M60 80C60 68.9543 68.9543 60 80 60H120C131.046 60 140 68.9543 140 80V110C140 121.046 131.046 130 120 130H100L80 150V130H80C68.9543 130 60 121.046 60 110V80Z" fill="url(#lg)" opacity="0.9"/>
        <circle cx="100" cy="95" r="12" fill="white"/>
        <path d="M90 95H110M100 85V105M92.9289 87.9289L107.071 102.071M107.071 87.9289L92.9289 102.071" stroke="white" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <span class="logo-text">Lexi</span>
    </div>
    <h1 class="headline">Offline AI <span>Translator</span><br/>for Everyone</h1>
    <p class="subheadline">Real-time interpreter, text translation, and neural TTS — all running on-device. No cloud. No data collection. Fully open source.</p>
    <div class="chips">
      <div class="chip"><span class="material-symbols-outlined">wifi_off</span>100% Offline</div>
      <div class="chip"><span class="material-symbols-outlined">shield</span>Private</div>
      <div class="chip"><span class="material-symbols-outlined">code</span>Open Source</div>
      <div class="chip"><span class="material-symbols-outlined">speed</span>&lt;300ms</div>
    </div>
    <div class="platforms">
      <div class="platform-badge">ANDROID</div>
      <div class="platform-badge">iOS</div>
      <div class="platform-badge">WINDOWS</div>
      <div class="platform-badge">LINUX</div>
      <div class="platform-badge">macOS</div>
    </div>
  </div>

  <div class="right">
    <div class="phone phone-1" id="phone1"><div class="phone-screen"></div></div>
    <div class="phone phone-2" id="phone2"><div class="phone-screen"></div></div>
    <div class="phone phone-3" id="phone3"><div class="island"></div><div class="phone-screen"></div></div>
  </div>
</div>
</body></html>`;

  // First take screenshots of 3 app screens to embed in the phones
  const screenFiles = [
    "../requirements/library_history/code.html",
    "../requirements/neural_speak_tts/code.html",
    "../requirements/live_ai_translator/code.html",
  ];

  const screenBase64s: string[] = [];
  for (const file of screenFiles) {
    const ctx = await browser.newContext({
      viewport: { width: 393, height: 852 },
      deviceScaleFactor: 2,
      colorScheme: "dark",
    });
    const page = await ctx.newPage();
    await page.goto(`file://${path.resolve(__dirname, file)}`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1500);
    const shot = await page.screenshot({ type: "png", fullPage: false });
    screenBase64s.push(shot.toString("base64"));
    await ctx.close();
  }

  // Render the feature graphic with embedded screenshots
  const ctx = await browser.newContext({
    viewport: { width: 1024, height: 500 },
    deviceScaleFactor: 2,
  });
  const page = await ctx.newPage();
  await page.setContent(html);
  await page.waitForLoadState("networkidle");
  await page.waitForTimeout(500);

  // Inject screenshots into phone screens
  await page.evaluate((screens) => {
    const phoneScreens = document.querySelectorAll(".phone-screen");
    screens.forEach((base64, i) => {
      if (phoneScreens[i]) {
        const img = document.createElement("img");
        img.src = `data:image/png;base64,${base64}`;
        img.style.width = "100%";
        img.style.height = "100%";
        img.style.objectFit = "cover";
        img.style.objectPosition = "top";
        phoneScreens[i].appendChild(img);
      }
    });
  }, screenBase64s);

  await page.waitForTimeout(500);

  const screenshot = await page.screenshot({ type: "png", fullPage: false });
  fs.writeFileSync(path.join(outputDir, "feature-graphic-1024x500.png"), screenshot);
  await ctx.close();
});
