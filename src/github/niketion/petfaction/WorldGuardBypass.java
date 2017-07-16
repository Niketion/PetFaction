package github.niketion.petfaction;

import github.niketion.petfaction.file.FilePet;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

class WorldGuardBypass implements Listener {
    /*
      This class has been designed to bypass the
      "mob-spawning" of worldguard or other plugins
     */

    /**
     * Get main instance
     */
    private Main main = Main.getInstance();

    /**
     * Owner's pet
     */
    private Player player;

    WorldGuardBypass(Player player) {
        this.player = player;
    }

    WorldGuardBypass() {
    }

    private boolean force = true;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(CreatureSpawnEvent e){
        if (force){
            e.setCancelled(false);
        }
    }

    void spawn() {
        force = false;
        try {
            for (World worlds : main.getServer().getWorlds())
                for (Entity entities : worlds.getEntities())
                    if (entities.hasMetadata(player.getName())) {
                        entities.remove();
                    }

            if (main.petDeath.contains(player.getName())) {
                player.sendMessage(main.getFormat(main.getConfig().getString("pet-death").replaceAll("%number%", String.valueOf(main.getConfig().getInt("pet-death-minutes")))));
                return;
            }

            String namePet = new FilePet(player).getPetConfig().getString("name");

            // Get pet, set character
            LivingEntity entity = (LivingEntity) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(new FilePet(player).getPetConfig().getString("pet")));
            if (entity instanceof Ageable)
                ((Ageable) entity).setBaby();

            double hearts = (double) main.getConfig().getInt("gui.1.hearts." + new FilePet(player).getPetConfig().getInt("hearts")) * 2;
            entity.setMaxHealth(hearts);
            entity.setHealth(hearts);

            if (!(new FilePet(player).getPetConfig().getInt("level") == 0)) {
                for (PotionEffect effect : player.getActivePotionEffects())
                    player.removePotionEffect(effect.getType());

                for (String strings : new FilePet(player).getPetConfig().getConfigurationSection("potion-pet").getKeys(false)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(strings), main.getConfig().getInt("duration-potion-pet") * 60 * 20,
                            new FilePet(player).getPetConfig().getInt("potion-pet." + strings) - 1));
                }
            }

            if (namePet != null) {
                entity.setCustomName(main.getFormat(namePet + main.getConfig().getString("level-pet").replaceAll("%level%", String.valueOf(new FilePet(player).getPetConfig().getInt("level")))));
            } else {
                entity.setCustomName(main.getFormat(main.getConfig().getString("default-name-pet").replaceAll("%player%", player.getName()) + " " + main.getConfig().getString("level-pet").replaceAll("%level%",
                        String.valueOf(new FilePet(player).getPetConfig().getInt("level")))));
            }
            entity.setMetadata(player.getName(), new FixedMetadataValue(Main.getInstance(), "yes!"));
            main.getPetFollow(player, entity);
        } catch (NullPointerException ignored) {}
        force = true;
    }
}
