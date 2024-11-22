package br.com.magnatasoriginal.lojaMagnatas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import br.com.magnatasoriginal.lojaMagnatas.LojaMagnatas;
import org.bukkit.Location;

public class TeleportLojaCommand implements CommandExecutor {
    private LojaMagnatas plugin;

    public TeleportLojaCommand(LojaMagnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("Por favor, forneça o nome do jogador cuja loja você deseja visitar.");
                return false;
            }

            String lojaOwner = args[0];
            Location loc = plugin.getLojaLocation(lojaOwner);

            if (loc == null) {
                player.sendMessage("A loja do jogador " + lojaOwner + " não foi encontrada.");
                return true;
            }

            player.teleport(loc);
            player.sendMessage("Você foi teleportado para a loja de " + lojaOwner + "!");

            // Logar a visita
            plugin.logVisit(player.getName(), lojaOwner);
        }
        return true;
    }
}
