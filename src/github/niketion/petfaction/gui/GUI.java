package github.niketion.petfaction.gui;

import github.niketion.petfaction.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

/**
 * Construction of the GUI
 */
public class GUI {

    private String name;
    private Inventory inventory;
    private static ArrayList<String> gui = new ArrayList<>();

    public GUI(String name) {
        this.name = name;
        int shopRows = ((getConfig().getConfigurationSection("shop").getKeys(false).size()/9)+1)*9;
        int guiRows = ((getConfig().getConfigurationSection("gui").getKeys(false).size()/9)+1)*9;

        int rows = Math.max(shopRows, guiRows);
        inventory = Bukkit.createInventory(null, rows, Main.getInstance().getFormat(name));

        gui.add(name);
    }

    /**
     * Get GUI
     *
     * @return Inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get config.yml
     *
     * @return FileConfiguration
     */
    private FileConfiguration getConfig() {
        return Main.getInstance().getConfig();
    }

    /**
     * Check if the gui was created by the plugin
     *
     * @return boolean
     */
    public boolean getGui() {
        return gui.contains(this.name);
    }
}
