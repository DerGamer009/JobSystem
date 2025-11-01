package net.devvoxel.jobs.config;

public class ActionFeedbackSettings {

    private final boolean enabled;
    private final String sound;
    private final float volume;
    private final float pitch;

    public ActionFeedbackSettings(boolean enabled, String sound, float volume, float pitch) {
        this.enabled = enabled;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
