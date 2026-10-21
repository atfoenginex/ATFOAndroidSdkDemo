#!/usr/bin/env python3
"""把 README 的 SDK 版本号同步为 Maven 仓库上 com.atfo.ad:core 的最新版本。

数据源是 Nexus releases 仓库的 maven-metadata.xml：
    https://maven.cxwlad.com/repository/maven-releases/com/atfo/ad/core/maven-metadata.xml

版本号约定永远是 x.y.z（无预发布后缀），取最大的一个即可。
"""
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
README = ROOT / "README.md"

METADATA_URL = (
    "https://maven.cxwlad.com/repository/maven-releases/"
    "com/atfo/ad/core/maven-metadata.xml"
)

START, END = "<!-- SDK-VERSION:START -->", "<!-- SDK-VERSION:END -->"

VERSION_LINE = (
    "**SDK 最新版本**：`{version}`\n\n"
    '<font color="red">⚠️ 版本号最新不代表合入我们最新的功能，使用版本号前请联系我们的商务。</font>'
)


def fetch_metadata(url):
    req = urllib.request.Request(url, headers={"User-Agent": "atfo-demo-readme-sync/1.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return resp.read().decode("utf-8")


def pick_latest_stable(versions):
    """从版本列表里选出最新版本（如 "2.10.0"），选不出时返回 None。

    版本号约定为 x.y.z，不能按字符串比较（"2.10.0" < "2.9.0" 是字典序陷阱），
    要拆成整数元组再比大小。
    """
    parsed = []
    for v in versions:
        parts = v.strip().split(".")
        if len(parts) != 3 or not all(p.isdigit() for p in parts):
            continue
        parsed.append(tuple(map(int, parts)))
    if not parsed:
        return None
    return ".".join(map(str, max(parsed)))


def main():
    xml_text = fetch_metadata(METADATA_URL)
    versions = [el.text.strip() for el in ET.fromstring(xml_text).iter("version") if el.text]
    version = pick_latest_stable(versions)
    if not version:
        sys.exit("ERROR: 无法从 maven-metadata.xml 选出最新的正式版本")

    readme = README.read_text(encoding="utf-8")
    if START not in readme or END not in readme:
        sys.exit("ERROR: README 缺少 SDK-VERSION 标记块")

    block = f"{START}\n{VERSION_LINE.format(version=version)}\n{END}"
    updated = re.sub(re.escape(START) + r".*?" + re.escape(END), block, readme, flags=re.S)

    # 三方 adapter 的示例依赖也硬编码了版本，一并同步
    updated = re.sub(r"(com\.atfo\.ad\.adapt:[a-z-]+:)\d+\.\d+\.\d+", rf"\g<1>{version}", updated)

    if updated == readme:
        print(f"OK: README 已是 {version}，无需修改")
        return
    README.write_text(updated, encoding="utf-8")
    print(f"OK: README 已同步到 {version}")


if __name__ == "__main__":
    main()
