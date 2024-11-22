package br.com.magnatasoriginal.lojaMagnatas;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LojaMenu {

    private final int TOTAL_PAGES = 3;
    private final Inventory[] pages = new Inventory[TOTAL_PAGES];
    private int currentPage = 0;

    private ItemStack previousPageIcon;
    private ItemStack nextPageIcon;
    private final ItemStack borderItem;

    public LojaMenu() {
        borderItem = createIcon(Material.YELLOW_STAINED_GLASS_PANE, " ");

        setDefaultIcons();
        for (int i = 0; i < TOTAL_PAGES; i++) {
            pages[i] = Bukkit.createInventory(null, 54, "Lojas dos Jogadores - Página " + (i + 1));
            addBorders(pages[i]);
            addIcons(pages[i]);
        }
    }

    private void setDefaultIcons() {
        previousPageIcon = createIcon(Material.ARROW, "Voltar Página");
        nextPageIcon = createIcon(Material.ARROW, "Avançar Página");
    }

    private ItemStack createIcon(Material material, String name) {
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(name);
        icon.setItemMeta(meta);
        return icon;
    }

    public void addBorders(Inventory inv) {
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52};
        for (int slot : borderSlots) {
            inv.setItem(slot, borderItem);
        }
    }

    public void setPreviousPageIcon(Material material, String name) {
        previousPageIcon = createIcon(material, name);
        for (Inventory page : pages) {
            page.setItem(45, previousPageIcon); // Mudar a posição para 45
        }
    }

    public void setNextPageIcon(Material material, String name) {
        nextPageIcon = createIcon(material, name);
        for (Inventory page : pages) {
            page.setItem(53, nextPageIcon);
        }
    }

    private void addIcons(Inventory inv) {
        inv.setItem(45, previousPageIcon); // Mudar a posição para 45
        inv.setItem(46, borderItem); // Adicionar vidro amarelo no lugar de 46
        inv.setItem(53, nextPageIcon);
    }

    public void openMenu(Player player, int page) {
        if (page >= 0 && page < TOTAL_PAGES) {
            currentPage = page;
            player.openInventory(pages[page]);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true); // Cancela o clique para impedir a remoção de itens
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot == 45) { // Mudar a verificação para 45
            int previousPage = (currentPage - 1 + TOTAL_PAGES) % TOTAL_PAGES;
            openMenu(player, previousPage);
        } else if (slot == 53) {
            int nextPage = (currentPage + 1) % TOTAL_PAGES;
            openMenu(player, nextPage);
        }
    }

    public int getTotalPages() {
        return TOTAL_PAGES;
    }

    public Inventory[] getPages() {
        return pages;
    }

    public ItemStack getPreviousPageIcon() {
        return previousPageIcon;
    }

    public ItemStack getNextPageIcon() {
        return nextPageIcon;
    }
}
