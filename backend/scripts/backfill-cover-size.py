#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
回填 media.cover_width / cover_height（v1.5 存量数据）
仅用标准库解析 JPEG/PNG 尺寸，服务器无需 PIL。

用法：
  mysql -uroot -p123456 -N -e "SELECT id, cover_image FROM media_website.media WHERE cover_image IS NOT NULL" \
    | python3 backfill-cover-size.py --root /data/media/uploads \
    | mysql -uroot -p123456 media_website

（本机 Windows 用法相同，--root 改为 D:/data/media/uploads）
"""
import argparse
import struct
import sys
from pathlib import Path


def jpeg_size(data: bytes):
    """扫 SOF marker 读宽高"""
    i = 2
    n = len(data)
    while i + 9 < n:
        if data[i] != 0xFF:
            i += 1
            continue
        marker = data[i + 1]
        if marker in (0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF):
            h, w = struct.unpack(">HH", data[i + 5:i + 9])
            return w, h
        if marker in (0xD8, 0x01) or 0xD0 <= marker <= 0xD7:
            i += 2
            continue
        seg_len = struct.unpack(">H", data[i + 2:i + 4])[0]
        i += 2 + seg_len
    return None


def png_size(data: bytes):
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    w, h = struct.unpack(">II", data[16:24])
    return w, h


def image_size(path: Path):
    try:
        with open(path, "rb") as f:
            head = f.read(32)
            if head[:2] == b"\xff\xd8":
                return jpeg_size(head + f.read(65536))
            if head[:8] == b"\x89PNG\r\n\x1a\n":
                return png_size(head)
    except OSError:
        return None
    return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", required=True, help="上传根目录，如 /data/media/uploads")
    args = ap.parse_args()
    root = Path(args.root)

    count = 0
    for line in sys.stdin:
        line = line.strip()
        if not line or "\t" not in line:
            continue
        mid, cover = line.split("\t", 1)
        rel = cover.replace("/uploads/", "", 1).lstrip("/")
        size = image_size(root / rel)
        if size and size[0] and size[1]:
            print(f"UPDATE media SET cover_width={size[0]}, cover_height={size[1]} WHERE id={mid};")
            count += 1
    sys.stderr.write(f"backfilled {count} rows\n")


if __name__ == "__main__":
    main()
