# skGull 🕊️
**High-performance entity interaction and profiling suite for Skript.**

`skGull` is a specialized Skript addon designed for high-precision entity detection, 1.21+ item component manipulation, and containment management. It is optimized for complex AI mechanics and horror-themed servers.

---

## 🛠 Features

### 1. Containment Zones & Boundary Events
`skGull` zones use Java-side spatial hashing to detect boundary crosses with zero constant CPU overhead.

* **Zone Management:**
    * `create a [new] containment zone named %string% from %location% to %location%`
    * `delete containment zone %string%`
    * `[all] [active] containment zones`
* **Events:**
    * `on [zone] enter [of %string%]`
    * `on [zone] (exit|leave) [of %string%]`
    > **Note:** Use `event-string` for the zone name and `event-entity` for the subject.

### 2. Pathfinding & AI Control
Direct access to the Minecraft Navigation engine. This allows you to force entities to move to specific locations without using complex "thrust" or "teleport" loops.

* **Pathing:**
    * `(pathfind|move) %entity% to %location% [at speed %number%]`
    * `stop pathfinding [of] %entity%`
* **Logic:**
    * `%entity% is pathfinding`: Condition to check if an entity is currently moving to a target.
    * `pathfinding target of %entity%`: Returns the location the entity is trying to reach.

### 3. Smooth Armor Stand Animations
Native NBT manipulation for fluid movement. These effects bypass the standard "choppiness" of manual Skript variable setting.

* **Effect:**
    * `animate %entity% (head|body|left arm|right arm|left leg|right leg) to %vector% over %timespan%`
    * **Example:** `animate player's target head to vector(45, 0, 0) over 2 seconds`

### 4. Advanced Raycasting & Vision
* **Sphere Raycast:** `sphere raycast from %location% with radius %number% for %number% blocks`
* **Look Events:**
    * `on player look at [%entitytype%]`: Fires **once** on initial contact.
    * `on player looking at [%entitytype%]`: Fires **continuously** while maintained.
* **Vision Logic:**
    * `%entity% is visible to %player%`: Checks for block occlusion.
    * `hit part of entity`: Returns `"head"`, `"body"`, or `"feet"`.

### 5. Item Components (1.21+)
Directly modifies the `minecraft:enchantment_glint_override` component.

* `make %itemstack% [look] shiny`: Forces the enchantment glow even without enchantments.
* `make %itemstack% [look] (dull|unshiny)`: Removes glint.

---

## 📖 Scripting Examples

### 1. Stalker AI Logic
```applescript
on player looking at a zombie:
    # If the player stares at the zombie, make it freeze and look shiny
    stop pathfinding event-entity
    make event-entity's tool shiny
    
on player look away from zombie:
    # When they look away, it begins hunting again
    make event-entity's tool dull
    pathfind event-entity to player at speed 1.2
