package net.d1axowy.chatterplus;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public final class ChatterPlus extends JavaPlugin implements CommandExecutor, Listener {


    boolean isChatLocked = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        System.out.println("ChatterPlus starting with " + Bukkit.getBukkitVersion() + " and " + Bukkit.getVersion());
        getServer().getPluginManager().registerEvents(this, this);

        reload();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission").replace("%player%", p.getDisplayName())));
                }
            } else {
                System.out.println("Sorry, but you can only execute /chatclear as a player.");
            }
        } else if (command.getName().equalsIgnoreCase("cplus")) {
            Player p = (Player) sender;
            if (p.hasPermission("cplus.reload")) {

                reload();

                p.sendMessage(ChatColor.GREEN + "Reloaded the config of ChatterPlus");
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


            }

        } else if(command.getName().equalsIgnoreCase("unlockchat")){

            Player p = (Player) sender;
            if(p.hasPermission("cplus.unlockchat")){
                if (isChatLocked == false){

                    isChatLocked = true;
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-unlocked")).replace("%player", p.getDisplayName()));


                }else if(isChatLocked == true){
                    isChatLocked = false;
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-lock-success-all")).replace("%player", p.getDisplayName()));
                    }
                }
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

    @EventHandler
    public void onMsgSend(AsyncPlayerChatEvent e){
        if(isChatLocked == true){
            Player p = e.getPlayer();





            if(!p.hasPermission("cplus.bypasschatlock")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.message-during-lock")));
                e.setCancelled(true);
            }
        }
    }
    List<String> badwords = null;

    @EventHandler
    public void BadWordDetect(AsyncPlayerChatEvent e){

        Player p = e.getPlayer();

        for(String s : badwords){
            if(e.getMessage().contains(s)){
                e.setCancelled(true);

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.contains-badword")));
            }
        }

    }

    public void reload(){
        reloadConfig();
        badwords = getConfig().getStringList("badwords");
    }

    HashMap<Player, Long> spam = new HashMap<Player, Long>();

    @EventHandler
    public void ChatCooldown(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (!p.hasPermission("cplus.bypasscooldown")) {
            if (spam.containsKey(p)) {
                if (spam.get(p) > System.currentTimeMillis()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.chat-cooldown-reached")).replace("%cooldown_time%", "hi"));

                    e.setCancelled(true);

                } else {
                    spam.put(p, System.currentTimeMillis() + getConfig().getInt("chat-cooldown"));
                }
            } else {

                spam.put(p, System.currentTimeMillis() + getConfig().getInt("chat-cooldown"));
            }
        }
    }


    @EventHandler
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
        String msg = getConfig().getString("chat-format");
        Player p = e.getPlayer();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {

            e.setCancelled(true);




            msg = PlaceholderAPI.setPlaceholders(e.getPlayer(), msg);


            for(Player all : Bukkit.getOnlinePlayers()){
                all.sendMessage(Utils.color(msg).replace("{PLAYER}", p.getDisplayName()).replace("{MSG}", e.getMessage()));
            }

        }else{
            e.setCancelled(true);


            for(Player all : Bukkit.getOnlinePlayers()){
                all.sendMessage(Utils.color(msg).replace("{PLAYER}", p.getDisplayName()).replace("{MSG}", e.getMessage()));
            }
        }

    }


}
