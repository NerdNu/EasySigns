package nu.nerd.easysigns.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class InventoryAction extends SignAction {

    private boolean clear = true;
    private Map<Integer, ItemStack> items;
    private boolean valid = true;

    public InventoryAction(SignData sign, String[] args) {
        this.items = new HashMap<>();
        try {
            if (args.length > 0) {
                clear = Boolean.parseBoolean(args[0]);
            }
            PlayerInventory inv = sign.getEditingPlayer().getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                items.put(i, inv.getItem(i));
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public InventoryAction(SignData sign, ConfigurationSection attributes) {
        this.clear = attributes.getBoolean("clear");
        this.items = new HashMap<>();
        ConfigurationSection sec = attributes.getConfigurationSection("items");
        for (String key : sec.getKeys(false)) {
            this.items.put(Integer.parseInt(key), (ItemStack) sec.get(key));
        }
    }

    @Override
    public String getName() {
        return "inventory";
    }

    @Override
    public String getUsage() {
        return "[<clear>]";
    }

    @Override
    public String getHelpText() {
        return "Clears the player's inventory and gives them an exact copy of your current inventory. If [<clear>] is false, " +
               "the player will be given the items normally, without clearing their inventory.";
    }

    @Override
    public String toString() {
        return String.format("%s %s", getName(), clear);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("clear", clear);
        map.put("items", this.items);
        return map;
    }

    @Override
    public boolean action(Player player) {
        if (clear) {
            player.getInventory().clear();
            for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
                player.getInventory().setItem(item.getKey(), item.getValue());
            }
        } else {
            List<Material> notGiven = new ArrayList<>();
            for (Map.Entry<Integer, ItemStack> item : items.entrySet()) {
                HashMap<Integer, ItemStack> res = player.getInventory().addItem(item.getValue());
                if (res.size() > 0) {
                    notGiven.add(item.getValue().getType());
                }
            }
            if (notGiven.size() > 0) {
                String message = String.format("Easysign action 'giveinventory' failed for player %s. No free inventory slot for %s",
                                               player.getName(),
                                               String.join(", ", notGiven.stream().map(Material::toString).collect(Collectors.toList())));
                EasySigns.instance.getLogger().info(message);
            }
        }
        return true;
    }

}
