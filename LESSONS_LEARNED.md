# Lessons Learned

## 2026-02-23: Skip Permissions Mode Hides Failures

Used `--dangerously-skip-permissions` inside a dev container which made development incredibly fast, but quality gates never actually ran because Gradle wasn't available inside the container. When skipping permissions, you lose the feedback loop that catches these silent failures. Always verify that quality checks are actually executing, not just being skipped silently.

## 2026-02-23: Structured Planning with Checkpoints Pays Off

This was the first project built with a detailed plan and many checkpoints defined upfront. Running it in a dev container with skip permissions made execution very fast. The checkpoint-based approach kept things organized and progressive.

## 2026-02-23: Do Deep Research Before Writing Hardware Firmware

The T-Watch S3 got bricked because firmware accidentally set PAD_HOLD on GPIO0 (a strapping pin). The splash screen was working, started building out the UI, then a subsequent update locked out the device entirely. If deep research on the T-Watch S3 pin map and ESP32-S3 strapping pins had been done upfront, this would have been avoided. For unfamiliar hardware: research the product thoroughly before writing any firmware, especially pin assignments, strapping pins, and power management behavior.

## 2026-02-23: Hardware Mistakes Can Be Permanent

Unlike software where you can always revert, hardware errors can be irreversible. The bricked watch may require burning a permanent eFuse (DIS_DOWNLOAD_MODE) to recover — a one-way operation that permanently disables USB download mode. The battery has to fully drain over weeks just to clear an RTC register. Lesson: treat hardware firmware with extreme caution, especially anything involving power management, deep sleep, or pin hold states.

## 2026-02-25: Git Worktrees Need Gitignored Files Copied Manually

When using `git worktree add` for isolated feature branches, gitignored files like `google-services.json` and `local.properties` don't exist in the new worktree. The build fails immediately because Firebase and Maps API keys are missing. Fix: after creating a worktree, copy these files from the main checkout before building. This is easy to forget and happens every time — consider automating it in the `/implement` skill or adding a post-worktree setup script.

## 2026-02-23: Android Compose Previews Don't Match Real Device Output

The default Compose preview in Android Studio shows a basic wireframe-style rendering that looks very different from what actually appears on a real phone once the app is installed. This makes it hard to design and iterate on UI confidently. Need to look into better tooling or frameworks for previewing real UI changes — things like interactive Compose previews, device mirroring, or live preview tools that more accurately reflect the final on-device appearance.
