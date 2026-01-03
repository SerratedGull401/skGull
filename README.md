# skGull 🕊️
**High-performance entity interaction and profiling suite for Skript.**

`skGull` is a specialized Skript addon designed for high-precision entity detection, 1.21+ item component manipulation, and containment management. It is optimized for complex AI mechanics and horror-themed servers.

---

## 🛠 Features

### 1. Containment Zones (High Performance)
Unlike traditional Skript loops, `skGull` zones use Java-side event listeners to detect boundary crosses with zero constant CPU overhead.

* **Zone Creation:** `create a [new] containment zone named %string% from %location% to %location%`
  * Saves automatically to `plugins/skGull/zones.yml`.
* **Zone Deletion:** `delete containment zone %string%`
  * Removes data from memory and the configuration file.
* **Zone Listing:** `[all] [active] containment zones`
  * Returns a list of strings containing the names of all loaded zones.



### 2. Advanced Raycasting & Vision
* **Sphere Raycast**
  * `sphere raycast from %location% with radius %number% for %number% blocks`
  * Performs a "thick" raycast detecting both blocks and entities.
* **Look Events**
  * `on player look at [%entitytype%] [within %number% blocks]`: Fires **once** when a player's crosshair meets an entity.
  * `on player looking at [%entitytype%] [within %number% blocks]`: Fires **continuously** while the gaze is maintained.
* **Vision & Logic**
  * `%entity% is visible to %player%`: Checks for block occlusion (walls/floors).
  * `hit part of entity`: Returns `"head"`, `"body"`, or `"feet"`.

### 3. Item Components (1.21+)
* `make %itemstack% [look] shiny`: Applies `enchantment_glint_override`.
* `make %itemstack% [look] (dull|unshiny)`: Removes glint.

---

## 📖 Scripting Examples

### 1. Managing Containment Zones
```applescript
# Creating a zone via command
command /setzone <text>:
    permission: op
    trigger:
        # Use a tool or variables to get locations
        create a new containment zone named arg-1 from {pos1} to {pos2}
        send "&aZone %arg-1% created and saved!"

# Listing all zones
command /zones:
    trigger:
        set {_all::*} to all active containment zones
        send "&6Active Zones: &f%{_all::*}%"

# Handling a breach
on zone exit:
    # event-string is the zone name, event-entity is the escaped entity
    broadcast "&c&lBREACH! &e%event-entity% &7left &f%event-string%!"
    
on zone enter of "SCP-173":
    broadcast "&a&lSECURED! &7The Sculpture is back in its cell."