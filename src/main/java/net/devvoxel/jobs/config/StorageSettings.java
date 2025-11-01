package net.devvoxel.jobs.config;

import org.bukkit.configuration.ConfigurationSection;

public class StorageSettings {

    private final ConfigurationSection mysql;
    private final ConfigurationSection sqlite;
    private final ConfigurationSection mongodb;

    public StorageSettings(ConfigurationSection mysql, ConfigurationSection sqlite, ConfigurationSection mongodb) {
        this.mysql = mysql;
        this.sqlite = sqlite;
        this.mongodb = mongodb;
    }

    public ConfigurationSection getMysql() {
        return mysql;
    }

    public ConfigurationSection getSqlite() {
        return sqlite;
    }

    public ConfigurationSection getMongodb() {
        return mongodb;
    }
}
