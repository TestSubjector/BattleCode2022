# CHANGELOG

## PageOneBot

* Implement archon fleeing, may tweak value and other stuff regulating when it flees
* Soldiers will now update comms with enemy archon location

## FoolsGoldBot

* Reintroduced update_enemy_archon location for soldiers
* Some tweaking to the retreat function calculation
* More accurate enemy locations and skip our own archon locations for small maps

## FoolsGoldNewBot

* Added reset miningLocation and inPlaceToMine when fleeing from enemy robot.

## GrindstoneBot

* Added offensive Archon repositioning code.

## TopTenBot

* Change all currentLocation mentions to rc.getLocation() as only 1 bytecode cost
* Improved offensive Archon Repositioning system by adding limiters (don't move when excessive lead reserve, increased turns waiting to move)
* Removed bug that didn't allow the update of our archon's locations in the relevant comms channels. This decreased performance somehow. Absolutely no idea why.
* Miner flee is more courageous on larger maps.
* Small maps made global, definition changed to 1000
* Some update visions when archon is moving and goodlocationtosettle() should use current location rubble as base rather
than max rubble.
* Soldiers will move to enemy archon location if they know it.

## SprintTwoBot

* Create bot for final Sprint 2 tournament submission
* Make soldiers more aggressive in 1vs1 situations
* Better retreat decision score evaluation
* Soldier will also now send enemyArchon location to comms

## SuccessSprintTwoBot:

* Added number of turns in move limitation to Archon Repostioning System for small maps.

## FinalSprintTwoBot

* Some bugfixes for archon
* Combat comms will only transmit enemy combat units
* Sages base code created

## Holiday Bot

* For the break between Sprint and spec change
* Finally have a better enemy archon guessing strategy
* Implement same guessing strategy for soldiers
* Soldiers now have opportunisticCombatDestination, to move to active combat locations
* Comm function for getting confirmed enemy archon location from Archon channels
* Healing states prevent moving deeper into combat
* Support ally using simpler tryToMove
* Bytecode optimisations for soldier
  
## SuperBot

* Improve soldier guessing more
* Fix a small bug in the healing change code
* Watchtower movement implemented

## Watchtower Bot

* Fixed mine depletion code.
* Improve watchtower movement
* Randomised production location for archon
* Better fleeing for miners

## LabBot

* Basic BotBuilder code written.
* Prioritise healing least damaged unit first
* Create Labs
* Transmute gold

## AnomalyBot

* Added comm functions for healing units count near archons.
* Miner disintegrate near archons when low health.
* Sage will not quickly run to archon now when action on cooldown, will wait for enemies in range.
* Created Anomaly.java
* Modified BotMiner: increased findOptimalMiningLocation(lead) lower bound to 8; Decreased ratio (dist to allied archon/ enemy archon) of mine depletion check to 1.2
* Laboratory now moves to adjacent lesser rubble area if present.

## CommMiningBot

* Comms: Bug Fix: commChannelStop of MINER isn't updated in updateComms() (as it's not needed since commChannelStop has been a fixed value for a long time now).
* Added WATCHTOWER channels segment to Comms.
* Increased combat channels

## SageActionBot

* Write to comms the locations that have large swaths (> 15) of lead mines.
* Sages can envision and step forward aggressively
* Soldiers will now go to Archon with less units to heal
* Insta-kill score for soldiers and healing for sages.
* Bug fix in isSafeToMine in BotMiner and added fleeing code to BotBuilder.

## PerfectProducerBot

* Miners now separate from other units better at start
* Miners flee better
* Some disintegration of units
* Some more util functions
* Minor change to how Watchtower weight is calculated

## PolishBot

* Make soldiers and other production queue separate for different maps
* Make standoff situations stop sending comms

## ASplitBot
