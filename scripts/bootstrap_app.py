#!/usr/bin/env python3
"""Rename CMP App Factory template identity in a copied project tree."""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

PACKAGE_RE = re.compile(r"^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$")

TEMPLATE_PACKAGE = "com.devindie.cmptemplate"
TEMPLATE_PACKAGE_PATH = "com/devindie/cmptemplate"
TEMPLATE_PASCAL = "CMPTemplate"
TEMPLATE_PASCAL_APP = "CmpTemplate"
TEMPLATE_SLUG = "cmptemplate"
TEMPLATE_FIREBASE_PROJECT_SLUG = "cmp-template"

TEXT_EXTENSIONS = {
    ".conf",
    ".gradle",
    ".json",
    ".kts",
    ".kt",
    ".md",
    ".plist",
    ".properties",
    ".pbxproj",
    ".pro",
    ".prof",
    ".sh",
    ".swift",
    ".txt",
    ".xcconfig",
    ".xml",
    ".yaml",
    ".yml",
}

SKIP_DIR_NAMES = {
    ".git",
    ".gradle",
    ".kotlin",
    ".codegraph",
    "build",
    ".venv",
    "__pycache__",
    ".pytest_cache",
}

RSYNC_EXCLUDES = [
    ".git",
    "build",
    ".gradle",
    ".kotlin",
    "architecture/bin",
    ".codegraph",
    "local.properties",
    "store/.raw",
    "store/output",
    "scripts/store/.venv",
    ".DS_Store",
    ".idea",
    "*.apk",
    "*.aab",
    "*.ipa",
    "*.dex",
    "*.class",
]


@dataclass(frozen=True)
class AppIdentity:
    package: str
    display_name: str

    @property
    def package_path(self) -> str:
        return self.package.replace(".", "/")

    @property
    def slug(self) -> str:
        return self.package.rsplit(".", 1)[-1].lower()

    @property
    def pascal_name(self) -> str:
        display_words = [w for w in re.split(r"\s+", self.display_name.strip()) if w]
        if len(display_words) > 1:
            return "".join(word.capitalize() for word in display_words)
        segment = self.package.rsplit(".", 1)[-1]
        return "".join(part.capitalize() for part in re.split(r"[_-]", segment))

    @property
    def ios_bundle_id(self) -> str:
        return f"{self.package}.{self.pascal_name}"

    @property
    def firebase_project_slug(self) -> str:
        return f"{self.slug}-template"

    @property
    def escaped_package(self) -> str:
        return self.package.replace(".", r"\.")

    def replacements(self) -> list[tuple[str, str]]:
        template_escaped = TEMPLATE_PACKAGE.replace(".", r"\.")
        return [
            (TEMPLATE_PACKAGE, self.package),
            (template_escaped, self.escaped_package),
            (TEMPLATE_PACKAGE_PATH, self.package_path),
            (TEMPLATE_PASCAL, self.pascal_name),
            (TEMPLATE_PASCAL_APP, self.pascal_name),
            (TEMPLATE_FIREBASE_PROJECT_SLUG, self.firebase_project_slug),
            (TEMPLATE_SLUG, self.slug),
        ]

    def filename_replacements(self) -> list[tuple[str, str]]:
        return [
            (TEMPLATE_PASCAL_APP, self.pascal_name),
            (TEMPLATE_PASCAL, self.pascal_name),
            (TEMPLATE_SLUG, self.slug),
        ]


def validate_package(package: str) -> None:
    if not PACKAGE_RE.match(package):
        raise ValueError(
            f"Invalid --package {package!r}. "
            "Use reverse-DNS segments of lowercase letters, digits, underscores "
            "(e.g. com.acme.myvault)."
        )


def is_probably_binary(path: Path) -> bool:
    if path.suffix.lower() not in TEXT_EXTENSIONS and path.suffix:
        return True
    try:
        with path.open("rb") as handle:
            chunk = handle.read(8192)
        return b"\0" in chunk
    except OSError:
        return True


def should_skip_path(path: Path) -> bool:
    return any(part in SKIP_DIR_NAMES for part in path.parts)


def replace_in_file(path: Path, replacements: list[tuple[str, str]], dry_run: bool) -> bool:
    if is_probably_binary(path):
        return False
    try:
        original = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return False
    updated = original
    for old, new in replacements:
        updated = updated.replace(old, new)
    if updated == original:
        return False
    if not dry_run:
        path.write_text(updated, encoding="utf-8")
    return True


def find_package_dirs(root: Path) -> list[Path]:
    matches: list[Path] = []
    for candidate in root.rglob("cmptemplate"):
        if not candidate.is_dir():
            continue
        if should_skip_path(candidate):
            continue
        parts = candidate.parts
        try:
            com_idx = parts.index("com")
        except ValueError:
            continue
        expected = parts[com_idx : com_idx + 3]
        if list(expected) != ["com", "devindie", "cmptemplate"]:
            continue
        if com_idx + 3 != len(parts):
            continue
        matches.append(candidate)
    return sorted(set(matches))


def rename_package_directories(root: Path, identity: AppIdentity, dry_run: bool) -> int:
    new_suffix = Path(identity.package_path)
    moved = 0
    for old_dir in find_package_dirs(root):
        new_dir = old_dir.parents[2] / new_suffix
        if old_dir.resolve() == new_dir.resolve():
            continue
        if dry_run:
            moved += 1
            continue
        new_dir.parent.mkdir(parents=True, exist_ok=True)
        if new_dir.exists():
            raise RuntimeError(f"Target package directory already exists: {new_dir}")
        shutil.move(str(old_dir), str(new_dir))
        _prune_empty_parents(old_dir.parent, stop_at=root)
        moved += 1
    return moved


