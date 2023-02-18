package nu.nerd.easysigns.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nu.nerd.easysigns.SignData;

public class TakeHeldAction extends SignAction {

    private final SignData sign;
    private ItemStack item;
    private String qtyMessage;
    private String itemMessage;
    boolean valid = true;

    public TakeHeldAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            item = sign.getEditingPlayer().getInventory().getItemInMainHand().clone();
            item.setAmount(Integer.parseInt(args[0]));
            if (args.length > 1) {
                String messages = translate(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                String[] parts = messages.split("\\|\\|");
                itemMessage = parts[0];
                qtyMessage = parts[1];
            } else {
                itemMessage = ChatColor.RED + "That's not the right item!";
                qtyMessage = ChatColor.RED + "Insufficient items.";
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public TakeHeldAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.item = (ItemStack) attributes.get("item");
        this.itemMessage = attributes.getString("itemmessage");
        this.qtyMessage = attributes.getString("qtymessage");
    }

    @Override
    public String getName() {
        return "takeheld";
    }

    @Override
    public String getUsage() {
        return "<qty> [<itemmsg>||<qtymsg>]";
    }

    @Override
    public String getHelpText() {
        return "Takes the item in the player's main hand if it matches what is expected. " +
               "If the wrong item or insufficient items are offered, subsequent sign actions " +
               "are not executed and either <itemmsg> or <qtymsg> is shown, as appropriate. " +
               "The <itemmsg> and <qtymsg> can be omitted, in which case they default to " +
               "'Insufficient items.' and 'That's not the right item!' respectively. " +
               "If messages are specified, they must be separated by a double bar, ||.";
    }

    @Override
    public String toString() {
        return String.format("%s %s %s||%s", getName(), item.toString(), itemMessage, qtyMessage);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("itemmessage", itemMessage);
        map.put("qtymessage", qtyMessage);
        return map;
    }

    @Override
    public boolean action(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held != null && held.isSimilar(item)) {
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
