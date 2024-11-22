package br.com.magnatasoriginal.lojaMagnatas.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import br.com.magnatasoriginal.lojaMagnatas.LojaMagnatas;
import br.com.magnatasoriginal.lojaMagnatas.LojaAtualizadaEvent;

public class SetLojaCommand implements CommandExecutor {
    private LojaMagnatas plugin;

    public SetLojaCommand(LojaMagnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getName();

            Location existingLoja = plugin.getLojaLocation(playerName);
            if (existingLoja != null) {
                player.sendMessage(plugin.getMessage("loja_existe"));
                return true;
            }

            Location loc = player.getLocation();
            plugin.saveLoja(playerName, loc);
            player.sendMessage(plugin.getMessage("loja_setada"));
            plugin.getServer().broadcastMessage(plugin.getMessage("broadcast_loja_criada", playerName));

            // Atualiza o menu de lojas após a criação
            plugin.getServer().getPluginManager().callEvent(new LojaAtualizadaEvent());
        } else {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
        }
        return true;
    }
}
