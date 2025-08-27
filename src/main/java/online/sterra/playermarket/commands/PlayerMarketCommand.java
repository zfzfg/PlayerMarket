package online.sterra.playermarket.commands;

import online.sterra.playermarket.PlayerMarket;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMarketCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerMarket plugin;
    
    public PlayerMarketCommand(PlayerMarket plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern verwendet werden!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Öffne Markt GUI
            plugin.getGUIManager().openMarketGUI(player, 0);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "sell":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Verwendung: /pm sell <preis> <anzahl>");
                    return true;
                }
                
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Du musst ein Item in der Hand halten!");
                    return true;
                }
                
                try {
                    double price = Double.parseDouble(args[1]);
                    int amount = Integer.parseInt(args[2]);
                    
                    if (price <= 0) {
                        player.sendMessage(ChatColor.RED + "Der Preis muss größer als 0 sein!");
                        return true;
                    }
                    
                    if (amount <= 0 || amount > itemInHand.getAmount()) {
                        player.sendMessage(ChatColor.RED + "Ungültige Anzahl! Du hast nur " + itemInHand.getAmount() + " Items.");
                        return true;
                    }
                    
                    // Item zum Markt hinzufügen
                    ItemStack sellItem = itemInHand.clone();
                    sellItem.setAmount(amount);
                    
                    if (plugin.getMarketManager().addListing(player, sellItem, amount, price)) {
                        // Item aus Inventar entfernen
                        if (amount == itemInHand.getAmount()) {
                            player.getInventory().setItemInMainHand(null);
                        } else {
                            itemInHand.setAmount(itemInHand.getAmount() - amount);
                        }
                        
                        player.sendMessage(ChatColor.GREEN + "Item erfolgreich zum Markt hinzugefügt!");
                        player.sendMessage(ChatColor.GRAY + "Anzahl: " + amount + " | Preis pro Stück: €" + price);
                    } else {
                        player.sendMessage(ChatColor.RED + "Fehler beim Hinzufügen des Items zum Markt!");
                    }
                    
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Ungültige Zahlen! Verwendung: /pm sell <preis> <anzahl>");
                }
                break;
                
            case "help":
                sendHelpMessage(player);
                break;
                
            case "listings":
            case "meine":
                plugin.getGUIManager().openPlayerListingsGUI(player, 0);
                break;
                
            case "reload":
                if (!player.hasPermission("playermarket.admin")) {
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl!");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMarketManager().loadListings();
                player.sendMessage(ChatColor.GREEN + "PlayerMarket wurde neu geladen!");
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Unbekannter Befehl! Nutze /pm help für Hilfe.");
        }
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "========== PlayerMarket Hilfe ==========");
        player.sendMessage(ChatColor.YELLOW + "/pm" + ChatColor.WHITE + " - Öffnet den Marktplatz");
        player.sendMessage(ChatColor.YELLOW + "/pm sell <preis> <anzahl>" + ChatColor.WHITE + " - Verkauft das Item in deiner Hand");
        player.sendMessage(ChatColor.YELLOW + "/pm listings" + ChatColor.WHITE + " - Zeigt deine aktiven Angebote");
        player.sendMessage(ChatColor.YELLOW + "/pm help" + ChatColor.WHITE + " - Zeigt diese Hilfe");
        if (player.hasPermission("playermarket.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/pm reload" + ChatColor.WHITE + " - Lädt das Plugin neu");
        }
        player.sendMessage(ChatColor.GOLD + "=====================================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("sell", "help", "listings", "meine"));
            if (sender.hasPermission("playermarket.admin")) {
                completions.add("reload");
            }
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {
            return Arrays.asList("<preis>");
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("sell")) {
            return Arrays.asList("<anzahl>");
        }
        
        return new ArrayList<>();
    }
    
    private List<String> filterCompletions(List<String> completions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String s : completions) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}