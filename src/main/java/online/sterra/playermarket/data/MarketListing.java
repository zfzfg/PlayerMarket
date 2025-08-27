package online.sterra.playermarket.data;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class MarketListing {
    private final UUID id;
    private final UUID seller;
    private final String sellerName;
    private final ItemStack item;
    private int amount;
    private final double price;
    private final long timestamp;
    
    public MarketListing(UUID seller, String sellerName, ItemStack item, int amount, double price) {
        this.id = UUID.randomUUID();
        this.seller = seller;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.amount = amount;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }
    
    public MarketListing(UUID id, UUID seller, String sellerName, ItemStack item, int amount, double price, long timestamp) {
        this.id = id;
        this.seller = seller;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.amount = amount;
        this.price = price;
        this.timestamp = timestamp;
    }
    
    public UUID getId() { return id; }
    public UUID getSeller() { return seller; }
    public String getSellerName() { return sellerName; }
    public ItemStack getItem() { return item.clone(); }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
    
    public void setAmount(int amount) { this.amount = amount; }
    
    public double getTotalPrice() {
        return price * amount;
    }
}