package br.com.magnatasoriginal.lojaMagnatas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import br.com.magnatasoriginal.lojaMagnatas.LojaMagnatas;
import br.com.magnatasoriginal.lojaMagnatas.LojaAtualizadaEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelLojaCommand implements CommandExecutor {
    private LojaMagnatas plugin;

    public DelLojaCommand(LojaMagnatas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getName();

            try (Connection conn = plugin.getDbManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM lojas WHERE playerName = ?")) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
                player.sendMessage(plugin.getMessage("loja_removida"));
                plugin.getServer().broadcastMessage(plugin.getMessage("broadcast_loja_removida", playerName));

                // Disparar o evento de atualização do menu
                plugin.getServer().getPluginManager().callEvent(new LojaAtualizadaEvent());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
