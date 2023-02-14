package me.nosta.nuzlockebr.commands;

import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.enums.Skin;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.*;
import me.nosta.nuzlockebr.nztypes.Normal;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.utils.Broadcaster;
import me.nosta.nuzlockebr.utils.Converter;
import me.nosta.nuzlockebr.utils.Disguiser;
import me.nosta.nuzlockebr.utils.MapReload;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class GameCommands implements CommandExecutor {

    private static GameCommands Instance;

    public static GameCommands getInstance() {
        if (Instance == null) Instance = new GameCommands();
        return Instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //CONSOLE
        if (!(sender instanceof Player)) return true;

        //PLAYER
        Player player = (Player)sender;

        if (!label.equalsIgnoreCase("nz") || args.length <= 0) return true;

        //NON OP COMMANDS
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) return cancelCommand(player,"/nz info <type>");

            NZType nzType = Converter.StringToNZType(args[1]);
            if (nzType == null) return cancelCommand(player,"Unknown type.");

            player.sendMessage(nzType.getDescription());
            return true;
        }

        else if (args[0].equalsIgnoreCase("creafairy")) {
            player.sendMessage(CreationLumiere.getInstance().getCreationInfo());
            return true;
        }

        else if (args[0].equalsIgnoreCase("creadark")) {
            player.sendMessage(CreationTenebres.getInstance().getCreationInfo());
            return true;
        }

        else if (args[0].equalsIgnoreCase("adapt")) {
            player.sendMessage(Normal.getAdaptInfo());
            return true;
        }

        //OP COMMANDS
        if (!player.isOp()) return cancelCommand(player,"Vous n'avez pas les droits nécessaires pour exécuter cette commande.");

        if (args[0].equalsIgnoreCase("start")) {
            if (GameManager.getInstance().gameState != NZGameState.Waiting) {
                return cancelCommand(player, "Une partie est déjà en cours !");
            }

            if (TypeManager.getInstance().typeList.size() < PlayerManager.getInstance().playerList.size()) {
                return cancelCommand(player, "Trop de joueurs pour lancer une partie ! (Max : "+TypeManager.getInstance().typeList.size()+")");
            }

            HashMap<NZPlayer,String> unreadyPlayers = PlayerManager.getInstance().getUnreadyPlayers();
            if (unreadyPlayers.size() > 0) {
                for (NZPlayer nzPlayer : unreadyPlayers.keySet()) {
                    cancelCommand(player,nzPlayer.getName()+" - "+unreadyPlayers.get(nzPlayer)); //Print reason
                }
                return cancelCommand(player, "Certains joueurs ne sont pas prêts pour le lancement de la partie !");
            }

            GameManager.getInstance().prepareGame();
        }

        else if (args[0].equalsIgnoreCase("end")) {
            if (GameManager.getInstance().gameState == NZGameState.Waiting) {
                return cancelCommand(player, "Aucune partie n'est en cours !");
            }

            GameManager.getInstance().endGame();
        }

        else if (args[0].equalsIgnoreCase("config")) {
            if (GameManager.getInstance().gameState != NZGameState.Waiting) {
                return cancelCommand(player, "Une partie est déjà en cours !");
            }

            Inventory configInventory = InventoryManager.getInstance().getConfigInventory();
            player.openInventory(configInventory);
        }

        else if (args[0].equalsIgnoreCase("settype")) {
            if (args.length < 3) return cancelCommand(player,"/nz settype <player> <type>");
            if (GameManager.getInstance().gameState != NZGameState.Waiting) return cancelCommand(player, "Une partie est déjà en cours !");

            NZPlayer nzPlayer = Converter.StringToNZPlayer(args[1]);
            if (nzPlayer == null) return cancelCommand(player,"Player inconnu.");
            NZType nzType = Converter.StringToNZType(args[2]);
            if (nzType == null) return cancelCommand(player,"Type inconnu.");
            if (nzType.getNZPlayer() != null) return cancelCommand(player,"Type déjà sélectionné !");

            nzPlayer.setNZType(nzType);
        }

        else if (args[0].equalsIgnoreCase("restore")) {
            if (GameManager.getInstance().gameState != NZGameState.Waiting)
                return cancelCommand(player, "Impossible de restaurer la carte durant une partie !");

            MapReload.restore();
        }

        else if (args[0].equalsIgnoreCase("target")) {
            if (GameManager.getInstance().gameState != NZGameState.Waiting)
                return cancelCommand(player, "Impossible d'effectuer cette commande pendant une partie !");

            if (args.length < 2) return cancelCommand(player,"/nz target <value>");
            try{
                int number = Integer.parseInt(args[1]);
                ScoreManager.getInstance().updateTarget(number);
                Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.GREEN+"Target défini sur : "+ChatColor.RED+ChatColor.BOLD+number);
            }
            catch (NumberFormatException ex){
                return cancelCommand(player,"/nz target <value>");
            }
        }

        else if (args[0].equalsIgnoreCase("test")) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            Disguiser.setSkin(nzPlayer, Skin.Asuka);
        }

        else if (args[0].equalsIgnoreCase("test2")) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            Disguiser.setSkin(nzPlayer, Skin.Spy);
        }

        else if (args[0].equalsIgnoreCase("reset")) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            Disguiser.setSkin(nzPlayer, Skin.Reset);
        }

        return true;
    }

    public boolean cancelCommand(Player player, String message) {
        player.sendMessage(ChatColor.DARK_RED+"[NZ] "+ChatColor.RED+message);
        return true;
    }
}
