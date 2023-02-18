package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class MsgAction extends SignAction {

    private String message;
    boolean valid = true;

    public MsgAction(SignData sign, String[] args) {
        if (args.length > 0) {
            message = translate(String.join(" ", args));
        } else {
            valid = false;
        }
    }

    public MsgAction(SignData sign, ConfigurationSection attributes) {
        this.message = attributes.getString("message");
    }

    @Override
    public String getName() {
        return "msg";
    }

    @Override
    public String getUsage() {
        return "<message...>";
    }

    @Override
    public String getHelpText() {
        return "Sends the player a message. Color codes with & are supported.";
    }

    @Override
    public String toString() {
        return String.format("%s %s", getName(), message);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        return map;
    }

    @Override
    public boolean action(Player player) {
        player.sendMessage(message);
        return true;
    }

}
