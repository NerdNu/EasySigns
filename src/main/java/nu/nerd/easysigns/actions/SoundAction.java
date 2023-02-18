package nu.nerd.easysigns.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import nu.nerd.easysigns.SignData;

public class SoundAction extends SignAction {

    private final SignData sign;
    private Sound sound;
    private float volume;
    private float pitch;
    private boolean ambient = false;
    private boolean valid = true;

    public SoundAction(SignData sign, String[] args) {
        this.sign = sign;
        try {
            sound = Sound.valueOf(args[0].toUpperCase());
            volume = Float.parseFloat(args[1]);
            pitch = Float.parseFloat((args[2]));
            if (volume <= 0.0 || pitch < 0.5 || pitch > 2.0) {
                valid = false;
                return;
            }
            if (args.length > 3) {
                ambient = Boolean.parseBoolean(args[3]);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            valid = false;
        }
    }

    public SoundAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
        this.sound = Sound.valueOf(attributes.getString("sound"));
        this.volume = ((Double) attributes.getDouble("volume")).floatValue();
        this.pitch = ((Double) attributes.getDouble("pitch")).floatValue();
        this.ambient = attributes.getBoolean("ambient");
    }

    @Override
    public String getName() {
        return "sound";
    }

    @Override
    public String getUsage() {
        return "<sound> <volume> <pitch> [<ambient>]";
    }

    @Override
    public String getHelpText() {
        return "Plays a sound to the player, or to everyone if [<ambient>] is true. " +
               "Sounds types are listed at https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html. " +
               "<volume> is the range of the sound in blocks, divided by 15. <pitch> varies from 0.5 to 2.0.";
    }

    @Override
    public String toString() {
        if (ambient) {
            return String.format("%s %s %.2f %.2f %s", getName(), sound.toString(), volume, pitch, ambient);
        } else {
            return String.format("%s %s %.2f %.2f", getName(), sound.toString(), volume, pitch);
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("sound", sound.name());
        map.put("volume", volume);
        map.put("pitch", pitch);
        map.put("ambient", ambient);
        return map;
    }

    @Override
    public boolean action(Player player) {
        if (ambient) {
            sign.getBlock().getLocation().getWorld().playSound(sign.getBlock().getLocation(), sound, volume, pitch);
        } else {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
        return true;
    }

}
