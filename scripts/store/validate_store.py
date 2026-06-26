#!/usr/bin/env python3
"""Validate store metadata and screenshot outputs for Play and App Store limits."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("ERROR: PyYAML required. pip3 install -r scripts/store/requirements.txt", file=sys.stderr)
    sys.exit(1)

try:
    from PIL import Image
except ImportError:
    Image = None  # type: ignore

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "store"

ANDROID_LIMITS = {
    "title.txt": 30,
    "short_description.txt": 80,
    "full_description.txt": 4000,
}

IOS_LIMITS = {
    "name.txt": 30,
    "subtitle.txt": 30,
    "keywords.txt": 100,
    "promotional_text.txt": 170,
}

IOS_69_INCH = (1290, 2796)
ANDROID_MIN_SHORT_SIDE = 320


def read_text(path: Path) -> str:
    if not path.is_file():
        raise FileNotFoundError(f"Missing file: {path}")
    return path.read_text(encoding="utf-8").strip()


def validate_length(label: str, content: str, max_len: int, errors: list[str]) -> None:
    if len(content) > max_len:
        errors.append(f"{label}: {len(content)} chars (max {max_len})")
    if not content:
        errors.append(f"{label}: must not be empty")


def validate_android_metadata(locale_dir: Path, errors: list[str]) -> None:
    android_dir = locale_dir / "android"
    for filename, max_len in ANDROID_LIMITS.items():
        path = android_dir / filename
        try:
            content = read_text(path)
        except FileNotFoundError as exc:
            errors.append(str(exc))
            continue
        validate_length(f"android/{filename}", content, max_len, errors)


def validate_ios_metadata(locale_dir: Path, errors: list[str]) -> None:
    ios_dir = locale_dir / "ios"
    for filename, max_len in IOS_LIMITS.items():
        path = ios_dir / filename
        try:
            content = read_text(path)
        except FileNotFoundError as exc:
            errors.append(str(exc))
            continue
        validate_length(f"ios/{filename}", content, max_len, errors)
        if filename == "keywords.txt":
            if re.search(r",\s", content):
                errors.append("ios/keywords.txt: no spaces after commas")
            if content.endswith(","):
                errors.append("ios/keywords.txt: must not end with a comma")

    desc_path = ios_dir / "description.txt"
    if desc_path.is_file() and not read_text(desc_path):
        errors.append("ios/description.txt: must not be empty")


def validate_captions(locale_dir: Path, errors: list[str]) -> None:
    path = locale_dir / "captions.yaml"
    if not path.is_file():
        errors.append(f"Missing file: {path}")
        return
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    slots = data.get("slots") if isinstance(data, dict) else None
    if not slots:
        errors.append("captions.yaml: 'slots' must be a non-empty list")
        return
    for index, slot in enumerate(slots):
        if not isinstance(slot, dict):
            errors.append(f"captions.yaml slot {index}: must be a mapping")
            continue
        if not slot.get("id"):
            errors.append(f"captions.yaml slot {index}: missing 'id'")
        if not slot.get("above"):
            errors.append(f"captions.yaml slot {index}: missing 'above'")


def validate_screenshots(locale: str, errors: list[str], warnings: list[str]) -> None:
    if Image is None:
        warnings.append("Pillow not installed — skipping screenshot dimension checks")
        return

    android_dir = STORE / "output" / locale / "android" / "phoneScreenshots"
    if android_dir.is_dir():
        pngs = sorted(android_dir.glob("*.png")) + sorted(android_dir.glob("*.jpg"))
        if len(pngs) < 2:
            errors.append(f"Play phoneScreenshots: need at least 2 images, found {len(pngs)}")
        if len(pngs) > 8:
            errors.append(f"Play phoneScreenshots: max 8 images, found {len(pngs)}")
        for png in pngs:
            with Image.open(png) as img:
                short = min(img.size)
                if short < ANDROID_MIN_SHORT_SIDE:
                    errors.append(f"{png.name}: short side {short}px < {ANDROID_MIN_SHORT_SIDE}px")
    else:
        warnings.append(f"No Android screenshots at {android_dir} (run capture + frame first)")

    ios_dir = STORE / "output" / locale / "ios" / "6.9-inch"
    if ios_dir.is_dir():
        pngs = sorted(ios_dir.glob("*.png"))
        if len(pngs) < 1:
            errors.append("iOS 6.9-inch: need at least 1 screenshot")
        for png in pngs:
            with Image.open(png) as img:
                if img.size != IOS_69_INCH:
                    errors.append(
                        f"{png.name}: {img.size[0]}x{img.size[1]} (expected {IOS_69_INCH[0]}x{IOS_69_INCH[1]})"
                    )
    else:
        warnings.append(f"No iOS screenshots at {ios_dir} (run capture + frame first)")


def validate_factory_config(errors: list[str]) -> None:
    path = STORE / "app.factory.yaml"
    if not path.is_file():
        errors.append(f"Missing file: {path}")
        return
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    app = data.get("app", {}) if isinstance(data, dict) else {}
    for key in ("displayName", "androidPackage", "iosBundleId"):
        if not app.get(key):
            errors.append(f"app.factory.yaml: app.{key} is required")


def run_gplay_if_available(locale: str, warnings: list[str]) -> None:
    import shutil
    import subprocess

    if not shutil.which("gplay"):
        warnings.append("gplay CLI not installed — skipping Play CLI validation")
        return

    metadata_dir = STORE / "metadata"
    try:
        subprocess.run(
            ["gplay", "validate", "listing", "--dir", str(metadata_dir), "--locale", locale],
            check=True,
            capture_output=True,
            text=True,
        )
        print("gplay validate listing: OK")
    except subprocess.CalledProcessError as exc:
        print(exc.stdout or exc.stderr, file=sys.stderr)
        raise SystemExit(exc.returncode) from exc

    output_dir = STORE / "output"
    if output_dir.is_dir() and any(output_dir.rglob("*.png")):
        try:
            subprocess.run(
                ["gplay", "validate", "screenshots", "--dir", str(output_dir), "--locale", locale],
                check=True,
                capture_output=True,
                text=True,
            )
            print("gplay validate screenshots: OK")
        except subprocess.CalledProcessError as exc:
            print(exc.stdout or exc.stderr, file=sys.stderr)
            raise SystemExit(exc.returncode) from exc


def main() -> None:
    parser = argparse.ArgumentParser(description="Validate store metadata and assets")
    parser.add_argument("--locale", default="en-US", help="Locale to validate (default: en-US)")
    parser.add_argument("--skip-gplay", action="store_true", help="Skip gplay CLI checks")
    parser.add_argument("--metadata-only", action="store_true", help="Skip screenshot checks")
    args = parser.parse_args()

    locale_dir = STORE / "metadata" / args.locale
    if not locale_dir.is_dir():
        print(f"ERROR: locale directory not found: {locale_dir}", file=sys.stderr)
        sys.exit(1)

    errors: list[str] = []
    warnings: list[str] = []

    validate_factory_config(errors)
    validate_android_metadata(locale_dir, errors)
    validate_ios_metadata(locale_dir, errors)
    validate_captions(locale_dir, errors)
    if not args.metadata_only:
        validate_screenshots(args.locale, errors, warnings)

    for warning in warnings:
        print(f"WARNING: {warning}")

    if errors:
        print("Validation failed:", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        sys.exit(1)

    print(f"Store validation passed for locale '{args.locale}'.")

    if not args.skip_gplay:
        run_gplay_if_available(args.locale, warnings)


if __name__ == "__main__":
    main()
