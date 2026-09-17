package org.Plugins.OPMace;

import org.bukkit.plugin.java.JavaPlugin;

public class OPMace extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new OPMaceListener(this), this);

        getLogger().info("OPMace has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPMace has been disabled!");
    }
}