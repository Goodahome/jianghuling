# -*- coding: utf-8 -*-
import json
import urllib.request

def get(path):
    with urllib.request.urlopen("http://localhost:8080/api/v1" + path) as r:
        return json.load(r)

lines = []
d = get("/notices?page=1&pageSize=20")
for x in d["data"]["list"]:
    c = x.get("content") or ""
    lines.append(
        f"id={x['id']} cat={x['category']} title={x['title']} len={len(c)} pinned={x.get('pinned')}"
    )
    lines.append("  " + c[:80].replace("\n", " "))

# compare expected N1-N3 markers from standard-notices
markers = {
    "RULES": "张贴悬赏须按令状填写",
    "ANTI_FRAUD": "线下看房选白天与公共场所",
    "ZUNYI_RENT": "本平台首发范围为遵义单城试点",
}
lines.append("--- full-body markers ---")
by_cat = {x["category"]: x for x in d["data"]["list"]}
for cat, marker in markers.items():
    body = (by_cat.get(cat) or {}).get("content") or ""
    lines.append(f"{cat}: has_full_marker={marker in body} len={len(body)}")

b = get("/bounties?page=1&pageSize=10&status=OPEN,IN_COLLAB")
lines.append("--- plaza ---")
for x in (b["data"].get("list") or [])[:8]:
    lines.append(
        f"id={x.get('id')} type={x.get('type')} typeDisplayName={x.get('typeDisplayName')} status={x.get('status')}"
    )

wt = get("/meta/warrant-templates")
lines.append("--- warrant-templates ---")
for t in wt["data"]:
    lines.append(
        f"code={t.get('code') or t.get('type')} name={t.get('name')} displayName={t.get('displayName')}"
    )

out = "\n".join(lines)
open(r"f:\Jinanghu_Ling\docs\_qa_run\v189_probe_utf8.txt", "w", encoding="utf-8").write(out)
print(out)
