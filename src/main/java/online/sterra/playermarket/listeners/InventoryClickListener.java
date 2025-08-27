package online.sterra.playermarket.listeners;

import online.sterra.playermarket.PlayerMarket;
import online.sterra.playermarket.data.MarketListing;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventoryClickListener implements Listener {
    
    private final PlayerMarket plugin;
    
    public InventoryClickListener(PlayerMarket plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.startsWith("§6§lSpielermarkt") && !title.startsWith("§6§lMeine Angebote")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        String mode = plugin.getGUIManager().getPlayerGUIMode(player.getUniqueId());
        int currentPage = plugin.getGUIManager().getPlayerPage(player.getUniqueId());
        
        // Navigation handling
        if (slot == 45 && clickedItem.getType() == Material.ARROW) {
            // Vorherige Seite
            if (mode.equals("market")) {
                plugin.getGUIManager().openMarketGUI(player, currentPage - 1);
            } else {
                plugin.getGUIManager().openPlayerListingsGUI(player, currentPage - 1);
            }
            return;
        }
        
        if (slot == 53 && clickedItem.getType() == Material.ARROW) {
            // Nächste Seite
            if (mode.equals("market")) {
                plugin.getGUIManager().openMarketGUI(player, currentPage + 1);
            } else {
                plugin.getGUIManager().openPlayerListingsGUI(player, currentPage + 1);
            }
            return;
        }
        
        // Spezielle Buttons
        if (slot == 49) {
            if (clickedItem.getType() == Material.EMERALD) {
                // Hilfe Button
                player.closeInventory();
                player.performCommand("pm help");
                return;
            } else if (clickedItem.getType() == Material.ARROW && mode.equals("listings")) {
                // Zurück zum Markt
                plugin.getGUIManager().openMarketGUI(player, 0);
                return;
            }
        }
        
        if (slot == 51 && clickedItem.getType() == Material.PLAYER_HEAD) {
            // Meine Angebote
            plugin.getGUIManager().openPlayerListingsGUI(player, 0);
            return;
        }
        
        // Markt-Item geklickt
        if (slot < 45) {
            UUID listingId = plugin.getGUIManager().getListingIdFromItem(clickedItem);
            if (listingId == null) return;
            
            if (mode.equals("market")) {
                handleMarketItemClick(player, listingId, event);
            } else if (mode.equals("listings")) {
                handleListingRemoval(player, listingId);
            }
        }
    }
    
    private void handleMarketItemClick(Player player, UUID listingId, InventoryClickEvent event) {
        MarketListing listing = plugin.getMarketManager().getListing(listingId);
        if (listing == null) {
            player.sendMessage(ChatColor.RED + "Dieses Angebot existiert nicht mehr!");
            plugin.getGUIManager().openMarketGUI(player, plugin.getGUIManager().getPlayerPage(player.getUniqueId()));
            return;
        }
        
        if (event.isLeftClick()) {
            // Kaufmenü öffnen
            openBuyConfirmation(player, listing);
        } else if (event.isRightClick()) {
            // Details anzeigen
            showListingDetails(player, listing);
        }
    }
    
    private void handleListingRemoval(Player player, UUID listingId) {
        if (plugin.getMarketManager().removeListing(listingId, player)) {
            player.sendMessage(ChatColor.GREEN + "Angebot wurde entfernt und die Items zurückgegeben!");
            plugin.getGUIManager().openPlayerListingsGUI(player, 0);
        } else {
            player.sendMessage(ChatColor.RED + "Fehler beim Entfernen des Angebots!");
        }
    }
    
    private void openBuyConfirmation(Player player, MarketListing listing) {
        player.closeInventory();
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "========== Kaufbestätigung ==========");
        player.sendMessage(ChatColor.YELLOW + "Item: " + ChatColor.WHITE + getItemName(listing.getItem()));
        player.sendMessage(ChatColor.YELLOW + "Verkäufer: " + ChatColor.WHITE + listing.getSellerName());
        player.sendMessage(ChatColor.YELLOW + "Anzahl verfügbar: " + ChatColor.WHITE + listing.getAmount());
        player.sendMessage(ChatColor.YELLOW + "Preis pro Stück: " + ChatColor.GREEN + "€" + String.format("%.2f", listing.getPrice()));
        player.sendMessage(ChatColor.YELLOW + "Gesamtpreis: " + ChatColor.GREEN + "€" + String.format("%.2f", listing.getTotalPrice()));
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Gib im Chat die Anzahl ein, die du kaufen möchtest.");
        player.sendMessage(ChatColor.GRAY + "Schreibe 'cancel' zum Abbrechen.");
        player.sendMessage(ChatColor.GOLD + "====================================");
        
        // Hier würde man normalerweise einen Chat-Listener implementieren
        // Für dieses Beispiel kaufen wir direkt alle Items
        plugin.getMarketManager().buyListing(player, listing.getId(), listing.getAmount());
    }
    
    private void showListingDetails(Player player, MarketListing listing) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "========== Item Details ==========");
        player.sendMessage(ChatColor.YELLOW + "Item: " + ChatColor.WHITE + getItemName(listing.getItem()));
        player.sendMessage(ChatColor.YELLOW + "Verkäufer: " + ChatColor.WHITE + listing.getSellerName());
        player.sendMessage(ChatColor.YELLOW + "Anzahl: " + ChatColor.WHITE + listing.getAmount());
        player.sendMessage(ChatColor.YELLOW + "Preis pro Stück: " + ChatColor.GREEN + "€" + String.format("%.2f", listing.getPrice()));
        
        if (listing.getItem().hasItemMeta() && listing.getItem().getItemMeta().hasLore()) {
            player.sendMessage(ChatColor.YELLOW + "Item-Beschreibung:");
            for (String loreLine : listing.getItem().getItemMeta().getLore()) {
                if (!loreLine.startsWith("§7§m") && !loreLine.startsWith("§e§l") && 
                    !loreLine.startsWith("§a§l") && !loreLine.startsWith("§0§k")) {
                    player.sendMessage(ChatColor.GRAY + "  " + loreLine);
                }
            }
        }
        
        if (listing.getItem().getEnchantments().size() > 0) {
            player.sendMessage(ChatColor.YELLOW + "Verzauberungen:");
            listing.getItem().getEnchantments().forEach((ench, level) -> {
                player.sendMessage(ChatColor.GRAY + "  - " + ench.getKey().getKey() + " " + level);
            });
        }
        
        player.sendMessage(ChatColor.GOLD + "==================================");
    }
    
    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString().replace("_", " ").toLowerCase();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            plugin.getGUIManager().clearPlayerData(player.getUniqueId());
        }
    }
}