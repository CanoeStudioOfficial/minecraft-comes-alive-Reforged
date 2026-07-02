#!/usr/bin/env python3
"""
Generate Minecraft Comes Alive 1.12.2-compatible skins from the high-version
layered skin assets.

High-version MCA stores villager visuals as separate body, face, clothing, and
hair layers. The 1.12.2 renderer in this project expects a single 64x64 PNG
path, so this script pre-composites a curated set of layered combinations and
emits an API skin manifest that the old runtime can load directly.
"""

from __future__ import annotations

import argparse
import json
import shutil
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence, Tuple

from PIL import Image


GENDERS = ("male", "female")
MAX_PER_GROUP = 30
RESOURCE_ROOT = Path("src/main/resources/assets/mca")
DEFAULT_HIGH_VERSION_ROOT = Path("old/minecraft-comes-alive-26.2/common/src/main/resources/assets/mca/skins")
LAYER_DEST = RESOURCE_ROOT / "skins/high_version/layers"
GENERATED_DEST = RESOURCE_ROOT / "skins/high_version/generated"
MANIFEST_PATH = RESOURCE_ROOT / "api/skins_high_version.json"

PROFESSION_CLOTHING = {
    "minecraft:farmer": "farmer",
    "minecraft:librarian": "librarian",
    "minecraft:priest": "cleric",
    "minecraft:smith": "toolsmith",
    "minecraft:butcher": "butcher",
    "mca:baker": "baker",
    "mca:miner": "miner",
    "mca:guard": "guard",
    "mca:bandit": "guard",
    "mca:child": "child",
    "mca:red_engineer": "mason",
    "mca:sinister_merchant": "none",
    "mca:alchemist": "cleric",
}

SKIN_TINTS = (
    0xFFE6B88A,
    0xFFD49A6A,
    0xFFB8784D,
    0xFF8D5638,
    0xFF5C3826,
)
HAIR_TINTS = (
    0xFF2E1A12,
    0xFF5B3320,
    0xFF8C5A2B,
    0xFFD6B15F,
    0xFFC8C8C8,
    0xFF101014,
)
EYE_TINTS = (
    0xFF4F7A3A,
    0xFF3A5E89,
    0xFF6D4E33,
    0xFF777777,
)


@dataclass(frozen=True)
class HairStyle:
    base: Path
    bangs: Optional[Path] = None
    back: Optional[Path] = None
    front: Optional[Path] = None
    extra: Optional[Path] = None

    def layers(self) -> Iterable[Path]:
        for layer in (self.back, self.base, self.bangs, self.front, self.extra):
            if layer is not None:
                yield layer


def numeric_pngs(path: Path) -> List[Path]:
    if not path.exists():
        return []
    return sorted(
        (p for p in path.glob("*.png") if p.stem.isdigit()),
        key=lambda p: int(p.stem),
    )


def tint_pixel(pixel: Tuple[int, int, int, int], tint: int) -> Tuple[int, int, int, int]:
    red, green, blue, alpha = pixel
    tint_alpha = (tint >> 24) & 0xFF
    if alpha == 0 or tint_alpha == 0:
        return 0, 0, 0, 0
    return (
        red * ((tint >> 16) & 0xFF) // 255,
        green * ((tint >> 8) & 0xFF) // 255,
        blue * (tint & 0xFF) // 255,
        alpha * tint_alpha // 255,
    )


def composite_layer(base: Image.Image, layer_path: Optional[Path], tint: Optional[int] = None) -> None:
    if layer_path is None:
        return
    with Image.open(layer_path).convert("RGBA") as layer:
        if layer.size != (64, 64):
            raise ValueError(f"{layer_path} is {layer.size}, expected 64x64")
        if tint is not None:
            get_pixels = getattr(layer, "get_flattened_data", layer.getdata)
            pixels = [tint_pixel(pixel, tint) for pixel in get_pixels()]
            layer.putdata(pixels)
        base.alpha_composite(layer)


def find_visible_bounds(image: Image.Image) -> Tuple[int, int, int, int]:
    alpha = image.getchannel("A")
    bounds = alpha.getbbox()
    if bounds is None:
        return 0, 0, 0, 0
    return bounds


def is_sclera(pixel: Tuple[int, int, int, int]) -> bool:
    red, green, blue, alpha = pixel
    if alpha == 1:
        return True
    if alpha != 255:
        return False
    channels = (red, green, blue)
    return min(channels) >= 160 and max(channels) - min(channels) <= 32


def composite_face(base: Image.Image, face_path: Path, eye_tint: int) -> None:
    with Image.open(face_path).convert("RGBA") as face:
        if face.size != (64, 64):
            raise ValueError(f"{face_path} is {face.size}, expected 64x64")
        min_x, _, max_x, _ = find_visible_bounds(face)
        split_x = min_x + (max_x - min_x) // 2

        sclera = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        iris = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        for y in range(64):
            for x in range(64):
                pixel = face.getpixel((x, y))
                if pixel[3] == 0:
                    continue
                if is_sclera(pixel):
                    sclera.putpixel((x, y), pixel)
                else:
                    iris.putpixel((x, y), tint_pixel(pixel, eye_tint))

        base.alpha_composite(sclera)
        base.alpha_composite(iris)


def build_legacy_hair_styles(root: Path, gender: str) -> List[HairStyle]:
    base_layers = numeric_pngs(root / "hair" / gender)
    return [HairStyle(base=layer) for layer in base_layers]


