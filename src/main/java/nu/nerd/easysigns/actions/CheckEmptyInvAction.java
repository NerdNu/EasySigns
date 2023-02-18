package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nu.nerd.easysigns.SignData;

public class CheckEmptyInvAction extends SignAction {

    private final SignData sign;
    private String message;

    public CheckEmptyInvAction(SignData sign, String[] args) {
        this.sign = sign;
        if (args.length > 0) {
            message = String.join(" ", args);
        }
    }

    public CheckEmptyInvAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.message = attributes.getString("message");
    }

    @Override
    public String getName() {
        return "check-empty-inventory";
    }

    @Override
    public String getUsage() {
        return "[<message>]";
    }

    @Override
    public String getHelpText() {
        return "If the player's inventory is not empty, do not execute any subsequent sign actions and show <message>, " +
               "if specified.";
    }

    @Override
    public String toString() {
        if (message != null && message.length() > 0) {
            return String.format("%s %s", getName(), message);
        } else {
            return getName();
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        return map;
    }

    @Override
    public boolean action(Player player) {
        if (hasNonEmptyInv(player)) {
            return false;
        }

        if (message != null) {
            player.sendMessage(ChatColor.RED + substitute(message, player, sign.getBlock()));
        }
        return true;
    }

    private boolean hasNonEmptyInv(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                return true;
            }
        }
        return false;
    }

}
