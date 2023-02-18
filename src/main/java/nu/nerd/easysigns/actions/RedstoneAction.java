package nu.nerd.easysigns.actions;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import nu.nerd.easysigns.EasySigns;
import nu.nerd.easysigns.SignData;

public class RedstoneAction extends SignAction {

    private final SignData sign;

    private static final BlockFace[] faces = {
        BlockFace.DOWN,
        BlockFace.UP,
        BlockFace.NORTH,
        BlockFace.EAST,
        BlockFace.SOUTH,
        BlockFace.WEST
    };

    public RedstoneAction(SignData sign, String[] args) {
        this.sign = sign;
    }

    public RedstoneAction(SignData sign, ConfigurationSection attributes) {
        this.sign = sign;
    }

    @Override
    public String getName() {
        return "redstone";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getHelpText() {
        return "Causes the sign to emit a redstone signal.";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    void setRedstone(Block block, boolean powered) {
        if (block.getBlockData() instanceof AnaloguePowerable) {
            AnaloguePowerable powerable = (AnaloguePowerable) block.getBlockData();
            int val = (powered) ? 15 : 0;
            powerable.setPower(val);
            block.setBlockData(powerable);
        } else if (block.getBlockData() instanceof Powerable) {
            Powerable powerable = (Powerable) block.getBlockData();
            powerable.setPowered(powered);
            block.setBlockData(powerable);
        }
        if (powered) {
            EasySigns.instance.getTickingRedstone().add(block);
        } else {
            EasySigns.instance.getTickingRedstone().remove(block);
        }
    }

    @Override
    public boolean action(Player player) {
        Block start = sign.getBlock();
        if (EasySigns.instance.isWallSign(start)) {
            // If this is a wall sign, power from the single block behind the
            // wall the sign is on.
            WallSign data = (WallSign) start.getBlockData();
            BlockFace behind = data.getFacing().getOppositeFace();
            final Block powered = start.getRelative(behind).getRelative(behind);
            setRedstone(powered, true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    setRedstone(powered, false);
                }
            }.runTaskLater(EasySigns.instance, 20L);
        } else {
            for (BlockFace face : faces) {
                Block powered = start.getRelative(face);
                if (powered.getBlockData() instanceof AnaloguePowerable ||
                    powered.getBlockData() instanceof Powerable) {
                    setRedstone(powered, true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            setRedstone(powered, false);
                        }
                    }.runTaskLater(EasySigns.instance, 20L);
                }
            }
        }
        return true;
    }

}
