package nu.nerd.easysigns.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nu.nerd.easysigns.SignData;

public class LoreAction extends SignAction {

    private final SignData sign;
    private ItemStack item;
    private String lore;
    private String itemMessage;
    private String qtyMessage;
    boolean valid = true;

    public LoreAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            String itemName = args[0];
            item = new ItemStack(Material.valueOf(itemName.toUpperCase()), Integer.parseInt(args[1]));
            String str = translate(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            String[] parts = str.split("\\|\\|");
            lore = parts[0];
            itemMessage = parts[1];
            qtyMessage = parts[2];
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public LoreAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.item = (ItemStack) attributes.get("item");
        this.lore = attributes.getString("lore");
        this.itemMessage = attributes.getString("itemmessage");
        this.qtyMessage = attributes.getString("qtymessage");
    }

    @Override
    public String getName() {
        return "lore";
    }

    @Override
    public String getUsage() {
        return "<item> <qty> <lore>||<itemmsg>||<qtymsg>";
    }

    @Override
    public String getHelpText() {
        return "Takes a specified quantity of an item from a player if it has the required lore. " +
               "Unlike the take action, the player must be holding the item in their main hand when they click the sign. " +
               "Colors in the lore are ignored and multiple lines are concatenated without spaces. " +
               "The item must be in the player's hand. If the wrong item is held, <itemmsg> is shown. " +
               "If it is the right item but insufficient in quantity, <qtymsg> is shown. The <itemmsg> " +
               "and <qtymsg> can be multiple words and color codes are allowed. The double-bar sequence, " +
               "'||', is used to separate those arguments. If the item is not taken for whatever reason, " +
               "subsequent sign actions are not processed. Caution: multiple consecutive spaces in any of " +
               "these strings will be replaced with single spaces.";
    }

    @Override
    public String toString() {
        return String.format("%s %s %s||%s||%s",
                             getName(),
                             item.toString(),
                             lore,
                             itemMessage,
                             qtyMessage);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("lore", lore);
        map.put("itemmessage", itemMessage);
        map.put("qtymessage", qtyMessage);
        return map;
    }

    @Override
    public boolean action(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        boolean hasItem = held != null && held.getType().equals(item.getType());
        boolean hasLore = held != null && held.hasItemMeta() && held.getItemMeta().hasLore();
        String heldItemLore = hasLore ? ChatColor.stripColor(String.join("", held.getItemMeta().getLore())) : "";

        if (hasItem && heldItemLore.equals(substitute(lore, player, sign.getBlock()))) {
            if (held.getAmount() >= item.getAmount()) {
                held.setAmount(held.getAmount() - item.getAmount());
                return true;
            } else {
                player.sendMessage(substitute(qtyMessage, player, sign.getBlock()));
            }
        } else {
            player.sendMessage(substitute(itemMessage, player, sign.getBlock()));
        }
        return false;
    }
}
