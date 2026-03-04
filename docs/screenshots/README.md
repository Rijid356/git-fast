# Screenshots

Visual chronicle of the git-fast app.

## Structure

```
docs/screenshots/
├── current/              # What the app looks like right now (categorized)
│   ├── analytics/        #   Analytics hub, trends, body comp, PRs, routes
│   ├── character/        #   Character sheets (user + Juniper)
│   ├── detail/           #   Workout detail screens
│   ├── history/          #   History list
│   ├── home/             #   Home screen
│   ├── settings/         #   Settings
│   ├── summary/          #   Post-workout summaries
│   └── workout/          #   Active workout screens + dialogs
├── comparisons/          # Before/after stitched comparisons (date/category/)
├── history/              # Numbered chronicle of the app's visual evolution
├── stitch.py             # Image stitching + sync utility
└── README.md             # This file
```

## Current Screenshots

Always-up-to-date reference of every major screen. Source: Roborazzi golden screenshots.

### Home

| File | Screen |
|------|--------|
| `home/home.png` | Home screen |

### Workout

| File | Screen |
|------|--------|
| `workout/active-run.png` | Active run |
| `workout/active-run-laps.png` | Active run in lap mode |
| `workout/active-run-paused.png` | Active run paused |
| `workout/active-dog-walk.png` | Active dog walk |
| `workout/active-dog-walk-event-wheel.png` | Dog walk with event wheel expanded |
| `workout/active-dog-walk-route-ghost.png` | Dog walk with route ghost |
| `workout/active-dog-run.png` | Active dog run |
| `workout/active-dog-run-sprinting.png` | Dog run sprinting mode |
| `workout/dialog-stop.png` | Stop confirmation dialog |
| `workout/dialog-back.png` | Back/navigate-away dialog |

### Summary

| File | Screen |
|------|--------|
| `summary/run-summary.png` | Run workout summary |
| `summary/dog-walk-summary.png` | Dog walk summary |

### Detail

| File | Screen |
|------|--------|
| `detail/run-detail.png` | Run detail with phases/laps/map |
| `detail/dog-walk-detail.png` | Dog walk detail with events/map |

### History

| File | Screen |
|------|--------|
| `history/history-list.png` | Workout history list |

### Character

| File | Screen |
|------|--------|
| `character/character-sheet-me.png` | Character sheet (user) |
| `character/character-sheet-juniper.png` | Character sheet (Juniper) |

### Analytics

| File | Screen |
|------|--------|
| `analytics/analytics.png` | Analytics hub |
| `analytics/trends.png` | Weekly/monthly trends |
| `analytics/personal-records.png` | Personal records dashboard |
| `analytics/route-overlay.png` | Route overlay comparison |
| `analytics/route-performance.png` | Route performance table |
| `analytics/body-comp.png` | Body composition tracking |

### Settings

| File | Screen |
|------|--------|
| `settings/settings.png` | Settings screen |

## History Log

Chronological record of the app's visual evolution. Each entry captures a specific milestone or UI change.

### Feb 17-20, 2026 — Days 1-4: First Working App

