#!/usr/bin/env python3
"""Composite raw Maestro captures into store-ready framed screenshots with captions."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

import yaml
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "store"


def load_yaml(path: Path) -> dict:
    return yaml.safe_load(path.read_text(encoding="utf-8")) or {}


def parse_hex_color(value: str, default: tuple[int, int, int]) -> tuple[int, int, int]:
    if not value or not value.startswith("#"):
        return default
    hex_value = value.lstrip("#")
    if len(hex_value) == 6:
        return tuple(int(hex_value[i : i + 2], 16) for i in (0, 2, 4))  # type: ignore
    return default


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for candidate in (
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ):
        path = Path(candidate)
        if path.is_file():
            return ImageFont.truetype(str(path), size=size)
    return ImageFont.load_default()


def fit_image(image: Image.Image, box_width: int, box_height: int) -> Image.Image:
    ratio = min(box_width / image.width, box_height / image.height)
    new_size = (max(1, int(image.width * ratio)), max(1, int(image.height * ratio)))
    resized = image.resize(new_size, Image.Resampling.LANCZOS)
    canvas = Image.new("RGB", (box_width, box_height), (0, 0, 0))
    offset = ((box_width - new_size[0]) // 2, (box_height - new_size[1]) // 2)
    canvas.paste(resized, offset)
    return canvas


def draw_centered_text(
    draw: ImageDraw.ImageDraw,
    text: str,
    y: int,
    width: int,
    font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
    fill: tuple[int, int, int],
) -> None:
    if not text:
        return
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    draw.text(((width - text_width) // 2, y), text, font=font, fill=fill)


def composite_screenshot(
    raw_path: Path,
    output_path: Path,
    above: str,
    below: str,
    canvas_size: tuple[int, int],
    style: dict,
    branding_color: tuple[int, int, int],
) -> None:
    layout = style.get("layout", {})
    font_cfg = style.get("font", {})
    band_ratio = float(layout.get("caption_band_ratio", 0.14))
    padding_ratio = float(layout.get("screenshot_padding_ratio", 0.04))

    width, height = canvas_size
    top_band = int(height * band_ratio)
    bottom_band = int(height * band_ratio)
    pad = int(width * padding_ratio)

    band_color = parse_hex_color(font_cfg.get("band_color"), branding_color)
    text_color = parse_hex_color(font_cfg.get("caption_color"), (255, 255, 255))
    above_font = load_font(int(font_cfg.get("above_size", 52)))
    below_font = load_font(int(font_cfg.get("below_size", 36)))

    canvas = Image.new("RGB", canvas_size, band_color)
    draw = ImageDraw.Draw(canvas)

    screenshot_box = (pad, top_band + pad, width - pad, height - bottom_band - pad)
    box_w = screenshot_box[2] - screenshot_box[0]
    box_h = screenshot_box[3] - screenshot_box[1]

    with Image.open(raw_path) as raw:
        fitted = fit_image(raw.convert("RGB"), box_w, box_h)
    canvas.paste(fitted, (screenshot_box[0], screenshot_box[1]))

    draw.rectangle((0, 0, width, top_band), fill=band_color)
    draw.rectangle((0, height - bottom_band, width, height), fill=band_color)

    draw_centered_text(draw, above, top_band // 4, width, above_font, text_color)
    draw_centered_text(
        draw,
        below,
        height - bottom_band + bottom_band // 4,
        width,
        below_font,
        text_color,
    )

    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path, format="PNG", optimize=True)


def collect_raw_images(raw_dir: Path) -> list[Path]:
    if not raw_dir.is_dir():
        return []
    images = sorted(raw_dir.glob("*.png")) + sorted(raw_dir.glob("*.jpg"))
    return images


def main() -> None:
    parser = argparse.ArgumentParser(description="Frame raw screenshots for store listings")
    parser.add_argument("--locale", default="en-US")
    parser.add_argument("--config", default=str(STORE / "app.factory.yaml"))
    args = parser.parse_args()

    config_path = Path(args.config)
    factory = load_yaml(config_path)
    style_path = STORE / "templates" / "caption_style.yaml"
    style = load_yaml(style_path) if style_path.is_file() else {}

    branding_hex = factory.get("branding", {}).get("primaryColor", "#6750A4")
    branding_color = parse_hex_color(branding_hex, (103, 80, 164))

    locale_dir = STORE / "metadata" / args.locale
    captions_path = locale_dir / "captions.yaml"
    if not captions_path.is_file():
        print(f"ERROR: missing {captions_path}", file=sys.stderr)
        sys.exit(1)

    slots = load_yaml(captions_path).get("slots", [])
    canvas_cfg = style.get("canvas", {})

    platforms = {
        "android": (
            STORE / ".raw" / "android",
            STORE / "output" / args.locale / "android" / "phoneScreenshots",
            tuple(canvas_cfg.get("android", {"width": 1080, "height": 1920}).values())
            if isinstance(canvas_cfg.get("android"), dict)
            else (1080, 1920),
        ),
        "ios": (
            STORE / ".raw" / "ios",
            STORE / "output" / args.locale / "ios" / "6.9-inch",
            tuple(canvas_cfg.get("ios", {"width": 1290, "height": 2796}).values())
            if isinstance(canvas_cfg.get("ios"), dict)
            else (1290, 2796),
        ),
    }

    generated = 0
    for platform, (raw_dir, out_dir, canvas_size) in platforms.items():
        raw_images = collect_raw_images(raw_dir)
        if not raw_images:
            print(f"WARNING: no raw images in {raw_dir} — skipping {platform}")
            continue
        if len(raw_images) < len(slots):
            print(
                f"WARNING: {platform} has {len(raw_images)} captures but {len(slots)} caption slots",
                file=sys.stderr,
            )
        for index, raw_path in enumerate(raw_images):
            slot = slots[index] if index < len(slots) else {"above": "", "below": ""}
            slot_id = slot.get("id", f"slot_{index + 1}")
            output_name = f"{index + 1:02d}_{slot_id}.png"
            composite_screenshot(
                raw_path=raw_path,
                output_path=out_dir / output_name,
                above=str(slot.get("above", "")),
                below=str(slot.get("below", "")),
                canvas_size=(int(canvas_size[0]), int(canvas_size[1])),
                style=style,
                branding_color=branding_color,
            )
            generated += 1
            print(f"Wrote {out_dir / output_name}")

    if generated == 0:
        print("ERROR: no screenshots generated. Run scripts/store/capture.sh first.", file=sys.stderr)
        sys.exit(1)

    print(f"Generated {generated} framed screenshot(s) for locale '{args.locale}'.")


if __name__ == "__main__":
    main()
