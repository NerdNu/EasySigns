package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import nu.nerd.easysigns.SignData;

public class LaunchAction extends SignAction {

    private Vector vector;
    private boolean valid = true;

    public LaunchAction(SignData sign, String[] args) {
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) >= 10) {
                valid = false;
            } else {
                vector = new Vector(x, y, z);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            valid = false;
        }
    }

    public LaunchAction(SignData sign, ConfigurationSection attributes) {
        this.vector = attributes.getVector("vector");
    }

    @Override
    public String getName() {
        return "launch";
    }

    @Override
    public String getUsage() {
        return "<x> <y> <z>";
    }

    @Override
    public String getHelpText() {
        return "Launches a player with the specified velocity vector. The magnitude of the vector cannot " +
               "exceed 10. That is, sqrt(x^2 + y^2 + z^2) must be < 10.";
    }

    @Override
    public String toString() {
        return String.format("%s %.2f %.2f %.2f", getName(), vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("vector", vector);
        return map;
    }

    @Override
    public boolean action(Player player) {
        player.setVelocity(vector);
        return true;
    }

}
