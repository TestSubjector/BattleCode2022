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


