#!/usr/bin/env python3
"""Generate original NeoCanvas grayscale source masks using deterministic geometry."""
from __future__ import annotations
import hashlib, json, math, random, struct, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "assets/brushes/source"
SIZE = 64
HI = 128

SHAPES = [
    "round-soft", "bristle-flat", "chisel", "charcoal",
    "leaf-broad-a", "leaf-broad-b", "leaf-broad-c", "leaf-fine-a", "leaf-fine-b",
    "leaf-oak-a", "leaf-oak-b", "leaf-tropical-a", "leaf-tropical-b",
    "grass-wild-a", "grass-wild-b", "grass-meadow-a", "grass-meadow-b",
    "fern-a", "fern-b", "pine-needle-a", "pine-needle-b", "pine-bough-a", "pine-bough-b",
    "hedge-a", "hedge-b", "branch-a", "branch-b", "twig-a", "twig-b",
    "bark-fragment-a", "bark-fragment-b", "moss-a", "moss-b",
]
GRAINS = ["grain-graphite", "grain-chalk", "grain-dry-paint", "grain-canvas", "grain-paper", "grain-bark", "grain-stone", "grain-organic"]


def png_chunk(kind: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)


def write_png(path: Path, pixels: bytes) -> None:
    rows = b"".join(b"\0" + pixels[y * SIZE:(y + 1) * SIZE] for y in range(SIZE))
    data = b"\x89PNG\r\n\x1a\n" + png_chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 0, 0, 0, 0))
    data += png_chunk(b"IDAT", zlib.compress(rows, 9)) + png_chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def downsample(values: list[int]) -> bytes:
    out = bytearray(SIZE * SIZE)
    for y in range(SIZE):
        for x in range(SIZE):
            total = sum(values[(y * 2 + yy) * HI + x * 2 + xx] for yy in range(2) for xx in range(2))
            out[y * SIZE + x] = total // 4
    return bytes(out)


def mask() -> list[int]: return [0] * (HI * HI)

def put(a: list[int], x: int, y: int, v: int = 255) -> None:
    if 0 <= x < HI and 0 <= y < HI: a[y * HI + x] = max(a[y * HI + x], max(0, min(255, v)))

def disc(a: list[int], cx: float, cy: float, radius: float, value: int = 255) -> None:
    left, right = int(cx-radius-1), int(cx+radius+2)
    top, bottom = int(cy-radius-1), int(cy+radius+2)
    for y in range(top, bottom):
        for x in range(left, right):
            d = math.hypot(x + .5 - cx, y + .5 - cy)
            if d <= radius + 1:
                put(a, x, y, int(value * max(0.0, min(1.0, radius + .75 - d))))

def line(a: list[int], x1: float, y1: float, x2: float, y2: float, r1: float, r2: float | None = None, value: int = 255) -> None:
    r2 = r1 if r2 is None else r2
    steps = max(2, int(math.hypot(x2-x1, y2-y1) * 1.4))
    for i in range(steps + 1):
        t = i / steps
        disc(a, x1 + (x2-x1)*t, y1 + (y2-y1)*t, r1 + (r2-r1)*t, value)

def leaf(a: list[int], variant: int, fine: bool = False, tropical: bool = False) -> None:
    rng = random.Random(100 + variant)
    x1, y1, x2, y2 = 18, 68 + variant * 2, 111, 55 - variant * 3
    length = math.hypot(x2-x1, y2-y1); ux=(x2-x1)/length; uy=(y2-y1)/length; nx=-uy; ny=ux
    width = 15 if fine else 30
    for i in range(101):
        t=i/100; cx=x1+(x2-x1)*t; cy=y1+(y2-y1)*t
        profile=(math.sin(math.pi*t) ** (.7 if not fine else 1.2))*width
        profile *= .9 + .1*math.sin((7+variant)*math.pi*t)
        if tropical and 0.12 < t < .92:
            profile *= .72 if int(t*12)%2 else 1.0
        line(a, cx+nx*profile, cy+ny*profile, cx-nx*profile, cy-ny*profile, 1.2, value=235+rng.randrange(21))
    line(a, x1, y1, x2, y2, 2.2, 1.0, 255)

