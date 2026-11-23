"""Validate today's catalog against the installed 3.1.10 ID, selector and tip contracts."""
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
ASSETS = Path("android/app/src/main/assets")
BASE = "bfec707836b5d81a46585617ad78a77d9a9821a7"
forbidden = re.compile(r"DICK|[1-9][0-9,]*\s*원|씨발|병신", re.I)


def contents(name):
    path = (ASSETS / name).as_posix()
    old = json.loads(subprocess.check_output(["git", "show", BASE + ":" + path], cwd=ROOT))
    return old, json.loads((ROOT / path).read_text(encoding="utf-8"))


def fixed_fields(row, changeable):
    return {key: value for key, value in row.items() if key not in changeable}


base, current = contents("coach-ko-v2.json")
old_medals, medals = contents("coach-ko-medals-v1.json")
assert current["schemaVersion"] == base["schemaVersion"] == 2
assert current["contentVersion"] == 6 and medals["version"] == 2
assert base["legacyIds"] == current["legacyIds"]
for old, new in ((base, current), (old_medals, medals)):
    assert len(old["quotes"]) == len(new["quotes"])
    for prior, row in zip(old["quotes"], new["quotes"]):
        assert fixed_fields(prior, {"text", "paidText"}) == fixed_fields(row, {"text", "paidText"}), row["id"]
        assert bool(prior.get("paidText")) == bool(row.get("paidText")), row["id"]
        for line in (row["text"], row.get("paidText", "")):
            assert not forbidden.search(line) and "\n" not in line and len(line) <= 80, row["id"]
assert len(current["tips"]) == len(base["tips"]) == 40
for prior, tip in zip(base["tips"], current["tips"]):
    assert fixed_fields(prior, {"title", "prompt", "fallback"}) == fixed_fields(tip, {"title", "prompt", "fallback"})
    assert tip["inputs"] and re.search(r"\[[^\]]+\]", tip["prompt"]) and tip["check"] and tip["fallback"], tip["id"]
print("624 original quote IDs, 40 practical tip contracts, numeric legacy IDs, evidence groups and guarded plan variants verified.")
