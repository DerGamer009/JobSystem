package net.devvoxel.jobs.util;

import org.bukkit.Material;

public class JobDefinition {

    private final String id;
    private String displayName;
    private String description;
    private String permission;
    private Material icon;
    private double experiencePerAction;
    private double moneyPerAction;

    public JobDefinition(String id, String displayName, String description, String permission, Material icon,
                         double experiencePerAction, double moneyPerAction) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.description = description;
        this.permission = permission;
        this.icon = icon == null ? Material.PAPER : icon;
        this.experiencePerAction = experiencePerAction;
        this.moneyPerAction = moneyPerAction;
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

    public double getExperiencePerAction() {
        return experiencePerAction;
    }

    public void setExperiencePerAction(double experiencePerAction) {
        this.experiencePerAction = experiencePerAction;
    }

    public double getMoneyPerAction() {
        return moneyPerAction;
    }

    public void setMoneyPerAction(double moneyPerAction) {
        this.moneyPerAction = moneyPerAction;
    }
}
