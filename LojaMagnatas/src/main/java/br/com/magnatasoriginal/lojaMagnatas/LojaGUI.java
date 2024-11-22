package br.com.magnatasoriginal.lojaMagnatas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class LojaGUI implements Listener {

    private final LojaMagnatas plugin;
    public LojaMenu lojaMenu;
    private final Map<Player, String> pendingDeletions = new HashMap<>();
    private final Map<Player, TeleportTask> pendingTeleports = new HashMap<>();

    public LojaGUI(LojaMagnatas plugin) {
        this.plugin = plugin;
        this.lojaMenu = new LojaMenu();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onLojaAtualizada(br.com.magnatasoriginal.lojaMagnatas.LojaAtualizadaEvent event) {
                updateMenu();
            }
        }, plugin);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTitle().startsWith("Lojas dos Jogadores")) {
            updateMenu();
        }
    }

    public void updateMenu() {
        for (Inventory page : lojaMenu.getPages()) {
            page.clear();
            lojaMenu.addBorders(page);
            addIcons(page);
        }

        List<Map.Entry<String, Location>> lojas = new ArrayList<>(plugin.getAllLojas().entrySet());
        int itemsPerPage = 28; // Available slots per page (54 - borders and navigation)

        for (int i = 0; i < lojas.size(); i++) {
            Map.Entry<String, Location> entry = lojas.get(i);
            int pageIndex = i / itemsPerPage;
            if (pageIndex >= lojaMenu.getTotalPages()) break;

            Inventory page = lojaMenu.getPages()[pageIndex];
            ItemStack lojaItem = createLojaItem(entry.getKey());

            int slot = getNextAvailableSlot(page);
            if (slot != -1) {
                page.setItem(slot, lojaItem);
            }
        }
    }

    private int getNextAvailableSlot(Inventory inventory) {
        int[] validSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int slot : validSlots) {
            if (inventory.getItem(slot) == null) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack createLojaItem(String playerName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        assert meta != null;
        meta.setOwner(playerName);
        meta.setDisplayName(ChatColor.YELLOW + playerName + " - Loja");

        int visitCount = plugin.getVisitCount(playerName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Visitas: " + visitCount);
        lore.add("");
        lore.add(ChatColor.WHITE + "Clique esquerdo para visitar");
        if (meta.getOwner() != null) {
            lore.add(ChatColor.WHITE + "Clique direito para remover (apenas dono)");
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private void addIcons(Inventory inv) {
        inv.setItem(45, lojaMenu.getPreviousPageIcon());
        inv.setItem(53, lojaMenu.getNextPageIcon());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Lojas dos Jogadores")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Material itemType = event.getCurrentItem().getType();
            Player player = (Player) event.getWhoClicked();

            if (itemType == Material.PLAYER_HEAD) {
                String displayName = Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName();
                String playerName = ChatColor.stripColor(displayName).split(" - ")[0];
                Location loc = plugin.getLojaLocation(playerName);

                if (event.isRightClick()) {
                    if (player.hasPermission("lojamagnatas.admin") || playerName.equals(player.getName())) {
                        pendingDeletions.put(player, playerName);
                        player.sendMessage(plugin.getMessage("confirmacao_remocao", playerName));
                    } else {
                        player.sendMessage("Você não tem permissão para excluir a loja de outros jogadores.");
                    }
                } else {
                    if (loc != null) {
                        teleportPlayer(player, loc, playerName);
                    } else {
                        player.sendMessage(plugin.getMessage("loja_nao_encontrada", playerName));
                    }
                }
            } else if (itemType == Material.ARROW) {
                lojaMenu.handleClick(event);
            }
        }
    }

    public void teleportPlayer(Player player, Location loc, String playerName) {
        if (player.hasPermission("lojamagnatas.vip")) {
            player.teleport(loc);
            plugin.logVisit(player.getName(), playerName);
            player.sendMessage(plugin.getMessage("teleporte_sucesso", playerName));
        } else {
            if (pendingTeleports.containsKey(player)) {
                player.sendMessage(plugin.getMessage("teleporte_ja_em_progresso"));
                return;
            }

            TeleportTask task = new TeleportTask(player, loc, playerName, 5);
            pendingTeleports.put(player, task);
            task.start();
        }
    }

    private class TeleportTask {
        private final Player player;
        private final Location targetLocation;
        private final String playerName;
        private final long startTime;
        private final long duration;
        private boolean cancelled = false;

        public TeleportTask(Player player, Location loc, String playerName, int seconds) {
            this.player = player;
            this.targetLocation = loc;
            this.playerName = playerName;
            this.startTime = System.currentTimeMillis();
            this.duration = seconds;
        }

        public void start() {
            new Thread(() -> {
                try {
                    for (long i = duration; i > 0; i--) {
                        if (cancelled) {
                            return;
                        }

                        final long count = i;

                        // Envia a mensagem na thread principal
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.sendMessage(plugin.getMessage("tempo_restante", String.valueOf(count)))
                        );

                        Thread.sleep(1000); // Espera 1 segundo real
                    }

                    // Executa o teleporte na thread principal
                    if (!cancelled) {
                        Bukkit.getScheduler().runTask(plugin, this::completeTeleport);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        public void cancel() {
            cancelled = true;
            pendingTeleports.remove(player);
            player.sendMessage(plugin.getMessage("teleporte_cancelado"));
        }

        private void completeTeleport() {
            if (!cancelled && pendingTeleports.containsKey(player)) {
                player.teleport(targetLocation);
                plugin.logVisit(player.getName(), playerName);
                player.sendMessage(plugin.getMessage("teleporte_sucesso", playerName));
                pendingTeleports.remove(player);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (pendingTeleports.containsKey(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
                pendingTeleports.get(player).cancel();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Confirmação de Remoção")) {
            Player player = (Player) event.getPlayer();
            String lojaOwner = pendingDeletions.remove(player);
            if (lojaOwner != null) {
                player.performCommand("delloja " + lojaOwner);
            }
        }
    }
}
