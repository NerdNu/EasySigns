package nu.nerd.easysigns;

import net.sothatsit.blockstore.BlockStoreApi;
import nu.nerd.easysigns.actions.SignAction;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Representation of an EasySign
 */
public class SignData {


    private Block block;
    private List<SignAction> actions;


    public SignData(Block block) {
        this.block = block;
        this.actions = new ArrayList<>();
    }


    public Block getBlock() {
        return block;
    }


    public List<SignAction> getActions() {
        return actions;
    }


    public void save() {
        List<String> list = actions.stream().map(SignAction::toString).collect(Collectors.toList());
        String[] pack = list.toArray(new String[list.size()]);
        BlockStoreApi.setBlockMeta(block, EasySigns.instance, EasySigns.prefix, pack);
    }


}