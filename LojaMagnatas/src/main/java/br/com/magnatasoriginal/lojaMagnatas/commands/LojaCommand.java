package br.com.magnatasoriginal.lojaMagnatas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import br.com.magnatasoriginal.lojaMagnatas.LojaGUI;
import br.com.magnatasoriginal.lojaMagnatas.LojaMagnatas;

public class LojaCommand implements CommandExecutor {
    private LojaMagnatas plugin;
    private LojaGUI lojaGUI;

    public LojaCommand(LojaMagnatas plugin) {
        this.plugin = plugin;
        this.lojaGUI = new LojaGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("setloja")) {
            plugin.saveLoja(player.getName(), player.getLocation());
            sender.sendMessage("Loja setada em: " + player.getLocation().toString());

            // Atualiza o menu de lojas após a criação
            lojaGUI.updateMenu();
        }

        if (label.equalsIgnoreCase("lojas")) {
            lojaGUI.lojaMenu.openMenu(player, 0); // Iniciando na página 0
        }

        return true;
    }
}