| # | File | Screen | Notes |
|---|------|--------|-------|
| 1 | `01-home-crash-recovery-dialog.png` | Home — Crash Recovery Dialog | "Incomplete Workout Found" dialog over the home screen |
| 2 | `02-dog-walk-detail-first-test-walk.png` | Dog Walk Detail | First completed workout — 1.26 mi dog walk, 34:40 |
| 3 | `03-home-screen-with-recent-walk.png` | Home Screen | "git-fast" title, "> ready_" prompt, START RUN / DOG WALK buttons, LV 1 badge |
| 4 | `04-history-dog-walks-filter.png` | History — Dog Walks Tab | History screen with All / Runs / Dog Walks filter chips |
| 5 | `05-dog-walk-detail-with-gps-map.png` | Dog Walk Detail (scrolled) | GPS route map with green track overlay, 521 GPS points |
| 6 | `06-character-sheet-level1-empty.png` | Character Sheet — Empty | Level 1, 0 XP, all stats at 1 |
| 7 | `07-settings-screen-early.png` | Settings | Auto-Pause, Distance Unit, Keep Screen On, About |
| 8 | `08-walk-complete-save-form.png` | Walk Complete — Save Form | Dog name, Route, Weather chips, Energy Level, Notes |
| 9 | `09-home-screen-pixel-font-xp.png` | Home Screen v2 — Pixel Font | PressStart2P pixel font, "+26 XP" on today's entry |
| 10 | `10-character-sheet-first-xp-earned.png` | Character Sheet — First XP | 26 XP earned, "First Mile" achievement unlocked |
| 11 | `11-active-workout-lap-mode-ghost.png` | Active Workout — Lap Mode + Ghost | Live lap timer, ghost runner delta, REC indicator |
| 12 | `12-run-details-phases-lap-chart.png` | Run Details — Top Half | Phase breakdown, lap trend chart, +158 XP |
| 13 | `13-run-details-lap-table-gps-map.png` | Run Details — Bottom Half | Lap table with deltas, GPS route map |
| 14 | `14-character-sheet-level3-achievements.png` | Character Sheet — Level 3 | 184 XP, Level 3, SPD 42 / END 49 / CON 12 |
| 15 | `15-character-sheet-v2-pixel-avatar-me.png` | Character Sheet v2 — Me Tab | Tabbed design (ME / JUNIPER), pixel art runner avatar, streak section |
| 16 | `16-character-sheet-v2-juniper-tab.png` | Character Sheet v2 — Juniper Tab | Pixel art dog avatar, Level 1, dog walk achievements |
| 17 | `17-run-details-phone-view.png` | Run Details (revisited) | XP breakdown and phase stats with adjusted layout |
| 18 | `18-walk-complete-form-with-selections.png` | Walk Complete — With Selections | "Park" route, Sunny + Cold weather, Normal energy |

### Feb 26, 2026 — Exercise Model + Dog Walk Events + Dialog Polish

| # | File | Screen | Notes |
|---|------|--------|-------|
| 19 | `19-exercise-model-home.png` | Home | After exercise model integration |
| 20 | `20-exercise-model-character-sheet-me.png` | Character Sheet (User) | With exercise model stats |
| 21 | `21-exercise-model-character-sheet-juniper.png` | Character Sheet (Juniper) | With exercise model stats |
| 22 | `22-dog-walk-event-strip.png` | Active Dog Walk | Event strip UI for logging walk events |
| 23 | `23-dog-run-event-strip.png` | Active Dog Run | Event strip during dog run |
| 24 | `24-dog-run-sprinting-event-strip.png` | Active Dog Run — Sprinting | Event strip with sprint detection active |
| 25 | `25-character-sheet-juniper-after-events.png` | Character Sheet (Juniper) | After dog walk event achievements |
| 26 | `26-stop-confirmation-dialog.png` | Stop Confirmation Dialog | Pixel-art styled stop dialog with color-coded buttons |
| 27 | `27-back-confirmation-dialog.png` | Back Confirmation Dialog | Navigate-away dialog with Go Home / Stop / Cancel |

### Feb 28, 2026 — Narrative Rewrite + Sprint MPH Fix

| # | File | Screen | Notes |
|---|------|--------|-------|
| 28 | `28-dog-walk-detail-narrative-rewrite.png` | Dog Walk Detail | Dynamic RPG narrative engine with randomized word pools |
| 29 | `29-sprint-mph-display-fix.png` | Active Dog Run — Sprinting | MPH speed display fix during sprinting |

## Workflow

When the UI changes:

1. Copy the old `current/<category>/<screen>.png` to `history/<next-number>-<description>.png`
2. Overwrite `current/<category>/<screen>.png` with the new Roborazzi golden
3. Add a row to the History Log table above

To sync all goldens to `current/` at once:

```bash
python docs/screenshots/stitch.py sync-current          # copy all
python docs/screenshots/stitch.py sync-current --prune   # copy + remove orphans
```

Comparisons are stored as `comparisons/<date>/<category>/<filename>.png`.
