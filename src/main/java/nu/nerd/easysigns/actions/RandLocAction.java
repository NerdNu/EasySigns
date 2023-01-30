package nu.nerd.easysigns.actions;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nu.nerd.easysigns.SignData;

public class RandLocAction extends SignAction {

    private final SignData sign;
    private int minDistance;
    private int maxDistance;
    private boolean valid = true;

    /**
     * Set to true when the action fails in order to stop subsequent actions.
     */
    boolean shouldExit;

    /**
     * Natural or player-placed materials that harm the player when stood upon.
     */
    private static EnumSet<Material> HARMFUL_MATERIALS = EnumSet.of(Material.FIRE, Material.LAVA, Material.MAGMA_BLOCK,
                                                                    Material.CACTUS, Material.CAMPFIRE, Material.SOUL_CAMPFIRE);

    /**
     * Maximum number of attempts to find a safe location before giving up.
     */
    private static int MAX_RETRIES = 10;

    public RandLocAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            if (args.length == 1) {
                minDistance = 0;
                maxDistance = Integer.parseInt(args[0]);
            } else if (args.length == 2) {
                minDistance = Integer.parseInt(args[0]);
                maxDistance = Integer.parseInt(args[1]);
            }
            valid = (args.length >= 1 && args.length <= 2 && minDistance >= 0 && maxDistance >= minDistance);
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            valid = false;
        }
    }

    public RandLocAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        // Backwards compatibility. Originally only "distance" was supported.
        if (attributes.contains("distance")) {
            this.minDistance = 0;
            this.maxDistance = attributes.getInt("distance");
        } else {
            this.minDistance = attributes.getInt("min-distance");
            this.maxDistance = attributes.getInt("max-distance");
        }
    }

    @Override
    public String getName() {
        return "randloc";
    }

    @Override
    public String getUsage() {
        return "[<min-distance>] <max-distance>";
    }

    @Override
    public String getHelpText() {
        return "Randomly spawns the player between <min-distance> and <max-distance> blocks from 0,0. "
               + "If <min-distance> is omitted, it defaults to 0. "
               + "If a safe location cannot be found after 10 tries, subsequent sign actions are aborted.";
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", getName(), minDistance, maxDistance);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("min-distance", minDistance);
        map.put("max-distance", maxDistance);
        return map;
    }

    @Override
    public boolean shouldExit(Player player) {
        return shouldExit;
    }

    @Override
    public void action(Player player) {
        shouldExit = false;
        for (int i = 0; i < MAX_RETRIES; ++i) {
            double angle = 2 * Math.PI * Math.random();
            double range = minDistance + (maxDistance - minDistance) * Math.random();
            int x = (int) (range * Math.cos(angle));
            int z = (int) (range * Math.sin(angle));
            int y = player.getWorld().getHighestBlockYAt(x, z);
            Location loc = new Location(player.getWorld(), x, y, z);
            Block block = loc.getBlock();

            // Disallow passable blocks (e.g. signs), which offer no support.
            if (!HARMFUL_MATERIALS.contains(block.getType()) && !block.isPassable()) {
                // Centre the player horizontally to avoid suffocation.
                // Put their feet into the space above the block.
                player.teleport(loc.add(0.5, 1.0, 0.5));
                return;
            }
        }

        // Give up.
        player.sendMessage(ChatColor.RED + "Sorry! We couldn't find a safe place to put you.");
        shouldExit = true;
    }

}
