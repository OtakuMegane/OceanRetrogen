package com.minefit.xerxestireiron.oceanretrogen;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

public class RetrogenChunk {
    public static boolean retrogen(World mainWorld, World templateWorld, int chunkX, int chunkZ) {
        System.out.println("Doing ocean retrogen for chunk at x:" + chunkX + " z:" + chunkZ);
        Chunk toChunk = mainWorld.getChunkAt(chunkX, chunkZ);
        Chunk fromChunk = templateWorld.getChunkAt(chunkX, chunkZ);
        int blockBaseX = chunkX * 16;
        int blockBaseZ = chunkZ * 16;
        boolean monumentChunk = false;

        // Test to see if we're near an existing ocean monument
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 64; y > 0; y--) {
                    Block testBlock = toChunk.getBlock(x, y, z);
                    Block testBlock2 = fromChunk.getBlock(x, y, z);

                    if (isMonumentPiece(testBlock)) {
                        monumentChunk = true;
                        break;
                    }
                }
            }
        }

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                boolean isOcean = false;
                boolean isBeach = false;
                Biome biome1 = mainWorld.getBiome(blockBaseX + x, blockBaseZ + z);
                Biome biome2 = templateWorld.getBiome(blockBaseX + x, blockBaseZ + z);
                Block surfaceBlock = mainWorld.getBlockAt(blockBaseX + x, 62, blockBaseZ + z);

                // Make sure we're only working with ocean or beach biomes
                if (biome1 == Biome.OCEAN || biome1 == Biome.DEEP_OCEAN) {
                    isOcean = true;

                    if (biome2 == Biome.OCEAN || biome2 == Biome.COLD_OCEAN || biome2 == Biome.DEEP_COLD_OCEAN
                            || biome2 == Biome.DEEP_FROZEN_OCEAN || biome2 == Biome.DEEP_LUKEWARM_OCEAN
                            || biome2 == Biome.DEEP_OCEAN || biome2 == Biome.DEEP_WARM_OCEAN
                            || biome2 == Biome.FROZEN_OCEAN || biome2 == Biome.LUKEWARM_OCEAN
                            || biome2 == Biome.WARM_OCEAN) {
                        mainWorld.setBiome(blockBaseX + x, blockBaseZ + z, biome2);
                    } else {
                        continue;
                    }

                    // If false, we are not over open ocean
                    if (surfaceBlock.getType() != Material.WATER) {
                        continue;
                    }

                } else if (biome1 == Biome.BEACH) {
                    if (biome2 == Biome.BEACH) {
                        isBeach = true;
                    } else {
                        continue;
                    }

                } else {
                    continue;
                }

                boolean atBottom = false;

                for (int y = 255; y > 0; --y) {
                    if (atBottom) {
                        break;
                    }

                    Block block = toChunk.getBlock(x, y, z);
                    Block block2 = fromChunk.getBlock(x, y, z);
                    Material blockType = block.getType();
                    Material block2Type = block2.getType();

                    // Avoid messing things up with terrain differences
                    if (isTopLayer(block2)) {
                        atBottom = true;
                    }

                    if (isOcean) {
                        // If this is a monument chunk we don't want to copy a new monument on top of an existing one
                        if (monumentChunk && (isMonumentPiece(block) || isMonumentPiece(block2))) {
                            continue;
                        }

                        if (blockType == Material.AIR || blockType == Material.WATER) {
                            copyBlock(block, block2);
                        } else if (isTopLayer(block)) {
                            // Fill in where caves and ravines would have been in newer world
                            if (block2Type == Material.WATER) {
                                if (biome2 == Biome.WARM_OCEAN || biome2 == Biome.DEEP_WARM_OCEAN
                                        || biome2 == Biome.LUKEWARM_OCEAN || biome2 == Biome.DEEP_LUKEWARM_OCEAN) {
                                    block2.setType(Material.SAND);
                                } else {
                                    block2.setType(Material.GRAVEL);
                                }
                            }

                            copyBlock(block, block2);
                            atBottom = true;
                        }
                    } else if (isBeach) {
                        if (!atBottom && (blockType == Material.AIR || blockType == Material.WATER)) {
                            if (isLeaves(block2)) {
                                continue;
                            }

                            copyBlock(block, block2);
                        } else {
                            atBottom = true;
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

        if (fromType == Material.CHEST || fromType == Material.TRAPPED_CHEST) {
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
}
