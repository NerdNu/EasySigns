package nu.nerd.easysigns;

import nu.nerd.easysigns.actions.SignAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.stream.Collectors;


public class SignListener implements Listener {


    private EasySigns plugin;


    public SignListener() {
        plugin = EasySigns.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || !plugin.isSign(event.getClickedBlock())) return;
        if (plugin.isEasySign(event.getClickedBlock())) {
            SignData sign = SignData.load(event.getClickedBlock());
            for (SignAction action : sign.getActions()) {
                action.action(event.getPlayer());
                if (action.shouldExit(event.getPlayer())) break;
            }
            plugin.getLogger().info(String.format(
                    "EasySign: player=%s sign_loc=%s sign_cmds=%s",
                    event.getPlayer().getName(),
                    sign.getBlock().getLocation().toString(),
                    sign.getActions().stream().map(SignAction::getName).collect(Collectors.joining(", "))
            ));
        }
    }


    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event) {
        //Keep the redstone powered by the sign going for more than one tick
        if (plugin.getTickingRedstone().contains(event.getBlock())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }


}
