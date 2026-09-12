# Omni Potion

A lightweight **Paper 1.21.11** server plugin that adds a craftable custom **Omni Potion**.

## What it does

It adds exactly **one** new crafting recipe. Throwing the potion gives you four effects at once, all lasting **8 minutes**:

| Effect                | Level | Duration |
|-----------------------|-------|----------|
| Strength              | II    | 8 min    |
| Speed                 | II    | 8 min    |
| Fire Resistance       | I     | 8 min    |
| Weaving               | I     | 8 min    |

## The Recipe

Combine these three items in **any crafting grid** (2x2 inventory crafting or a crafting table):

```
Nether Wart    +    Water Bottle
                      +
                Diamond Sword
```

Laid out in a 2x2 grid:

```
Nether Wart   Water Bottle
Diamond Sword (empty)
```

That's it — the result is a bright, aqua-colored **Omni Potion** you can throw or drink.

## Installation

1. Build the plugin or download the jar.
2. Drop `AllInOnePotion-1.0.0.jar` into your server's `plugins/` folder.
3. Restart the server (or use `/reload confirm`).
4. No configuration or permissions required — it just works.

## Building from source

The plugin targets the Paper API **1.21.11** and runs on **Java 21**.

```bash
gradle build
```

The compiled jar will be at `build/libs/AllInOnePotion-1.0.0.jar`.

## Notes

- The recipe requires a **plain Water Bottle** specifically — awkward, mundane, or any other potion will not work.
- The diamond sword is consumed in the craft, like any normal crafting ingredient.
- Works in both the player inventory crafting grid and the crafting table.
- The potion is a **splash** potion, so the effects also apply to entities hit by it.