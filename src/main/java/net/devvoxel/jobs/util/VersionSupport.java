package net.devvoxel.jobs.util;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility helpers for checking whether the current Paper server version is supported.
 */
public final class VersionSupport {

    private static final int[] MIN_VERSION = {1, 20, 4};
    private static final int[] MAX_VERSION = {1, 21, 10};

    private VersionSupport() {
    }

    public static boolean isSupportedServer() {
        return isSupported(Bukkit.getMinecraftVersion());
    }

    public static String getSupportedRange() {
        return formatVersion(MIN_VERSION) + " - " + formatVersion(MAX_VERSION);
    }

    public static boolean isSupported(String versionString) {
        int[] version = parse(versionString);
        if (version == null) {
            return true;
        }
        return compare(version, MIN_VERSION) >= 0 && compare(version, MAX_VERSION) <= 0;
    }

    private static int compare(int[] left, int[] right) {
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? left[i] : 0;
            int r = i < right.length ? right[i] : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    private static int[] parse(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            return null;
        }
        String[] tokens = versionString.split("[^0-9]+");
        List<Integer> parts = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                try {
                    parts.add(Integer.parseInt(token));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        int[] result = new int[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            result[i] = parts.get(i);
        }
        return result;
    }

    private static String formatVersion(int[] version) {
        return version[0] + "." + version[1] + "." + version[2];
    }
}
