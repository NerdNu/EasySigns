package nu.nerd.easysigns.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nu.nerd.easysigns.SignData;

public class TakeAction extends SignAction {

    private final SignData sign;
    private ItemStack item;
    private String failMessage;
    boolean valid = true;

    public TakeAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            String itemName = args[0];
            item = new ItemStack(Material.valueOf(itemName.toUpperCase()), Integer.parseInt(args[1]));
            if (args.length > 2) {
                failMessage = translate(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            } else {
                valid = false;
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public TakeAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.item = (ItemStack) attributes.get("item");
        this.failMessage = attributes.getString("failmessage");
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getUsage() {
        return "<item> <qty> <failmsg>";
    }

    @Override
    public String getHelpText() {
        return "Takes an item from a player. If they don't have enough, the failmsg is shown, " +
               "and no other commands will be run.";
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getName(), item.toString(), failMessage);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("failmessage", failMessage);
        return map;
    }

    @Override
    public boolean action(Player player) {
        if (player.getInventory().contains(item.getType(), item.getAmount())) {
            player.getInventory().removeItem(item);
            return true;
        } else {
            player.sendMessage(substitute(failMessage, player, sign.getBlock()));
            return false;
        }
    }

}
