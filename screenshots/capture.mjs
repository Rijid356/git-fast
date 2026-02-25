import { chromium } from 'playwright';
import { fileURLToPath } from 'url';
import { dirname, resolve, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const journeys = [
  'run-workout',
  'dog-walk',
  'history-detail',
  'analytics',
  'rpg',
  'settings',
];

const outputDir = resolve(__dirname, '..', 'app', 'src', 'test', 'snapshots', 'composites');

async function main() {
  // Ensure output directory exists
  const { mkdirSync } = await import('fs');
  mkdirSync(outputDir, { recursive: true });

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    deviceScaleFactor: 2,
    viewport: { width: 1920, height: 1080 },
  });

  for (const journey of journeys) {
    const htmlPath = resolve(__dirname, 'journeys', `${journey}.html`);
    // Normalize to forward slashes for file:// URL on Windows
    const fileUrl = `file:///${htmlPath.replace(/\\/g, '/')}`;

    const page = await context.newPage();
    await page.goto(fileUrl, { waitUntil: 'networkidle' });

    // Wait for all images to load (or fail gracefully for missing screenshots)
    await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      return Promise.allSettled(
        images.map(img =>
          img.complete
            ? Promise.resolve()
            : new Promise((resolve) => {
                img.onload = resolve;
                img.onerror = resolve; // Don't block on missing images
              })
        )
      );
    });

    // Small delay for CSS rendering
    await page.waitForTimeout(500);

    const outputPath = join(outputDir, `${journey}.png`);
    await page.screenshot({ path: outputPath, fullPage: true });
    console.log(`Captured: ${journey}.png`);

    await page.close();
  }

  await browser.close();
  console.log(`\nAll composites saved to: ${outputDir}`);
}

main().catch((err) => {
  console.error('Capture failed:', err);
  process.exit(1);
});
