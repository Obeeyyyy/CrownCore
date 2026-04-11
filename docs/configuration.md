---
description: Default config.yml file for the CrownCore and their explanation.
title: Configuration
icon: file-lines
---

# Default config.yml

```yaml title="config.yml"
update-reminder: true
debug-mode: false

offline-mode: false

data-cache-time: 3600000

teleport:
  delay: 5              
  message-type: bossbar  

instant-teleport:
  always: false      
  worlds: []        
  regions: []        

cooldown:
  message: 0      
  command: 0     

number-formatting: "en_US"

time-formats:
  default: "%hh%:%mm%:%ss%"  
  teleportation: "%ss%.%t%" 
```

# What these values mean

## update-reminder
* `update-reminder` (G*): Either `true` or `false`. Toggles the update reminder for admins.

## debug-mode
* `debug-mode`: Either `true` or `false`. Toggles the debug mode for more console output. Can be toggled via command `/crowncore debug`.

## offline-mode
* `offline-mode`: Either `true` or `false`. Changes internal functions like UUID fetching.

## data-cache-time
* `data-cache-time` (G*): The time in ms player data will be kept in cache after leaving. This will probably be removed soon.

## teleport
* `teleport` (G*)
    * `delay`: Is the duration for the teleportation in seconds.
    * `message-type`: Options are: `bossbar` and `actionbar`. The place the teleport message with the remaining time will be shown.

## instant-teleport
* `instant-teleport` (G*)
    * `always`: Either `true` or `false`. With this enabled, all teleportations will always be instant, no animation.
    * `worlds`: A [YAML Array](../../guides/yaml#array). Teleportations from worlds listed here will be instant.
    * `regions`: A [YAML Array](../../guides/yaml#array). Teleportations from [WorldGuard](https://dev.bukkit.org/projects/worldguard) Regions listed here will be instant.

## cooldown
* `cooldown`
    * `message`: An optional setting regulating the cooldown until players can send another chat message. This value is represented in milliseconds.
    * `command`: An optional setting regulating the cooldown until players can send another command. This value is represented in milliseconds.

## number-formatting
* `number-formating` (G*): This option defines what local to use for number formating across our plugins. Example: `en_US -> 10,000.0` and `de_DE -> 10.000,0`.

## time-formats
* `time-formats` (G*)
    * `default`: This is the default time format the core will use if nothing else is specified.
    * `teleportation`: This time format will be used in the teleportation messages.

`Most Plugins have a seperate time format configuration in their own configuration files.`

> The doubles will basicly always show a minimum of 2 numbers. `Example:` `%dd%` with a value of 2 will show `02` where `%d%` would show the raw vlaue.

> **Available aliases**:
* `%dd% %d%`: Days
* `%hh% %h%`: Hours
* `%mm% %m%`: Minutes
* `%ss% %s%`: Seconds
* `%SSS% %S%`: Milliseconds
* `%tt% %t%`: Remaining Decimals. `Example:` `%ss%.%t%s` for a value of `12900 ms` would show `12.9s`.

### Legend

> G* = A setting that is applied to all plugins using the core.