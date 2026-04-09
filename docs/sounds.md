---
description: The default sounds.yml for CrownCore and an explanation of its content.
icon: music
title: Sounds
---

# Default sounds.yml

```yaml title="sounds.yml"
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
```

* `gg-wave-started-1`: This sound is played when a GGWave stars.
* `gg-wave-started-2`: This sound is played when a GGWave stars.
* `gg-wave-message`: This sound is played when a player triggers the GGWave. The pitch increases every time up until a specific point then resets again.
* `purchase-message`: This is played when a purchase is broadcasted
* `sale-activated`: This is played when a sale was activated. 

# What does the value represent ?
> These are simple key value pairs. The key represents the internal sound name and the value consists of `mc sound namespace and name:volume:pitch`.

# Can I disable a sound ?
> You can disable a sound by simply setting its value to an empty string. Ex: `test-sound: ""`