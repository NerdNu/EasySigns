package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Abstract base class for sign actions. Actions, such as WarpAction, inherit
 * from here. Constructors, when coming from a command, should take SignData
 * sign, String[] args and make isValid() return false if a valid action could
 * not be constructed. When loading an action from BlockStore, the constructor
 * with SignData sign, ConfigurationSection attributes will be used.
 */
public abstract class SignAction {

    /**
     * The canonical name of this action, for user commands and serialization
     */
    abstract public String getName();

    /**
     * The arguments this action takes
     */
    abstract public String getUsage();

    /**
     * The help text (excluding arguments) displayed by the command
     */
    abstract public String getHelpText();

    /**
     * Was the action successfully constructed with valid arguments? This is
     * used instead of having the constructor throw an exception, because you
     * can't really get at the usage or help text without a class...
     *
     * @return true if the action was successfully constructed
     */
    abstract public boolean isValid();

    /**
     * If the action class has any attributes to persist to BlockStore, it
     * should override this method and supply a key/value map of the objects.
     * When the signs are loaded, one of the two constructors will load them
     * with the Bukkit ConfigurationSection API.
     */
    public Map<String, Object> serialize() {
        return new HashMap<>();
    }

    /**
     * This should be overriden so /easy-sign-info will show the correct
     * representation of the action stored. For a simple action with no
     * arguments, this can be left as just the name.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * The action performed when the sign is clicked.
     *
     * @param player the player clicking the sign.
     * @return true if subsequent actions should be executed; false to abort
     *         subsequent sign actions.
     */
    abstract public boolean action(Player player);

    /**
     * Translate formatting codes beginning with '&' and replace doubled-up
     * ampersands with a single ampersand.
     *
     * @param message the message string to be translated.
     * @return the translated message.
     */
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("&&", "\f")).replace("\f", "&");
    }

    /**
     * Translate formatting codes, substitute variables surrounded by % and
     * replace %% with % to yield a formatted string.
     *
     * As a special case for backwards compatibility, the string %s is replaced
     * by the player's name.
     *
     * @param format the format string into which variables are substituted.
     * @param player the player from whom some variables take their values.
     * @param sign   the sign block from which some variables take their values.
     * @return the translated, formatted string.
     */
    public static String substitute(String format, Player player, Block sign) {
        return substitute(translate(format), getStandardVariables(player, sign));
    }

    /**
     * Return a map containing the standard substitution variables documented in
     * the README.md.
     *
     * The map maps variable name to a Supplier<> that returns a String
     * representation of the variable's value.
     *
     * @param player the player from whom some variables take their values.
     * @param sign   the sign block from which some variables take their values.
     * @return a map of standard variables.
     */
    public static Map<String, Supplier<String>> getStandardVariables(Player player, Block sign) {
        Map<String, Supplier<String>> variables = new HashMap<>();
        variables.put("x", () -> Integer.toString(sign.getLocation().getBlockX()));
        variables.put("y", () -> Integer.toString(sign.getLocation().getBlockY()));
        variables.put("z", () -> Integer.toString(sign.getLocation().getBlockZ()));
        variables.put("w", () -> sign.getLocation().getWorld().getName());
        variables.put("p", () -> player.getName());
        variables.put("px", () -> Integer.toString(player.getLocation().getBlockX()));
        variables.put("py", () -> Integer.toString(player.getLocation().getBlockY()));
        variables.put("pz", () -> Integer.toString(player.getLocation().getBlockZ()));
        variables.put("px.", () -> String.format("%.3f", player.getLocation().getX()));
        variables.put("py.", () -> String.format("%.3f", player.getLocation().getY()));
        variables.put("pz.", () -> String.format("%.3f", player.getLocation().getZ()));
        return variables;
    }

    /**
     * Perform variable substitution on a format containing variable references
     * of the form %name%.
     *
     * The sequence %% is replaced with a single %.
     *
     * As a special case for backwards compatibility, %s is treated as
     * equivalent to %p%.
     *
     * @param format    the format specifier.
     * @param variables a map from variable name (without %) to an object that
     *                  supplies its String representation.
     * @return the format with all defined variable references replaced;
     *         undefined variables are not replaced and %% is converted to %.
     */
    public static String substitute(String format, Map<String, Supplier<String>> variables) {
        StringBuilder result = new StringBuilder();
        StringBuilder segment = new StringBuilder();

        // True if inside a %variable% reference:
        boolean inVar = false;
        for (int i = 0; i < format.length(); ++i) {
            char c = format.charAt(i);
            if (c == '%') {
                if (inVar) {
                    // End this variable reference.
                    inVar = false;
                    String variableName = segment.toString();
                    segment.setLength(0);

                    if (variables.containsKey(variableName)) {
                        result.append(variables.get(variableName).get());
                    } else {
                        result.append('%').append(variableName).append('%');
                    }
                } else {
                    // Non-variable, literal text.
                    char next = (i + 1 < format.length()) ? format.charAt(i + 1) : '\0';
                    if (next == '%') {
                        // "%%" => literal '%'
                        segment.append(c);
                        ++i;
                    } else {
                        // End this literal segment; start a variable.
                        inVar = true;
                        if (segment.length() > 0) {
                            result.append(segment.toString());
                            segment.setLength(0);
                        }
                    }
                }
            } else if (inVar && c == 's' && segment.length() == 0) {
                // Backwards compatibility support for %s as equivalent to %p%.
                // TODO: drop this prior to preparation of a revision. It
                // prevents %s.*% variables.
                inVar = false;
                result.append(variables.get("p").get());
            } else {
                segment.append(c);
            }
        } // for

        // Last segment.
        if (inVar)

        {
            // Mis-matched % at start of variable reference becomes literal.
            segment.insert(0, '%');
        }

        if (segment.length() > 0) {
            result.append(segment.toString());
        }

        return result.toString();
    }
}
