package nu.nerd.easysigns.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import nu.nerd.easysigns.SignData;

public class LeatherAction extends SignAction {

    private Color color;
    private List<String> armor = new ArrayList<>();
    private Map<String, Integer> armorMap = new HashMap<>();
    boolean valid = true;

    public LeatherAction(SignData sign, String[] args) {
        setArmorMap();
        try {
            int red = Integer.parseInt(args[0]);
            int green = Integer.parseInt(args[1]);
            int blue = Integer.parseInt(args[2]);
            color = Color.fromRGB(red, green, blue);
            for (String arg : Arrays.copyOfRange(args, 3, args.length)) {
                if (armorMap.keySet().contains(arg.toLowerCase())) {
                    armor.add(arg.toLowerCase());
                }
            }
            if (armor.size() < 1) {
                valid = false;
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public LeatherAction(SignData sign, ConfigurationSection attributes) {
        setArmorMap();
        this.color = (Color) attributes.get("color");
        this.armor = attributes.getStringList("armor");
    }

    @Override
    public String getName() {
        return "leather";
    }

    @Override
    public String getUsage() {
        return "<red> <green> <blue> <item1> ... <itemN>";
    }

    @Override
    public String getHelpText() {
        return "Gives the player leather armor with the specified red, green and blue color " +
               "components (0 - 255). <item1> to <itemN> are a list of items to give and can " +
               "only be: helmet, chestplate, leggings or boots.";
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d %s",
                             getName(),
                             color.getRed(),
                             color.getGreen(),
                             color.getBlue(),
                             String.join(" ", armor));
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("color", color);
        map.put("armor", armor);
        return map;
    }

    private void setArmorMap() {
        armorMap.put("helmet", 39);
        armorMap.put("chestplate", 38);
        armorMap.put("leggings", 37);
        armorMap.put("boots", 36);
    }

    private ItemStack fabricateArmor(String name) {
        ItemStack item = new ItemStack(Material.valueOf("LEATHER_" + name.toUpperCase()));
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean action(Player player) {
        PlayerInventory inv = player.getInventory();
        for (String name : armor) {
            if (inv.getItem(armorMap.get(name)) == null) {
                inv.setItem(armorMap.get(name), fabricateArmor(name));
                player.sendMessage("Equip");
            } else {
                inv.addItem(fabricateArmor(name));
                player.sendMessage("Give");
            }
        }
        return true;
    }

}
