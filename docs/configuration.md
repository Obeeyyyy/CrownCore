---
description: Default config.yml file for the CrownCore and their explanation.
title: Configuration
icon: file-lines
---

# Default config.yml

```yaml title="config.yml"
#
# if you have any questions or suggestions, please do not hesitate and
# join our discord: https://discord.com/invite/bJQRV5GeZg
# we are there to assist you
#

update-reminder: true # toggle the update reminder when joining (only players with permission see the reminder)
debug-mode: false # toggle this to enable in depth debug output

offline-mode: false # toggle this if your server is cracked

data-cache-time: 3600000 # the time player data stays cached after a player left in ms

teleport:
  delay: 5 # this is the teleport delay in s
  message-type: bossbar # message type: bossbar, actionbar
  particles: true # toggle the particles when teleporting

instant-teleport:
  always: false # set this to true if you don't want to use the teleport animation, also players in gmc will always be teleported without the animation
  # players in these worlds will be teleported without a delay
  worlds: []
  # players in these regions will be teleported without a delay
  regions: []

cooldown:
  message: 0 # cooldown before being able to send another chat message in milliseconds
  command: 0 # cooldown before being able to send another command in milliseconds

number-format:
  locale: "en_US" # set the language default for number formatting. EX: en-US -> 10,000.00
  default-format: "#,###.##" # the default number format
  use-short-format: false # toggle this to use the short format (1k, 1m etc)

time-formats:
  default: "%hh%:%mm%:%ss%" # the default format to use when nothing else is specified
  teleportation: "%ss%.%t%" # the format for the teleportation messages
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