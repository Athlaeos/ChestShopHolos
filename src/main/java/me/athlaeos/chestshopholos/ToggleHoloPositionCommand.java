package me.athlaeos.chestshopholos;

import me.athlaeos.chestshopholos.managers.HoloLocationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ToggleHoloPositionCommand implements TabExecutor {

    private final String errorNoPermission;
    private final String toggleOnSign;
    private final String toggleOnBlock;

    public ToggleHoloPositionCommand(){
        errorNoPermission = Main.getPlugin().getConfig().getString("error_no_permission");
        toggleOnSign = Main.getPlugin().getConfig().getString("toggle_on_sign");
        toggleOnBlock = Main.getPlugin().getConfig().getString("toggle_on_block");

        Main.getPlugin().getCommand("chestshopholo").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("toggle")){
                if (!(sender instanceof Player)){
                    sender.sendMessage(Utils.chat("&cOnly players can use this command"));
                    return true;
                }
                if (sender.hasPermission("shopholo.toggle")){
                    HoloLocationManager.getInstance().toggleHolosAboveBlock(((Player) sender).getUniqueId());
                    if (HoloLocationManager.getInstance().placeHoloAboveBlock(((Player) sender).getUniqueId())){
                        sender.sendMessage(Utils.chat(toggleOnBlock));
                    } else {
                        sender.sendMessage(Utils.chat(toggleOnSign));
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
            return Arrays.asList("toggle");
        }
        return null;
    }
}
