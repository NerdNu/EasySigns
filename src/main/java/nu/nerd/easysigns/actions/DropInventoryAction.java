package nu.nerd.easysigns.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import nu.nerd.easysigns.SignData;

public class DropInventoryAction extends SignAction {

    private List<ItemStack> items;
    private Location loc;
    private boolean scatter = false;
    private boolean valid = true;

    public DropInventoryAction(SignData sign, String[] args) {
        this.items = new ArrayList<>();
        Collections.addAll(items, sign.getEditingPlayer().getInventory().getContents());
        try {
            World world = sign.getBlock().getWorld();
            int x, y, z;
            if (args.length > 3 && Bukkit.getWorld(args[0]) != null) {
                world = Bukkit.getWorld(args[0]);
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
                if (args.length > 4) {
                    scatter = Boolean.parseBoolean(args[4]);
                }
            } else {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
                if (args.length > 3) {
                    scatter = Boolean.parseBoolean(args[3]);
                }
            }
            loc = new Location(world, x, y, z);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public DropInventoryAction(SignData sign, ConfigurationSection attributes) {
        this.loc = (Location) attributes.get("loc");
        this.scatter = attributes.getBoolean("scatter");
        this.items = new ArrayList<>();
        List<?> list = attributes.getList("items");
        items.addAll(list.stream().map(i -> (ItemStack) i).collect(Collectors.toList()));
    }

    @Override
    public String getName() {
        return "dropinventory";
    }

    @Override
    public String getUsage() {
        return "[<world>] <x> <y> <z> [<scatter>]";
    }

    @Override
    public String getHelpText() {
        return "Drops a copy of your current inventory at the specified coordinates. Specify true/false " +
               "as the [<scatter>] argument to give the dropped items random velocities";
    }

    @Override
    public String toString() {
        return String.format("%s %s %d %d %d %s",
                             getName(),
                             loc.getWorld().getName(),
                             loc.getBlockX(),
                             loc.getBlockY(),
                             loc.getBlockZ(),
                             scatter);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("loc", loc);
        map.put("scatter", scatter);
        map.put("items", this.items);
        return map;
    }

    @Override
    public boolean action(Player player) {
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (!scatter) {
                Item ent = loc.getWorld().dropItem(loc, item);
                ent.setVelocity(new Vector(0, 0, 0));
            } else {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }
        return true;
    }

}