def grass(a: list[int], variant: int, meadow: bool) -> None:
    rng=random.Random(200+variant+(50 if meadow else 0)); base=108
    count=13 if meadow else 8
    for i in range(count):
        x=20+i*(88/(count-1))+rng.uniform(-3,3); height=rng.uniform(48,92 if not meadow else 72)
        bend=rng.uniform(-18,18); line(a,x,base,x+bend,base-height,3.0 if meadow else 2.1,.5,220+rng.randrange(36))
        if meadow and i%3==0: disc(a,x+bend,base-height,4.5,230)

def fern(a: list[int], variant: int) -> None:
    line(a,22,105,103,25+variant*5,2.5,1.0,245)
    for i in range(2,10):
        t=i/11; cx=22+(103-22)*t; cy=105+(25+variant*5-105)*t
        length=(1-t)*28+8; angle=math.atan2((25+variant*5)-105,103-22); nx=-math.sin(angle); ny=math.cos(angle)
        line(a,cx,cy,cx+nx*length+math.cos(angle)*7,cy+ny*length+math.sin(angle)*7,2.0,.4,230)
        line(a,cx,cy,cx-nx*length+math.cos(angle)*7,cy-ny*length+math.sin(angle)*7,2.0,.4,230)

def pine(a: list[int], variant: int, bough: bool) -> None:
    line(a,18,96,109,35+variant*5,3.0,1.2,245)
    rng=random.Random(300+variant+(20 if bough else 0))
    for i in range(4,18):
        t=i/20; cx=18+91*t; cy=96+((35+variant*5)-96)*t
        length=rng.uniform(12,28 if bough else 20)
        line(a,cx,cy,cx+rng.uniform(-8,8),cy-length,1.2,.35,220)
        line(a,cx,cy,cx+rng.uniform(-8,8),cy+length,1.2,.35,220)

def branch(a: list[int], variant: int, twig: bool) -> None:
    points=[(13,95),(38,82-variant*4),(66,68+variant*3),(91,43),(114,31+variant*4)]
    for i,(p,q) in enumerate(zip(points,points[1:])): line(a,*p,*q,5-i*.8 if not twig else 2.5-i*.35,3.8-i*.7 if not twig else 1.7-i*.3,245)
    for i,(x,y) in enumerate(points[1:-1]):
        side=-1 if (i+variant)%2 else 1; line(a,x,y,x+18,y+side*(22+i*4),2.4 if not twig else 1.3,.5,225)

def clustered(a: list[int], variant: int, moss: bool=False, hedge: bool=False) -> None:
    rng=random.Random(400+variant+(20 if moss else 0)+(40 if hedge else 0))
    count=22 if moss else (14 if hedge else 9)
    for _ in range(count):
        cx=rng.uniform(20,108); cy=rng.uniform(40 if hedge else 28,100); r=rng.uniform(5,12 if moss else 18)
        disc(a,cx,cy,r,180+rng.randrange(76))

def bark(a: list[int], variant: int) -> None:
    rng=random.Random(500+variant)
    for i in range(8):
        x=24+i*11+rng.uniform(-3,3); line(a,x,20+rng.uniform(0,10),x+rng.uniform(-8,8),108-rng.uniform(0,8),rng.uniform(2,5),rng.uniform(1,3),180+rng.randrange(76))

