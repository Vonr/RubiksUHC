# RubiksUHC

RubiksUHC is a plugin written for Rubik's Craft, which is a SMP Server I'm very inactive on. 

I made this plugin for fun but I do plan to make it functional.

Planned Features:

 - In-game Config Editor

### Config

Default Config:
```yaml
world:
  overworld:
    name: world
uhc:
  border:
    size: 5000
    time: 0
  game:
    gracePeriod: 0
    scatterSize: 4800
    lateScatter: false
  scenarios:
    cutClean: false
    quickTools: false
    infiniteEnchants: false
    doubleHealth: false
    treeCapitator: false
```
world.overworld.name is the name of the folder containing your overworld data.

uhc.border.size is the size of the border in each direction. This means the border in play is twice as large as defined.

uhc.border.time is the time taken in seconds for the border to shrink to x0 z0. If less than or equal to 0, the border will not shrink.

uhc.game.gracePeriod is the time in seconds of the grace period if less than or equal to 0, there will be no grace period.

uhc.game.lateScatter controls whether players late to the game by 3 minutes maximum can join the game as a player. If set to false, they will join as spectators.

uhc.game.scenarios stores boolean values for each scenario controlling whether they are enabled. These can be changed in game.

### Permissions

rubiksuhc.uhc.start - Allows the player to start the UHC Game.

rubiksuhc.uhc.changeScenarios - Allows the player to enable and disable scenarios.

### Scenarios

CutClean - Automatically smelts Gold and Iron Ores.

Quick Tools - Enchants crafted tools with Efficiency 3 and Unbreaking 1.

Infinite Enchants - Gives you infinite xp levels and a hefty enchantment kit.

Double Health - Spawns players with 2x the normal health.

TreeCapitator - Chops down logs directly connected vertically
