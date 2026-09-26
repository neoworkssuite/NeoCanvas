#!/usr/bin/env python3
"""Build labelled SVG review sheets and similarity evidence from raster preview artifacts."""

from __future__ import annotations

import argparse
import base64
import binascii
import html
import json
import math
import struct
import subprocess
import zlib
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_INPUT = ROOT / "renderer" / "build" / "brush-review"
DEFAULT_OUTPUT = ROOT / "build" / "brush-review-sheets"


def png_bytes(width: int, height: int, rgba: bytes) -> bytes:
    if len(rgba) != width * height * 4:
        raise ValueError("invalid RGBA preview length")
    raw = b"".join(b"\0" + rgba[y * width * 4 : (y + 1) * width * 4] for y in range(height))

    def chunk(kind: bytes, data: bytes) -> bytes:
        checksum = binascii.crc32(kind + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", checksum)

    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)) +
            chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


def alpha(rgba: bytes) -> bytes:
    return rgba[3::4]


def cosine(left: bytes, right: bytes) -> float:
    dot = sum(a * b for a, b in zip(left, right))
    ll = sum(a * a for a in left)
    rr = sum(b * b for b in right)
    return dot / math.sqrt(ll * rr) if ll and rr else 0.0


def load_manifest(directory: Path) -> list[dict[str, str]]:
    lines = (directory / "manifest.tsv").read_text(encoding="utf-8").splitlines()
    keys = lines[0].split("\t")
    return [dict(zip(keys, line.split("\t"))) for line in lines[1:] if line]


def render_sheet(category: str, rows: list[dict[str, str]], source: Path, output: Path) -> None:
    card_width, card_height, columns = 560, 250, 2
    sheet_width = card_width * columns
    sheet_height = 70 + math.ceil(len(rows) / columns) * card_height
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{sheet_width}" height="{sheet_height}" viewBox="0 0 {sheet_width} {sheet_height}">',
        '<rect width="100%" height="100%" fill="#15181d"/>',
        f'<text x="24" y="42" fill="#f5f7fa" font-family="system-ui,sans-serif" font-size="28" font-weight="700">NeoCanvas · {html.escape(category)}</text>',
    ]
    for index, row in enumerate(rows):
        x = (index % columns) * card_width + 20
        y = (index // columns) * card_height + 66
        rgba = (source / row["standard"]).read_bytes()
        encoded = base64.b64encode(png_bytes(360, 116, rgba)).decode("ascii")
        parts += [
            f'<rect x="{x}" y="{y}" width="520" height="224" rx="16" fill="#242a32"/>',
            f'<image x="{x + 14}" y="{y + 14}" width="360" height="116" href="data:image/png;base64,{encoded}"/>',
            f'<text x="{x + 390}" y="{y + 42}" fill="#ffffff" font-family="system-ui,sans-serif" font-size="18" font-weight="650">{html.escape(row["name"])}</text>',
            f'<text x="{x + 390}" y="{y + 68}" fill="#9ca7b5" font-family="system-ui,sans-serif" font-size="11">{html.escape(row["id"])}</text>',
            f'<foreignObject x="{x + 14}" y="{y + 142}" width="490" height="68"><div xmlns="http://www.w3.org/1999/xhtml" style="font:14px system-ui;color:#cbd2dc;line-height:1.35">{html.escape(row["description"])}</div></foreignObject>',
        ]
    parts.append("</svg>")
    filename = category.lower().replace(" ", "-") + ".svg"
    (output / filename).write_text("\n".join(parts), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--skip-render", action="store_true")
    args = parser.parse_args()
    if not args.skip_render:
        subprocess.run([str(ROOT / "gradlew"), ":renderer:desktopTest", "--tests", "*BrushReviewArtifactTest"], cwd=ROOT, check=True)
    rows = load_manifest(args.input)
    args.output.mkdir(parents=True, exist_ok=True)
    grouped: dict[str, list[dict[str, str]]] = defaultdict(list)
    samples: dict[str, bytes] = {}
    for row in rows:
        grouped[row["category"]].append(row)
        standard = (args.input / row["standard"]).read_bytes()
        large = (args.input / row["large"]).read_bytes()
        samples[row["id"]] = alpha(standard) + alpha(large)
        png_name = Path(row["standard"]).stem + ".png"
        (args.output / png_name).write_bytes(png_bytes(360, 116, standard))
    exact: list[list[str]] = []
    flagged: list[dict[str, object]] = []
    ids = list(samples)
    for index, left_id in enumerate(ids):
        for right_id in ids[index + 1:]:
            left, right = samples[left_id], samples[right_id]
            score = cosine(left, right)
            if left == right:
                exact.append([left_id, right_id])
            if score >= 0.92:
                flagged.append({"left": left_id, "right": right_id, "cosine": round(score, 6)})
    evidence = {"brushCount": len(rows), "exactDuplicates": exact, "highSimilarity": flagged}
    (args.output / "similarity.json").write_text(json.dumps(evidence, indent=2) + "\n", encoding="utf-8")
    for category, category_rows in grouped.items():
        render_sheet(category, category_rows, args.input, args.output)
    if exact:
        raise SystemExit(f"exact duplicate previews found: {exact}")
    print(f"Rendered {len(rows)} brushes into {len(grouped)} collection sheets; {len(flagged)} high-similarity pairs flagged.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