def make_shape(name: str) -> bytes:
    a=mask(); variant=1 if name.endswith("-b") else 2 if name.endswith("-c") else 0
    if name=="round-soft":
        for y in range(10,118):
            for x in range(10,118):
                d=math.hypot(x-64,y-64)/54; put(a,x,y,int(255*max(0,1-d)**1.8))
    elif name=="bristle-flat":
        for x in range(18,111): line(a,x,38+(x%7),x,91-(x%5),1.3,value=170+(x*17)%86)
    elif name=="chisel":
        for i in range(66): line(a,24+i,36,40+i,92,1.4,value=245)
    elif name=="charcoal":
        for i in range(34): disc(a,64+math.cos(i*2.4)*i*.8,64+math.sin(i*1.8)*i*.7,random.Random(600+i).uniform(4,14),180+(i*19)%76)
    elif name.startswith("leaf-broad"): leaf(a,variant)
    elif name.startswith("leaf-fine"): leaf(a,variant, fine=True)
    elif name.startswith("leaf-tropical"): leaf(a,variant,tropical=True)
    elif name.startswith("leaf-oak"): clustered(a,variant)
    elif name.startswith("grass-wild"): grass(a,variant,False)
    elif name.startswith("grass-meadow"): grass(a,variant,True)
    elif name.startswith("fern"): fern(a,variant)
    elif name.startswith("pine-needle"): pine(a,variant,False)
    elif name.startswith("pine-bough"): pine(a,variant,True)
    elif name.startswith("hedge"): clustered(a,variant,hedge=True)
    elif name.startswith("branch"): branch(a,variant,False)
    elif name.startswith("twig"): branch(a,variant,True)
    elif name.startswith("bark-fragment"): bark(a,variant)
    elif name.startswith("moss"): clustered(a,variant,moss=True)
    else: raise ValueError(name)
    for edge in range(4):
        for coordinate in range(HI):
            a[edge * HI + coordinate] = 0
            a[(HI - 1 - edge) * HI + coordinate] = 0
            a[coordinate * HI + edge] = 0
            a[coordinate * HI + HI - 1 - edge] = 0
    return downsample(a)

def make_grain(name: str) -> bytes:
    seed=int.from_bytes(hashlib.sha256(name.encode()).digest()[:8],"big"); rng=random.Random(seed); out=bytearray(SIZE*SIZE)
    for y in range(SIZE):
        for x in range(SIZE):
            noise=rng.random(); wave=math.sin(2*math.pi*x/SIZE*(2+(seed%5))) * math.sin(2*math.pi*y/SIZE*(3+(seed%3)))
            if name=="grain-canvas": value=150+55*math.sin(2*math.pi*x/8)**2+45*math.sin(2*math.pi*y/8)**2
            elif name=="grain-bark": value=105+110*abs(math.sin(2*math.pi*x/13 + math.sin(2*math.pi*y/SIZE)))
            elif name=="grain-paper": value=175+55*noise+20*wave
            elif name=="grain-stone": value=115+90*(.55*noise+.45*abs(wave))
            elif name=="grain-chalk": value=70+175*(noise>.34)*noise
            elif name=="grain-dry-paint": value=50+205*(noise>.48)*noise
            elif name=="grain-organic": value=90+140*(.5+.5*wave)*(.5+.5*noise)
            else: value=90+150*noise
            out[y*SIZE+x]=max(0,min(255,int(value)))
    return bytes(out)

def neomask(pixels: bytes) -> bytes:
    out=bytearray(b"NEOMASK1"+struct.pack(">HH",SIZE,SIZE)); i=0
    while i<len(pixels):
        value=pixels[i]; count=1
        while i+count<len(pixels) and pixels[i+count]==value and count<65535: count+=1
        out += struct.pack(">HB",count,value); i+=count
    return bytes(out)

def main() -> None:
    assets=[]
    for kind,names in (("shape",SHAPES),("grain",GRAINS)):
        folder="shapes" if kind=="shape" else "grains"
        for name in names:
            pixels=make_shape(name) if kind=="shape" else make_grain(name)
            path=SOURCE/folder/f"{name}.png"; write_png(path,pixels)
            assets.append({"id":name,"kind":kind,"source":f"source/{folder}/{name}.png","width":SIZE,"height":SIZE,"sha256":hashlib.sha256(neomask(pixels)).hexdigest()})
    manifest={"format":1,"assets":sorted(assets,key=lambda item:item["id"])}
    target=ROOT/"assets/brushes/manifest.json"; target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(json.dumps(manifest,indent=2)+"\n",encoding="utf-8")
    print(f"Generated {len(assets)} original brush assets")

if __name__ == "__main__": main()
