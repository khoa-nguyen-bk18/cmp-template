"""Tests for scripts/bootstrap_app.py"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))

from bootstrap_app import (  # noqa: E402
    AppIdentity,
    find_package_dirs,
    rename_package_directories,
    replace_in_file,
    transform_tree,
    validate_package,
)


def test_pascal_name_from_display_name() -> None:
    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")
    assert identity.pascal_name == "MyVault"
    assert identity.slug == "myvault"
    assert identity.ios_bundle_id == "com.acme.myvault.MyVault"


def test_pascal_name_from_package_segment() -> None:
    identity = AppIdentity(package="com.acme.my_vault", display_name="my_vault")
    assert identity.pascal_name == "MyVault"
    assert identity.slug == "my_vault"
    assert identity.ios_bundle_id == "com.acme.my_vault.MyVault"
    assert identity.firebase_project_slug == "my_vault-template"


def test_replacements_update_escaped_regex_package() -> None:
    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")
    text = 'Regex("""com\\.devindie\\.cmptemplate\\.feature\\.([^.]+)\\.impl(\\..+)?""")'
    updated = text
    for old, new in identity.replacements():
        updated = updated.replace(old, new)
    assert r"com\.acme\.myvault\.feature" in updated
    assert "devindie" not in updated


def test_replacements_order_preserves_package() -> None:
    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")
    text = (
        "package com.devindie.cmptemplate\n"
        "class CmpTemplateApplication\n"
        "PRODUCT_NAME=CMPTemplate\n"
        "appId: com.devindie.cmptemplate\n"
        "project_id: cmp-template-placeholder\n"
    )
    updated = text
    for old, new in identity.replacements():
        updated = updated.replace(old, new)
    assert "com.devindie.cmptemplate" not in updated
    assert "com.acme.myvault" in updated
    assert "CmpTemplateApplication" not in updated
    assert "MyVaultApplication" in updated
    assert "CMPTemplate" not in updated
    assert "myvault-template-placeholder" in updated
    assert "myvault-template-placeholder" in updated


def test_validate_package_rejects_invalid() -> None:
    with pytest.raises(ValueError):
        validate_package("Com.Acme.App")
    with pytest.raises(ValueError):
        validate_package("not-a-package")


def test_replace_in_file(tmp_path: Path) -> None:
    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")
    file_path = tmp_path / "Sample.kt"
    file_path.write_text("package com.devindie.cmptemplate\n", encoding="utf-8")
    assert replace_in_file(file_path, identity.replacements(), dry_run=False)
    assert file_path.read_text(encoding="utf-8") == "package com.acme.myvault\n"


def test_moves_kotlin_package_tree(tmp_path: Path) -> None:
    old_dir = tmp_path / "domain/src/commonMain/kotlin/com/devindie/cmptemplate"
    old_dir.mkdir(parents=True)
    (old_dir / "Sample.kt").write_text("package com.devindie.cmptemplate\n", encoding="utf-8")

    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")
    transform_tree(tmp_path, identity, dry_run=False)

    new_dir = tmp_path / "domain/src/commonMain/kotlin/com/acme/myvault"
    assert new_dir.is_dir()
    assert not old_dir.exists()
    assert (new_dir / "Sample.kt").read_text(encoding="utf-8") == "package com.acme.myvault\n"


def test_find_package_dirs_ignores_unrelated_paths(tmp_path: Path) -> None:
    unrelated = tmp_path / "docs/cmptemplate"
    unrelated.mkdir(parents=True)
    real = tmp_path / "shared/src/commonMain/kotlin/com/devindie/cmptemplate"
    real.mkdir(parents=True)

    found = find_package_dirs(tmp_path)
    assert found == [real]


def test_rename_package_directories_dry_run(tmp_path: Path) -> None:
    old_dir = tmp_path / "data/src/commonMain/kotlin/com/devindie/cmptemplate"
    old_dir.mkdir(parents=True)
    identity = AppIdentity(package="com.acme.myvault", display_name="My Vault")

    moved = rename_package_directories(tmp_path, identity, dry_run=True)
    assert moved == 1
    assert old_dir.exists()


def test_bootstrap_script_dry_run() -> None:
    result = subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/bootstrap_app.py"),
            "--dry-run",
            "--package",
            "com.acme.myvault",
            "--display-name",
            "My Vault",
            "--output-dir",
            "/tmp/cmp-bootstrap-dry-run",
        ],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    assert result.returncode == 0
    assert "com.acme.myvault" in result.stdout
