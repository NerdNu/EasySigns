package nu.nerd.easysigns.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class AnnounceAction extends SignAction {

    private final SignData sign;
    private String message;
    boolean valid = true;

    public AnnounceAction(SignData sign, String[] args) {
        this.sign = sign;
        if (args.length > 0) {
            message = String.join(" ", args);
            message = translate(message);
        } else {
            valid = false;
        }
    }

    public AnnounceAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.message = attributes.getString("message");
    }

    @Override
    public String getName() {
        return "announce";
    }

    @Override
    public String getUsage() {
        return "<message>";
    }

    @Override
    public String getHelpText() {
        return "Sets up an announcement sign. The message is broadcast (only once) when a player " +
               "clicks the sign. The message supports colors with the same notation as /signtext, " +
               "and %s is replaced with the player's name. Include \"--unlimited\" flag before " +
               "message to allow unlimited announcements (hint: put announce after a \"max\" action" +
               "to limit max announcements to a custom value).";
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

    public boolean hasUsed(UUID playerUUID) {
        String uuidString = playerUUID.toString();
        return getAnnouncedUUIDs().contains(uuidString);
    }

    public void setUsed(UUID playerUUID, boolean used) {
        String uuidString = playerUUID.toString();
        Set<String> announced = getAnnouncedUUIDs();
        if (used) {
            announced.add(uuidString);
        } else {
            announced.remove(uuidString);
        }
        BlockStoreApi.setBlockMeta(sign.getBlock(), EasySigns.instance, "announce", announced.toArray(new String[announced.size()]));
    }

    @Override
    public boolean action(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!hasUsed(playerUUID)) {
            player.getServer().broadcastMessage(substitute(message.replaceFirst("^--unlimited ", ""), player, sign.getBlock()));
            if (!message.startsWith("--unlimited ")) {
                setUsed(playerUUID, true);
            }
            return true;
        } else {
            player.sendMessage(ChatColor.GREEN + "[SIGN] " + ChatColor.WHITE + "You can only announce here once!");
            return false;
        }
    }

    /**
     * Return the set of UUIDs (as Strings) of players that have triggered this
     * action.
     *
     * @return the non-null set of UUID Strings.
     */
    private Set<String> getAnnouncedUUIDs() {
        String[] used = (String[]) BlockStoreApi.getBlockMeta(sign.getBlock(), EasySigns.instance, "announce");
        return used != null ? new HashSet<>(Arrays.asList(used)) : new HashSet<>();
    }
}
