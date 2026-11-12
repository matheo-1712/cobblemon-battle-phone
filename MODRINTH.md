<p align="center">
  <strong>Every trainer of your world in your pocket - who is left to beat, what they pay, and a button to call them over.</strong>
</p>

<p align="center">
  <sub><strong>Compatible with</strong> · Minecraft 1.21.1 · Fabric - Neoforge · Cobblemon 1.8 · Cobblemon Trainers 2.0.0+ · Java 21 · needed on client <em>and</em> server</sub>
</p>

> **This mod is an add-on for [Cobblemon Trainers](https://modrinth.com/mod/cobblemon-trainers-rerebleue)
> and does nothing on its own.** The trainers, their victories, their locks and their rewards all
> come from there. Cobblemon BattlePhone only brings the item, its screen and what feeds it.

## What it is

Up to version 1.x, the Battle Phone shipped inside Cobblemon Trainers. **Since Cobblemon
Trainers 2.0.0 it is its own mod**, so a server can run trainers without the phone, and the
phone can grow without touching the trainers.

`/cobblemontrainers list` is for operators. The **Battle Phone** is the same board for everyone
else: right-click it and every trainer of the world is there, with their skin, their rewards,
and whether you have beaten them. And for the trainers who said where they would be, it is
also **how you find them** - Cobblemon Trainers never spawns a trainer on its own, so the phone
is the player's way to make one show up.

---

## What you get

- **Every trainer, one board per player** - sorted by datapack, then by category.
- **A full page per trainer** - level, team size, state, and what is still missing to face them.
- **Rewards before the fight** - what beating them pays, once or every time.
- **Teams unlocked by victory** - six closed slots that open on the real models.
- **Calling a trainer** - a **Call** button when you stand where they answer.
- **Their battle theme** - replay the music of a trainer you have beaten.
- **Seven colours** - pick yours with the apricorn in the recipe.
- **Old phones kept** - same item IDs as in Cobblemon Trainers 1.x.

---

## In detail

**Every trainer, one board per player.** Trainers are **sorted by datapack**: the arrows at the
top of the list - or the left and right keys - move from one pack to the next, with an "All
datapacks" tab first as soon as there are several. Inside a pack, one heading per **category**,
the folder the pack files its trainers in, each with its own counter.

**A full page per trainer.** Click a line and the trainer opens on the right: level, team size,
and their state - defeated, defeated with no rematch, still standing, or not accepting
challenges. A locked trainer shows the list of what is still missing in place of their team:
a badge, an advancement, eight victories, a Pokémon in your party.

**Rewards before the fight.** Next to the trainer, what beating them pays: one slot per item
with its count, and past four, the last slot counts the rest and names them on hover. A reward
paid on the first win only carries a marker, and greys out once claimed. A pack can keep a
reward secret until it is won.

**Teams unlocked by victory.** Six team slots stay closed until you beat the trainer, then show
their Pokémon as real models - forms and shinies included. Nothing about a team is sent before
that: the phone never spoils a fight.

**Calling a trainer.** A trainer that declares **where they are** - a biome, a structure, an
area, a time, a weather - shows that place on their page, with a **Call** button next to it.
Stand there and press it: they arrive a few dozen blocks away. Somewhere else, the refusal names
exactly what is missing. A trainer that declares nothing has no button and is found on foot.

**Their battle theme.** Once a trainer is beaten, their page gets a music button: it plays the
theme of their battle, and stops as soon as you leave their page.

**Nothing to lose.** The phone remembers nothing itself: it reads the progress saved by the
server. Two copies show the same thing, and losing one loses nothing.

---

## The recipe

Four iron ingots in the corners, four copper ingots on the sides, and **any apricorn** in the
centre. Its colour gives the phone's: black, blue, green, pink, red, white or yellow. Every
colour opens the same trainers; the frame and the screen colours follow the phone's.

<p align="center"><img src="https://raw.githubusercontent.com/matheo-1712/cobblemon-trainers/refs/heads/master/assets/battle_phone_craft.png" alt="Battle Phone recipe"></p>

It is also in the **Cobblemon Trainers** creative tab, or with:

```
/give @s cobblemon-trainers:battle_phone
```

For another colour: `battle_phone_black`, `battle_phone_green`, `battle_phone_pink`,
`battle_phone_red`, `battle_phone_white` or `battle_phone_yellow`, still after
`cobblemon-trainers:`.

---

## Updating from Cobblemon Trainers 1.x

The phones keep the IDs they had inside Cobblemon Trainers (`cobblemon-trainers:battle_phone`
and its colours), so a world that already has some finds them again.

Install Cobblemon BattlePhone **at the same time** as Cobblemon Trainers 2.0.0, **before** you
open the world: without it Minecraft no longer knows the item, and phones already in
inventories are lost. Trainer progress is not affected either way - it is saved by Cobblemon
Trainers, not by the phone.

Do not run it next to a Cobblemon Trainers **1.x**: both would register the same phones and
the game would not start. That is why it asks for 2.0.0 or later.

---

## For pack authors

The phone shows what your trainers declare, nothing more - there is nothing to configure here.

- `"listed": false` keeps a trainer off the phone.
- A locked trainer can stay hidden until its conditions are met.
- `"hideAfterDefeat": true` takes a trainer off the phone once you have beaten them.
- A reward with `"hidden": true` stays secret until it is won.
- A `location` block is what gives a trainer a **Call** button.

Everything is in the Cobblemon Trainers docs:
[progress tracking](https://github.com/matheo-1712/cobblemon-trainers/blob/master/docs/en/DATAPACK.md#progress-tracking)
and [calling a trainer](https://github.com/matheo-1712/cobblemon-trainers/blob/master/docs/en/SPAWNING.md).
Or build your pack in the [online editor](https://matheo-1712.github.io/cobblemon-trainers/).

---

## Requirements

| | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Loader | Fabric Loader 0.19.5+, or NeoForge 21.1.251+ |
| Java | 21 |
| Cobblemon | 1.8 |
| [Cobblemon Trainers](https://modrinth.com/mod/cobblemon-trainers-rerebleue) | 2.0.0 or later |
| Fabric API, Fabric Language Kotlin | with Fabric |
| Kotlin for Forge | with NeoForge |

**Needed on both sides in multiplayer**: the screen only exists on a client that has the mod.

---

## Credits

Developed by **Mathéo** ([matheo-1712](https://github.com/matheo-1712)).

## Usage terms

- **Modpacks: yes.** You are free to include this mod in any modpack, public or private,
  free or otherwise. No permission needed, no message required.
- **Servers: yes.** Run it on any server you like.
- **Reuploading: no.** Do not republish the mod on Modrinth, CurseForge, or any other
  distribution site, mirror, or ad-wrapped download page. Link to this page instead - that way
  everyone gets the real file, the right version, and the changelog with it.

## Links

- **Cobblemon Trainers:** [on Modrinth](https://modrinth.com/mod/cobblemon-trainers-rerebleue) -
  the mod this one is built on
- **Trainer docs:** [the wiki](https://github.com/matheo-1712/cobblemon-trainers/blob/master/docs/en/README.md)
- **Issues:** [github.com/matheo-1712/cobblemon-trainers/issues](https://github.com/matheo-1712/cobblemon-trainers/issues)

---

Found a bug, or a trainer missing from the phone? Open an issue on GitHub with the trainer's
JSON and the server log.
