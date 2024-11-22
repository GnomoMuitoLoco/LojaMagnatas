package br.com.magnatasoriginal.lojaMagnatas;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import br.com.magnatasoriginal.lojaMagnatas.commands.LojaCommand;
import br.com.magnatasoriginal.lojaMagnatas.commands.SetLojaCommand;
import br.com.magnatasoriginal.lojaMagnatas.commands.DelLojaCommand;
import br.com.magnatasoriginal.lojaMagnatas.commands.TeleportLojaCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LojaMagnatas extends JavaPlugin {
    private DatabaseManager dbManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        dbManager = new DatabaseManager(getDataFolder());
        dbManager.connect();

        getLogger().info("LojaMagnatas foi ativado!");

        // Registrar comandos
        getCommand("lojas").setExecutor(new LojaCommand(this));
        getCommand("setloja").setExecutor(new SetLojaCommand(this));
        getCommand("delloja").setExecutor(new DelLojaCommand(this));
        getCommand("tploja").setExecutor(new TeleportLojaCommand(this));
    }

    @Override
    public void onDisable() {
        dbManager.close();
        getLogger().info("LojaMagnatas foi desativado!");
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public String getMessage(String key, String... args) {
        String message = config.getString("messages." + key, "Mensagem não encontrada: " + key);
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            for (int i = 0; i < args.length; i++) {
                message = message.replace("%" + (i + 1), args[i]); // Corrigido para substituir %1, %2, etc.
            }
        }
        return message;
    }

    public void saveLoja(String playerName, Location loc) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO lojas (playerName, world, x, y, z, pitch, yaw, visitCount) VALUES (?, ?, ?, ?, ?, ?, ?, 0)")) {
            stmt.setString(1, playerName);
            stmt.setString(2, loc.getWorld().getName());
            stmt.setDouble(3, loc.getX());
            stmt.setDouble(4, loc.getY());
            stmt.setDouble(5, loc.getZ());
            stmt.setFloat(6, loc.getPitch());
            stmt.setFloat(7, loc.getYaw());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getLojaLocation(String playerName) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT world, x, y, z, pitch, yaw FROM lojas WHERE playerName = ?")) {
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    World world = getServer().getWorld(rs.getString("world"));
                    if (world == null) return null;
                    return new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Location> getAllLojas() {
        Map<String, Location> lojas = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT playerName, world, x, y, z, pitch, yaw FROM lojas")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    World world = getServer().getWorld(rs.getString("world"));
                    if (world == null) continue;
                    String playerName = rs.getString("playerName");
                    Location loc = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                    lojas.put(playerName, loc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lojas;
    }

    public int getVisitCount(String playerName) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT visitCount FROM lojas WHERE playerName = ?")) {
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("visitCount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void incrementVisitCount(String playerName) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE lojas SET visitCount = visitCount + 1 WHERE playerName = ?")) {
            stmt.setString(1, playerName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logVisit(String playerName, String lojaOwner) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO logs (playerName, lojaOwner, timestamp) VALUES (?, ?, ?)")) {
            stmt.setString(1, playerName);
            stmt.setString(2, lojaOwner);
            stmt.setString(3, new java.sql.Timestamp(System.currentTimeMillis()).toString());
            stmt.executeUpdate();
            incrementVisitCount(lojaOwner);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