def _prune_empty_parents(path: Path, stop_at: Path) -> None:
    current = path.resolve()
    stop = stop_at.resolve()
    while current != stop and current.is_dir() and not any(current.iterdir()):
        parent = current.parent
        current.rmdir()
        current = parent


def rename_template_files(root: Path, identity: AppIdentity, dry_run: bool) -> int:
    renamed = 0
    replacements = identity.filename_replacements()
    files = [p for p in root.rglob("*") if p.is_file() and not should_skip_path(p)]
    for path in sorted(files, key=lambda p: len(p.parts), reverse=True):
        new_name = path.name
        for old, new in replacements:
            new_name = new_name.replace(old, new)
        if new_name == path.name:
            continue
        target = path.with_name(new_name)
        if dry_run:
            renamed += 1
            continue
        if target.exists():
            raise RuntimeError(f"Cannot rename {path} -> {target}: target exists")
        path.rename(target)
        renamed += 1
    return renamed


def transform_tree(root: Path, identity: AppIdentity, dry_run: bool) -> None:
    replacements = identity.replacements()
    print("Replacing template identifiers in files...")
    changed = 0
    for path in root.rglob("*"):
        if not path.is_file() or should_skip_path(path):
            continue
        if replace_in_file(path, replacements, dry_run):
            changed += 1
    print(f"  updated {changed} file(s)")

    print("Moving Kotlin package directories...")
    moved = rename_package_directories(root, identity, dry_run)
    print(f"  moved {moved} package tree(s)")

    print("Renaming template-specific files...")
    renamed = rename_template_files(root, identity, dry_run)
    print(f"  renamed {renamed} file(s)")


def rsync_template(template_root: Path, output_dir: Path) -> None:
    if shutil.which("rsync") is None:
        raise RuntimeError("rsync is required but not found on PATH")

    output_dir.parent.mkdir(parents=True, exist_ok=True)
    exclude_args: list[str] = []
    for pattern in RSYNC_EXCLUDES:
        exclude_args.extend(["--exclude", pattern])

    cmd = [
        "rsync",
        "-a",
        *exclude_args,
        f"{template_root}/",
        f"{output_dir}/",
    ]
    print("Copying template tree...")
    print(f"  {' '.join(cmd)}")
    subprocess.run(cmd, check=True)


def init_git_repo(output_dir: Path) -> None:
    subprocess.run(["git", "init"], cwd=output_dir, check=True)
    subprocess.run(["git", "add", "."], cwd=output_dir, check=True)
    subprocess.run(
        [
            "git",
            "commit",
            "-m",
            "Initial commit from CMP App Factory template.\n",
        ],
        cwd=output_dir,
        check=True,
    )


def bootstrap(
    template_root: Path,
    output_dir: Path,
    identity: AppIdentity,
    *,
    dry_run: bool = False,
    force: bool = False,
    init_git: bool = True,
) -> None:
    output_dir = output_dir.resolve()
    template_root = template_root.resolve()

    if output_dir.exists():
        if not force:
            raise RuntimeError(
                f"Output directory already exists: {output_dir}. Use --force to replace it."
            )
        if not dry_run:
            shutil.rmtree(output_dir)

    print("Bootstrap configuration:")
    print(f"  package:      {identity.package}")
    print(f"  display name: {identity.display_name}")
    print(f"  pascal name:  {identity.pascal_name}")
    print(f"  slug:         {identity.slug}")
    print(f"  iOS bundle:   {identity.ios_bundle_id}")
    print(f"  output:       {output_dir}")

    if dry_run:
        print("\n[dry-run] Planned transforms:")
        print(f"  package trees to move: {len(find_package_dirs(template_root))}")
        print(f"  replacements: {identity.replacements()}")
        print("\n[dry-run] Would rsync template, apply transforms, and git init.")
        return

    rsync_template(template_root, output_dir)
    transform_tree(output_dir, identity, dry_run=False)

    if init_git:
        print("Initializing fresh git repository...")
        init_git_repo(output_dir)

    print("\nDone.")
    print(f"  cd {output_dir}")
    print("  cp local.properties.example local.properties  # set sdk.dir")
    print("  ./gradlew qualityCheck")
    print("\nReplace Firebase placeholder configs before shipping:")
    print("  androidApp/google-services.json")
    print("  iosApp/iosApp/GoogleService-Info.plist")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Bootstrap a new app from the CMP App Factory template.",
    )
    parser.add_argument(
        "--package",
        required=True,
        help="Reverse-DNS application id (e.g. com.acme.myvault)",
    )
    parser.add_argument(
        "--display-name",
        required=True,
        help='Human-readable app name (e.g. "My Vault")',
    )
    parser.add_argument(
        "--output-dir",
        required=True,
        type=Path,
        help="Destination directory for the new project",
    )
    parser.add_argument(
        "--template-root",
        type=Path,
        default=None,
        help="Template repository root (default: parent of scripts/)",
    )
    parser.add_argument("--dry-run", action="store_true", help="Preview changes only")
    parser.add_argument("--force", action="store_true", help="Replace existing output directory")
    parser.add_argument("--no-git", action="store_true", help="Skip git init and initial commit")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)

    try:
        validate_package(args.package)
    except ValueError as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1

    template_root = args.template_root or Path(__file__).resolve().parent.parent
    identity = AppIdentity(package=args.package, display_name=args.display_name)

    try:
        bootstrap(
            template_root=template_root,
            output_dir=args.output_dir,
            identity=identity,
            dry_run=args.dry_run,
            force=args.force,
            init_git=not args.no_git,
        )
    except (RuntimeError, subprocess.CalledProcessError, OSError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
