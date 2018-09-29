package com.minefit.xerxestireiron.oceanretrogen;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;

public class RetrogenChunk {
    public static boolean retrogen(World mainWorld, World templateWorld, int chunkX, int chunkZ) {
        System.out.println("Doing ocean retrogen for chunk at x:" + chunkX + " z:" + chunkZ);
        Chunk toChunk = mainWorld.getChunkAt(chunkX, chunkZ);
        Chunk fromChunk = templateWorld.getChunkAt(chunkX, chunkZ);
        int blockBaseX = chunkX * 16;
        int blockBaseZ = chunkZ * 16;
        boolean isMonumentChunk = false;

        // Test to see if we're near an existing ocean monument
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 64; y > 0; y--) {
                    Block testBlock = toChunk.getBlock(x, y, z);
                    Block testBlock2 = fromChunk.getBlock(x, y, z);

                    if (isMonumentPiece(testBlock)) {
                        isMonumentChunk = true;
                        break;
                    }
                }
            }
        }

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                boolean isOcean = false;
                boolean isBeach = false;
                boolean isRiver = false;
                boolean isSwamp = false;
                Biome biome1 = mainWorld.getBiome(blockBaseX + x, blockBaseZ + z);
                Biome biome2 = templateWorld.getBiome(blockBaseX + x, blockBaseZ + z);
                Block surfaceBlock = mainWorld.getBlockAt(blockBaseX + x, 62, blockBaseZ + z);

                // Make sure we're only working with ocean or beach biomes
                if (isOcean(biome1) && isOcean(biome2)) {
                    isOcean = true;
                    mainWorld.setBiome(blockBaseX + x, blockBaseZ + z, biome2);

                    // If false, we are not over open, safe to modify ocean
                    if (surfaceBlock.getType() != Material.WATER) {
                        continue;
                    }
                } else if (isBeach(biome1) && isBeach(biome2)) {
                    isBeach = true;
                } else if (biome1 == Biome.RIVER && biome2 == Biome.RIVER) {
                    isRiver = true;
                } else if (biome1 == Biome.SWAMP && biome2 == Biome.SWAMP) {
                    isSwamp = true;
                } else {
                    continue;
                }

                boolean atBottom = false;
                boolean atBottom2 = false;

                for (int y = 255; y > 0; --y) {
                    Block block = toChunk.getBlock(x, y, z);
                    Block block2 = fromChunk.getBlock(x, y, z);
                    Material blockType = block.getType();
                    Material block2Type = block2.getType();
                    atBottom = (atBottom) ? atBottom : isTopLayer(block);
                    atBottom2 = (atBottom2) ? atBottom2 : isTopLayer(block2);

                    if (atBottom) {
                        // Copy over buried chests
                        if (isChest(block2)) {
                            copyBlock(block, block2);
                            continue;
                        }

                        if (!isTopLayer(block)) {
                            break;
                        }
                    }

                    // If we're into rock, we're done
                    if (atBottom2 && !isTopLayer(block2)) {
                        break;
                    }

                    if (isOcean) {
                        if (isMonumentChunk) {
                            // We don't want to copy a new monument on top of an existing one
                            if (isMonumentPiece(block) || isMonumentPiece(block2)) {
                                continue;
                            }
                        }

                        if (atBottom) {
                            // Fill in top level where caves or ravines would be
                            if (block2Type == Material.WATER) {
                                if (biome2 == Biome.WARM_OCEAN || biome2 == Biome.DEEP_WARM_OCEAN
                                        || biome2 == Biome.LUKEWARM_OCEAN || biome2 == Biome.DEEP_LUKEWARM_OCEAN) {
                                    block.setType(Material.SAND);
                                } else {
                                    block.setType(Material.GRAVEL);
                                }
                            } else {
                                copyBlock(block, block2);
                            }
                        } else {
                            // Copy block, unless it would alter terrain heightmap in main world
                            if (!atBottom2 && isWater(block)) {
                                copyBlock(block, block2);
                            }
                        }
                    } else if (isBeach) {
                        // Copy blocks in beach unless it's air or water
                        if (atBottom) {
                            if (!isAir(block2) && !isWater(block2)) {
                                copyBlock(block, block2);
                                continue;
                            }
                        }

                        if (blockType == Material.AIR || blockType == Material.WATER) {
                            // Sometimes trees overhang the beach
                            if (isLeaves(block2)) {
                                continue;
                            }

                            copyBlock(block, block2);
                        }
                    } else if (isRiver) {
                        if (!atBottom) {
                            if (blockType == Material.WATER) {
                                if (block2Type == Material.SEAGRASS || block2Type == Material.TALL_SEAGRASS) {
                                    copyBlock(block, block2);
                                }
                            }
                        }
                    } else if (isSwamp) {
                        if (!atBottom) {
                            if (blockType == Material.WATER) {
                                if (block2Type == Material.SEAGRASS || block2Type == Material.TALL_SEAGRASS) {
                                    copyBlock(block, block2);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static void copyBlock(Block toBlock, Block fromBlock) {
        Material fromType = fromBlock.getType();
        toBlock.setType(fromType);
        toBlock.setBlockData(fromBlock.getBlockData());

        if (isChest(fromBlock)) {
            Chest fromChest;
            Chest toChest;

            try {
                fromChest = (Chest) fromBlock.getState();
                toChest = (Chest) toBlock.getState();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            toChest.setLootTable(fromChest.getLootTable());
            toChest.setSeed(fromChest.getSeed());
            toChest.update();
        }
    }

    private static boolean isMonumentPiece(Block block) {
        Material blockType = block.getType();
        return blockType == Material.PRISMARINE || blockType == Material.PRISMARINE_BRICK_SLAB
                || blockType == Material.PRISMARINE_BRICK_STAIRS || blockType == Material.PRISMARINE_BRICKS
                || blockType == Material.PRISMARINE_SLAB || blockType == Material.PRISMARINE_STAIRS
                || blockType == Material.DARK_PRISMARINE || blockType == Material.DARK_PRISMARINE_SLAB
                || blockType == Material.DARK_PRISMARINE_STAIRS || blockType == Material.SPONGE
                || blockType == Material.WET_SPONGE || blockType == Material.GOLD_BLOCK
                || blockType == Material.SEA_LANTERN;
    }

    private static boolean isTopLayer(Block block) {
        Material blockType = block.getType();
        return blockType == Material.SAND || blockType == Material.GRAVEL || blockType == Material.DIRT
                || blockType == Material.CLAY;
    }

    private static boolean isLeaves(Block block) {
        Material blockType = block.getType();
        return blockType == Material.ACACIA_LEAVES || blockType == Material.BIRCH_LEAVES
                || blockType == Material.DARK_OAK_LEAVES || blockType == Material.JUNGLE_LEAVES
                || blockType == Material.OAK_LEAVES || blockType == Material.SPRUCE_LEAVES;
    }

    private static boolean isOcean(Biome biome) {
        return biome == Biome.OCEAN || biome == Biome.COLD_OCEAN || biome == Biome.DEEP_COLD_OCEAN
                || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.DEEP_OCEAN
                || biome == Biome.DEEP_WARM_OCEAN || biome == Biome.FROZEN_OCEAN || biome == Biome.LUKEWARM_OCEAN
                || biome == Biome.WARM_OCEAN;
    }

    private static boolean isBeach(Biome biome) {
        return biome == Biome.BEACH || biome == Biome.SNOWY_BEACH;
    }

    private static boolean isChest(Block block) {
        Material blockType = block.getType();
        return blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST;
    }

    private static boolean isWater(Block block) {
        Material blockType = block.getType();
        return blockType == Material.WATER;
    }

    private static boolean isAir(Block block) {
        Material blockType = block.getType();
        return blockType == Material.AIR;
    }
}
