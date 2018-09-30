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

                    if (isMonumentPiece(testBlock)) {
                        isMonumentChunk = true;
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
                    if (surfaceBlock.getType() != Material.WATER
                            && surfaceBlock.getRelative(BlockFace.DOWN).getType() != Material.WATER) {
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

                boolean atBottomLayer = false;
                boolean atBottomLayer2 = false;
                boolean atBedrock = false;
                boolean atBedrock2 = false;

                for (int y = 255; y > 0; --y) {
                    Block block = toChunk.getBlock(x, y, z);
                    Block block2 = fromChunk.getBlock(x, y, z);
                    Block blockDown = toChunk.getBlock(x, y - 1, z);
                    Block blockDown2 = fromChunk.getBlock(x, y - 1, z);
                    Material blockType = block.getType();
                    Material block2Type = block2.getType();

                    // High probability this isn't natural and we want to leave it alone
                    if (!isWater(block) && !isAir(block) && !isBottomLayerBlock(block) && !isBedrockBlock(block)
                            && !isUnderwaterPlant(block)) {
                        continue;
                    }

                    // We've reached bedrock in the target world
                    if (isBedrockBlock(block)) {
                        if (atBottomLayer || isBedrockBlock(blockDown)) {
                            atBedrock = true;
                        }
                    }

                    // We've reached bedrock in the template world
                    if (isBedrockBlock(block2)) {
                        if (atBottomLayer2 || isBedrockBlock(blockDown2)) {
                            atBedrock2 = true;
                        }
                    }

                    // We've reached a bottom layer block in the target world with bedrock beneath
                    if (isBottomLayerBlock(block) && (isBottomLayerBlock(blockDown) || isBedrockBlock(blockDown))) {
                        atBottomLayer = true;
                    }

                    // We've reached a bottom layer block in thes template world with bedrock beneath
                    if (isBottomLayerBlock(block2) && isBedrockBlock(blockDown2)) {
                        atBottomLayer2 = true;
                    }

                    // We are at bedrock and finished with this column
                    if (atBedrock || atBedrock2) {
                        // Copy over buried chests
                        if (isChest(block2)) {
                            copyBlock(block, block2);
                        }

                        break;
                    }

                    if (isOcean) {
                        // We are either at the bottom layer or have hit a block that could be part of it
                        if (atBottomLayer) {
                            // Fill in top level where caves or ravines would be
                            if (isWater(block2) || isUnderwaterPlant(block2)) {
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
                            // We don't want to copy a new monument or odd blocks on top of an existing one
                            if (isMonumentChunk) {
                                continue;
                            }

                            // Should be a normal block to copy over
                            if (isWater(block) || isAir(block)) {
                                copyBlock(block, block2);
                            }
                        }
                    } else if (isBeach) {
                        // Copy blocks in beach unless it's air or water
                        if (atBottomLayer) {
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
                        if (!atBottomLayer) {
                            if (blockType == Material.WATER) {
                                if (block2Type == Material.SEAGRASS || block2Type == Material.TALL_SEAGRASS) {
                                    copyBlock(block, block2);
                                }
                            }
                        }
                    } else if (isSwamp) {
                        if (!atBottomLayer) {
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

    private static boolean isBottomLayerBlock(Block block) {
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

    private static boolean isUnderwaterPlant(Block block) {
        Material blockType = block.getType();
        return blockType == Material.KELP || blockType == Material.KELP_PLANT || blockType == Material.SEAGRASS
                || blockType == Material.TALL_SEAGRASS;
    }

    private static boolean isBedrockBlock(Block block) {
        Material blockType = block.getType();
        return blockType == Material.STONE || blockType == Material.GRANITE || blockType == Material.DIORITE
                || blockType == Material.ANDESITE || blockType == Material.COAL_ORE || blockType == Material.IRON_ORE
                || blockType == Material.GOLD_ORE || blockType == Material.LAPIS_ORE
                || blockType == Material.REDSTONE_ORE || blockType == Material.DIAMOND_ORE;
    }
}
