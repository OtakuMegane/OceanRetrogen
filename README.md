# OceanRetrogen

Written for a server I run and is posted for educational purposes. You really shouldn't use it and if you ignore that, at least test the results first.

It was created based on a specific set of circumstances:
 - Server does not have many underwater builds
 - Terrain generation is primarily 1.7+ and generally matches the layout produced in 1.13
 - The only terrain that doesn't match the 1.7+ layout contains no ocean or beach biomes (is old alpha/beta terrain with corrected biomes)
 
Coordinates of all chunks successfully processed are stored in files in the plugin folder so if things are interrupted it will not redo the same chunks again. The retrogen is started with the command `/retrogen start <main world name> <template world name>`