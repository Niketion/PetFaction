package github.niketion.petfaction;

import github.niketion.petfaction.file.FilePet;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
  This class has been designed to bypass the
  "mob-spawning" of worldguard or other plugins
 */
class SpawnEntity implements Listener {

    private Main main = Main.getInstance();

    private Player player;

    /** Check if entity spawned is a pet */
    private boolean isPet = false;

    SpawnEntity(Player player) {
        this.player = player;
    }

    SpawnEntity() { }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().contains("/pet")) {
            isPet = true;
            main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                @Override
                public void run() {
                    isPet = false;
                }
            }, 5L);
        }
    }

    private boolean force = true;
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event){
        if (force){
            if (isPet)
                event.setCancelled(false);
        }
    }

    void spawn() {
        force = false;
        try {
            FileConfiguration config = main.getConfig();
            FileConfiguration petConfig = new FilePet(player).getPetConfig();
            World worldPlayer = player.getWorld();

            for (World worlds : main.getServer().getWorlds())
                for (Entity entities : worlds.getEntities())
                    if (entities.hasMetadata(player.getName())) {
                        entities.remove();
                    }

            if (main.petDeath.contains(player.getName())) {
                player.sendMessage(main.getFormat(config.getString("pet-death").replaceAll("%number%", String.valueOf(config.getInt("pet-death-minutes")))));
                return;
            }

            String namePet = petConfig.getString("name");

            // Get pet, set character
            LivingEntity entity = (LivingEntity) worldPlayer.spawnEntity(player.getLocation(), EntityType.valueOf(petConfig.getString("pet")));
            if (entity instanceof Ageable) {
                ((Ageable) entity).setBaby();
                ((Ageable) entity).setAgeLock(true);
            }

            double hearts = (double) config.getInt("gui.1.hearts." + petConfig.getInt("hearts")) * 2;
            entity.setMaxHealth(hearts);
            entity.setHealth(hearts);

            if (!(petConfig.getInt("level") == 0)) {
                for (PotionEffect effect : player.getActivePotionEffects())
                    player.removePotionEffect(effect.getType());

                for (String strings : petConfig.getConfigurationSection("potion-pet").getKeys(false)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(strings), config.getInt("duration-potion-pet") * 60 * 20,
                            petConfig.getInt("potion-pet." + strings) - 1));
                }
            }

            if (namePet != null) {
                entity.setCustomName(main.getFormat(namePet + config.getString("level-pet").replaceAll("%level%", String.valueOf(petConfig.getInt("level")))));
            } else {
                entity.setCustomName(main.getFormat(config.getString("default-name-pet").replaceAll("%player%", player.getName()) + " " + config.getString("level-pet").replaceAll("%level%",
                        String.valueOf(petConfig.getInt("level")))));
            }
            entity.setMetadata(player.getName(), new FixedMetadataValue(Main.getInstance(), "yes!"));

            main.getPetFollow(player, entity);
        } catch (NullPointerException ignored) {}
        force = true;
    }
}
