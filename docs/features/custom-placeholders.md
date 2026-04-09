---
title: Custom Placeholders
description: Explanation of the custom placeholder feature of the CrownCore.
icon: percent
---

# Custom Placeholders

***

## What are custom placeholders and how to they work ?

> During my time working on servers, I realised that having a way to quickly create a placeholder for PlaceholderAPI that holds a static value can be very useful.

### Example use cases:

* **A Discord invite URL**: Your Discord link is most likely displayed in multiple locations. Now imagine your invite link changes, now you would have to update it in every single location. Very time consuming and prone to errors.
* **A store URL**: Same scenario as above.
* **Monthly crates**: If you have seasonal crates, you probably display them on your server using holograms, NPCs and more. Each time you change the Crate, you would have to update each and every singl instance again.

The solution to this problem is simple. **Placeholders**. You just define your placeholder in the `placeholders.yml` file and display it in game using `%cc_<placeholder name>%`

`Example`: I have a placeholder called `discord`. To display its value in game, I use `%cc_discord%`.

#### The placeholders.yml

> This file holds your custom placeholders represented as key value pairs.

```yml
placeholders:
  discord: "https://discord.gg/crowndev"
  store: "https://store.my-store.com"

  monthly-crate-display: "Your fancy monthly crate display"
  monthly-crate-color: "#ffaaaa"
  monthly-crate-text: "Only this eastern the %cc_monthly-crate-display% Crate."
```

Now you can simply call these static values in-game by using a placeholder.

#### Example

`%cc_discord%` will return the value `https://discord.gg/crowndev`.

