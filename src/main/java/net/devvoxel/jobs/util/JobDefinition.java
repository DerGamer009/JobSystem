package net.devvoxel.jobs.util;

import org.bukkit.Material;

public class JobDefinition {

    private final String id;
    private String displayName;
    private String description;
    private String permission;
    private Material icon;

    public JobDefinition(String id, String displayName, String description, String permission, Material icon) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.description = description;
        this.permission = permission;
        this.icon = icon == null ? Material.PAPER : icon;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }
}
