package me.athlaeos.chestshopholos;

import me.athlaeos.chestshopholos.managers.HoloOptionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ToggleHoloOptionsCommand implements TabExecutor {

    private final String errorNoPermission;
    private final String toggleOnSign;
    private final String toggleOnBlock;
    private final String toggleIconOn;
    private final String toggleIconOff;
    private final String toggleHolosOn;
    private final String toggleHolosOff;

    public ToggleHoloOptionsCommand(){
        errorNoPermission = Main.getPlugin().getConfig().getString("error_no_permission");
        toggleOnSign = Main.getPlugin().getConfig().getString("toggle_on_sign");
        toggleOnBlock = Main.getPlugin().getConfig().getString("toggle_on_block");
        toggleIconOn = Main.getPlugin().getConfig().getString("toggle_icon_on");
        toggleIconOff = Main.getPlugin().getConfig().getString("toggle_icon_off");
        toggleHolosOn = Main.getPlugin().getConfig().getString("toggle_holos_on");
        toggleHolosOff = Main.getPlugin().getConfig().getString("toggle_holos_off");

        Main.getPlugin().getCommand("chestshopholo").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("toggle_placement")){
                if (!(sender instanceof Player)){
                    sender.sendMessage(Utils.chat("&cOnly players can use this command"));
                    return true;
                }
                if (sender.hasPermission("shopholo.toggle") || sender.hasPermission("shopholo.toggle.placement")){
                    HoloOptionManager.getInstance().toggleHolosAboveBlock(((Player) sender).getUniqueId());
                    if (HoloOptionManager.getInstance().placeHoloAboveBlock(((Player) sender).getUniqueId())){
                        sender.sendMessage(Utils.chat(toggleOnBlock));
                    } else {
                        sender.sendMessage(Utils.chat(toggleOnSign));
                    }
                } else {
                    sender.sendMessage(Utils.chat(errorNoPermission));
                }
            } else if (args[0].equalsIgnoreCase("toggle_holos")){
                if (!(sender instanceof Player)){
                    sender.sendMessage(Utils.chat("&cOnly players can use this command"));
                    return true;
                }
                if (sender.hasPermission("shopholo.toggle") || sender.hasPermission("shopholo.toggle.holograms")){
                    HoloOptionManager.getInstance().toggleHolosEnabled(((Player) sender).getUniqueId());
                    if (HoloOptionManager.getInstance().placeHolo(((Player) sender).getUniqueId())){
                        sender.sendMessage(Utils.chat(toggleHolosOn));
                    } else {
                        sender.sendMessage(Utils.chat(toggleHolosOff));
                    }
                } else {
                    sender.sendMessage(Utils.chat(errorNoPermission));
                }
            } else if (args[0].equalsIgnoreCase("toggle_item_icon")){
                if (!(sender instanceof Player)){
                    sender.sendMessage(Utils.chat("&cOnly players can use this command"));
                    return true;
                }
                if (sender.hasPermission("shopholo.toggle") || sender.hasPermission("shopholo.toggle.icons")){
                    HoloOptionManager.getInstance().toggleHoloItemIcons(((Player) sender).getUniqueId());
                    if (HoloOptionManager.getInstance().placeHoloItemIcon(((Player) sender).getUniqueId())){
                        sender.sendMessage(Utils.chat(toggleIconOn));
                    } else {
                        sender.sendMessage(Utils.chat(toggleIconOff));
                    }
                } else {
                    sender.sendMessage(Utils.chat(errorNoPermission));
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            return Arrays.asList("toggle_holos", "toggle_placement", "toggle_item_icon");
        }
        return null;
    }
}
