package nu.nerd.easysigns.actions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class WarpAction extends SignAction {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.#");
    private Location loc;
    private boolean valid;

    /**
     * If non-null, this overrides the player's yaw angle (degrees) on teleport.
     */
    private Double yaw;

    /**
     * If non-null, this overrides the player's pitch angle (degrees) on
     * teleport.
     */
    private Double pitch;

    /**
     * Construct new action from command arguments
     *
     * @param sign the sign this action is being applied to.
     * @param args raw command arguments from the CommandExecutor.
     */
    public WarpAction(SignData sign, String[] args) {
        try {
            // Check for a valid world first, then check the remaining args.
            int xIndex;
            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                world = sign.getBlock().getWorld();
                xIndex = 0;
            } else {
                xIndex = 1;
            }

            int remainingArgs = args.length - xIndex;
            if (remainingArgs < 3 || remainingArgs > 5) {
                // Invalid by default.
                return;
            }

            double x = Double.parseDouble(args[xIndex]);
            double y = Double.parseDouble(args[xIndex + 1]);
            double z = Double.parseDouble(args[xIndex + 2]);
            loc = new Location(world, x, y, z);

            if (remainingArgs >= 4) {
                yaw = Double.parseDouble(args[xIndex + 3]);
                if (remainingArgs == 5) {
                    pitch = Double.parseDouble(args[xIndex + 4]);
                }
            }
            valid = true;
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
        }
    }

    public WarpAction(SignData sign, ConfigurationSection attributes) {
        this.loc = (Location) attributes.get("loc");
        this.yaw = (Double) attributes.get("yaw");
        this.pitch = (Double) attributes.get("pitch");
    }

    @Override
    public String getName() {
        return "warp";
    }

    @Override
    public String getUsage() {
        return "[<world>] <x> <y> <z> [<yaw>] [<pitch>]";
    }

    @Override
    public String getHelpText() {
        return "Teleports the player to the specified X, Y and Z coordinates in the specified world, " +
               "or the current world if no world is specified. If the yaw and pitch, are specified, they set the " +
               "player's look angles in degrees at the new location.";
    }

    /**
     * Return the string representation of the sign action for /easy-sign-info.
     *
     * @return String in the form "warp world 0 70 0" or "warp world 0 70 0 180
     *         -45".
     */
    @Override
    public String toString() {
        String yawText = yaw != null ? NUMBER_FORMAT.format(yaw) : "";
        String pitchText = pitch != null ? NUMBER_FORMAT.format(pitch) : "";
        return String.format("%s %s %s %s %s %s %s",
                             getName(), loc.getWorld().getName(),
                             NUMBER_FORMAT.format(loc.getX()), NUMBER_FORMAT.format(loc.getY()), NUMBER_FORMAT.format(loc.getZ()),
                             yawText, pitchText);
    }

    /**
     * The action was successfully constructed with valid arguments
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Used to serialize the action into BlockStore.
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("loc", loc);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        return map;
    }

    /**
     * When the player clicks the sign, teleport them to the stored location,
     * inserting the player's current yaw and pitch look angles if they are not
     * explicitly overridden by the sign.
     *
     * @param player the player clicking the sign
     */
    @Override
    public boolean action(Player player) {
        Location playerLoc = player.getLocation();
        Location destination = loc.clone();
        destination.setYaw((float) (yaw != null ? yaw : playerLoc.getYaw()));
        destination.setPitch((float) (pitch != null ? pitch : playerLoc.getPitch()));
        player.teleport(destination);
        return true;
    }
}
