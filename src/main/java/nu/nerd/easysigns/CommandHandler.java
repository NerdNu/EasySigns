package nu.nerd.easysigns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.actions.AnnounceAction;
import nu.nerd.easysigns.actions.MaxAction;
import nu.nerd.easysigns.actions.SignAction;

public class CommandHandler implements TabExecutor {

    private final EasySigns plugin;
    private final Map<Player, List<SignAction>> clipboard;

    public CommandHandler() {
        plugin = EasySigns.instance;
        clipboard = new HashMap<>();
        plugin.getCommand("easy-sign").setExecutor(this);
        plugin.getCommand("easy-sign-delete").setExecutor(this);
        plugin.getCommand("easy-sign-info").setExecutor(this);
        plugin.getCommand("easy-sign-remove").setExecutor(this);
        plugin.getCommand("easy-sign-reorder").setExecutor(this);
        plugin.getCommand("easy-sign-copy").setExecutor(this);
        plugin.getCommand("easy-sign-paste").setExecutor(this);
        plugin.getCommand("easy-sign-used").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use EasySigns.");
            return true;
        }
        switch (cmd.getName().toLowerCase()) {
        case "easy-sign":
            addAction(sender, args);
            break;
        case "easy-sign-delete":
            deleteSign(sender, args);
            break;
        case "easy-sign-info":
            signInfo(sender, args);
            break;
        case "easy-sign-remove":
            removeAction(sender, args);
            break;
        case "easy-sign-reorder":
            reorderActions(sender, args);
            break;
        case "easy-sign-copy":
            copySign(sender, args);
            break;
        case "easy-sign-paste":
            pasteSign(sender, args);
            break;
        case "easy-sign-used":
            usedSign(sender, args);
            break;
        }
        return true;
    }

    /**
     * Suppress tab completion, except for the /easy-sign command, which should
     * suggest valid actions.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("easy-sign") && args.length == 1) {
            List<String> completions = new ArrayList<>();
            String action = args[0];
            if (action.equals("")) {
                completions.addAll(plugin.getValidActions());
            } else {
                plugin.getValidActions().stream().filter(s -> s.startsWith(args[0])).forEach(completions::add);
            }
            return completions;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Add an action to a sign, registering it if it's not yet a valid Easysign
     */
    private void addAction(CommandSender sender, String[] args) {

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);
        SignData sign;

        // Print command help if no action is specified
        if (args.length < 1) {
            addActionCommandHelpText(sender);
            return;
        }

        // Exit early if the player is not looking at a sign
        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That's not a sign. Look at a sign to run this command.");
            return;
        }

        // Create a new sign, or load an existing one
        if (plugin.isEasySign(looking)) {
            sign = SignData.load(looking);
        } else {
            sign = new SignData(looking);
        }
        sign.setEditingPlayer(player);

        String actionName = args[0];
        String[] arguments = Arrays.copyOfRange(args, 1, args.length);
        Class<?> c = plugin.getActionClassByName(actionName.toLowerCase());

        if (c == null) {
            sender.sendMessage(ChatColor.RED + "Invalid action: " + actionName + ". Run /easy-sign for usage.");
            return;
        }

        // Instantiate the correct class and save it to the sign
        try {
            SignAction action = (SignAction) c.getConstructor(SignData.class, String[].class).newInstance(sign, arguments);
            if (action.isValid()) {
                sign.getActions().add(action);
                sign.save();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Easy sign action added!");
            } else {
                sender.sendMessage(String.format("%sUsage: /easy-sign %s %s", ChatColor.RED, action.getName(), action.getUsage()));
                sender.sendMessage(action.getHelpText());
            }
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Error running command.");
            ex.printStackTrace();
        }

    }

    /**
     * Remove all actions from a sign and unregister it as an EasySign
     */
    private void deleteSign(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Unexpected arguments. Usage: /easy-sign-delete");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);

        if (!plugin.isSign(looking) || !plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "That doesn't appear to be an easy sign.");
            return;
        }

        SignData.delete(looking);
        sender.sendMessage(ChatColor.RED + "Sign removed.");
    }

    /**
     * Determine if this is a valid EasySign and list actions on it
     */
    private void signInfo(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Unexpected arguments. Usage: /easy-sign-info");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);

        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That isn't a sign.");
            return;
        }

        if (!plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "No EasySign actions are assigned to that sign.");
            return;
        }

        SignData sign = SignData.load(looking);
        String rowFmt = ChatColor.YELLOW + "(%d) " + ChatColor.LIGHT_PURPLE + "%s ";
        int i = 1;
        for (SignAction action : sign.getActions()) {
            sender.sendMessage(String.format(rowFmt, i, action.toString()));
            i++;
        }
    }

    /**
     * Remove the specified action from the sign
     */
    private void removeAction(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Unexpected arguments. Usage: /easy-sign-remove <index>");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);
        SignData sign;
        int index;

        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That isn't a sign.");
            return;
        }

        if (!plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "No EasySign actions are assigned to that sign.");
            return;
        }

        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            index = -1;
        }
        sign = SignData.load(looking);

        if (index < 1 || index > sign.getActions().size()) {
            sender.sendMessage(ChatColor.RED + "The index must be an integer between 1 and the number of actions on the sign.");
            return;
        }

        sign.getActions().remove(index - 1);
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "EasySign action removed.");
        if (sign.getActions().size() > 0) {
            sign.save();
        } else {
            SignData.delete(sign.getBlock());
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Empty sign deleted.");
        }
    }

    /**
     * Reorder an action on a sign
     */
    private void reorderActions(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /easy-sign-reorder <from> <to>");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);
        SignData sign;
        SignAction action;
        int from, to;

        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That isn't a sign.");
            return;
        }

        if (!plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "No EasySign actions are assigned to that sign.");
            return;
        }

        try {
            from = Integer.parseInt(args[0]);
            to = Integer.parseInt(args[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            from = -1;
            to = -1;
        }
        sign = SignData.load(looking);

        if (from < 1 || to < 1 || from > sign.getActions().size() || to > sign.getActions().size()) {
            sender.sendMessage(ChatColor.RED + "The indices must be integers between 1 and the number of actions on the sign.");
            return;
        }

        if (from == to) {
            sender.sendMessage(ChatColor.RED + "Nothing to do; the 'from' and 'to' indices are the same.");
            return;
        }

        action = sign.getActions().remove(from - 1);
        sign.getActions().add(to - 1, action);
        sign.save();
        sender.sendMessage(String.format("%sEasy sign action %d moved to position %d.", ChatColor.LIGHT_PURPLE, from, to));
    }

    /**
     * Copy the sign to the clipboard
     */
    private void copySign(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Unexpected arguments. Usage: /easy-sign-copy");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);
        SignData sign;

        if (!plugin.isSign(looking) || !plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "That's not an EasySign. Look at a sign to run this command.");
            return;
        }

        sign = SignData.load(looking);
        clipboard.put(player, sign.getActions());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Sign copied to clipboard.");
    }

    /**
     * Paste the sign currently in the clipboard to a new block
     */
    private void pasteSign(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Unexpected arguments. Usage: /easy-sign-paste");
            return;
        }

        Player player = (Player) sender;
        Block looking = player.getTargetBlock(null, 5);
        SignData sign;

        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That's not a sign. Look at a sign to run this command.");
            return;
        }

        if (!clipboard.containsKey(player)) {
            sender.sendMessage(ChatColor.RED + "You must copy a sign before you can paste!");
            return;
        }

        sign = new SignData(looking);
        sign.setEditingPlayer(player);
        sign.getActions().addAll(clipboard.get(player));
        sign.save();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Sign actions pasted.");
    }

    /**
     * Handle /easy-sign-used <player> [<count>].
     */
    private void usedSign(CommandSender sender, String[] args) {
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: /easy-sign-used <player> [<count>]");
            sender.sendMessage(ChatColor.RED + "Get or set the number of times the specified player has used this sign.");
            return;
        }

        Player creator = (Player) sender;
        Block looking = creator.getTargetBlock(null, 5);
        if (!plugin.isSign(looking)) {
            sender.sendMessage(ChatColor.RED + "That isn't a sign.");
            return;
        }
        if (!plugin.isEasySign(looking)) {
            sender.sendMessage(ChatColor.RED + "No EasySign actions are assigned to that sign.");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.isOnline() && !player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "That player has not been seen before.");
            return;
        }

        // count == -1 means "read the number of times used".
        int count = -1;

        // 2 args means setting the count, otherwise it is reading the count.
        if (args.length == 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (IllegalArgumentException ex) {
            }
            if (count < 0) {
                sender.sendMessage(ChatColor.RED + "The count must be zero or greater.");
                return;
            }
        }

        SignData sign = SignData.load(looking);
        boolean relevant = false;
        for (SignAction action : sign.getActions()) {
            if (action.getName().equals("announce")) {
                relevant = true;
                AnnounceAction announce = (AnnounceAction) action;
                if (count < 0) {
                    boolean used = announce.hasUsed(player.getUniqueId());
                    sender.sendMessage(ChatColor.GOLD + "The announce action has " +
                                       (used ? "been used" : "not been used") + " by " + player.getName() + ".");
                } else {
                    boolean used = (count != 0);
                    announce.setUsed(player.getUniqueId(), used);
                    sender.sendMessage(ChatColor.GOLD + "The announce action now records " + player.getName() +
                                       " as having " + (used ? "used" : "never used") + " the sign.");
                }
            } else if (action.getName().equals("max")) {
                relevant = true;
                MaxAction max = (MaxAction) action;
                if (count < 0) {
                    count = max.getTimesUsed(player.getUniqueId());
                    sender.sendMessage(ChatColor.GOLD + "The sign has been used by " + player.getName() + " " + count + " times.");
                } else {
                    max.setTimesUsed(player.getUniqueId(), count);
                    sender.sendMessage(ChatColor.GOLD + "The use count for " + player.getName() + " was set to " + count + ".");
                }
            }
        }

        if (!relevant) {
            sender.sendMessage(ChatColor.GOLD + "The sign has no actions that keep count.");
        }
    }

    /**
     * Print the big help text block when /easy-sign is run with no arguments
     */
    private void addActionCommandHelpText(CommandSender sender) {
        Player player = (Player) sender;
        SignData sign = new SignData(player.getLocation().getBlock());
        sign.setEditingPlayer(player);

        String actionFmt = ChatColor.BLUE + "%s %s" + ChatColor.WHITE + " - %s";

        // List available actions and their usage
        sender.sendMessage(ChatColor.RED + "Usage: /easy-sign <action> [<args>]");
        for (String actionName : plugin.getValidActions()) {
            try {
                Class<?> c = plugin.getActionClassByName(actionName);
                SignAction action = (SignAction) c.getConstructor(SignData.class, String[].class).newInstance(sign, new String[0]);
                sender.sendMessage(String.format(actionFmt, action.getName(), action.getUsage(), action.getHelpText()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // More commands
        sender.sendMessage(ChatColor.GOLD + "Multiple actions can be added to each sign and are run in the order they are listed.");
        sender.sendMessage(ChatColor.GOLD + "Related commands:");
        sender.sendMessage(ChatColor.YELLOW + "/easy-sign-info" + ChatColor.GRAY + ", " +
                           ChatColor.WHITE + "/easy-sign-remove <index>" + ChatColor.GRAY + ", " +
                           ChatColor.YELLOW + "/easy-sign-reorder <from> <to>" + ChatColor.GRAY + ", " +
                           ChatColor.WHITE + "/easy-sign-delete" + ChatColor.GRAY + ", " +
                           ChatColor.YELLOW + "/easy-sign-copy" + ChatColor.GRAY + ", " +
                           ChatColor.WHITE + "/easy-sign-paste" + ChatColor.GRAY + ", " +
                           ChatColor.YELLOW + "/easy-sign-used <player> <count>");

        // Colors
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                                  "&6Color reference: &00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &AA &BB &CC &DD &EE &FF"));

        // Tips
        sender.sendMessage(ChatColor.GOLD + "Remember, you also have access to /signtext.");
        sender.sendMessage("" + ChatColor.BLUE + ChatColor.UNDERLINE + "https://github.com/NerdNu/EasySigns");
    }

}
