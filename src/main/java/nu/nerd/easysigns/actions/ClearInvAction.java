package nu.nerd.easysigns.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class ClearInvAction extends SignAction {

    public ClearInvAction(SignData sign, String[] args) {
    }

    public ClearInvAction(SignData sign, ConfigurationSection attributes) {
    }

    @Override
    public String getName() {
        return "ci";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Clears the player's inventory";
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
        player.getInventory().clear();
        return true;
    }

}
