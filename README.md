# skGull 🕊️
**High-performance entity interaction and profiling suite for Skript.**

`skGull` is a specialized Skript addon designed for high-precision entity detection, 1.21+ item component manipulation, and containment management. It is optimized for complex AI mechanics and immersive horror-themed servers.

---

## 🛠 Features

### 1. Containment Zones & Boundary Events
`skGull` zones use Java-side spatial hashing to detect boundary crosses with zero constant CPU overhead.

* **Zone Management:**
  * `create [a] [new] containment zone named %string% from %location% to %location%`
  * `(delete|remove) containment zone %string%`
  * `[all] [active] containment zones` (Expression)
* **Events:**
  * `on zone (enter|exit) [of %string%]`
  > **Event Values:** `event-text` (Zone Name), `event-entity` (The Subject)

### 2. Pathfinding & AI Control
Direct access to the Minecraft Navigation engine. This allows you to force entities to move to specific locations without using laggy "teleport" loops.

* **Pathing & AI:**
  * `pathfind %entity% to %location% [at speed %number%]`
  * `force %entity% to (patrol|loop path) %locations% at speed %number%`
  * `(enable|disable) [the] ai of %entities%`
  * `allow %entity% to (pass|open) doors` / `disallow %entity% from (passing|opening) doors`

### 3. Smooth Armor Stand Animations
Native NBT manipulation for fluid movement. These effects bypass the standard "choppiness" of manual Skript variable setting.

* **Effect:**
  * `animate %entity%'s [part] %number% to %number%, %number%, %number% over %number% ticks`
  > **Note:** Rotation values (X, Y, Z) are in degrees. Ticks define the duration of the interpolation.

### 4. Advanced Raycasting & Vision
* **Sphere Raycast:** `sphere raycast from %location% with radius %number% for %number% blocks [and store [result] in %objects%]`
* **Look Events:**
  * `on player look at [%entitytype%] [within %number% blocks]`: Fires **once** on contact.
  * `on player looking at [%entitytype%] [within %number% blocks]`: Fires **every 2 ticks** while maintained.
  * `on player see [a|an] %entitytype%`: Fires when an entity enters the player's FOV.
* **Vision Logic:**
  * `%entity% is [not] visible to %player%`: Checks for block occlusion.
  * `[the] [hit] (part|bone|section)`: Returns the specific area targeted (head, body, etc).

### 5. Audio & Item Effects
* **Audio:** `emit %string% from %entity% every %timespan% [with volume %number%] [with pitch %number%]`
* **Glint Control:**
  * `make %itemstacks% [look] shiny`
  * `make %itemstacks% [look] (dull|unshiny)`

---

## 📖 Scripting Examples

### 1. Weeping Angel / Stalker AI
This script makes zombies freeze when looked at and hunt the player when they turn away.

```applescript
# When the player makes eye contact, the zombie freezes
on player look at a zombie within 20 blocks:
    disable the ai of event-entity
    make event-entity's tool shiny
    emit "entity.warden.heartbeat" from event-entity every 1 second with volume 1.5

# Check periodically to resume movement if not being watched
every 10 ticks:
    loop all zombies:
        set {_p} to a random player
        # If the zombie isn't being looked at, it resumes the hunt
        if loop-zombie is not visible to {_p}:
            enable the ai of loop-zombie
            make loop-zombie's tool dull
            stop emitting sounds from loop-zombie
            pathfind loop-zombie to {_p} at speed 1.2