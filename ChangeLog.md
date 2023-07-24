# Elytra Dashboard 0.2.2

This is a **pre-release** version.

## Info

- Current Mod Version: `0.2.2`
- *Previous Mod Version:* [`0.2.1`](https://github.com/Rainyaphthyl/ElytraDashboard/releases/tag/v0.2.1)
- Supported Minecraft Version: `1.12.2`

## New Features

- More Information is added to the dashboard:
    - total displacement and average velocity (XYZ vector)
    - total distance and average speed (XZ path integral)
    - firework efficiency with distance and displacement
    - altitude, relative height, and ground level
- A flight warning system is added:
    - warning of `PULL UP!`, displayed when `height < 100` AND `falldingDamage >= health`

## Modified Features

- The dashboard is displayed on the right edge of the screen.
- Transparency of the dashboard varies when crossing the world height limit.

## Code Changes

- The file of [Change Log](ChangeLog.md) is included in the repository.
