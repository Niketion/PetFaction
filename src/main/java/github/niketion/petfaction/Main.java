package github.niketion.petfaction;

import github.niketion.petfaction.command.CommandPet;
import github.niketion.petfaction.file.FilePet;
import github.niketion.petfaction.listener.ListenerPetFaction;
import github.niketion.petfaction.petfollow.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Main class
 */
public class Main extends JavaPlugin {

    private PetFollow petFollow;
    private PluginManager pluginManager = getServer().getPluginManager();
    private ArrayList<String> petDeath = new ArrayList<>();

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
     * The method is invoked at the time the plugin was enabled
     */
    @Override
    public void onEnable() {
        if (setupPetFollow() && setupEconomy()) {
            instance = this;
            log(Level.INFO, "Enabling the plugin...", 0);

            saveDefaultConfig();

            try {
                pluginManager.registerEvents(new ListenerPetFaction(), this);
                getCommand("pet").setExecutor(new CommandPet());
            } catch (Exception exception) {
                log(null, ChatColor.RED+"Error on load commands/listeners, exception: "+exception.getClass().getSimpleName(), 1);

                pluginManager.disablePlugin(this);
            }
        } else {
            log(null, ChatColor.RED+" Failed to setup PetFaction", 1);
            log(null, ChatColor.RED+" Your server version is compatible with this plugin (1.7.x-1.12.x)?", 1);
            log(null, ChatColor.RED+" You've install depends? (Depend: "+getDescription().getSoftDepend()+")", 1);

            pluginManager.disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (Player players : getServer().getOnlinePlayers())
            for (World worlds : getServer().getWorlds())
                for (Entity entities : worlds.getEntities())
                    if (entities.hasMetadata(players.getName()))
                        if (entities instanceof Monster || entities instanceof Animals) {
                            entities.remove();
                            players.sendMessage(getFormat(getConfig().getString("pet-despawn")));
                            removePotion(players);
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
    private void getPetFollow(Player player, Entity entity) {
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
     * Spawn pet of player in his location
     *
     * @param player - Pet owner
     */
    public void spawnPetHere(Player player) {
        for (World worlds : getServer().getWorlds())
            for (Entity entities : worlds.getEntities())
                if (entities.hasMetadata(player.getName()))
                    entities.remove();

        if (petDeath.contains(player.getName())) {
            player.sendMessage(getFormat(getConfig().getString("pet-death").replaceAll("%number%", String.valueOf(getConfig().getInt("pet-death-minutes")))));
            return;
        }

        String namePet = new FilePet(player).getPetConfig().getString("name");

        // Get pet, set character
        LivingEntity entity = (LivingEntity) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(new FilePet(player).getPetConfig().getString("pet")));
        if (entity instanceof Ageable)
            ((Ageable) entity).setBaby();

        double hearts = (double) getConfig().getInt("gui.1.hearts." + new FilePet(player).getPetConfig().getInt("1"))*2;
        entity.setMaxHealth(hearts);
        entity.setHealth(hearts);

        if (!(new FilePet(player).getPetConfig().getInt("level") == 0)) {
            for (PotionEffect effect : player.getActivePotionEffects())
                player.removePotionEffect(effect.getType());

            for(int i=2; i < 7; i ++) {
                if (getNumber(player, i) != 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(getConfig().getString("gui."+i+".type")), Integer.MAX_VALUE,
                            new FilePet(player).getPetConfig().getInt(String.valueOf(i))));
                }
            }
        }

        if (namePet != null) {
            entity.setCustomName(getFormat(namePet + getConfig().getString("level-pet").replaceAll("%level%", String.valueOf(new FilePet(player).getPetConfig().getInt("level")))));
        } else {
            entity.setCustomName(getFormat(getConfig().getString("default-name-pet").replaceAll("%player%", player.getName()) + " " + getConfig().getString("level-pet").replaceAll("%level%",
                    String.valueOf(new FilePet(player).getPetConfig().getInt("level")))));
        }
        entity.setMetadata(player.getName(), new FixedMetadataValue(Main.getInstance(), "yes!"));
        getPetFollow(player, entity);
    }

    private int getNumber(Player player, int number) {
        return new FilePet(player).getPetConfig().getInt(String.valueOf(number));
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
