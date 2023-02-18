package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class CoolDownAction extends SignAction {
    // Durations in seconds.
    private static final int MINUTE = 60;
    private static final int HOUR = MINUTE * 60;
    private static final int DAY = HOUR * 24;

    private static final String IDENTIFIER = "cooldown";

    private final SignData sign;
    private int coolDownSeconds;

    /**
     * Map from player UUID (String) to next allowed use time (milliseconds
     * since epoch) as a FileConfiguration stored as BlockStore string data.
     *
     * When a player clicks the signs, "next use" times that have already passed
     * are removed from the map, irrespective of which player they apply to.
     * Thus, the map tends to store the minimum amount of data necessary.
     */
    FileConfiguration nextUseTimeMap = new YamlConfiguration();
    boolean valid = true;

    public CoolDownAction(SignData sign, String[] args) {
        this.sign = sign;
        loadNextUseTimeMap();
        try {
            coolDownSeconds = Integer.parseInt(args[0]);
            if (coolDownSeconds < 1) {
                valid = false;
            }
        } catch (IllegalArgumentException ex) {
            valid = false;
        }
    }

    public CoolDownAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.coolDownSeconds = attributes.getInt(IDENTIFIER);
        loadNextUseTimeMap();
    }

    @Override
    public String getName() {
        return IDENTIFIER;
    }

    @Override
    public String getUsage() {
        return "<seconds>";
    }

    @Override
    public String getHelpText() {
        return "Prevents the sign from being used again by the same player until <seconds> have elapsed. Subsequent sign actions are blocked until the cooldown period has elapsed.";
    }

    @Override
    public String toString() {
        return String.format("%s %d", getName(), coolDownSeconds);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean action(Player player) {
        UUID playerUUID = player.getUniqueId();
        int seconds = getCoolDownSeconds(playerUUID);
        if (seconds > 0) {
            int amount;
            String units;
            if (seconds >= 2 * DAY) {
                amount = seconds / DAY;
                units = "days";
            } else if (seconds >= 2 * HOUR) {
                amount = seconds / HOUR;
                units = "hours";
            } else if (seconds >= 2 * MINUTE) {
                amount = seconds / MINUTE;
                units = "minutes";
            } else {
                amount = seconds;
                units = "seconds";
            }
            player.sendMessage(String.format("%sYou must wait %d %s to use this sign again!", ChatColor.GREEN, amount, units));
            return false;
        } else {
            updateCoolDownSeconds(playerUUID);
            return true;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(IDENTIFIER, coolDownSeconds);
        return map;
    }

    public int getCoolDownSeconds(UUID playerUUID) {
        long nextUseTime = nextUseTimeMap.getLong(playerUUID.toString(), 0L);
        long now = System.currentTimeMillis();
        return (int) Math.min(Integer.MAX_VALUE,
                              Math.max(0L, (nextUseTime - now) / 1000L));
    }

    /**
     * Set the "next use time" of this sign for the specified player to
     * {@link coolDownSeconds} from now.
     *
     * @param playerUUID the affected player.
     */
    public void updateCoolDownSeconds(UUID playerUUID) {
        // Clear out expired "next use" times.
        long now = System.currentTimeMillis();
        for (String key : nextUseTimeMap.getKeys(false)) {
            long nextUseTime = nextUseTimeMap.getLong(key, 0);
            if (now > nextUseTime) {
                nextUseTimeMap.set(key, null);
            }
        }

        // Update the next use time for the current player.
        nextUseTimeMap.set(playerUUID.toString(), now + 1000L * coolDownSeconds);

        // Save to BlockStore.
        BlockStoreApi.setBlockMeta(sign.getBlock(), EasySigns.instance,
                                   IDENTIFIER, nextUseTimeMap.saveToString());
    }

    /**
     * Load the YAML-encoded map from player UUID (String) to cooldown in
     * seconds, which is stored in the block meta.
     */
    private void loadNextUseTimeMap() {
        try {
            String str = (String) BlockStoreApi.getBlockMeta(sign.getBlock(), EasySigns.instance, IDENTIFIER);
            if (str != null) {
                nextUseTimeMap.loadFromString(str);
            }
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }
}
