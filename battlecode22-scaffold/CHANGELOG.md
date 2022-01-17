# CHANGELOG

## PageOneBot

* Implement archon fleeing, may tweak value and other stuff regulating when it flees
* Soldiers will now update comms with enemy archon location

## FoolsGoldBot

* Reintroduced update_enemy_archon location for soldiers
* Some tweaking to the retreat function calculation
* More accurate enemy locations and skip our own archon locations for small maps

## FoolsGoldNewBot:

* Added reset miningLocation and inPlaceToMine when fleeing from enemy robot.

## GrindstoneBot:

* Added offensive Archon repositioning code.

## TopTenBot

* Change all currentLocation mentions to rc.getLocation() as only 1 bytecode cost
* Improved offensive Archon Repositioning system by adding limiters (don't move when excessive lead reserve, increased turns waiting to move)
* Removed bug that didn't allow the update of our archon's locations in the relevant comms channels. This decreased performance somehow. Absolutely no idea why.