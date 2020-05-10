# RubiksUHC

RubiksUHC is a plugin written for Rubik's Craft, which is a SMP Server I'm very inactive on. 

I made this plugin for fun but I do plan to make it functional.

Planned Features:

 - Scenarios

 - Permissions

 - In-game Config Editor


### Config

Default Config:
```yaml
world:
  overworld:
    name: overworld
uhc:
  border:
    size: 5000
    time: 0
  game:
    gracePeriod: 0
    scatterSize: 4800
    lateScatter: false
```
world.overworld.name is the name of the folder containing your overworld data.

uhc.border.size is the size of the border in each direction. This means the border in play is twice as large as defined.

uhc.border.time is the time taken in seconds for the border to shrink to x0 z0. If less than or equal to 0, the border will not shrink.

uhc.game.gracePeriod is the time in seconds of the grace period if less than or equal to 0, there will be no grace period.

uhc.game.lateScatter controls whether players late to the game by 3 minutes maximum can join the game as a player. If set to false, they will join as spectators.
