* We can get enemy lead and gold reserves, use that information.
~~* Miners should request for new Lead locations if they don't see any mining locations around~~
* Every turns all bots can +1 on a specific index depending on type. Next turn as Archons start first, then can read these
    index values to find what type of unit and what number currently exists on map. Use this to polish production.
* Have 1x1, 2x2, 3x3, 4x4 fixed arrays representing archon locations on map an the default policy ot use then, for
    map takeover. Has to be generic.
~~* After some number of reads of the rubble map, stop reading and updating the map. You're done.~~
* Voronoi type partitions of the maps
* Set max bytecode that every function can use, so the no function of bot starves (in the loops)
* Teach Miners to avoid mining locations where there's enemy presence without proper ally defense present. Should give that awareness to all non combat units.
* While comms seems efficient at the moment, I think it's going to blow up in bytecode consumption as soon as there's proper communication going through it. Might need correction.
* Perfect Lead Farms generation.
* Put everything in try/catch exception
* Factor in rubble to mine location, attack location, watchtower placement, miner suicide location etc
~~* Read all the elements of the shared array at once and store them.~~
* 4 tree for unexplored tiles (Discord: general, Jan 9; 12:25PM)
* Boundary Volume Hierarchy??
~~* Make the weak archons travel to the stronger archon to form a defensive line.~~
~~* If the combat unit kills enemy unit and then senses that unit location, will that sense return true or false? Ask in Discord~~
