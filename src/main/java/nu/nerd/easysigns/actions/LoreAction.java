package nu.nerd.easysigns.actions;


import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoreAction extends SignAction {


    private SignData sign;
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
            String str = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String[] parts = str.split("\\|\\|");
            lore = parts[0];
            itemMessage = parts[1];
            qtyMessage = parts[2];
        } catch (IndexOutOfBoundsException|IllegalArgumentException ex) {
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


    public String getName() {
        return "lore";
    }


    public String getUsage() {
        return "<item> <qty> <lore>||<itemmsg>||<qtymsg>";
    }


    public String getHelpText() {
        return "Takes a specified quantity of an item from a player if it has the required lore. " +
                "Colors in the lore are ignored and multiple lines are concatenated without spaces. " +
                "The item must be in the player's hand. If the wrong item is held, <itemmsg> is shown. " +
                "If it is the right item but insufficient in quantity, <qtymsg> is shown. The <itemmsg> " +
                "and <qtymsg> can be multiple words and color codes are allowed.  The double-bar sequence, " +
                "'||', is used to separate those arguments.  If the item is not taken for whatever reason, " +
                "subsequent sign actions are not processed. Caution: multiple consecutive spaces in any of " +
                "these strings will be replaced with single spaces.";
    }


    public String toString() {
        return String.format(
                "%s %s %s||%s||%s",
                getName(),
                item.toString(),
                lore,
                ChatColor.translateAlternateColorCodes('&', itemMessage),
                ChatColor.translateAlternateColorCodes('&', qtyMessage)
        );
    }


    public boolean isValid() {
        return valid;
    }


    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("lore", lore);
        map.put("itemmessage", itemMessage);
        map.put("qtymessage", qtyMessage);
        return map;
    }


    public boolean shouldExit(Player player) {
        if (player.hasMetadata("easysigns.lore")) {
            //action() has evaluated that the item is here, so don't return
            player.removeMetadata("easysigns.lore", EasySigns.instance);
            return false;
        }
        return true;
    }


    public void action(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        boolean hasItem = held != null && held.getType().equals(item.getType());
        boolean hasQty = held != null && held.getAmount() >= item.getAmount();
        boolean hasLore = held != null && held.hasItemMeta() && held.getItemMeta().hasLore();
        boolean matching = false;
        String loreStr;

        if (hasLore) {
            loreStr = String.join("", held.getItemMeta().getLore());
            loreStr = ChatColor.stripColor(loreStr);
            if (loreStr.equals(lore)) {
                matching = true;
            }
        }

        if (matching) {
            if (hasQty) {
                held.setAmount(held.getAmount() - item.getAmount());
                player.setMetadata("easysigns.lore", new FixedMetadataValue(EasySigns.instance, true));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', qtyMessage));
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', itemMessage));
        }
    }


}