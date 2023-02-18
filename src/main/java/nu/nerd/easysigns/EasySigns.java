package nu.nerd.easysigns;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.actions.AnnounceAction;
import nu.nerd.easysigns.actions.CartAction;
import nu.nerd.easysigns.actions.CheckEmptyInvAction;
import nu.nerd.easysigns.actions.ClearInvAction;
import nu.nerd.easysigns.actions.ClearPotionsAction;
import nu.nerd.easysigns.actions.CmdAction;
import nu.nerd.easysigns.actions.CoolDownAction;
import nu.nerd.easysigns.actions.DropInventoryAction;
import nu.nerd.easysigns.actions.GiveAction;
import nu.nerd.easysigns.actions.HealAction;
import nu.nerd.easysigns.actions.HungerAction;
import nu.nerd.easysigns.actions.InventoryAction;
import nu.nerd.easysigns.actions.LaunchAction;
import nu.nerd.easysigns.actions.LeatherAction;
import nu.nerd.easysigns.actions.LoreAction;
import nu.nerd.easysigns.actions.MaxAction;
import nu.nerd.easysigns.actions.MsgAction;
import nu.nerd.easysigns.actions.OpCmdAction;
import nu.nerd.easysigns.actions.PotionAction;
import nu.nerd.easysigns.actions.RandLocAction;
import nu.nerd.easysigns.actions.RedstoneAction;
import nu.nerd.easysigns.actions.SetBedAction;
import nu.nerd.easysigns.actions.SleepAction;
import nu.nerd.easysigns.actions.SoundAction;
import nu.nerd.easysigns.actions.TakeAction;
import nu.nerd.easysigns.actions.TakeHeldAction;
import nu.nerd.easysigns.actions.TeleportBedAction;
import nu.nerd.easysigns.actions.WarpAction;

public class EasySigns extends JavaPlugin {

    public static EasySigns instance;
    public static String key = "actions";
    private static Set<Material> SIGN_MATERIALS;
    private static Set<Material> WALL_SIGN_MATERIALS;

    private Map<String, Class<?>> actionAtlas;
    private final Set<Block> tickingRedstone = new HashSet<>();

    @Override
    public void onEnable() {
        EasySigns.instance = this;
        SIGN_MATERIALS = Tag.SIGNS.getValues();
        WALL_SIGN_MATERIALS = Tag.WALL_SIGNS.getValues();

        createActionAtlas();
        new CommandHandler();
        new SignListener();
    }

    /**
     * Register SignAction subclasses for use, in the order they should be
     * listed in help.
     */
    private void createActionAtlas() {
        actionAtlas = new LinkedHashMap<>();
        actionAtlas.put("announce", AnnounceAction.class);
        actionAtlas.put("cart", CartAction.class);
        actionAtlas.put("check-empty-inventory", CheckEmptyInvAction.class);
        actionAtlas.put("ci", ClearInvAction.class);
        actionAtlas.put("clearpotions", ClearPotionsAction.class);
        actionAtlas.put("cmd", CmdAction.class);
        actionAtlas.put("cooldown", CoolDownAction.class);
        actionAtlas.put("dropinventory", DropInventoryAction.class);
        actionAtlas.put("give", GiveAction.class);
        actionAtlas.put("heal", HealAction.class);
        actionAtlas.put("hunger", HungerAction.class);
        actionAtlas.put("inventory", InventoryAction.class);
        actionAtlas.put("launch", LaunchAction.class);
        actionAtlas.put("leather", LeatherAction.class);
        actionAtlas.put("lore", LoreAction.class);
        actionAtlas.put("max", MaxAction.class);
        actionAtlas.put("msg", MsgAction.class);
        actionAtlas.put("opcmd", OpCmdAction.class);
        actionAtlas.put("potion", PotionAction.class);
        actionAtlas.put("randloc", RandLocAction.class);
        actionAtlas.put("redstone", RedstoneAction.class);
        actionAtlas.put("setbed", SetBedAction.class);
        actionAtlas.put("sleep", SleepAction.class);
        actionAtlas.put("sound", SoundAction.class);
        actionAtlas.put("take", TakeAction.class);
        actionAtlas.put("takeheld", TakeHeldAction.class);
        actionAtlas.put("tpbed", TeleportBedAction.class);
        actionAtlas.put("warp", WarpAction.class);
    }

    /**
     * Look up the Class of an action by the action's name
     *
     * @param name name of the action to look up
     * @return the matching Class object or null
     */
    public Class<?> getActionClassByName(String name) {
        return actionAtlas.get(name);
    }

    /**
     * Returns a Set of valid action names
     */
    public Set<String> getValidActions() {
        return actionAtlas.keySet();
    }

    /**
     * All of the action classes
     */
    public Collection<Class<?>> getActionClasses() {
        return actionAtlas.values();
    }

    /**
     * Check if a block is a sign.
     *
     * @param block the block to check
     * @return true if the block's material is that of a sign of any type.
     */
    public boolean isSign(Block block) {
        return SIGN_MATERIALS.contains(block.getType());
    }

    /**
     * Check if a block is a wall sign
     *
     * @param block the block to check
     * @return true if the block's material is that of a wall sign of any type.
     */
    public boolean isWallSign(Block block) {
        return WALL_SIGN_MATERIALS.contains(block.getType());
    }

    /**
     * Consult BlockStore to see if this block is registered as an EasySign
     *
     * @param block the block to check
     * @return true if EasySign
     */
    public boolean isEasySign(Block block) {
        if (!isSign(block)) {
            return false;
        }
        Object object = BlockStoreApi.getBlockMeta(block, this, key);
        return object != null;
    }

    public Set<Block> getTickingRedstone() {
        return tickingRedstone;
    }

}
