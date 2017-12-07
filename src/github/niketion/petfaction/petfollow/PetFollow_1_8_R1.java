package github.niketion.petfaction.petfollow;

import github.niketion.petfaction.Main;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.PathEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PetFollow_1_8_R1 implements PetFollow {

    @Override
    public void petFollow(final Player player, final Entity pet) {
        new BukkitRunnable() {
            public void run() {
                try {
                    if ((!pet.isValid() || (!player.isOnline()))) {
                        this.cancel();
                    }
                    net.minecraft.server.v1_8_R1.Entity pett = ((CraftEntity) pet).getHandle();
                    ((EntityInsentient) pett).getNavigation().a(2);
                    Object petf = ((CraftEntity) pet).getHandle();
                    Location targetLocation = player.getLocation();
                    PathEntity path;
                    path = ((EntityInsentient) petf).getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1);
                    if (path != null) {
                        ((EntityInsentient) petf).getNavigation().a(path, 1.0D);
                        ((EntityInsentient) petf).getNavigation().a(2.0D);
                    }

                    ((EntityInsentient) petf).getAttributeInstance(GenericAttributes.d).setValue(0.16000000298023224D);

                    if (Bukkit.getPlayer(player.getName()).getLocation().getWorld().equals(pet.getLocation().getWorld())) {
                        int distance = (int) Bukkit.getPlayer(player.getName()).getLocation().distance(pet.getLocation());
                        if (distance > 10 && !pet.isDead() && player.isOnGround()) {
                            pet.teleport(player.getLocation());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

}
