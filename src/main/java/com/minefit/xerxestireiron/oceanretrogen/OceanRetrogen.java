package com.minefit.xerxestireiron.oceanretrogen;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class OceanRetrogen extends JavaPlugin {
    protected Map<World, Set<Point>> finishedChunks = new HashMap<>();
    protected Map<World, BukkitTask> worldIterators = new HashMap<>();
    protected BukkitTask chunkUpdater;
    protected Commands commands = new Commands(this);

    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        this.saveDefaultConfig();
        getCommand("retrogen").setExecutor(this.commands);
    }

    public void onDisable() {

        for (BukkitTask worldIterator : this.worldIterators.values()) {
            worldIterator.cancel();
        }

        for (Entry<World, Set<Point>> entry : this.finishedChunks.entrySet()) {
            saveFinishedChunksMap(entry.getKey());
        }
    }

    @SuppressWarnings("unchecked")
    public boolean loadFinishedChunksMap(World world) {
        try {
            File fixedFile = new File(getDataFolder(), world.getName() + "_finishedChunks.dat");
            fixedFile.createNewFile();
            ObjectInputStream ois;
            ois = new ObjectInputStream(new FileInputStream(fixedFile));
            this.finishedChunks.put(world, (HashSet<Point>) ois.readObject());
            ois.close();
        } catch (Exception e) {
            this.finishedChunks.put(world, new HashSet<Point>());
            return false;
        }

        return true;
    }

    public boolean saveFinishedChunksMap(World world) {
        try {
            File fixedFile = new File(getDataFolder(), world.getName() + "_finishedChunks.dat");
            fixedFile.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fixedFile));
            oos.writeObject(this.finishedChunks.get(world));
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
