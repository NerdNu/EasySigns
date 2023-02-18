package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class GiveAction extends SignAction {

    private ItemStack item;
    private int slot = -1;
    private boolean valid = true;

    public GiveAction(SignData sign, String[] args) {
        try {
            String itemName = args[0];
            if (itemName.equalsIgnoreCase("held")) {
                item = sign.getEditingPlayer().getInventory().getItemInMainHand();
                if (args.length > 1) {
                    slot = Integer.parseInt(args[1]);
                }
            } else {
                item = new ItemStack(Material.valueOf(itemName.toUpperCase()), Integer.parseInt(args[1]));
                if (args.length > 2) {
                    slot = Integer.parseInt(args[2]);
                }
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public GiveAction(SignData sign, ConfigurationSection attributes) {
        this.item = (ItemStack) attributes.get("item");
        this.slot = attributes.getInt("slot");
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "<item> <qty> [<slot>] or held [<slot>]";
    }

    @Override
    public String getHelpText() {
        return "Gives the player an item. Place it in the specified slot number, or a free slot if not specified. " +
               "If 'held' is used in place of an item name, the item in your main hand will be used.";
    }

    @Override
    public String toString() {
        if (slot > -1) {
            return String.format("%s %s %d", getName(), item.toString(), slot);
        } else {
            return String.format("%s %s", getName(), item.toString());
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("slot", slot);
        return map;
    }

    @Override
    public boolean action(Player player) {
        if (slot > -1) {
            if (player.getInventory().getItem(slot) == null) {
                player.getInventory().setItem(slot, item);
            }
        } else {
            Map<Integer, ItemStack> notGiven = player.getInventory().addItem(item);
            if (notGiven.size() > 0) {
                String message = String.format("Easysign action 'give' failed for player %s. %d/%d item %s not given.",
                                               player.getName(),
                                               notGiven.size(),
                                               item.getAmount(),
                                               item.getType().toString());
                EasySigns.instance.getLogger().info(message);
            }
        }
        return true;
    }

}
