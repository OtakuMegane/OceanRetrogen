# OceanRetrogen

Written for a server I run to retrogen (or more accurately retrocopy) underwater content introduced in Update Aquatic. You probably shouldn't use this unless you really understand the workings and you will receive no support. Period. If you do try it out, at least test on a copy of the world and check carefully to ensure you like the results!

It was created based on a specific set of circumstances which apply to the server:
 - Server does not have many underwater builds
 - What underwater builds do exist will not be harmed by the new content or can be cleaned up by server admin
 - Terrain generation is primarily 1.7+ and generally matches the layout produced in 1.13
 - The only terrain that doesn't match the 1.7+ layout contains no ocean, beach, swamp or river biomes (in the target case, the center is old alpha/beta terrain with corrected biomes)
 
Coordinates of all chunks successfully processed are stored in files in the plugin folder so if things are interrupted it will not redo the same chunks again. The retrogen is started with the command `/retrogen start <main world name> <template world name>` and can be stopped with `/retrogen stop <main world name>`.

Running the retrogen will take a long time (possibly hours, even for smaller maps). If you have a large world, the process will likely lag out or crash the server after a while unless you've given it ridiculous amounts of memory. If this happens you should be able to restart and it will pick up roughly where it stopped.

There will also be cleanup afterwards since there's no sane way to get 100% perfect detection and copy.