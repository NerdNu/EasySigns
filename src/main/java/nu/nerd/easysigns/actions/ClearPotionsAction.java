package nu.nerd.easysigns.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import nu.nerd.easysigns.SignData;

public class ClearPotionsAction extends SignAction {

    public ClearPotionsAction(SignData sign, String[] args) {
    }

    public ClearPotionsAction(SignData sign, ConfigurationSection attributes) {
    }

    @Override
    public String getName() {
        return "clearpotions";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Clears all potion effects.";
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
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        return true;
    }

}
