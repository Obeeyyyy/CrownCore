# GUI System

CrownCore includes a highly customizable, configuration-driven GUI (Graphical User Interface) system. Plugins built on top of CrownCore can easily define menus using YAML files, which are parsed and rendered dynamically.

---

## Commands and Permissions

The GUI system provides a command `/crowngui` for administrators to manage and test menus.

* **Permission**: `core.command.gui`
* **Subcommands**:
  * `/crowngui list`: Lists all loaded GUI keys registered in the system.
  * `/crowngui open <gui-key> [player]`: Opens the specified menu for yourself or another online player.
  * `/crowngui reload`: Reloads all GUI configurations across all registered Crown plugins.

---

## GUI File Configuration

GUIs are loaded from the `gui` directory within each plugin's data folder. Each `.yml` file in that folder represents a GUI. The filename (without the `.yml` extension) acts as the GUI ID. The full key of the GUI is `<plugin-name>:<gui-id>` (e.g., `CrownCore:example`).

### Configuration Options

| Option | Type | Description |
| :--- | :--- | :--- |
| `title` | String | The title of the menu (supports MiniMessage color tags and placeholders). |
| `size` | Integer | The size of the inventory (must be a multiple of 9, between 9 and 54). |
| `cache` | Boolean | If `true`, a single inventory instance is created once and shared globally. |
| `cache-per-player` | Boolean | If `true`, a separate inventory instance is cached and maintained for each individual player. |
| `default-flags` | List of Strings | Default ItemFlags applied to all items in this GUI unless overridden by the item. |
| `dynamic-slots` | Section | Named lists of slot indices reserved for dynamically populated contents. |
| `open-sound` | String | Sound played when opening, formatted as `SOUND:volume:pitch` (e.g. `BLOCK_CHEST_OPEN:1.0:1.0`). |
| `close-sound` | String | Sound played when closing, formatted as `SOUND:volume:pitch` (e.g. `BLOCK_CHEST_CLOSE:1.0:1.0`). |
| `fill` | Section | Configuration to automatically fill empty slots in the menu. |
| `items` | Section | Map of items to display in the menu. |

---

## Automatic Resource Extraction

To make updates seamless, the GUI system scans the plugin jar's `gui/` directory on every startup/load:
* **Extraction**: Any GUI configurations packaged inside the jar that do not exist on disk in the plugin's `gui/` data folder are automatically copied over.
* **Preserving Modifications**: Existing files are **never** overwritten, preserving any modifications made by administrators on disk.
* **Logging**: A message is printed to the console (`Extracting new GUI config: <name> for <plugin>`) only when a new file is actually copied to disk.

---

## Caching GUIs

### Global Cache (`cache: true`)
A single inventory instance is created and shared globally across all players. Useful for static selectors or server selectors where content is identical for all players.

### Player Cache (`cache-per-player: true`)
A separate inventory instance is created and cached for each player. 
* **Dynamic Placeholders**: Player-specific placeholder values (e.g. current player coins, levels) are resolved individually for each player and cached under their UUID.
* **Auto Invalidation**: To prevent memory leaks, a quit listener automatically removes the player's cached inventories when they disconnect.

### Querying Cached Views

To interact with cached inventories programmatically from other plugins, `CrownGuiService` provides several query methods:

#### Get a Player's Cached Inventory
```java
Inventory cachedInv = CrownGuiService.getPlayerCachedInventory("CrownCore:example", player);
```

#### Get Global Cached Inventory
```java
Inventory globalCachedInv = CrownGuiService.getCachedInventory("CrownCore:example");
```

#### Get All Cached Inventories
```java
Collection<Inventory> activeViews = CrownGuiService.getCachedInventories("CrownCore:example");
```

---

## Named Dynamic Slots (`dynamic-slots`)

The `dynamic-slots` configuration block allows admins to define lists of slots under distinct custom names, which developers can fetch at runtime to populate dynamic content (e.g., active coinflip games or items).

### YAML Configuration

```yaml
title: "&eActive Coinflips"
size: 27
dynamic-slots:
  coinflips: [10, 11, 12, 13, 14, 15, 16]
  other_stats: [18, 19, 20]
```

### Java API Usage

```java
import de.obey.crown.core.gui.CrownGuiService;
import java.util.List;

// Fetch the list of slots under the "coinflips" key
List<Integer> slots = CrownGuiService.getDynamicSlots("CrownCore:coinflip_menu", "coinflips");

int index = 0;
for (CoinflipGame game : activeGames) {
    if (index >= slots.size()) break;
    int slot = slots.get(index);
    inventory.setItem(slot, createGameItem(game));
    index++;
}
```

---

## Re-Adding Sequential Items (`reAddItems`)

