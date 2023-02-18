package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class SetBedAction extends SignAction {

    private Location loc;
    private boolean valid = true;

    public SetBedAction(SignData sign, String[] args) {
        int x, y, z;
        World world;
        try {
            if (args.length == 4) {
                world = Bukkit.getWorld(args[0]);
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
                loc = new Location(world, x, y, z);
            } else {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
                loc = new Location(sign.getBlock().getWorld(), x, y, z);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            valid = false;
        }
    }

    public SetBedAction(SignData sign, ConfigurationSection attributes) {
        this.loc = (Location) attributes.get("loc");
    }

    @Override
    public String getName() {
        return "setbed";
    }

    @Override
    public String getUsage() {
        return "[<world>] <x> <y> <z>";
    }

    @Override
    public String getHelpText() {
        return "Sets the player's bed location at the specified position.";
    }

    @Override
    public String toString() {
        return String.format(
                             "%s %s %d %d %d",
                             getName(),
                             loc.getWorld().getName(),
                             loc.getBlockX(),
                             loc.getBlockY(),
                             loc.getBlockZ());
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("loc", loc);
        return map;
    }

    @Override
    public boolean action(Player player) {
        player.setBedSpawnLocation(loc, true);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your bed has now been set!");
        return true;
    }

}
