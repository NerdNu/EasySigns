EasySigns
=========
Create signs with custom actions.


## Features

 * Any sign can store a list of actions, which are triggered by left-clicking
   or right-clicking the sign.
 * Actions include sending messages, teleporting players, editing player
   inventories, sending redstone signals and even executing arbitrary commands.
 * Sign actions can be edited and copied from one sign to another.
 * Sign actions are stored using BlockStore, and so will be preserved in
   map downloads for use past the end of a server revision.


## Commands and Aliases

The EasySigns plugin is a reimplementation of a CommandHelper package documented
at http://wiki.nerd.nu/wiki/EasySign. There are some differences in the options
provided by this plugin.

The CommandHelper `/easy-sign` command shadows the corresponding command in
this plugin. You can access this plugin's command as `/easysigns:easy-sign`, but
to simplify access, all commands in this plugin also have an alias beginning 
with `/es` instead of `/easy-sign`, e.g. `/es-info` instead of `/easy-sign-info`.

For a list of all commands and options, simply run `/es`. Use `/help <command>`,
e.g. `/help /es-reorder` for documentation on specific commands.


## Item Materials

Various sign actions specify items as `<item>` and `<qty>`; the former is the
case insensitive name of a [Bukkit API Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)
like "dirt" and the latter is the quantity as an integer.


## Formatting Codes

