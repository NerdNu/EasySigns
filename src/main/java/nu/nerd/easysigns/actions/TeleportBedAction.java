package nu.nerd.easysigns.actions;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class TeleportBedAction extends SignAction {

    public TeleportBedAction(SignData sign, String[] args) {
    }

    public TeleportBedAction(SignData sign, ConfigurationSection attributes) {
    }

    @Override
    public String getName() {
        return "tpbed";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Teleports the player back to their bed.";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean action(Player player) {
        if (player.getBedSpawnLocation() != null) {
            player.sendMessage("Returning to your bed");
            Location loc = player.getBedSpawnLocation();
            loc.setDirection(player.getLocation().getDirection());
            player.teleport(loc);
            return true;
        } else {
            player.sendMessage("You don't have a bed set!");
            return false;
        }
    }

}
