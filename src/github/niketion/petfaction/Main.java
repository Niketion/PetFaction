package github.niketion.petfaction;

import github.niketion.petfaction.command.CommandPet;
import github.niketion.petfaction.listener.ListenerPetFaction;
import github.niketion.petfaction.petfollow.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Main class
 */
public class Main extends JavaPlugin {

    private PetFollow petFollow;
    private PluginManager pluginManager = getServer().getPluginManager();
    ArrayList<String> petDeath = new ArrayList<>();

    /**
     * Instance the class
     */
    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    /**
     * Setup vault
     */
    private Economy economy = null;

    private boolean setupEconomy() {
        if (pluginManager.getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        }
    }

    /**
     * Check if "Faction" plugin is enabled
     *
     * @return Boolean
     */
    public boolean getFaction() {
        if (getConfig().getBoolean("faction-depend"))
            if (pluginManager.getPlugin("Factions") != null || pluginManager.getPlugin("LegacyFactions") != null)
                if (getConfig().getBoolean("faction-depend"))
                    return true;
        return false;
    }

    /**
     * The method is invoked at the time the plugin was enabled
     */
    @Override
    public void onEnable() {
        if (setupPetFollow() && setupEconomy()) {
            if (!getFaction()) {
                log(Level.INFO, "Factions not found.", 0);
            }

            instance = this;
            log(Level.INFO, "Enabling the plugin...", 0);

            saveDefaultConfig();

            try {
                pluginManager.registerEvents(new ListenerPetFaction(), this);
                pluginManager.registerEvents(new WorldGuardBypass(), this);
                getCommand("pet").setExecutor(new CommandPet());
            } catch (Exception exception) {
                log(null, ChatColor.RED+"Error on load commands/listeners, exception: "+exception.getClass().getSimpleName(), 1);

                pluginManager.disablePlugin(this);
                return;
            }

            //Set adult pet to baby (3 minutes)
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    for (World worlds : Bukkit.getWorlds())
                        for (Entity entities : worlds.getEntities())
                            if (entities instanceof Ageable)
                                if (((Ageable) entities).isAdult())
                                    ((Ageable) entities).setBaby();
                }
            }, 60 * 3);
        } else {
            log(null, ChatColor.RED + " Failed to setup PetFaction", 1);
            log(null, ChatColor.RED + " Your server version is compatible with this plugin (1.7.x-1.12.x)? Version server is " + getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3], 1);
            log(null, ChatColor.RED + " You've install depends? (Depend: " + getDescription().getDepend() + " and a plugin support economy)", 1);
            log(null, ChatColor.RED + " (SoftDepend: " + getDescription().getSoftDepend() + ")", 1);

            pluginManager.disablePlugin(this);
        }
    }

    /**
     * The method is invoked at the time the plugin was disabled
     */
    @Override
    public void onDisable() {
        removeAllPet();
    }

    /**
     * Remove all pet in the server
     */
    private void removeAllPet() {
        for (World worlds : getServer().getWorlds())
            for (Player players : worlds.getPlayers())
                for (Entity entities : worlds.getEntities())
                    if (entities.hasMetadata(players.getName()))
                        if (entities instanceof Monster || entities instanceof Animals) {
                            entities.remove();
                            players.sendMessage(getFormat(getConfig().getString("pet-despawn")));
                            removePotion(players);
                            Bukkit.createInventory(null, InventoryType.ANVIL);
                        }
    }

    /**
     * Remove all potion to player
     *
     * @param player - Who has the potions
     */
    public void removePotion(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }

    /**
     * Log message
     *
     * @param level - Level warning
     * @param messageLog - Message to be sent
     * @param type - Type of log
     */
    private void log(Level level, String messageLog, int type) {
        if (type == 0) {
            getLogger().log(level, messageLog);
        } else {
            getServer().getConsoleSender().sendMessage("["+getDescription().getName()+"]"+messageLog);
        }
    }

    /**
     * Translate color of config.yml
     *
     * @param message - Message to be translate
     * @return String
     */
    public String getFormat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Setup version of server (Check version of server)
     * Support: 1.7.x - 1.8.x
     *
     * @return Boolean
     */
    private boolean setupPetFollow() {
        String version;
        try {
            version = getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            return false;
        }

        switch (version) {
            case "v1_12_R1":
                petFollow = new PetFollow_1_12_R1();
                break;
            case "v1_11_R1":
                petFollow = new PetFollow_1_11_R1();
                break;
            case "v1_10_R1":
                petFollow = new PetFollow_1_10_R1();
                break;
            case "v1_9_R2":
                petFollow = new PetFollow_1_9_R2();
                break;
            case "v1_9_R1":
                petFollow = new PetFollow_1_9_R1();
                break;
            case "v1_8_R1":
                petFollow = new PetFollow_1_8_R1();
                break;
            case "v1_8_R2":
                petFollow = new PetFollow_1_8_R2();
                break;
            case "v1_8_R3":
                petFollow = new PetFollow_1_8_R3();
                break;
            case "v1_7_R1":
                petFollow = new PetFollow_1_7_R1();
                break;
            case "v1_7_R2":
                petFollow = new PetFollow_1_7_R2();
                break;
            case "v1_7_R3":
                petFollow = new PetFollow_1_7_R3();
                break;
            case "v1_7_R4":
                petFollow = new PetFollow_1_7_R4();
                break;
        }
        return petFollow != null;
    }

    /**
     * Get follow pet by interface
     */
    void getPetFollow(Player player, Entity entity) {
        petFollow.petFollow(player, entity);
    }

    /**
     * Get economy of vault
     *
     * @return Economy;
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Get version of server
     *
     * @return String
     */
    public boolean getVersionServer(String version) {
        return getServer().getVersion().contains(version);
    }

    /**
     * Spawn pet of player in his location
     *
     * @param player - Pet owner
     */
    @Deprecated
    public void spawnPetHere(Player player) {
        new WorldGuardBypass(player).spawn();
    }

    /**
     * Check if pet was death
     */
    public ArrayList<String> getPetDeath() {
        return petDeath;
    }

    /**
     * Check if player has a pet
     *
     * @param player - Owner pet
     * @return Boolean
     */
    public boolean hasPet(Player player) {
        if (new File(Main.getInstance().getDataFolder() + "/userpet/"+player.getName()+".yml").exists()) {
            return true;
        } else {
            player.sendMessage(getFormat(getConfig().getString("have-not-pet")));
            return false;
        }
    }
}
