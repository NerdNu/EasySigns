package nu.nerd.easysigns.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PayAction extends SignAction {

    private final SignData sign;
    private ItemStack item;
    private int payAmount;
    private TextComponent itemMessage = Component.text().content("Hey, that's not the right item!")
            .color(TextColor.fromHexString("#FF5555"))
            .decoration(TextDecoration.BOLD, true)
            .build();
    private boolean valid = true;

    public PayAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            this.item = sign.getEditingPlayer().getInventory().getItemInMainHand().clone();
            this.item.setAmount(1);

            this.payAmount = Integer.parseInt(args[0]);
        } catch(IndexOutOfBoundsException | IllegalArgumentException exception) {
            valid = false;
        }
    }

    public PayAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.item = (ItemStack) attributes.get("item");
        this.item.setAmount(1);
        this.payAmount = attributes.getInt("paid");
    }

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public String getUsage() {
        return "<amountDue>";
    }

    @Override
    public String getHelpText() {
        return "Takes items from the player's main hand until the specified amount due is reached. " +
                "After that, this returns true every time without taking additional items.";
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public int getPaidAmount(UUID playerUUID) {
        FileConfiguration paid = loadPayMap();
        return paid.getInt(playerUUID.toString(), 0);
    }

    public void setPaidAmount(UUID playerUUID, int amount) {
        FileConfiguration paid = loadPayMap();
        paid.set(playerUUID.toString(), amount);
        BlockStoreApi.setBlockMeta(sign.getBlock(), EasySigns.instance, "paid", paid.saveToString());
    }

    public TextComponent dueMessage(int amountDue) {
        return Component.text("You still owe ").color(TextColor.fromHexString("#FFAA00"))
                .append(Component.text(amountDue, NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" of the required item!", TextColor.fromHexString("#FFAA00")));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("paid", payAmount);
        return map;
    }

    @Override
    public boolean action(Player player) {
        UUID playerUUID = player.getUniqueId();
        int paid = getPaidAmount(playerUUID);
        if(paid < payAmount) {
            int amountDue = payAmount - paid;
            ItemStack held = player.getInventory().getItemInMainHand();
            if(held != null && held.isSimilar(item)) {
                int heldAmount = held.getAmount();
                if(heldAmount > 0) {
                    if(heldAmount >= amountDue) {
                        held.setAmount(heldAmount - amountDue);
                        setPaidAmount(playerUUID, paid + amountDue);
                        System.out.println("EasySigns Payment: " + player.getName() + " paid " + amountDue + item.getType());
                        return true;
                    } else {
                        setPaidAmount(playerUUID, paid + heldAmount);
                        held.setAmount(0);
                        amountDue = payAmount - getPaidAmount(playerUUID);
                        player.sendMessage(dueMessage(amountDue));
                        System.out.println("EasySigns Payment: " + player.getName() + " paid " + heldAmount + item.getType());
                        return false;
                    }
                }
            }
        } else {
            return true;
        }
        player.sendMessage(dueMessage(payAmount - paid));
        return false;
    }

    private FileConfiguration loadPayMap() {
        FileConfiguration configuration = new YamlConfiguration();
        try {
            String string = (String) BlockStoreApi.getBlockMeta(sign.getBlock(), EasySigns.instance, "paid");
            if(string != null) {
                configuration.loadFromString(string);
            }
        } catch(InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
        return configuration;
    }
}
