---
title: Sounds
description: The default sounds.yml for CrownCore and an explanation of its content.
icon: music
---

# Sounds

## Default sounds.yml

```yaml
sounds:
  no-permission: "minecraft:entity.generic.explode:0.2:1.0"
  player-invalid: "minecraft:entity.generic.explode:0.2:1.0"
  player-offline: "minecraft:entity.generic.explode:0.2:1.0"
  message-cooldown: "minecraft:entity.villager.no:0.2:1.0"
  command-cooldown: "minecraft:entity.villager.no:0.2:1.0"
  location-invalid: "minecraft:entity.generic.explode:0.2:1.0"
  teleport-instant-1: "minecraft:entity.splash_potion.break:0.5:3.0"
  teleport-instant-2: "minecraft:entity.shulker.teleport:0.5:3.0"
  teleport-cancelled: "minecraft:entity.item.break:0.5:3.0"
  teleport-tick: "minecraft:block.note_block.banjo:0.5:0.6"
  not-enough-money: "minecraft:entity.generic.explode:0.2:1.0"
```

* `no-permission`: Used when a player gets the no permission message.
* `player-invalid`: Used when the provided player input is invalid.
* `player-offline`: Used when the proivided player is not online.
* `message-cooldown`: Used when the player is chatting too fast.
* `command-cooldown`: Used when the player is using commands too fast.
* `location-invalid`: Used when the location used does not exist. (For the internal location system)
* `teleport-instant-1`: Used when instantly teleported. (For the internal teleport system)
* `teleport-instant-2`: Used when instantly teleported. (For the internal teleport system)
* `teleport-cancelled`: Used when the teleportation is cancelled. (By movement for ex.)
* `teleport-tick`: Used for every tick during teleportation.
* `not-enough-money`: Used when the player does not have enough money.

## What does the value represent ?

> These are simple key value pairs. The key represents the internal sound name and the value consists of `mc sound namespace and name:volume:pitch`.

## Can I disable a sound ?

> You can disable a sound by simply setting its value to an empty string. Ex: `test-sound: ""`
