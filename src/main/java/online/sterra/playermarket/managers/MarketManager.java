package online.sterra.playermarket.managers;

import online.sterra.playermarket.PlayerMarket;
import online.sterra.playermarket.data.MarketListing;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MarketManager {
    
    private final PlayerMarket plugin;
    private final Map<UUID, MarketListing> listings;
    private final int maxListingsPerPlayer;
    private final double taxRate;
    
    public MarketManager(PlayerMarket plugin) {
        this.plugin = plugin;
        this.listings = new HashMap<>();
        this.maxListingsPerPlayer = plugin.getConfig().getInt("max-listings-per-player", 10);
        this.taxRate = plugin.getConfig().getDouble("tax-rate", 0.05);
    }
    
    public boolean addListing(Player seller, ItemStack item, int amount, double price) {
        // Überprüfe maximale Anzahl an Listings
        long currentListings = listings.values().stream()
                .filter(l -> l.getSeller().equals(seller.getUniqueId()))
                .count();
        
        if (currentListings >= maxListingsPerPlayer && !seller.hasPermission("playermarket.unlimited")) {
            seller.sendMessage("§cDu hast bereits die maximale Anzahl an Angeboten (" + maxListingsPerPlayer + ") erreicht!");
            return false;
        }
        
        MarketListing listing = new MarketListing(
                seller.getUniqueId(),
                seller.getName(),
                item,
                amount,
                price
        );
        
        listings.put(listing.getId(), listing);
        saveListings();
        return true;
    }
    
    public boolean buyListing(Player buyer, UUID listingId, int amount) {
        MarketListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }
        
        if (listing.getSeller().equals(buyer.getUniqueId())) {
            buyer.sendMessage("§cDu kannst deine eigenen Items nicht kaufen!");
            return false;
        }
        
        if (amount > listing.getAmount()) {
            buyer.sendMessage("§cNicht genügend Items verfügbar!");
            return false;
        }
        
        double totalPrice = listing.getPrice() * amount;
        
        // Hier würde normalerweise die Economy-Integration stattfinden
        // Für dieses Beispiel überspringen wir die Bezahlung
        
        // Gib Item dem Käufer
        ItemStack purchasedItem = listing.getItem();
        purchasedItem.setAmount(amount);
        
        HashMap<Integer, ItemStack> leftover = buyer.getInventory().addItem(purchasedItem);
        if (!leftover.isEmpty()) {
            // Inventar voll, Items droppen
            for (ItemStack item : leftover.values()) {
                buyer.getWorld().dropItemNaturally(buyer.getLocation(), item);
            }
            buyer.sendMessage("§eDein Inventar war voll! Einige Items wurden gedroppt.");
        }
        
        // Update oder entferne Listing
        if (amount == listing.getAmount()) {
            listings.remove(listingId);
        } else {
            listing.setAmount(listing.getAmount() - amount);
        }
        
        saveListings();
        
        buyer.sendMessage("§aErfolgreich " + amount + " Items für €" + String.format("%.2f", totalPrice) + " gekauft!");
        
        return true;
    }
    
    public boolean removeListing(UUID listingId, Player requester) {
        MarketListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }
        
        if (!listing.getSeller().equals(requester.getUniqueId()) && !requester.hasPermission("playermarket.admin")) {
            return false;
        }
        
        // Gib Items zurück
        ItemStack returnItem = listing.getItem();
        returnItem.setAmount(listing.getAmount());
        
        HashMap<Integer, ItemStack> leftover = requester.getInventory().addItem(returnItem);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                requester.getWorld().dropItemNaturally(requester.getLocation(), item);
            }
            requester.sendMessage("§eDein Inventar war voll! Einige Items wurden gedroppt.");
        }
        
        listings.remove(listingId);
        saveListings();
        
        return true;
    }
    
    public List<MarketListing> getAllListings() {
        return new ArrayList<>(listings.values());
    }
    
    public List<MarketListing> getPlayerListings(UUID playerId) {
        return listings.values().stream()
                .filter(l -> l.getSeller().equals(playerId))
                .collect(Collectors.toList());
    }
    
    public MarketListing getListing(UUID listingId) {
        return listings.get(listingId);
    }
    
    public void loadListings() {
        listings.clear();
        ConfigurationSection section = plugin.getDataConfig().getConfigurationSection("listings");
        if (section == null) return;
        
        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                UUID seller = UUID.fromString(section.getString(key + ".seller"));
                String sellerName = section.getString(key + ".sellerName");
                ItemStack item = section.getItemStack(key + ".item");
                int amount = section.getInt(key + ".amount");
                double price = section.getDouble(key + ".price");
                long timestamp = section.getLong(key + ".timestamp");
                
                MarketListing listing = new MarketListing(id, seller, sellerName, item, amount, price, timestamp);
                listings.put(id, listing);
            } catch (Exception e) {
                plugin.getLogger().warning("Fehler beim Laden von Listing " + key + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info(listings.size() + " Listings geladen.");
    }
    
    public void saveListings() {
        plugin.getDataConfig().set("listings", null);
        
        for (MarketListing listing : listings.values()) {
            String path = "listings." + listing.getId().toString();
            plugin.getDataConfig().set(path + ".seller", listing.getSeller().toString());
            plugin.getDataConfig().set(path + ".sellerName", listing.getSellerName());
            plugin.getDataConfig().set(path + ".item", listing.getItem());
            plugin.getDataConfig().set(path + ".amount", listing.getAmount());
            plugin.getDataConfig().set(path + ".price", listing.getPrice());
            plugin.getDataConfig().set(path + ".timestamp", listing.getTimestamp());
        }
        
        plugin.saveDataConfig();
    }
}