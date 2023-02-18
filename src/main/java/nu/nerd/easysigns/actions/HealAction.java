package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class HealAction extends SignAction {
    private final float ONE_TICK_SECONDS = 0.05f;
    float gap = 0;
    boolean valid = true;

    public HealAction(SignData sign, String[] args) {
        if (args.length > 0) {
            try {
                gap = Float.parseFloat(args[0]);
                if (gap < 0) {
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                valid = false;
            }
        }
    }

    public HealAction(SignData sign, ConfigurationSection attributes) {
        this.gap = (float) attributes.getDouble("gap");
    }

    @Override
    public String getName() {
        return "heal";
    }

    @Override
    public String getUsage() {
        return "[<gap>]";
    }

    @Override
    public String getHelpText() {
        return "Refills a player's health. If gap is provided (it defaults to 0) then the player gets a half a " +
               "heart every gap seconds. 0 means fill it up instantly.";
    }

    @Override
    public String toString() {
        return String.format("%s %.1g", getName(), gap);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("gap", gap);
        return map;
    }

    @Override
    public boolean action(Player player) {
        final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        if (gap < ONE_TICK_SECONDS) {
            player.setHealth(maxHealth);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getHealth() < maxHealth) {
                        player.setHealth(Math.min(player.getHealth() + 1.0, maxHealth));
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(EasySigns.instance, 20L, Math.round(20L * gap));
        }
        return true;
    }

}