def build_layered_hair_styles(root: Path) -> List[HairStyle]:
    layered = root / "layered_hair"
    bases = numeric_pngs(layered / "base")
    bangs = numeric_pngs(layered / "bangs")
    backs = numeric_pngs(layered / "back")
    fronts = numeric_pngs(layered / "front")
    extras = numeric_pngs(layered / "extra")

    styles: List[HairStyle] = []
    for index, base in enumerate(bases):
        styles.append(
            HairStyle(
                back=backs[index % len(backs)] if backs else None,
                base=base,
                bangs=bangs[index % len(bangs)] if bangs else None,
                front=fronts[index % len(fronts)] if fronts else None,
                extra=extras[index % len(extras)] if extras and index % 3 == 0 else None,
            )
        )
    return styles


def compact_cycle(items: Sequence[Path], target: int) -> List[Path]:
    if not items:
        return []
    if len(items) >= target:
        step = max(1, len(items) // target)
        selected = list(items[::step])
        return selected[:target]
    return list(items)


def compose_one(
    body: Path,
    face: Path,
    clothing: Path,
    hair: HairStyle,
    output: Path,
    skin_tint: int,
    hair_tint: int,
    eye_tint: int,
) -> None:
    result = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    composite_layer(result, body, skin_tint)
    composite_face(result, face, eye_tint)
    composite_layer(result, clothing)
    for layer in hair.layers():
        composite_layer(result, layer, hair_tint)

    output.parent.mkdir(parents=True, exist_ok=True)
    result.save(output)
    result.close()


def resource_path(path: Path) -> str:
    relative = path.relative_to(RESOURCE_ROOT / "skins").as_posix()
    return f"mca:skins/{relative}"


def clear_directory(path: Path) -> None:
    if path.exists():
        for attempt in range(5):
            try:
                shutil.rmtree(path)
                break
            except PermissionError:
                if attempt == 4:
                    raise
                time.sleep(0.5)
    path.mkdir(parents=True, exist_ok=True)


def copy_layers(source_root: Path) -> None:
    clear_directory(LAYER_DEST)
    for child in source_root.iterdir():
        destination = LAYER_DEST / child.name
        if child.is_dir():
            shutil.copytree(child, destination)
        elif child.is_file():
            shutil.copy2(child, destination)


def generate_manifest(source_root: Path) -> List[Dict[str, object]]:
    clear_directory(GENERATED_DEST)
    manifest: List[Dict[str, object]] = []
    faces = [p for p in numeric_pngs(source_root / "face" / "normal") if p.name != "blink.png"]
    layered_styles = build_layered_hair_styles(source_root)

    for profession, clothing_profession in PROFESSION_CLOTHING.items():
        for gender in GENDERS:
            bodies = numeric_pngs(source_root / "skin" / gender)
            clothes = numeric_pngs(source_root / "clothing" / "normal" / gender / clothing_profession)
            if not clothes:
                clothes = numeric_pngs(source_root / "clothing" / "normal" / "neutral" / clothing_profession)
            hair_styles = build_legacy_hair_styles(source_root, gender) + layered_styles

            if not bodies or not faces or not clothes or not hair_styles:
                print(f"Skipping {gender} {profession}: missing one or more source layer sets")
                continue

            selected_clothes = compact_cycle(clothes, 8)
            paths: List[str] = []
            for index in range(MAX_PER_GROUP):
                body = bodies[index % len(bodies)]
                face = faces[(index * 3) % len(faces)]
                clothing = selected_clothes[index % len(selected_clothes)]
                hair = hair_styles[(index * 5 + len(profession)) % len(hair_styles)]
                output = GENERATED_DEST / gender / sanitize_profession(profession) / f"{sanitize_profession(profession)}_{index:02d}.png"

                compose_one(
                    body=body,
                    face=face,
                    clothing=clothing,
                    hair=hair,
                    output=output,
                    skin_tint=SKIN_TINTS[(index + len(gender)) % len(SKIN_TINTS)],
                    hair_tint=HAIR_TINTS[(index * 2 + len(profession)) % len(HAIR_TINTS)],
                    eye_tint=EYE_TINTS[(index + len(clothing_profession)) % len(EYE_TINTS)],
                )
                paths.append(resource_path(output))

            manifest.append(
                {
                    "gender": gender,
                    "profession": profession,
                    "paths": paths,
                }
            )

    return manifest


def sanitize_profession(profession: str) -> str:
    return profession.replace(":", "_").replace("/", "_")


def validate_manifest(manifest: Sequence[Dict[str, object]]) -> None:
    missing: List[str] = []
    for group in manifest:
        for value in group["paths"]:
            path = str(value)
            if not path.startswith("mca:skins/"):
                missing.append(path)
                continue
            local = RESOURCE_ROOT / "skins" / path.removeprefix("mca:skins/")
            if not local.exists():
                missing.append(path)
    if missing:
        raise RuntimeError(f"Manifest references missing files: {missing[:10]}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--source",
        default=str(DEFAULT_HIGH_VERSION_ROOT),
        help="Path to the high-version assets/mca/skins directory.",
    )
    parser.add_argument(
        "--skip-layer-copy",
        action="store_true",
        help="Do not refresh assets/mca/skins/high_version/layers.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    source_root = Path(args.source)
    if not source_root.exists():
        raise SystemExit(f"High-version skin source does not exist: {source_root}")

    if not args.skip_layer_copy:
        copy_layers(source_root)

    manifest = generate_manifest(source_root)
    validate_manifest(manifest)
    MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST_PATH.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")

    generated_count = sum(len(group["paths"]) for group in manifest)
    print(f"Generated {generated_count} composed skins across {len(manifest)} skin groups")
    print(f"Wrote {MANIFEST_PATH}")


if __name__ == "__main__":
    main()
