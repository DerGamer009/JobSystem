package net.devvoxel.jobs.config;

public class GuiSettings {

    private final String title;
    private final int size;
    private final int animationDelay;
    private final String decorativeMaterial;

    public GuiSettings(String title, int size, int animationDelay, String decorativeMaterial) {
        this.title = title;
        this.size = size;
        this.animationDelay = animationDelay;
        this.decorativeMaterial = decorativeMaterial;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public String getDecorativeMaterial() {
        return decorativeMaterial;
    }
}
