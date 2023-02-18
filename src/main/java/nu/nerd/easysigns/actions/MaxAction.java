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

public class MaxAction extends SignAction {

    private final SignData sign;
    private int uses;
    boolean valid = true;

    public MaxAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            uses = Integer.parseInt(args[0]);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public MaxAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.uses = attributes.getInt("uses");
    }

    @Override
    public String getName() {
        return "max";
    }

    @Override
    public String getUsage() {
        return "<uses>";
    }

    @Override
    public String getHelpText() {
        return "Allows the sign to be used <uses> times and no more. No other commands will be run once limit is reached.";
    }

    @Override
    public String toString() {
        return String.format("%s %d", getName(), uses);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("uses", uses);
        return map;
    }

    public int getTimesUsed(UUID playerUUID) {
        FileConfiguration uses = loadUseMap();
        return uses.getInt(playerUUID.toString(), 0);
    }

    public void setTimesUsed(UUID playerUUID, int count) {
        FileConfiguration uses = loadUseMap();
        uses.set(playerUUID.toString(), count);
        BlockStoreApi.setBlockMeta(sign.getBlock(), EasySigns.instance, "max", uses.saveToString());
    }

    @Override
    public boolean action(Player player) {
        UUID playerUUID = player.getUniqueId();
        int times = getTimesUsed(playerUUID);
        if (times < uses) {
            setTimesUsed(playerUUID, ++times);
            return true;
        } else {
            player.sendMessage(String.format("%sMaximum uses reached! (%d)", ChatColor.GREEN, uses));
            return false;
        }
    }

    /**
     * Load the YAML-encoded map from player UUID (String) to use count, which
     * is stored in the block meta.
     *
     * @return the YAML map of use counts by player UUID string.
     */
    private FileConfiguration loadUseMap() {
        FileConfiguration config = new YamlConfiguration();
        try {
            String str = (String) BlockStoreApi.getBlockMeta(sign.getBlock(), EasySigns.instance, "max");
            if (str != null) {
                config.loadFromString(str);
            }
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
