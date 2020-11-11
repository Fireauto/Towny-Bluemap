package com.github.Jena;

import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class TownyBlue extends JavaPlugin {
    public static Plugin plugin;
    public static FileConfiguration config;
    public static MarkerSet set;
    public static MarkerAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setConfig();
        setPlugin();
        TownyBlueUpdater.CompleteUpdate();
    }

    public void setPlugin() {
        plugin = this;
    }

    public void setConfig() {
        config = this.getConfig();
    }

    @Override
    public Logger getLogger() {
        return this.getServer().getLogger();
    }

}
