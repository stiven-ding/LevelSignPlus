package com.licrafter.levelSign;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Created by lijx on 16/4/23.
 */
public class DataConfiguration {

    private SignExtend plugin;
    private FileConfiguration configuration = null;
    private File configurationFile = null;
    private String fileName;

    public DataConfiguration(SignExtend plugin, String fileName){
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void reloadDataConfig(){
        if (configurationFile==null){
            configurationFile = new File(plugin.getDataFolder(),fileName);
        }
        configuration = YamlConfiguration.loadConfiguration(configurationFile);
        InputStream defInputStream = plugin.getResource(fileName);
        if (defInputStream != null) {
            InputStreamReader reader = new InputStreamReader(defInputStream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            configuration.setDefaults(defConfig);
        }
    }

    public FileConfiguration getDataConfig() {
        if (configuration == null) {
            this.reloadDataConfig();
        }
        return configuration;
    }

    public void saveDataConfig() {
        if (configuration == null || configurationFile == null){
            return;
        } try {
            getDataConfig().save(configurationFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configurationFile, ex);
        }
    }
}
