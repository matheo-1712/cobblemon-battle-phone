"""Validate a loader artifact before publishing it to any destination."""

import json
import sys
import tomllib
from zipfile import ZipFile

MOD_ID = "cobblemon_battlephone"


def verify(path, version, loader="fabric"):
    with ZipFile(path) as jar:
        names = jar.namelist()
        if len(names) != len(set(names)):
            raise ValueError("Duplicate entries in the release jar")
        if loader == "fabric":
            metadata = json.loads(jar.read("fabric.mod.json"))
            mod_id, mod_version = metadata["id"], metadata["version"]
            for side in ("main", "client"):
                for entry in metadata["entrypoints"][side]:
                    name = entry["value"] if isinstance(entry, dict) else entry
                    jar.getinfo(name.replace(".", "/") + ".class")
            if "META-INF/neoforge.mods.toml" in names:
                raise ValueError("NeoForge metadata in Fabric jar")
        elif loader == "neoforge":
            metadata = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode())
            mod_id, mod_version = metadata["mods"][0]["modId"], metadata["mods"][0]["version"]
            for name in ("BattlePhoneNeoForge", "BattlePhoneNeoForgeClient"):
                jar.getinfo(f"matheo1712/battlephone/neoforge/{name}.class")
            if "fabric.mod.json" in names or any("/fabric/" in name and name.endswith(".class") for name in names):
                raise ValueError("Fabric content in NeoForge jar")
        else:
            raise ValueError("Unknown loader: " + loader)
        if mod_id != MOD_ID or mod_version != version:
            raise ValueError(f"Release jar is {mod_id} {mod_version}, expected {MOD_ID} {version}")
        # The phones keep the IDs they had inside Cobblemon Trainers, so worlds that already hold
        # some find them again: losing these files would turn every phone into a missing item.
        jar.getinfo("assets/cobblemon-trainers/models/item/battle_phone.json")
        jar.getinfo("data/cobblemon-trainers/recipe/battle_phone.json")
        jar.getinfo("assets/cobblemon_battlephone/lang/en_us.json")
    print(f"Verified {loader} release {version}: {path}")


if __name__ == "__main__":
    if len(sys.argv) not in (3, 4):
        raise SystemExit("Usage: verify-release.py JAR VERSION [fabric|neoforge]")
    verify(*sys.argv[1:])
