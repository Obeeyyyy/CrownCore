---
title: Messages
description: Default messages.yml file for the CrownCore and their explanation.
icon: file-lines
---

# Default messages.yml

```yaml
prefix: "&6&lCROWNCORE &8●&f"
white: "&f"
accent: "&e"

messages:
  no-permission: "%prefix% You are not allowed to do that! &8(%white%%permission%&8)"
  command-syntax: "%prefix% Usage for %accent%%command%%white%:"
  command-syntax-prefix: "&8  ➥ &7"
  command-cooldown: "%prefix% Please wait %accent%%remaining%%white% before executing another command."
  message-cooldown: "%prefix% Please wait %accent%%remaining%%white% before sending another message."
  invalid-number: "%prefix% Please enter a valid number."
  invalid-input: "%prefix% Invalid input. Follow this format %format%."
  invalid-name: "%prefix% Please enter a name with less than 10 letters and no spaces."
  invalid-license: "%prefix% Your license for %accent%%plugin%%white% is invalid."
  no-item-in-hand: "%prefix% You need to hold an item in your hand."
  plugin-reloaded: "%prefix% [%plugin%] You have reloaded config and messages."
  messages-reloaded: "%prefix% [%plugin%] You have reloaded the messages."
  config-reloaded: "%prefix% [%plugin%] You have reloaded the config."
  player-invalid: "%prefix% %accent%%name%%white% never played on the server!"
  player-offline: "%prefix% %accent%%name%%white% is not online!"
  location-set: "%prefix% You have set the location named %accent%%name%%white%!"
  location-deleted: "%prefix% You have deleted the location named %accent%%name%%white%!"
  location-invalid: "%prefix% There is no location named %accent%%name%%white%!"
  teleportation-message: '%prefix% &fTeleporting in %accent%&o%remaining%%white%!'
  teleported-message: '%prefix% &fTeleportation complete.'
  teleportation-cancelled: '%prefix% &c&oTeleportation cancelled.'
  not-enough-money: "%prefix% You dont have enough money, you are missing %accent%-$%missing%%white%."
```

# What these values mean

## prefix

* `prefix`: This is the prefix that is used in all our plugins with `%prefix%` in the message files and `%cc_prefix%` using [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

## white

* `white`: This is the default color that is used in all our plugins with `%white%` in the message files and `%cc_white%` using [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

## accent

* `accent`: This is the accent color that is used in all our plugins with `%accent%` in the message files and `%cc_accent%` using [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

{% hint style="info" %}
You can configure plugin specific values for what is mentioned above by simply adding them into the plugins `messages.yml` and using `%pluginname_key%` instead of `%key%`. `Example:` `%crownkits_prefix%` will use the `prefix` defined in the `messages.yml` from `CrownKits` instead of the Cores.
{% endhint %}

# Messages

> These are simple key value pairs that are global, meaning used in all the crown plugins.

* `no-permission`: This message is sent when a player does not have permission for an action. The needed permission can be displayed using `%permission%`.
* `command-syntax`: This is sent when the used command is missing arguments. This will be the first line and the used command can be displayed using `%command%`.
* `command-syntax-prefix`: Following the above. The predefined options of arguments will be printed, prefixed by this.
* `command-cooldown`: Sent when the player is not allowed to use another command yet. `%remaining%` to display the remaining time.
* `message-cooldown`: Sent when the player is not allowed to send another message yet. `%remaining%` to display the remaining time.
* `invalid-number`: Sent when an invalid number was input.
* `invalid-input`: Sent when the input is invalid. `%format%` to show the correct way.
* `invalid-name`: I don't remember this. need to update later. TODO
* `invalid-license`: This will be removed.
* `no-item-in-hand`: Sent when a player is required to hold an item for the action but does not.
* `plugin-reloaded`: The plugin reload message. `%plugin%` for the plugin name.
* `messages-reloaded`: The messages reload message. `%plugin%` for the plugin name.
* `config-reloaded`: The config reload message. `%plugin%` for the plugin name.
* `player-invalid`: When the input is supposed to represent a player but no player with the provided name was found. `%name%` will display the input.
* `player-offline`: When the input is supposed to represent an online player but no online player with the provided name was found. `%name%` will display the input.
* `location-set`: Sent when a location is set. `%name%` for the location name.
* `location-deleted`: Sent when a location is deleted. `%name%` for the location name.
* `location-invalid`: Sent when the provided location name does not match with any existing locaton. `%name%` for the input.
* `teleportation-message`: The message that is shown while teleporting. Only shown when animation is enabled. `%remaining%` displays the remaning time using the configured format.
* `teleported-message`: Sent after succesfully teleported.
* `teleportation-cancelled`: Sent when the teleportation was cancelled (by moving for example).
* `not-enough-money`: Sent whenever a player does not have enough money for an action. `%missing%` can be used to display the missing amount.

# Multi Line Messages

> There are no multi line messages for the CrownCore but these are key and [Array](../guides/yaml/#array) pairs.
