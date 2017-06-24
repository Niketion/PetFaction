package github.niketion.petfaction.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class FilePet {
    /**
     * File for pet-player
     */
    private File petFile;
    private FileConfiguration petConfig;

    /**
     * Get player's file pet
     *
     * @param player - Owner file pet
     */
    public FilePet(Player player) {
        petFile = new File("plugins/PetFaction/userpet/" + player.getName() + ".yml");
        petConfig = YamlConfiguration.loadConfiguration(petFile);
    }

    /**
     * Save file of pet-player
     */
    public void saveNewConfig(){
        try{
            petConfig.save(petFile);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Get pet-user config
     *
     * @return FileConfiguration
     */
    public FileConfiguration getPetConfig() {
        return petConfig;
    }

    /**
     * Set a value and save file pet-user
     *
     * @param path - Path in the file
     * @param value - Value in the file
     */
    public void set(String path, Object value) {
        petConfig.set(path, value);
        try{
            petConfig.save(petFile);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
