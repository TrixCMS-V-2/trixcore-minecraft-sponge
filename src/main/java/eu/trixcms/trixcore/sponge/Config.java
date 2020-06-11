package eu.trixcms.trixcore.sponge;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class Config {

    private static Config instance = new Config();

    public static Config getInstance() {
        return instance;
    }

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode config;

    public void setup(File file, ConfigurationLoader<CommentedConfigurationNode> loader) {
        this.configLoader = loader;
        if (!file.exists()) {
            try {
                file.createNewFile();
                loadConfig();
                config.getNode("port").setValue(null);
                config.getNode("secretKey").setValue(null);
                saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadConfig();
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}