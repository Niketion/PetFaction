package github.niketion.petfaction.listener;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Rel;
import github.niketion.petfaction.Main;
import github.niketion.petfaction.file.FilePet;
import github.niketion.petfaction.gui.GUI;
import net.milkbowl.vault.economy.Economy;
import net.redstoneore.legacyfactions.Relation;
import net.redstoneore.legacyfactions.entity.FactionColl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class ListenerPetFaction implements Listener {
    // Get instance of main
    private Main main = Main.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (main.hasPet(event.getPlayer()))
            main.spawnPetHere(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (main.hasPet(event.getPlayer())) {
            for (World worlds : Bukkit.getWorlds())
                for (Entity entities : worlds.getEntities())
                    if (entities.hasMetadata(event.getPlayer().getName()))
                        if (entities instanceof Monster || entities instanceof Animals)
                            entities.remove();

            main.removePotion(event.getPlayer());
        }
    }
    /**
     * On click gui shop
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot() + 1;

            int moneyShop = getConfig().getInt("shop." + slot + ".prize");
            String entityShop = getConfig().getString("shop." + slot + ".entity");

            String potionGUI = getConfig().getString("gui." + slot + ".name");

            if (GUI.getGui(ChatColor.stripColor(event.getInventory().getTitle()))) {
                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(format(getConfig().getString("first-color-shop") + entityShop))) {
                    if (getConfig().getBoolean("shop." + slot + ".only-vip")) {
                        if (player.hasPermission("petfaction.vip")) {
                            balanceExecuteShop(player, moneyShop, entityShop);
                        } else {
                            player.sendMessage(format(getConfig().getString("vip-pet")));
                        }
                    } else {
                        balanceExecuteShop(player, moneyShop, entityShop);
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(format(getConfig().getString("first-color-gui") + potionGUI))) {
                    balanceExecuteGUI(player, slot);
                }
            }
        } catch (NullPointerException ignored) {}
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        try {
            if (event.getDamager() instanceof Player) {
                if (event.getEntity().hasMetadata(((Player) event.getDamager()).getName())) {
                    event.setCancelled(true);
                    ((Player) event.getDamager()).sendMessage(format(getConfig().getString("hits-the-pet")));
                    return;
                }

                try {
                    if (main.getFaction())
                        if (main.getServer().getPluginManager().getPlugin("Factions") != null) {
                            for (World worlds : Bukkit.getWorlds())
                                for (Player players : worlds.getPlayers()) {
                                    Faction factionPlayers = FPlayers.i.get(players).getFaction();
                                    Faction factionDamager = FPlayers.i.get((Player) event.getDamager()).getFaction();
                                    if (event.getEntity().hasMetadata(players.getName()))
                                        if (factionDamager.getRelationWish(factionPlayers).isAtLeast(Rel.ALLY) || factionDamager.equals(factionPlayers)) {
                                            event.setCancelled(true);
                                            ((Player) event.getDamager()).sendMessage(format(getConfig().getString("pet-member-faction")));
                                        }
                                }
                        } else if (main.getServer().getPluginManager().getPlugin("LegacyFactions") != null) {
                            for (World worlds : Bukkit.getWorlds())
                                for (Player players : worlds.getPlayers()) {
                                    net.redstoneore.legacyfactions.entity.Faction factionDamager = FactionColl.get(event.getDamager());
                                    net.redstoneore.legacyfactions.entity.Faction factionPlayer = FactionColl.get(players);
                                    if (event.getEntity().hasMetadata(players.getName())) {
                                        if (factionDamager.getRelationWish(factionPlayer).equals(Relation.ALLY) || factionDamager.equals(factionPlayer)) {
                                            event.setCancelled(true);
                                            ((Player) event.getDamager()).sendMessage(format(getConfig().getString("pet-member-faction")));
                                        }
                                    }
                                }
                        }
                } catch (NoClassDefFoundError error) {
                    main.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[PetFaction] Version of faction too new, please change it with FactionsOne or LegacyFactions");
                    main.getConfig().set("faction-depend", false);
                    main.saveDefaultConfig();
                }
            }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        for (World worlds : Bukkit.getWorlds())
            for (Player player : worlds.getPlayers())
                if (event.getEntity().hasMetadata(player.getName()))
                    if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                        event.setCancelled(true);
                    }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        for (World world : Bukkit.getWorlds()) {
            for (final Player player : world.getPlayers())
                if (event.getEntity().hasMetadata(player.getName())) {
                    event.getDrops().clear();
                    event.getEntity().getKiller().sendMessage(format(getConfig().getString("kill-pet.killer").replaceAll("%player%", player.getName())));
                    player.sendMessage(format(getConfig().getString("kill-pet.owner-pet").replaceAll("%player%", event.getEntity().getKiller().getName())));

                    main.removePotion(player);

                    main.getPetDeath().add(player.getName());
                    main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                        public void run() {
                            main.getPetDeath().remove(player.getName());
                        }
                    }, getConfig().getInt("pet-death-minutes") * 60 * 20);
                }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getPlayer().hasPermission("petfaction.sign"))
            if (event.getLine(0).contains("[PetFaction]"))
                event.setLine(0, ChatColor.DARK_BLUE + "[PetFaction]");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if(block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                if (sign.getLine(0).contains("[PetFaction]"))
                    if (sign.getLine(1).contains("Shop")) {
                        event.getPlayer().performCommand("pet shop");
                    } else if (sign.getLine(1).contains("GUI")) {
                        event.getPlayer().performCommand("pet gui");
                    }
            }
        }
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
     * Get vault economy
     *
     * @return Economy
     */
    private Economy economy() {
        return Main.getInstance().getEconomy();
    }

    /**
     * Format chatcolor
     *
     * @param message - Translate message
     * @return String
     */
    private String format(String message) {
        return Main.getInstance().getFormat(message);
    }

    private void balanceExecuteGUI(Player player, int id) {
        int localLevel = new FilePet(player).getPetConfig().getInt(String.valueOf(id));
        int globalLevel = new FilePet(player).getPetConfig().getInt("level");

        int levelSeeGUI = localLevel+1;
        int prize = getConfig().getInt("gui."+id+".level."+levelSeeGUI);

        if (!(localLevel > getConfig().getInt("potion-max-amplifier") - 1)) {
            if (economy().getBalance(player) >= prize) {
                new FilePet(player).set("level", globalLevel + 1);
                new FilePet(player).set(String.valueOf(id), localLevel + 1);

                economy().withdrawPlayer(player, prize);
                player.sendMessage(format(getConfig().getString("take-money").replaceAll("%money%", String.valueOf(prize))));
                player.closeInventory();

                main.spawnPetHere(player);
            } else {
                player.sendMessage(format(getConfig().getString("not-enough-money")));
            }
        } else {
            player.sendMessage(format(getConfig().getString("max-reached")));
        }
    }

    /**
     * Execute shop buy pet
     *
     * @param player - Owner pet
     * @param money - Money to take
     * @param entity - Pet
     */
    private void balanceExecuteShop(Player player, int money, String entity) {
        if (economy().getBalance(player) >= money) {
            // Create file of player-pet
            new FilePet(player).set("pet", entity);

            // Take money by player's balance
            economy().withdrawPlayer(player, money);
            player.sendMessage(format(getConfig().getString("take-money").replaceAll("%money%", String.valueOf(money))));
            player.closeInventory();

            main.spawnPetHere(player);
        } else {
            player.sendMessage(format(getConfig().getString("not-enough-money")));
        }
    }
}
