from __future__ import annotations

import base64
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PARTS_DIR = ROOT / "incoming_asset_parts"
OUT_DIR = ROOT / "incoming_assets"

PART_RE = re.compile(
    r"^(HSK[1-6]_SC\d{3}_(?:visual|audio)_assets\.zip)\.b64\.part(\d{2,})$"
)


def main() -> int:
    if not PARTS_DIR.exists():
        print("No incoming_asset_parts directory; nothing to reconstruct.")
        return 0

    groups: dict[str, list[tuple[int, Path]]] = {}
    for path in sorted(PARTS_DIR.iterdir()):
        if not path.is_file():
            continue
        match = PART_RE.match(path.name)
        if not match:
            continue
        package_name = match.group(1)
        part_no = int(match.group(2))
        groups.setdefault(package_name, []).append((part_no, path))

    if not groups:
        print("No base64 package parts found.")
        return 0

    OUT_DIR.mkdir(parents=True, exist_ok=True)

    for package_name, parts in sorted(groups.items()):
        parts.sort(key=lambda item: item[0])
        expected = list(range(len(parts)))
        actual = [n for n, _ in parts]
        if actual != expected:
            raise RuntimeError(
                f"Non-contiguous parts for {package_name}: expected={expected}, actual={actual}"
            )

        encoded = "".join(
            part.read_text(encoding="utf-8").strip()
            for _, part in parts
        )
        try:
            payload = base64.b64decode(encoded, validate=True)
        except Exception as exc:
            raise RuntimeError(f"Invalid base64 for {package_name}: {exc}") from exc

        if not payload.startswith(b"PK\x03\x04"):
            raise RuntimeError(f"Reconstructed file is not a ZIP: {package_name}")

        destination = OUT_DIR / package_name
        destination.write_bytes(payload)
        print(
            f"Reconstructed {destination.relative_to(ROOT)} "
            f"from {len(parts)} parts ({len(payload)} bytes)."
        )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
