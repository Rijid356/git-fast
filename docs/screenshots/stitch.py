#!/usr/bin/env python3
"""
Screenshot comparison tool with Pixel 9 phone frames.

Usage:
    # Stitch two screenshots into a side-by-side comparison with phone frames
    python docs/screenshots/stitch.py stitch <before> <after> <output>

    # Archive only the changed golden screenshots (detects via git diff)
    python docs/screenshots/stitch.py archive --tag "2026-03-03_pr126-dog-walk-events"

    # Compare two archives (stitches all matching screenshots)
    python docs/screenshots/stitch.py compare 2026-03-01_initial 2026-03-03_events
"""

import argparse
import os
import shutil
import subprocess
import sys
from datetime import date
from pathlib import Path
from typing import List, Optional

from PIL import Image, ImageDraw, ImageFilter, ImageFont

# -- Paths --
GOLDENS_DIR = Path("app/src/test/snapshots")
HISTORY_DIR = Path("docs/screenshots/history")
COMPARISONS_DIR = Path("docs/screenshots/comparisons")

# -- Theme (matches git-fast app) --
BG_COLOR = (13, 17, 23)        # #0D1117
LABEL_FG = (255, 255, 255)     # white
NEON_GREEN = (57, 255, 20)     # #39FF14 (primary)
FRAME_BORDER = (68, 76, 86)    # #444c56
FRAME_OUTLINE = (48, 54, 61)   # #30363d
PUNCH_HOLE_BG = (26, 31, 38)   # #1a1f26

# -- Phone frame dimensions (base = 280px wide, scale proportionally) --
BASE_FRAME_W = 280
BASE_FRAME_H = 625
BASE_BORDER = 3
BASE_RADIUS = 36
BASE_INSET = 8
BASE_SCREEN_RADIUS = 28
BASE_PUNCH_DIAMETER = 10
BASE_PUNCH_TOP = 14


def get_font(size: int):
    """Load a font, preferring bold for labels."""
    for name in ["arialbd.ttf", "Arial Bold.ttf", "arial.ttf", "Arial.ttf",
                  "DejaVuSans-Bold.ttf", "DejaVuSans.ttf"]:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def draw_rounded_rect(draw: ImageDraw.Draw, xy, radius: int,
                      fill=None, outline=None, width: int = 1):
    """Draw a rounded rectangle (Pillow < 9.0 compat wrapper)."""
    try:
        draw.rounded_rectangle(xy, radius=radius, fill=fill,
                               outline=outline, width=width)
    except AttributeError:
        # Fallback for older Pillow: draw regular rect + corner arcs
        x0, y0, x1, y1 = xy
        draw.rectangle([x0 + radius, y0, x1 - radius, y1], fill=fill)
        draw.rectangle([x0, y0 + radius, x1, y1 - radius], fill=fill)
        draw.pieslice([x0, y0, x0 + 2 * radius, y0 + 2 * radius], 180, 270, fill=fill)
        draw.pieslice([x1 - 2 * radius, y0, x1, y0 + 2 * radius], 270, 360, fill=fill)
        draw.pieslice([x0, y1 - 2 * radius, x0 + 2 * radius, y1], 90, 180, fill=fill)
        draw.pieslice([x1 - 2 * radius, y1 - 2 * radius, x1, y1], 0, 90, fill=fill)
        if outline:
            draw.arc([x0, y0, x0 + 2 * radius, y0 + 2 * radius], 180, 270, fill=outline, width=width)
            draw.arc([x1 - 2 * radius, y0, x1, y0 + 2 * radius], 270, 360, fill=outline, width=width)
            draw.arc([x0, y1 - 2 * radius, x0 + 2 * radius, y1], 90, 180, fill=outline, width=width)
            draw.arc([x1 - 2 * radius, y1 - 2 * radius, x1, y1], 0, 90, fill=outline, width=width)
            draw.line([x0 + radius, y0, x1 - radius, y0], fill=outline, width=width)
            draw.line([x0 + radius, y1, x1 - radius, y1], fill=outline, width=width)
            draw.line([x0, y0 + radius, x0, y1 - radius], fill=outline, width=width)
            draw.line([x1, y0 + radius, x1, y1 - radius], fill=outline, width=width)


