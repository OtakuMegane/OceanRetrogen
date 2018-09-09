package com.minefit.xerxestireiron.oceanretrogen;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.World;

public class ChunkMapping {
    public static Set<Point> getWorldChunkMap(World world) {
        Set<Point> chunkCoordinates = new LinkedHashSet<>();

        // Borrowing most of this from dynmap (https://github.com/webbukkit/dynmap)
        File f = world.getWorldFolder();
        File regiondir = new File(f, "region");
        File[] lst = regiondir.listFiles();
        if (lst != null) {
            byte[] hdr = new byte[4096];
            for (File rf : lst) {
                if (!rf.getName().endsWith(".mca")) {
                    continue;
                }
                String[] parts = rf.getName().split("\\.");
                if ((!parts[0].equals("r")) && (parts.length != 4))
                    continue;

                RandomAccessFile rfile = null;
                int x = 0, z = 0;
                try {
                    x = Integer.parseInt(parts[1]);
                    z = Integer.parseInt(parts[2]);
                    rfile = new RandomAccessFile(rf, "r");
                    rfile.read(hdr, 0, hdr.length);
                } catch (IOException iox) {
                    Arrays.fill(hdr, (byte) 0);
                } catch (NumberFormatException nfx) {
                    Arrays.fill(hdr, (byte) 0);
                } finally {
                    if (rfile != null) {
                        try {
                            rfile.close();
                        } catch (IOException iox) {
                        }
                    }
                }
                for (int i = 0; i < 1024; i++) {
                    int v = hdr[4 * i] | hdr[4 * i + 1] | hdr[4 * i + 2] | hdr[4 * i + 3];
                    if (v == 0)
                        continue;
                    int xx = (x << 5) | (i & 0x1F);
                    int zz = (z << 5) | ((i >> 5) & 0x1F);

                    chunkCoordinates.add(new Point(xx, zz));
                }
            }
        }

        return chunkCoordinates;
    }
}
