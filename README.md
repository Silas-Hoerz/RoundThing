# RoundThing
![Java](https://img.shields.io/badge/Java-17-blue)
![API](https://img.shields.io/badge/API-Paper%20/%20Folia%201.21.8%2B-orange)
![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey)

**Never build ugly circles again! RoundThing is a Minecraft (Paper/Folia) plugin that visualizes perfect, block-snapped circles, spheres, and lines with particles as the ultimate building guide.**
![1.png](images/1.png)
This plugin is your personal architect for all complex construction projects. It helps players visualize mathematically perfect shapes by displaying a grid of particles exactly where blocks need to be placed. The result is flawless structures without the headache.
![3.png](images/3.png)
---

## ✨ Features

- **Three Shape Types:** Create detailed **Circles**, **Spheres**, and **Lines**.
- **Block-Perfect:** Particles always snap to the center of a block, creating a clear "stair-step effect" as a perfect building template.
![2.png](images/2.png)
- **Full 3D Control:** Freely rotate circles in 3D space! Tilt them on the X and Z axes (`-90°` to `+90°`) to create vertical rings, diagonal platforms, and more.
- **Thickness & Filling:** Create thick rings or completely filled disks and spheres.
- **Persistent Storage:** Every shape created by a player is saved and survives a server restart.
- **Server Protection:** A configurable per-player particle limit prevents the server from being overloaded by too many particles.
- **Fully Configurable:** Admins can adjust the limit at any time in-game or in the `config.yml`.
- **Multilingual (i18n):** Automatically adapts to the player's client language. (Supports English, German, Spanish, French, and many more).
- **Intelligent Commands:** Thanks to tab-completion, commands, shape names, colors, and coordinates are suggested as you type.

---

## 📦 Installation

1.  Download the latest `.jar` file from the [Releases Tab](https://github.com/Silas-Hoerz/RoundThing/releases).
2.  Place the `.jar` file into the `plugins` folder of your Paper/Folia server.
3.  Restart the server.

---

## ⌨️ Commands

### Player Commands

#### Circles (`/c` or `/circle`)
- **Display Help:**
  /c help

- **List all circles:**
  /c list

- **Delete circle(s):**
  /c delete <name|all>

- **Create/update a circle:**
  /c create <name> <diameter> [thickness] [x y z] [color] [rotX] [rotZ]


#### Spheres (`/s` or `/sphere`)
- **Display Help:**
  /s help

- **List all spheres:**
  /s list

- **Delete sphere(s):**
  /s delete <name|all>

- **Create/update a sphere:**
  /s create <name> <diameter> [thickness] [x y z] [color]


#### Lines (`/l` or `/line`)
- **Display Help:**
  /l help

- **List all lines:**
  /l list

- **Delete line(s):**
  /l delete <name|all>

- **Create/update a line:**
  /l create <name> <x1> <y1> <z1> <x2> <y2> <z2> [color]


### Admin Commands

#### Administration (`/roundthing`)
- **Set particle limit:**
  /roundthing setlimit <amount>

- **Reload configuration:**
  /roundthing reload


---

## 🔐 Permissions

| Permission              | Description                                      | Default  |
|-------------------------|--------------------------------------------------|----------|
| `roundthing.circle.use` | Allows the use of circle commands (`/c`).        | `true`   |
| `roundthing.sphere.use` | Allows the use of sphere commands (`/s`).        | `true`   |
| `roundthing.line.use`   | Allows the use of line commands (`/l`).          | `true`   |
| `roundthing.admin`      | Allows the use of admin commands (`/roundthing`). | `op`     |

---

## ⚙️ Configuration

The plugin creates a `config.yml` in the `plugins/RoundThing/` folder.

**`config.yml`**
```yaml
# The maximum particle budget that each player is allowed to use in total.
particle-limit: 10000
Customizing Languages
All messages sent by the plugin can be customized by editing the language files in the plugins/RoundThing/lang/ folder.

🤝 Contributing
Found a bug or have an idea for a new feature? Feel free to create an Issue or a Pull Request!
