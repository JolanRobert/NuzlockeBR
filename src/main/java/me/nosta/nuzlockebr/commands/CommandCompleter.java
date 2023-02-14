package me.nosta.nuzlockebr.commands;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.TypeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements TabCompleter {

    private static CommandCompleter Instance;

    public static CommandCompleter getInstance() {
        if (Instance == null) Instance = new CommandCompleter();
        return Instance;
    }

    private List<String> result = new ArrayList<String>();
    private List<String> op = new ArrayList<String>();
    private List<String> nonOp = new ArrayList<String>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        result.clear();
        Player player = (Player)sender;

        if (!label.equalsIgnoreCase("nz")) return result;

        result.addAll(nonOpCommands(args));
        if (player.isOp()) result.addAll(opCommands(args));

        return result;
    }

    public List<String> nonOpCommands(String[] args) {
        nonOp.clear();

        if (args.length == 1) {
            if ("info".startsWith(args[0].toLowerCase())) nonOp.add("info");
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                for (NZType nzType : TypeManager.getInstance().typeList) {
                    if (nzType.getName().toLowerCase().startsWith(args[1].toLowerCase())) nonOp.add(nzType.getName());
                }
            }
        }

        return nonOp;
    }

    public List<String> opCommands(String[] args) {
        op.clear();

        if (args.length == 1) {
            if ("start".startsWith(args[0].toLowerCase())) op.add("start");
            if ("end".startsWith(args[0].toLowerCase())) op.add("end");
            if ("config".startsWith(args[0].toLowerCase())) op.add("config");
            if ("settype".startsWith(args[0].toLowerCase())) op.add("settype");
            if ("restore".startsWith(args[0].toLowerCase())) op.add("restore");
            if ("target".startsWith(args[0].toLowerCase())) op.add("target");
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("settype")) {
                for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
                    if (nzPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) op.add(nzPlayer.getName());
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settype")) {
                for (Type type : Type.values()) {
                    if (type.name().toLowerCase().startsWith(args[2].toLowerCase())) op.add(type.name());
                }
            }
        }

        return op;
    }




}
