* We can get enemy lead and gold reserves, use that information.
* Miners should request for new Lead locations if they don't see any mining locations around
* Every turns all bots can +1 on a specific index depending on type. Next turn as Archons start first, then can read these
    index values to find what type of unit and what number currently exists on map. Use this to polish production.
* Have 1x1, 2x2, 3x3, 4x4 fixed arrays representing archon locations on map an the default policy ot use then, for
    map takeover. Has to be generic.
* After some number of reads of the rubble map, stop reading and updating the map. You're done.
* Voronoi type partitions of the maps
* Set max bytecode that every function can use, so the no function of bot starves (in the loops)