def render_phone_frame(screenshot: Image.Image, frame_width: int = 420) -> Image.Image:
    """Render a screenshot inside a Pixel 9-style phone frame."""
    scale = frame_width / BASE_FRAME_W
    frame_h = int(BASE_FRAME_H * scale)
    border = max(2, int(BASE_BORDER * scale))
    radius = int(BASE_RADIUS * scale)
    inset = int(BASE_INSET * scale)
    screen_radius = int(BASE_SCREEN_RADIUS * scale)
    punch_d = int(BASE_PUNCH_DIAMETER * scale)
    punch_top = int(BASE_PUNCH_TOP * scale)

    screen_w = frame_width - 2 * inset - 2 * border
    screen_h = frame_h - 2 * inset - 2 * border

    # Scale screenshot to fit screen area
    img_ratio = screenshot.width / screenshot.height
    screen_ratio = screen_w / screen_h
    if img_ratio > screen_ratio:
        new_w = screen_w
        new_h = int(screen_w / img_ratio)
    else:
        new_h = screen_h
        new_w = int(screen_h * img_ratio)
    scaled = screenshot.resize((new_w, new_h), Image.LANCZOS)

    # Shadow canvas (larger for shadow spread)
    shadow_pad = 20
    total_w = frame_width + 2 * shadow_pad
    total_h = frame_h + 2 * shadow_pad

    # Draw shadow layer
    shadow = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    draw_rounded_rect(shadow_draw,
                      [shadow_pad, shadow_pad + 6,
                       shadow_pad + frame_width, shadow_pad + frame_h + 6],
                      radius, fill=(0, 0, 0, 120))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=12))

    # Draw phone frame on top
    frame = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    fd = ImageDraw.Draw(frame)

    # Outer frame outline
    draw_rounded_rect(fd,
                      [shadow_pad - 1, shadow_pad - 1,
                       shadow_pad + frame_width + 1, shadow_pad + frame_h + 1],
                      radius + 1, fill=FRAME_OUTLINE)

    # Frame body
    draw_rounded_rect(fd,
                      [shadow_pad, shadow_pad,
                       shadow_pad + frame_width, shadow_pad + frame_h],
                      radius, fill=FRAME_BORDER)

    # Inner bezel (dark background)
    draw_rounded_rect(fd,
                      [shadow_pad + border, shadow_pad + border,
                       shadow_pad + frame_width - border,
                       shadow_pad + frame_h - border],
                      radius - border, fill=BG_COLOR)

    # Screen area mask (rounded rect)
    screen_x = shadow_pad + inset + border
    screen_y = shadow_pad + inset + border
    screen_mask = Image.new("L", (total_w, total_h), 0)
    mask_draw = ImageDraw.Draw(screen_mask)
    draw_rounded_rect(mask_draw,
                      [screen_x, screen_y,
                       screen_x + screen_w, screen_y + screen_h],
                      screen_radius, fill=255)

    # Place screenshot centered in screen area
    offset_x = screen_x + (screen_w - new_w) // 2
    offset_y = screen_y + (screen_h - new_h) // 2
    screen_layer = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    screen_layer.paste(scaled, (offset_x, offset_y))

    # Punch-hole camera
    punch_x = shadow_pad + frame_width // 2
    punch_y = shadow_pad + punch_top + punch_d // 2
    fd.ellipse([punch_x - punch_d // 2 - 1, punch_y - punch_d // 2 - 1,
                punch_x + punch_d // 2 + 1, punch_y + punch_d // 2 + 1],
               fill=FRAME_OUTLINE)
    fd.ellipse([punch_x - punch_d // 2, punch_y - punch_d // 2,
                punch_x + punch_d // 2, punch_y + punch_d // 2],
               fill=PUNCH_HOLE_BG)

    # Composite: shadow → frame → screen (masked)
    result = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    result = Image.alpha_composite(result, shadow)
    result = Image.alpha_composite(result, frame)
    result.paste(screen_layer, mask=screen_mask)

    # Re-draw punch-hole on top of screen content
    top_draw = ImageDraw.Draw(result)
    top_draw.ellipse([punch_x - punch_d // 2 - 1, punch_y - punch_d // 2 - 1,
                      punch_x + punch_d // 2 + 1, punch_y + punch_d // 2 + 1],
                     fill=FRAME_OUTLINE)
    top_draw.ellipse([punch_x - punch_d // 2, punch_y - punch_d // 2,
                      punch_x + punch_d // 2, punch_y + punch_d // 2],
                     fill=PUNCH_HOLE_BG)

    return result


def stitch(before_path: str, after_path: str, output_path: str,
           label_before: str = "BEFORE", label_after: str = "AFTER",
           frame_width: int = 420) -> str:
    """Stitch two screenshots in Pixel 9 phone frames side-by-side."""
    before = Image.open(before_path)
    after = Image.open(after_path)

    # Render each in a phone frame
    before_framed = render_phone_frame(before, frame_width)
    after_framed = render_phone_frame(after, frame_width)

    # Layout: label above each phone, gap between
    label_font = get_font(42)
    gap = 40
    label_h = 70

    total_w = before_framed.width + gap + after_framed.width
    total_h = label_h + max(before_framed.height, after_framed.height)

    canvas = Image.new("RGBA", (total_w, total_h), BG_COLOR + (255,))
    draw = ImageDraw.Draw(canvas)

    # Draw BEFORE label (centered above left phone)
    bb = draw.textbbox((0, 0), label_before, font=label_font)
    tw = bb[2] - bb[0]
    draw.text(
        ((before_framed.width - tw) // 2, (label_h - (bb[3] - bb[1])) // 2),
        label_before, fill=LABEL_FG, font=label_font,
    )

    # Draw AFTER label (centered above right phone)
    ab = draw.textbbox((0, 0), label_after, font=label_font)
    tw2 = ab[2] - ab[0]
    draw.text(
        (before_framed.width + gap + (after_framed.width - tw2) // 2,
         (label_h - (ab[3] - ab[1])) // 2),
        label_after, fill=NEON_GREEN, font=label_font,
    )

    # Paste phone frames
    canvas.alpha_composite(before_framed, (0, label_h))
    canvas.alpha_composite(after_framed, (before_framed.width + gap, label_h))

    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    canvas.convert("RGB").save(output_path, quality=95)
    return output_path


def get_changed_screenshots() -> List[str]:
    """Detect which golden screenshots changed vs the last commit on main."""
    try:
        result = subprocess.run(
            ["git", "diff", "--name-only", "HEAD~1", "--",
             str(GOLDENS_DIR / "**/*.png")],
            capture_output=True, text=True, cwd="."
        )
        if result.returncode == 0 and result.stdout.strip():
            return [f for f in result.stdout.strip().split("\n") if f.endswith(".png")]
    except Exception:
        pass
    return []


def archive_changed(tag: str) -> str:
    """Archive only golden screenshots that changed (via git diff)."""
    dest = HISTORY_DIR / tag
    if dest.exists():
        print(f"Archive {dest} already exists, skipping.")
        return str(dest)

    changed = get_changed_screenshots()
    if not changed:
        # Fall back to archiving all
        print("No git diff detected, archiving all goldens.")
        return archive_all(tag)

    count = 0
    for filepath in changed:
        src = Path(filepath)
        if src.exists():
            rel = src.relative_to(GOLDENS_DIR)
            target = dest / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, target)
            count += 1

    print(f"Archived {count} changed screenshots to {dest}")
    return str(dest)


def archive_all(tag: Optional[str] = None) -> str:
    """Archive all current golden screenshots."""
    tag = tag or date.today().isoformat()
    dest = HISTORY_DIR / tag

    if dest.exists():
        print(f"Archive {dest} already exists, skipping.")
        return str(dest)

    count = 0
    for png in GOLDENS_DIR.rglob("*.png"):
        rel = png.relative_to(GOLDENS_DIR)
        target = dest / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(png, target)
        count += 1

    print(f"Archived {count} screenshots to {dest}")
    return str(dest)


def compare_archives(old_tag: str, new_tag: str) -> List[str]:
    """Stitch before/after for every screenshot that exists in both archives."""
    old_dir = HISTORY_DIR / old_tag
    new_dir = HISTORY_DIR / new_tag

    if not old_dir.exists():
        print(f"Error: archive {old_dir} not found")
        sys.exit(1)
    if not new_dir.exists():
        print(f"Error: archive {new_dir} not found")
        sys.exit(1)

    out_dir = COMPARISONS_DIR / f"{old_tag}_vs_{new_tag}"
    out_dir.mkdir(parents=True, exist_ok=True)

    outputs = []
    for old_png in sorted(old_dir.rglob("*.png")):
        rel = old_png.relative_to(old_dir)
        new_png = new_dir / rel
        if new_png.exists():
            out_path = out_dir / rel
            out_path.parent.mkdir(parents=True, exist_ok=True)
            stitch(str(old_png), str(new_png), str(out_path),
                   label_before=old_tag, label_after=new_tag)
            outputs.append(str(out_path))
            print(f"  Stitched: {rel}")

    for new_png in sorted(new_dir.rglob("*.png")):
        rel = new_png.relative_to(new_dir)
        if not (old_dir / rel).exists():
            print(f"  NEW: {rel}")

    print(f"\n{len(outputs)} comparisons saved to {out_dir}")
    return outputs


def main():
    parser = argparse.ArgumentParser(description="Screenshot comparison tool")
    sub = parser.add_subparsers(dest="command")

    # Stitch two images
    single = sub.add_parser("stitch", help="Stitch two images in phone frames")
    single.add_argument("before", help="Path to before image")
    single.add_argument("after", help="Path to after image")
    single.add_argument("output", help="Output path")
    single.add_argument("--label-before", default="BEFORE")
    single.add_argument("--label-after", default="AFTER")

    # Archive screenshots
    arch = sub.add_parser("archive", help="Archive golden screenshots")
    arch.add_argument("--tag", required=True,
                      help="Archive tag (e.g. 2026-03-03_pr126-dog-walk-events)")
    arch.add_argument("--all", action="store_true",
                      help="Archive all goldens (not just changed)")

    # Compare two archives
    cmp = sub.add_parser("compare", help="Compare two archives")
    cmp.add_argument("old", help="Old archive tag")
    cmp.add_argument("new", help="New archive tag")

    args = parser.parse_args()

    if args.command == "stitch":
        out = stitch(args.before, args.after, args.output,
                     args.label_before, args.label_after)
        print(f"Saved: {out}")

    elif args.command == "archive":
        if args.all:
            archive_all(args.tag)
        else:
            archive_changed(args.tag)

    elif args.command == "compare":
        compare_archives(args.old, args.new)

    else:
        parser.print_help()


if __name__ == "__main__":
    main()
