<p align="center">
  <img src="https://raw.githubusercontent.com/SlimefunNewHorizons/AxGraves-Drakes/main/banner.svg" width="100%" alt="AXGRAVES animated banner" />
</p>

# AxGraves-Drakes

> ### 🏰 Comunidad Oficial de DrakesCraft
>
> * 🎮 **IP del Servidor**: `mc.drakescraft.cl` *(Java & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/rv3vtXZTk7](https://discord.gg/rv3vtXZTk7)
> * 🌐 **Portal Web & Guía**: [web.drakescraft.cl](https://web.drakescraft.cl)
> * 🛒 **Tienda Oficial**: [drakescraft-v2.tebex.io](https://drakescraft-v2.tebex.io)

---

> **Estado (Temporada 2 — rama `port-26x`):** Probado en producción sobre **Purpur 26.2** y **Java 25** (bytecode Java 21 compatible con Java 21–25). También disponible como módulo integrado (`axgraves`) en `drakes-server` dentro de [Drakes-Suites](https://github.com/SlimefunNewHorizons/Drakes-Suites).

A Minecraft plugin fork maintained for DrakesCraft that creates a persistent death chest (grave) at the location where a player dies so they can safely recover their items and XP, with Soulbound anti-duplication guards for Slimefun.

## Features

- Creates a protected grave storing the player's items and XP at the death location.
- Configurable despawn timer, per-player grave limits, world filters, and death-cause blacklists.
- Optional grave rotation and holograms.
- Persistent storage across restarts (`axgraves.db` / SQLite).
- Folia-compatible (`folia-supported: true`).
- Zero telemetry, zero external update checks.

## Configuration

All options live in `plugins/AxGraves/config.yml` (auto-generated on first run). Notable keys:

| Key | Default | Description |
| :--- | :--- | :--- |
| `despawn-time-seconds` | `468000` (130 hours) | Seconds before an empty / expired grave is removed. Set to `-1` to disable. |
| `drop-items` | `true` | Whether remaining items fall to the ground when the grave expires. |
| `store-items` | `true` | Whether to move items into the grave. |
| `store-xp` | `true` | Whether to store XP. |
| `xp-keep-percentage` | `1.0` | Fraction of stored XP returned to the player. |
| `override-keep-inventory` | `false` | Keep protected inventories authoritative; a grave is created only when the final death event does not retain items. |
| `interact-only-own` | `false` | Restrict grave opening to the owner only. |
| `disabled-worlds` | `[]` | Worlds where graves are not created. |
| `blacklisted-death-causes` | `[]` | Death causes that skip grave creation (e.g. `VOID`). |
| `death-listener-priority` | `MONITOR` | Bukkit event priority for the death listener. |
| `save-graves.enabled` | `true` | Persist graves across server restarts. |

## Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/graves help` | Show the in-game help. | `axgraves.help` |
| `/graves reload` | Reload `config.yml` and `messages.yml`. | `axgraves.reload` |
| `/graves list [player]` | List your (or another player's) active graves. | `axgraves.list` / `axgraves.list.other` |
| `/graves tp <id>` | Teleport to a grave by ID. | `axgraves.tp` |

## Permissions

| Permission | Default | Description |
| :--- | :--- | :--- |
| `axgraves.allowgraves` | `true` | Allow grave creation on death. |
| `axgraves.list` | `true` | List your own graves. |
| `axgraves.list.other` | `op` | List any player's graves. |
| `axgraves.tp` | `op` | Teleport to a grave. |
| `axgraves.tp.bypass` | `op` | Bypass ownership checks when teleporting. |
| `axgraves.help` | `true` | Access the `/graves help` command. |
| `axgraves.reload` | `op` | Reload configuration. |

## Placeholders

- Internal: `%axgraves_despawn-time_<graveId>%`, `%axgraves_despawn-time%`
- PlaceholderAPI: `%axgraves_grave_count%`, `%axgraves_has_grave%`

## Compatibility

| Server | Supported |
| :--- | :--- |
| Purpur **26.2** (Season 2 production) | Yes |
| Paper / Purpur **1.21.x** | Yes |
| Folia | Yes (`folia-supported: true`) |

Compiled with **JDK 21** bytecode; runs on **Java 21** and **Java 25** (LTS). Version: `1.32.1-Drake.1`.

## Soft Dependencies & Operación Drake

- **Slimefun (`Soulbound` Anti-Dupe):** La decisión de conservar un objeto Soulbound pertenece a Slimefun; `AxGraves-Drakes` excluye los ítems marcados como `Soulbound` (por interfaz o lore de `Soulbound Rune`) de la tumba y restaura los casos pendientes al reconectar para evitar duplicaciones. Funciona con normalidad si Slimefun no está instalado.
- **Exclusividad:** No cargar otro plugin de tumbas en paralelo (p. ej. `MoreGraves` debe mantenerse deshabilitado).
- **Sin telemetría:** Este fork no ejecuta comprobaciones de actualizaciones ni descargas automáticas externas.

## Building

Requirements: **JDK 21+** and **Maven 3.8+**.

```bash
mvn clean test
mvn package
```

The shaded jar is produced at `target/AxGraves-<version>-Drake-all.jar`.

---

## 📄 License & Upstream Attribution

This project is a sovereign fork maintained by [**JackStar6677-1**](https://github.com/JackStar6677-1) and **Chagui68** under [**DrakesCraft Labs**](https://github.com/SlimefunNewHorizons).

- **Original Project:** Created by Artillex-Studios and the open-source community.
- **DrakesCraft Optimizations:** Modernized for Purpur 26.2 / Paper 1.21.x+, Java 21–25, high concurrency, asynchronous safety, and Soulbound anti-duplication guards.
- **License:** Distributed under the original **GNU General Public License v3.0 (GPLv3)**. See the [`LICENSE`](LICENSE) file for complete terms.
