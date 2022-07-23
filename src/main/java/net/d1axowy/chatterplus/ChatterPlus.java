package net.d1axowy.chatterplus;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ChatterPlus extends JavaPlugin implements CommandExecutor, Listener {



    boolean isChatLocked = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getLogger().info("ChatterPlus starting with " + Bukkit.getBukkitVersion() + " and " + Bukkit.getVersion());
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")==null){
            getLogger().warning("PlaceholderAPI not detected, this might end up with errors!");
        }
        getServer().getPluginManager().registerEvents(this, this);

        reload();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("ChatterPlus has ended it's job with "+Bukkit.getBukkitVersion() + " and " + Bukkit.getVersion());
    }


    @EventHandler
    public void UpdateChecker(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        try {
            if (p.hasPermission("cplus.check-updates{")) {
                HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=103626").openConnection();
                con.setRequestMethod("GET");
                String onlineVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
                boolean availableUpdate = !onlineVersion.equals(getDescription().getVersion());

                if(availableUpdate){
                    p.sendMessage(ChatColor.GREEN + "New version of ChatterPlus available: " + onlineVersion);
                }
            }
            } catch(MalformedURLException exception){

            } catch(IOException exception){

            }
        }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatclear")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.hasPermission("cplus.chatclear")) {
                    for (int i = 0; i < 100; ++i) {
                        for (Player people : Bukkit.getOnlinePlayers()) {
                            people.sendMessage(ChatColor.BLUE + "");
                        }
                    }
                    for (Player people : Bukkit.getOnlinePlayers()) {
                        people.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-cleared-global").replace("%player%", p.getDisplayName())));
                    }
                }else{
                    p.sendMessage(Utils.color(getConfig().getString("messages.noperms")));
                }
            } else {
                System.out.println("Sorry, but you can only execute /chatclear as a player.");
            }
        } else if (command.getName().equalsIgnoreCase("cplus")) {
            Player p = (Player) sender;

            if(args.length == 0){
                if(p.hasPermission("cplus.view")){
                    p.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.RED + "/cplus" + ChatColor.GRAY + " - Shows the list of commands");
                    p.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.RED + "/cplus reload" + ChatColor.GRAY + " - Reloads the config");
                }else{
                    p.sendMessage(Utils.color(getConfig().getString("messages.noperms")));
                }

            }else if(args[0].equalsIgnoreCase("reload")){
                if(p.hasPermission("cplus.reload")){
                    reload();
                    p.sendMessage(ChatColor.GREEN + "Reloaded the config of ChatterPlus");
                }else{
                    p.sendMessage(Utils.color(getConfig().getString("messages.noperms")));
                }

            }else{
                p.sendMessage(ChatColor.RED + "Please enter a valid command!");
            }

        } else if (command.getName().equalsIgnoreCase("lockchat")) {

            Player p = (Player) sender;

            if (p.hasPermission("cplus.lockchat")) {
                if (isChatLocked == true) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-locked")).replace("%player", p.getDisplayName()));
                } else {
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-lock-success-all")).replace("%player", p.getDisplayName()));
                    }
                    isChatLocked = true;
                }


            }else{
                p.sendMessage(Utils.color(getConfig().getString("messages.noperms")));
            }

        } else if(command.getName().equalsIgnoreCase("unlockchat")){

            Player p = (Player) sender;
            if(p.hasPermission("cplus.unlockchat")){
                if (isChatLocked == false){

                    isChatLocked = true;
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-unlocked")).replace("%player", p.getDisplayName()));


                }else if(isChatLocked == true){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-notlocked")).replace("%player", p.getDisplayName()));
                    }
                }
            }else{
                p.sendMessage(Utils.color(getConfig().getString("messages.noperms")));
            }

        }else if(command.getName().equalsIgnoreCase("sudoall")){
            Player p = (Player) sender;
            if(p.hasPermission("cplus.sudoall")){
                for(Player target : Bukkit.getOnlinePlayers()){
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < args.length; i++){
                        sb.append(args[i]).append(" ");
                    }

                    String message = sb.toString().trim();
                    target.chat(message);
                }
            }else{

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noperms")));

            }

        }else if(command.getName().equalsIgnoreCase("sudo")){
            Player p = (Player) sender;
            if(p.hasPermission("cplus.sudo")){
                if(args.length==0){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.sudo-less-args")));
                }
                else if(args.length==1){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.sudo-less-args")));
                }else{
                    Player target = getServer().getPlayer(args[0]);
                    if(target != null && target.isOnline()){
                        StringBuilder sb = new StringBuilder();
                        for(int i = 1; i < args.length; i++){
                            sb.append(args[i]).append(" ");
                        }
                        String message = sb.toString().trim();
                        target.chat(message);
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.sudo-player-offline")));
                    }
                }


            }else{
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noperms")));
            }
        }


        return true;
        }

    @EventHandler(priority = EventPriority.LOWEST)
    public void LockChecker(AsyncPlayerChatEvent e){
        if(isChatLocked == true){
            Player p = e.getPlayer();





            if(!p.hasPermission("cplus.bypasschatlock")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.message-during-lock")));
                e.setCancelled(true);
            }
        }
    }
    List<String> badwords = null;

    @EventHandler(priority = EventPriority.LOWEST)
    public void BadWordDetect(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if(p.hasPermission("cplus.anti-badword-bypass")){
            for(String s : badwords){
                if(e.getMessage().contains(s)){
                    e.setCancelled(true);

                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.contains-badword")));
                }
            }
        }else{

        }


    }

    public void reload(){
        reloadConfig();
        badwords = getConfig().getStringList("badwords");
    }


    private HashMap<UUID, Long> cooldown = new HashMap<>();
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if(!p.hasPermission("cplus.chat-cooldown-bypass")){
            if (!cooldown.containsKey(p.getUniqueId())) {
                cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                return;
            }

            long timeElapsed = System.currentTimeMillis() - cooldown.get(p.getUniqueId());

            if (timeElapsed >= getConfig().getInt("chat-cooldown")) {
                cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                return;
            }

            long remaining = getConfig().getInt("chat-cooldown");


            e.setCancelled(true);
            remaining = remaining - timeElapsed;
            p.sendMessage(Utils.color(getConfig().getString("messages.chat-cooldown-reached").replace("%seconds%", Integer.toString(Math.round(remaining/1000)))));
        }else{

        }

    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void Antylink(AsyncPlayerChatEvent e){
        String message = e.getMessage();
        Player p = e.getPlayer();
        if(message.contains("https://")){
            if(!p.hasPermission("cplus.bypassantylink")){
                if(getConfig().getBoolean("antylink-remove-dots")){

                    e.setMessage(message.replace(".", " "));

                }else if(getConfig().getBoolean("antylink-cancel-send-if-link-detected")){
                    e.setCancelled(true);

                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.contains-link")));
                }
            }

        }
    }
    @EventHandler
    public void ChatFormatting(AsyncPlayerChatEvent e){
        if(getConfig().getBoolean("enable-chat-formatting")){
            String msg = e.getMessage();
            String format = getConfig().getString("chat-format");
            Player p = e.getPlayer();
            if(msg.contains("%")){
                msg = msg.replace("%", "%%");
            }
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                format = PlaceholderAPI.setPlaceholders(e.getPlayer(), format);
                e.setMessage(msg);
                e.setFormat(ChatColor.translateAlternateColorCodes('&', format).replace("%player%", p.getDisplayName()).replace("%message%", msg));
            }else{
                e.setFormat(ChatColor.translateAlternateColorCodes('&', format).replace("%player%", p.getDisplayName()).replace("%message%", msg).replace("%", "%%"));
            }
        }


    }

}
