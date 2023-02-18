package nu.nerd.easysigns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.actions.SignAction;

/**
 * Representation of an EasySign
 */
public class SignData {

    private Block block;
    private Player editingPlayer;
    private List<SignAction> actions;

    public SignData(Block block) {
        this.block = block;
        this.editingPlayer = null;
        this.actions = new ArrayList<>();
    }

    public Block getBlock() {
        return block;
    }

    public List<SignAction> getActions() {
        return actions;
    }

    /**
     * Get the Player editing this sign.
     *
     * @see SignData#setEditingPlayer(Player editingPlayer)
     */
    public Player getEditingPlayer() {
        return editingPlayer;
    }

    /**
     * Get the Player editing this sign. This is used so SignAction constructors
     * can access the Player object, and is not persisted in any way or present
     * outside of the scope of a command.
     */
    public void setEditingPlayer(Player editingPlayer) {
        this.editingPlayer = editingPlayer;
    }

    /**
     * Persist the sign to BlockStore
     */
    public void save() {
        List<String> list = new ArrayList<>();
        for (SignAction action : actions) {
            FileConfiguration yaml = new YamlConfiguration();
            yaml.set("action", action.getName());
            yaml.set("attributes", action.serialize());
            list.add(yaml.saveToString());
        }
        String[] pack = list.toArray(new String[list.size()]);
        BlockStoreApi.setBlockMeta(block, EasySigns.instance, EasySigns.key, pack);
    }

    /**
     * Load the sign at the specified block from BlockStore
     */
    public static SignData load(Block block) {
        SignData sign = new SignData(block);
        String[] pack = (String[]) BlockStoreApi.getBlockMeta(block, EasySigns.instance, EasySigns.key);
        for (String s : pack) {
            try {
                FileConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(s);
                String name = yaml.getString("action");
                ConfigurationSection attr = yaml.getConfigurationSection("attributes");
                Class<?> c = EasySigns.instance.getActionClassByName(name.toLowerCase());
                SignAction action = (SignAction) c.getConstructor(SignData.class, ConfigurationSection.class)
                    .newInstance(sign, attr);
                sign.actions.add(action);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return sign;
    }

    /**
     * Remove the sign at the specified block from BlockStore
     */
    public static void delete(Block block) {
        BlockStoreApi.removeBlockMeta(block, EasySigns.instance, EasySigns.key);
        BlockStoreApi.removeBlockMeta(block, EasySigns.instance, "announce");
        BlockStoreApi.removeBlockMeta(block, EasySigns.instance, "max");
    }

}
