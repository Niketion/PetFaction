package github.niketion.petfaction.command;

import github.niketion.petfaction.Main;
import github.niketion.petfaction.file.FilePet;
import github.niketion.petfaction.gui.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CommandPet implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            listArguments(commandSender);
            return false;
        }

        assert commandSender instanceof Player;
        Player player = (Player) commandSender;

        Inventory inventory;
        switch (strings[0]) {
            case "shop":
                if (hasPermission(player, "shop")) {
                    if (Main.getInstance().hasPet(player)) {
                        player.sendMessage(format(getString("already-have-pet")));
                        return false;
                    }

                    inventory = new GUI(getString("shop-name")).getInventory();
                    for (int i = 1; i < getConfig().getConfigurationSection("shop").getKeys(false).size() + 1; i++) {
                        inventory.setItem(i - 1, itemStackShop(i));
                    }
                    player.openInventory(inventory);
                    return true;
                }
            case "here":
                if (hasPermission(player, "here"))
                    if (Main.getInstance().hasPet(player)) {
                        Main.getInstance().spawnPetHere(player);
                    }
                return true;
            case "away":
                if (hasPermission(player, "away"))
                    if (Main.getInstance().hasPet(player)) {
                        // "Kill" pet
                        for (World worlds : Bukkit.getServer().getWorlds())
                            for (Entity entities : worlds.getEntities())
                                if (entities.hasMetadata(player.getName()))
                                    entities.remove();

                        player.sendMessage(format(getString("pet-despawn")));
                        Main.getInstance().removePotion(player);
                    }
                return true;
            case "name":
                if (hasPermission(player, "name"))
                    if (Main.getInstance().hasPet(player)) {
                        String name = "";
                        for (int i = 1; i != strings.length; i++)
                            name += strings[i] + " ";
                        if (!(name.length() > 28)) {
                            new FilePet(player).set("name", name);
                            Main.getInstance().spawnPetHere(player);
                        } else {
                            player.sendMessage(format(getString("max-char-name")));
                        }
                    }

                return true;
            case "change":
                if (hasPermission(player, "change"))
                    if (Main.getInstance().hasPet(player)) {
                        inventory = new GUI(getString("shop-name")).getInventory();
                        for (int i = 1; i < getConfig().getConfigurationSection("shop").getKeys(false).size() + 1; i++) {
                            inventory.setItem(i - 1, itemStackShop(i));
                        }
                        player.openInventory(inventory);
                    }
                return true;
            case "gui":
                if (hasPermission(player, "gui"))
                    if (Main.getInstance().hasPet(player)) {
                        inventory = new GUI(getString("gui-name")).getInventory();
                        for (int i = 1; i < getConfig().getConfigurationSection("gui").getKeys(false).size() + 1; i++) {
                            inventory.setItem(i - 1, itemStackGUI(i, player));
                        }
                        player.openInventory(inventory);
                    }
                return true;
        }

        listArguments(commandSender);
        commandSender.sendMessage(format(getString("arguments-not-found")));
        return false;
    }

    /**
     * List arguments of command and credits
     *
     * @param commandSender - Who receives the message
     */
    private void listArguments(CommandSender commandSender) {
        for (String loopMessages : getConfig().getStringList("list-commands")) {
            commandSender.sendMessage(format(loopMessages));
        }
        commandSender.sendMessage(ChatColor.DARK_AQUA+"Plugin developed by "+ChatColor.AQUA+"@Niketion");
    }

    /**
     * Check if player has permission
     *
     * @param value - Value permission
     * @return Boolean
     */
     private boolean hasPermission(Player player, String value) {
         if (player.hasPermission("petfaction."+value)) {
             return true;
         } else {
             player.sendMessage(format(getString("permission-denied")));
             return false;
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
     * Set the monster_egg for the shop
     *
     * @param id - Slot (in the for)
     * @return ItemStack
     */
    private ItemStack itemStackShop(int id) {
        String displayName = format(getString("first-color-shop") + getString("shop." + id + ".entity"));
        List<String> lore = Arrays.asList((format(getString("shop-lore").replaceAll("%amount%", String.valueOf(getConfig().getInt("shop." + id + ".prize")))
                .replaceAll("%boolean%", String.valueOf(getConfig().getBoolean("shop." + id + ".only-vip"))))).split("\n"));

        if (Main.getInstance().getVersionServer("1.12") || Main.getInstance().getVersionServer("1.11") || Main.getInstance().getVersionServer("1.10") || Main.getInstance().getVersionServer("1.9")) {
            ItemStack item = new ItemStack(Material.MOB_SPAWNER);
            ItemMeta itemMeta = item.getItemMeta();

            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(lore);

            item.setItemMeta(itemMeta);
            return item;
        } else {
            ItemStack item = new ItemStack(Material.MONSTER_EGG, 1, (short) getConfig().getInt("shop." + id + ".data-egg"));
            ItemMeta itemMeta = item.getItemMeta();

            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(lore);

            item.setItemMeta(itemMeta);
            return item;
        }
    }

    /**
     * Set the item for the GUI
     *
     * @param id - Slot (in the for)
     * @param player - Owner pet
     * @return ItemStack
     */
    private ItemStack itemStackGUI(int id, Player player) {
        ItemStack item = new ItemStack(getConfig().getInt("gui."+id+".id"), 1, (short) getConfig().getInt("gui."+id+".data"));
        ItemMeta itemMeta = item.getItemMeta();

        int localLevel = new FilePet(player).getPetConfig().getInt(String.valueOf(id));
        int globalLevel = new FilePet(player).getPetConfig().getInt("level");

        int levelSeeGUI = localLevel+1;

        String prize = String.valueOf(getConfig().getInt("gui."+id+".level."+levelSeeGUI));

        itemMeta.setDisplayName(format(getString("first-color-gui") + getString("gui."+id+".name")));

        if (!(localLevel > getConfig().getConfigurationSection("gui."+id+".level").getKeys(false).size()-1)) {
            itemMeta.setLore(Arrays.asList(format(getString("gui-lore").replaceAll("%levelPet%", String.valueOf(localLevel))
                    .replaceAll("%globalLevelPet%", String.valueOf(globalLevel))
                    .replaceAll("%prize%", prize)).split("\n")));
        } else {
            itemMeta.setLore(Arrays.asList(format(getString("gui-lore").replaceAll("%levelPet%", String.valueOf("MAX"))
                    .replaceAll("%globalLevelPet%", String.valueOf(globalLevel))
                    .replaceAll("%prize%", "MAX")).split("\n")));
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    /**
     * Translate message chatcolor
     *
     * @param message - Message to translate
     * @return String
     */
    private String format(String message) {
        return Main.getInstance().getFormat(message);
    }

    /**
     * Get string from config.yml
     *
     * @param path - Path
     * @return String
     */
    private String getString(String path) {
        return getConfig().getString(path);
    }
}
