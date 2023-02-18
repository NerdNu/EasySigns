package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class CmdAction extends SignAction {

    private final SignData sign;
    private String command;
    boolean valid = true;

    public CmdAction(SignData sign, String[] args) {
        this.sign = sign;
        if (args.length > 0) {
            command = String.join(" ", args);
        } else {
            valid = false;
        }
    }

    public CmdAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.command = attributes.getString("command");
    }

    @Override
    public String getName() {
        return "cmd";
    }

    @Override
    public String getUsage() {
        return "<command>";
    }

    @Override
    public String getHelpText() {
        return "Runs a command as the user. Omit the leading slash.";
    }

    @Override
    public String toString() {
        return String.format("%s %s", getName(), command);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("command", command);
        return map;
    }

    @Override
    public boolean action(Player player) {
        try {
            return player.performCommand(substitute(command, player, sign.getBlock()));
        } catch (Exception ex) {
            EasySigns.instance.getLogger().severe("EasySigns: " + getName() + ": " +
                                                  ex.getClass().getName() + ": " + ex.getMessage());
            return false;
        }
    }

}
