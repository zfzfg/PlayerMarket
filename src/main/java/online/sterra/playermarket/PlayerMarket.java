package online.sterra.playermarket;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import online.sterra.playermarket.commands.PlayerMarketCommand;
import online.sterra.playermarket.listeners.InventoryClickListener;
import online.sterra.playermarket.managers.MarketManager;
import online.sterra.playermarket.managers.GUIManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class PlayerMarket extends JavaPlugin {
    
    private static PlayerMarket instance;
    private MarketManager marketManager;
    private GUIManager guiManager;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Config erstellen
        saveDefaultConfig();
        setupDataFile();
        
        // Manager initialisieren
        this.marketManager = new MarketManager(this);
        this.guiManager = new GUIManager(this);
        
        // Commands registrieren
        getCommand("pm").setExecutor(new PlayerMarketCommand(this));
        getCommand("pm").setTabCompleter(new PlayerMarketCommand(this));
        
        // Listener registrieren
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Daten laden
        marketManager.loadListings();
        
        getLogger().info("PlayerMarket wurde erfolgreich aktiviert!");
    }
    
    @Override
    public void onDisable() {
        // Daten speichern
        if (marketManager != null) {
            marketManager.saveListings();
        }
        
        getLogger().info("PlayerMarket wurde deaktiviert!");
    }
    
    private void setupDataFile() {
        dataFile = new File(getDataFolder(), "listings.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Konnte listings.yml nicht erstellen!", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Konnte listings.yml nicht speichern!", e);
        }
    }
    
    public static PlayerMarket getInstance() {
        return instance;
    }
    
    public MarketManager getMarketManager() {
        return marketManager;
    }
    
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
}