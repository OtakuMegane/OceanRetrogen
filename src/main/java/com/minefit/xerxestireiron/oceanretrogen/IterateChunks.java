package com.minefit.xerxestireiron.oceanretrogen;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class IterateChunks extends BukkitRunnable {
    private final OceanRetrogen plugin;
    private final World mainWorld;
    private final World templateWorld;
    private Set<Point> worldChunkMap;
    private Iterator<Point> worldChunkMapIterator;
    private int tickDelay = 0;

    @SuppressWarnings("unchecked")
    public IterateChunks(OceanRetrogen instance, World mainWorld, World templateWorld) {
        this.plugin = instance;
        this.mainWorld = mainWorld;
        this.templateWorld = templateWorld;

        try {
            File initialFile = new File(this.plugin.getDataFolder(), mainWorld.getName() + "_initialChunks.dat");
            initialFile.createNewFile();
            ObjectInputStream ois;
            ois = new ObjectInputStream(new FileInputStream(initialFile));
            this.worldChunkMap = (HashSet<Point>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            this.worldChunkMap = ChunkMapping.getWorldChunkMap(mainWorld);
        }

        try {
            File initialFile = new File(this.plugin.getDataFolder(), mainWorld.getName() + "_initialChunks.dat");
            initialFile.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(initialFile));
            oos.writeObject(this.worldChunkMap);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        this.plugin.loadFinishedChunksMap(mainWorld);
        this.worldChunkMap.removeAll(this.plugin.finishedChunks.get(mainWorld));
        this.worldChunkMapIterator = this.worldChunkMap.iterator();

        status();
    }

    public void status() {
        System.out.println("Chunks completed: " + this.plugin.finishedChunks.get(mainWorld).size()
                + ". Chunks remaining for main world: " + this.worldChunkMap.size() + ".");
    }

    @Override
    public void run() {
        ++this.tickDelay;

        if (this.tickDelay == 1000) {
            this.plugin.saveFinishedChunksMap(mainWorld);
            this.tickDelay = 0;
        }

        while (true) {
            if (this.worldChunkMapIterator.hasNext()) {
                Point chunkPoint = this.worldChunkMapIterator.next();
                int chunkX = chunkPoint.x;
                int chunkZ = chunkPoint.y;

                if (this.plugin.finishedChunks.get(mainWorld).contains(chunkPoint)) {
                    System.out.println("Chunk at " + chunkX + " z:" + chunkZ + " already done. Skipping.");
                    continue;
                } else {
                    if (RetrogenChunk.retrogen(mainWorld, templateWorld, chunkX, chunkZ)) {
                        this.plugin.finishedChunks.get(mainWorld).add(chunkPoint);
                    }

                    break;
                }
            } else {
                // All done!
                this.cancel();
                return;
            }
        }
    }
}