EasySigns actions that send messages support [formatting codes](https://minecraft.gamepedia.com/Formatting_codes#Color_codes) 
using '&' followed by '0' - '9', 'a' - 'f', 'k' - 'o' and 'r'.


## Variable Substitution

Sign actions that send messages or run commands are subject to variable
substitution, whereby character sequences of the form `%variable%` are replaced
with computed values. The sequence `%%` can be used to signify a single `%`
character.

Replaced sequences and their substitution text are listed below.

| Variable | Substitution |
| :---     | :---         |
| %x%      | Integer X coordinate of the sign. |
| %y%      | Integer Y coordinate of the sign. |
| %z%      | Integer Z coordinate of the sign. |
| %w%      | World containing the player and sign. |
| %p%      | The name of the player who clicked the sign that is performing the action. | 
| %px%     | Integer X coordinate of the player. |
| %py%     | Integer Y coordinate of the player. |
| %pz%     | Integer Z coordinate of the player. |
| %px.%    | Floating point X coordinate of the player. |
| %py.%    | Floating point Y coordinate of the player. |
| %pz.%    | Floating point Z coordinate of the player. |

For backwards compatibility, some actions also support the use of `%s` to
signify the player's name.

The following sign actions support variable substitution: `announce`,
`check-empty-inventory`, `cmd`, `lore`, `msg`, `opcmd`, `take` and `takeheld`.


## CommandHelper Integration

When `cmd` and `opcmd` actions run CommandHelper aliases, it is necessary to
use the CommandHelper `/runalias` command to run the command. For example,
given a CommandHelper `/say` alias, you might do:

    /es opcmd runalias say Hello %p%!

This advice only applies to commands written in CommandHelper *MethodScript*.

 
## Sign Actions

The `/easy-sign` or `/es` command appends one "action" to the list of actions
performed by the sign. It has the syntax:

 * Either: `/easy-sign <action> [<args> ...]`
 * Or equivlently: `/es <action> [<args> ...]`

Multiple actions can be added to each sign, and are run in the same order that
they are listed, when clicked by a player.

In order to edit the actions performed by a sign, you must have the requisite
permissions and you must be looking at a sign.

The first argument to the `/es` or `/easy-sign` command is the "action" performed
by the sign. Subsequent arguments vary with the action and provide additional
information specific to the action performed.

The syntax of action arguments is represented in the following ways:

 * `<value>` signifies a value that must be supplied by the user running the
   command.
 * `[<value>]` signifies that the value can be omitted.
 * `<value1> ... <valueN>` signifies a series of N values of similar type and purpose.
 * `<value1> ...` signifies a varying length series of 1 or more similar values.

The following table describes the syntax of arguments to the various EasySigns
actions.


| Action | Arguments | Description |
| :--- | :--- |:--- |
| announce | `<message>` | Sets up an announcement sign. The message is broadcast (only once) when a player clicks the sign. The message supports formatting codes and variable substitution. |
| cart | `[<world>] <x> <y> <z>` | Spawns a minecart at the specified location. |
| check-empty-inventory | `[<message>]` | If the player's inventory is not empty, do not execute any subsequent sign actions and show `<message>`, if specified. Formatting codes and variable substitution are supported. |
| ci | | Clears the player's inventory. |
| clearpotions | | Clears all potion effects. |
| cmd | `<command>` | Runs a command as the user. Omit the leading slash. Formatting codes and variable substitution are supported. |
| dropinventory | `[<world>] <x> <y> <z> [<scatter>]` | Drops a copy of your current inventory at the specified coordinates. If `<scatter>` is `true`, give the dropped items random velocities. |
| give | `<item> <qty> [<slot>]` or `held [<slot>]` | Gives the player an item, specified either as a material and quantity, or as the word 'held' signifying the item currently held by the user configuring the sign. Place it in the specified inventory slot number, or a free slot if not specified. |
| heal | `[<gap>]` | Refills a player's health. If `<gap>` is provided (it defaults to 0) then the player gets a half a heart every `<gap>` seconds. 0 means fill it up instantly. |
| hunger | | Refills a player's hunger bar. |
| inventory | `[<clear>]` | Clears the player's inventory and gives them an exact copy of your current inventory. If `<clear>` is true, the player's inventory will be cleared before giving them the items; otherwise the items will be added to what the player has in their inventory. |
| launch | `<x> <y> <z>` | Launches a player with the specified velocity vector. The magnitude of the vector cannot exceed 10. That is, `sqrt(x^2 + y^2 + z^2)` must be less than or equal to 10. |
| leather | `<red> <green> <blue> <item1> ... <itemN>` | Gives the player leather armor with the specified red, green and blue color components (0 - 255). `<item1>` to `<itemN>` are a list of items to give and can only be: `helmet`, `chestplate`, `leggings` or `boots`. |
| lore | `<item> <qty> <lore>\|\|<itemmsg>\|\|<qtymsg>` | Takes a specified quantity of an item from a player if it has the required lore. Unlike the take action, the player must be holding the item in their main hand when they click the sign. Colors in the lore are ignored and multiple lines are concatenated without spaces. The item must be in the player's hand. If the wrong item is held, `<itemmsg>` is shown. If it is the right item but insufficient in quantity, `<qtymsg>` is shown. The `<lore>`, `<itemmsg>` and `<qtymsg>` can all be multiple words. Formatting codes and variable substitution are allowed. The double-bar sequence, `\|\|`, is used to separate those arguments. If the item is not taken for whatever reason, subsequent sign actions are not processed. Caution: multiple consecutive spaces in any of these strings will be replaced with single spaces. |
| max | `<uses>` | Allows the sign to be used `<uses>` times and no more. No other commands will be run once the limit is reached. |
| msg | `<message>` | Sends the player a message. Formatting codes and variable substitution are supported. |
| opcmd | `<command>` | Runs a command as OP. Omit the leading slash. Formatting codes and variable substitution are supported. |
| potion | `<effect> <strength> <seconds>` | Applies a [potion effect](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html) to the player. `<strength>` must be at least 1 and `<duration>` is in seconds. |
| randloc | `[<min-distance>] <max-distance>` | Teleports the player to a randomly selected surface location between `<min-distance>` and `<max-distance>` blocks from 0,0. If `<min-distance>` is omittted, it is assumed to be 0. The plugin makes 10 attempts to find a safe location to put the player, and then gives up with an error message, aborting subsequent sign actions. |
| redstone | | Causes the sign to emit a redstone signal. |
| setbed | `[<world>] <x> <y> <z>` | Sets the player's bed respawn location to the specified position. |
| sleep | | Sets the player's bed respawn location to their position when they clicked the sign. |
| sound | `<sound> <volume> <pitch> [<ambient>]` | Plays a sound to the player, or to everyone if `<ambient>` is `true`. Sounds types are those specified by the [Bukkit Sound API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html). `<volume>` is the range of the sound in blocks, divided by 15; that is, a volume of 1.0 corresponds to a range of about 15 blocks. `<pitch>` varies from 0.5 (half speed playback) to 2.0 (double speed playback). |
| take | `<item> <qty> <failmsg>` | Takes `<qty>` of the material `<item>` from a player. If they don't have enough, the `<failmsg>` is shown, and no other commands will be run. Formatting codes and variable substitution are supported. |
| takeheld | `<qty> [<itemmsg>\|\|<qtymsg>]` | Takes the item in the player's main hand if it matches what is expected. If the wrong item or insufficient items are offered, subsequent sign actions are not executed and either `<itemmsg>` or `<qtymsg>` is shown, as appropriate. The `<itemmsg>` and `<qtymsg>` can be omitted, in which case they default to 'Insufficient items.' and 'That's not the right item!' respectively. If messages are specified, they must be separated by a double bar, `\|\|`. Formatting codes and variable substitution are supported. |
| tpbed | | Teleports the player back to their bed. |
| warp | `[<world>] <x> <y> <z> [<yaw>] [<pitch>]` | Teleports the player to the specified X, Y and Z coordinates in the specified world, or the current world if no world is specified. If the yaw and pitch, are specified, they set the player's look angles in degrees at the new location.|


## Other Commands

 * `/easy-sign-info` - List all actions on the sign you are currently looking at.
   * Alias: `/es-info`

 * `/easy-sign-remove <index>` - Remove the action with the specified numeric 
   index, where index 1 is the first action.
   * Alias: `/es-remove`
 
 * `/easy-sign-reorder <from-index> <to-index>` - Remove the action at the
   specified "from" index and re-add it at the specified "to" index.
   * Alias: `/es-reorder`

 * `/easy-sign-delete` - Remove all actions from a sign.
   * Alias: `/es-delete`

 * `/easy-sign-copy` - Copy the sign's actions to the clipboard.
   * Alias: `/es-copy`

 * `/easy-sign-paste` - Paste the copied actions from the clipboard to another
   sign.
   * Alias: `/es-paste`

 * `/easy-sign-used <player> <count>` - Record that `<player>` has used a sign
   `<count>` times.
   * Alias: `/es-used`


Dependencies
------------
This plugin depends on BlockStore.


Permissions
-----------
No permissions are required to execute sign actions by clicking on a sign.

Full access to all commands is granted by the `easysigns.commands.*` permission.
That permission encompasses the following permissions:

| Permission | Description |
| :---       | :---        |
| `easysigns.commands.easy-sign` | Access to the `/easy-sign` command. |
| `easysigns.commands.easy-sign-info` | Access to the `/easy-sign-info` command. |
| `easysigns.commands.easy-sign-remove` | Access to the `/easy-sign-remove` command. |
| `easysigns.commands.easy-sign-reorder` | Access to the `/easy-sign-reorder` command. |
| `easysigns.commands.easy-sign-delete` | Access to the `/easy-sign-delete` command. |
| `easysigns.commands.easy-sign-copy` | Access to the `/easy-sign-copy` command. |
| `easysigns.commands.easy-sign-paste` | Access to the `/easy-sign-paste` command. |
| `easysigns.commands.easy-sign-used` | Access to the `/easy-sign-used` command. |