If you programmatically place items into dynamic slots in a cached inventory, the layout of `add: true` items might need to be recalculated (so they fill the newly remaining empty slots instead of overlapping with your dynamic items).

Call `CrownGuiService.reAddItems(inventory)` to automatically clear and re-add all `add: true` items:

```java
// 1. Programmatically place dynamic items (e.g. coinflips)
for (int slot : slots) {
    inventory.setItem(slot, gameItem);
}

// 2. Re-calculate and re-add sequential items into the remaining empty slots
CrownGuiService.reAddItems(inventory);
```

---

## Item Configuration

Items in the `items` section are defined with their config name and support the following properties:

| Option | Type | Description |
| :--- | :--- | :--- |
| `slot` | Integer | The slot index in the inventory (0-indexed). Mutually exclusive with `slots` and `add`. |
| `slots` | List of Integers | A list of slot indices to place the item in multiple positions. Mutually exclusive with `slot` and `add`. |
| `add` | Boolean | If `true`, the item is added to the first available empty slot sequentially. Mutually exclusive with `slot` and `slots`. |
| `material` | String | The Bukkit Material name (e.g., `DIAMOND`, `PLAYER_HEAD`). |
| `name` | String | Custom display name of the item. |
| `lore` | List of Strings | Lore lines for the item. |
| `amount` | Integer | Amount of the item (defaults to 1). |
| `glow` | Boolean | If `true`, makes the item look enchanted. |
| `custom-model-data` | Integer | Custom model data value for resource packs. |
| `texture` | String | Base64 texture value (only applies to `PLAYER_HEAD`). |
| `owner` | String | Player name/uuid for head skull (only applies to `PLAYER_HEAD`). |
| `permission` | String | Bungee/Spigot permission required to view the item. |
| `enchantments` | Section | List of enchantments and their levels. |
| `flags` | List of Strings | Item flags (e.g. `HIDE_ENCHANTS`, `HIDE_ATTRIBUTES`). Overrides `default-flags`. |
| `click` | Section | Click action configuration. |
| `action` | String | Key of a registered custom action handler. |

---

## Custom Action System

In addition to built-in click actions, you can register custom action handlers in Java and link them to GUI items using the `action` configuration key.

### Java API

To register an action, use `GuiActionRegistry.register`:

```java
import de.obey.crown.core.gui.GuiActionRegistry;

GuiActionRegistry.register("create-cf", (player, item, event) -> {
    player.sendMessage("Creating Coinflip Game...");
    // custom game creation code
});
```

* **Handler signature**: `(Player player, GuiItem item, InventoryClickEvent event)`. You receive the player, the clicked item object, and the raw click event.

### YAML Configuration

```yaml
items:
  create_coinflip:
    slot: 11
    material: GOLD_INGOT
    name: "&aCreate Coinflip"
    action: "create-cf" # <--- Registered key
```

---

## Click Actions

Each item can define an action under the `click` key to run logic when clicked:

```yaml
click:
  type: COMMAND
  value: "/spawn"
  close: true
```

### Click Action Options

* **`type`**: The action type. Supported types:
  * `NONE`: No action is performed.
  * `CLOSE`: Closes the inventory view.
  * `OPEN_GUI`: Opens another GUI. Specify the full key of the GUI in `value` (e.g. `CrownCore:settings`).
  * `COMMAND`: Runs a command as the player. Placeholders in `value` are resolved. Any leading `/` is automatically stripped.
  * `CONSOLE_COMMAND`: Runs a command as the console. Placeholders in `value` are resolved. Any leading `/` is automatically stripped.
* **`value`**: The string argument passed to the action (e.g., command string or GUI key).
* **`close`**: A boolean flag. If `true`, closes the active menu immediately when clicked (regardless of the click action type).

---

## GUI-Scoped Custom Placeholders

You can pass a set of custom placeholders and their replacements when opening a GUI via the Java API. These placeholders are resolved in the **title**, **item display names**, **item lores**, **skull owners**, and **fill items** of the GUI, behaving identically to the `Messanger` system.

### How to use in Java

Call the overloaded `open` method in `CrownGuiService` or `GuiRenderer`:

```java
// Define placeholders and their replacements
String[] placeholders = new String[] { "coins", "level" };
String[] replacements = new String[] { "1,500", "42" };

// Open the menu with the placeholders resolved
CrownGuiService.open(player, "my_menu", placeholders, replacements);
```

### How they are parsed in YAML

In your GUI YAML configurations, reference the custom placeholders using `%placeholder_name%`:

```yaml
title: "&eStats Menu"
size: 9
items:
  stats_item:
    slot: 4
    material: DIAMOND
    name: "&bYour Level: &f%level%"
    lore:
      - "&7Coins: &6%coins%"
```
