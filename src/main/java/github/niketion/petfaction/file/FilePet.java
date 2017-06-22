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

    public FileConfiguration getPetConfig() {
        return petConfig;
    }

    public void set(String path, Object value) {
        petConfig.set(path, value);
        try{
            petConfig.save(petFile);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
