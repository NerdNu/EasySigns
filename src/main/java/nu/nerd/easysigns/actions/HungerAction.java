package nu.nerd.easysigns.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class HungerAction extends SignAction {

    public HungerAction(SignData sign, String[] args) {
    }

    public HungerAction(SignData sign, ConfigurationSection attributes) {
    }

    @Override
    public String getName() {
        return "hunger";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Refills a player's hunger bar";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean action(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20);
        return true;
    }

}
