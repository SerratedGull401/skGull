# skGull 🕊️
**High-performance entity interaction and profiling suite for Skript.**

`skGull` is a specialized Skript addon designed for high-precision entity detection, 1.21+ item component manipulation, and server performance monitoring. It is optimized for complex AI mechanics and horror-themed servers.

---

## 🛠 Features

### 1. Advanced Raycasting & Vision
* **Sphere Raycast**
    * `sphere raycast from %location% with radius %number% for %number% blocks [and store result in %-objects%]`
    * Performs a "thick" raycast detecting both blocks and entities.
    * Automatically ignores the executor (source) to prevent self-collision.
    * **Returns:** The exact `Location` of the hit.

* **Look Events**
    * `on player look at [%entitytype%] [within %number% blocks]`: Fires **once** when a player's crosshair meets an entity. Includes a proximity "re-fire" logic if the player moves closer.
    * `on player looking at [%entitytype%] [within %number% blocks]`: Fires **continuously** (every 2 ticks) while the gaze is maintained.

* **Vision & Logic**
    * `%entity% is visible to %player%`: A condition that checks for block occlusion (walls/floors) between the player and target.
    * `hit part of entity`: An expression returning `"head"`, `"body"`, or `"feet"` based on the vertical hit position.

### 2. Item Components (1.21+)
* `make %itemstack% [look] shiny`: Applies the modern `enchantment_glint_override`.
* `make %itemstack% [look] (dull|unshiny)`: Removes the glint override.

### 3. Performance Profiler
* `/skriptprofile start/stop`: Record real-time execution timings.
* `/skriptprofile status`: Check if the recorder is active.
* `/skriptprofile report [count]`: Generate a chat-based breakdown of the laggiest events.

---

## 📖 Scripting Examples

### 1. Area-of-Effect "Heavy" Beam

```applescript
on rightclick with netherite shovel:
    # Uses a 2-block wide "thick" ray to make aiming easier
    sphere raycast from player's eye location with radius 2.0 for 25 blocks and store result in {_hit}
    if {_hit} is set:
        play mob spawner flames at {_hit}
        damage all entities in radius 3 around {_hit} by 5
        send "&cTarget Neutralized."