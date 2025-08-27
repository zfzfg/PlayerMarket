package online.sterra.playermarket.managers;

import online.sterra.playermarket.PlayerMarket;
import online.sterra.playermarket.data.MarketListing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class GUIManager {
    
    private final PlayerMarket plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> playerGUIMode = new HashMap<>();
    private static final int ITEMS_PER_PAGE = 45;
    
    public GUIManager(PlayerMarket plugin) {
        this.plugin = plugin;
    }
    
    public void openMarketGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        playerGUIMode.put(player.getUniqueId(), "market");
        
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lSpielermarkt - Seite " + (page + 1));
        
        List<MarketListing> allListings = plugin.getMarketManager().getAllListings();
        allListings.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allListings.size());
        
        // Füge Items hinzu
        for (int i = startIndex; i < endIndex; i++) {
            MarketListing listing = allListings.get(i);
            ItemStack displayItem = createMarketItem(listing);
            gui.addItem(displayItem);
        }
        
        // Navigations-Items
        addNavigationItems(gui, page, allListings.size(), player);
        
        player.openInventory(gui);
    }
    
    public void openPlayerListingsGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        playerGUIMode.put(player.getUniqueId(), "listings");
        
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lMeine Angebote - Seite " + (page + 1));
        
        List<MarketListing> playerListings = plugin.getMarketManager().getPlayerListings(player.getUniqueId());
        playerListings.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, playerListings.size());
        
        // Füge Items hinzu
        for (int i = startIndex; i < endIndex; i++) {
            MarketListing listing = playerListings.get(i);
            ItemStack displayItem = createPlayerListingItem(listing);
            gui.addItem(displayItem);
        }
        
        // Navigations-Items
        addNavigationItems(gui, page, playerListings.size(), player);
        
        // Zurück zum Markt Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§a§lZurück zum Markt");
        backButton.setItemMeta(backMeta);
        gui.setItem(49, backButton);
        
        player.openInventory(gui);
    }
    
    private ItemStack createMarketItem(MarketListing listing) {
        ItemStack displayItem = listing.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7§m                              ");
        lore.add("§e§lVerkäufer: §f" + listing.getSellerName());
        lore.add("§e§lAnzahl: §f" + listing.getAmount());
        lore.add("§e§lPreis pro Stück: §f€" + String.format("%.2f", listing.getPrice()));
        lore.add("§e§lGesamtpreis: §a€" + String.format("%.2f", listing.getTotalPrice()));
        lore.add("");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        lore.add("§7Eingestellt: " + sdf.format(new Date(listing.getTimestamp())));
        lore.add("");
        lore.add("§a§l► Linksklick zum Kaufen");
        lore.add("§e§l► Rechtsklick für Details");
        lore.add("§7§m                              ");
        
        // Speichere Listing-ID in den Lore
        lore.add("§0§k" + listing.getId().toString());
        
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        displayItem.setAmount(Math.min(listing.getAmount(), 64));
        
        return displayItem;
    }
    
    private ItemStack createPlayerListingItem(MarketListing listing) {
        ItemStack displayItem = listing.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
        }
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7§m                              ");
        lore.add("§e§lAnzahl: §f" + listing.getAmount());
        lore.add("§e§lPreis pro Stück: §f€" + String.format("%.2f", listing.getPrice()));
        lore.add("§e§lGesamtwert: §a€" + String.format("%.2f", listing.getTotalPrice()));
        lore.add("");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        lore.add("§7Eingestellt: " + sdf.format(new Date(listing.getTimestamp())));
        lore.add("");
        lore.add("§c§l► Klicken zum Entfernen");
        lore.add("§7§m                              ");
        
        // Speichere Listing-ID in den Lore
        lore.add("§0§k" + listing.getId().toString());
        
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        displayItem.setAmount(Math.min(listing.getAmount(), 64));
        
        return displayItem;
    }
    
    private void addNavigationItems(Inventory gui, int currentPage, int totalItems, Player player) {
        // Vorherige Seite
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName("§a§l◄ Vorherige Seite");
            prevMeta.setLore(Arrays.asList("§7Gehe zu Seite " + currentPage));
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }
        
        // Nächste Seite
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName("§a§lNächste Seite ►");
            nextMeta.setLore(Arrays.asList("§7Gehe zu Seite " + (currentPage + 2)));
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }
        
        // Info Item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lInformationen");
        infoMeta.setLore(Arrays.asList(
                "§7Willkommen am Spielermarkt!",
                "",
                "§e§lSeite: §f" + (currentPage + 1) + "/" + Math.max(1, totalPages),
                "§e§lAngebote gesamt: §f" + totalItems,
                "",
                "§7Nutze §e/pm help §7für Befehle"
        ));
        info.setItemMeta(infoMeta);
        gui.setItem(47, info);
        
        // Spielerkopf für eigene Listings
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§e§lMeine Angebote");
        skullMeta.setLore(Arrays.asList(
                "§7Klicke hier um deine",
                "§7eigenen Angebote zu sehen",
                "",
                "§e» Klicken zum Öffnen"
        ));
        playerHead.setItemMeta(skullMeta);
        gui.setItem(51, playerHead);
        
        // Hilfe Item
        ItemStack help = new ItemStack(Material.EMERALD);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§a§lHilfe & Befehle");
        helpMeta.setLore(Arrays.asList(
                "§7Klicke für eine Übersicht",
                "§7aller verfügbaren Befehle",
                "",
                "§e» Klicken für Hilfe"
        ));
        help.setItemMeta(helpMeta);
        gui.setItem(49, help);
    }
    
    public UUID getListingIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore.isEmpty()) return null;
        
        String lastLine = lore.get(lore.size() - 1);
        if (lastLine.startsWith("§0§k")) {
            try {
                return UUID.fromString(lastLine.substring(4));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        return null;
    }
    
    public String getPlayerGUIMode(UUID playerId) {
        return playerGUIMode.getOrDefault(playerId, "market");
    }
    
    public int getPlayerPage(UUID playerId) {
        return playerPages.getOrDefault(playerId, 0);
    }
    
    public void clearPlayerData(UUID playerId) {
        playerPages.remove(playerId);
        playerGUIMode.remove(playerId);
    }
}