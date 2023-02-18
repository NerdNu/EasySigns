package nu.nerd.easysigns.actions;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class SleepAction extends SignAction {

    public SleepAction(SignData sign, String[] args) {
    }

    public SleepAction(SignData sign, ConfigurationSection attributes) {
    }

    @Override
    public String getName() {
        return "sleep";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Makes this a sleep sign. Anyone that activates the sign will have their bed respawn point set";
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
        player.setBedSpawnLocation(player.getLocation(), true);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your bed has now been set!");
        return true;
    }

}
