package me.athlaeos.chestshopholos;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&(#\\w{6})");

    public static String chat(String message) {
        {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
            while (matcher.find()) {
                String group = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                        + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                        + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                        + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
                );
            }
            return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
        }
    }

    public static String getItemName(ItemStack i){
        String name;
        assert i.getItemMeta() != null;
        if (i.getItemMeta().hasDisplayName()){
            name = Utils.chat(i.getItemMeta().getDisplayName());
        } else if (i.getItemMeta().hasLocalizedName()){
            name = Utils.chat(i.getItemMeta().getLocalizedName());
        } else {
            name = i.getType().toString().toLowerCase().replace("_", " ");
        }
        return name;
    }

    public static String holoLocationToString(Location placementLocation, Location signLocation){
        return String.format("%f;%f;%f|%f;%f;%f",
                placementLocation.getX(), placementLocation.getY(), placementLocation.getZ(),
                signLocation.getX(), signLocation.getY(), signLocation.getZ());
    }

    public static Location stringToPlacementLocation(String l, World w){
        try {
            String placementLocation = l.split("\\|")[0];
            String[] splitLocation = placementLocation.split(";");
            double x = Double.parseDouble(splitLocation[0]);
            double y = Double.parseDouble(splitLocation[1]);
            double z = Double.parseDouble(splitLocation[2]);
            return new Location(w, x, y, z);
        } catch (NullPointerException e){
            System.out.println("Invalid location stored in ChestShop hologram, " + l + " was stored");
            e.printStackTrace();
            return null;
        }
    }

    public static Location stringToSignLocation(String l, World w){
        try {
            String signLocation = l.split("\\|")[1];
            String[] splitLocation = signLocation.split(";");
            double x = Double.parseDouble(splitLocation[0]);
            double y = Double.parseDouble(splitLocation[1]);
            double z = Double.parseDouble(splitLocation[2]);
            return new Location(w, x, y, z);
        } catch (NullPointerException e){
            System.out.println("Invalid location stored in ChestShop hologram, " + l + " was stored");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean areEqualEnough(double a, double b){
        BigDecimal aa = new BigDecimal(a);
        BigDecimal bb = new BigDecimal(b);
        aa = aa.setScale(3);
        bb = bb.setScale(3);
        return aa.equals(bb);
    }
}
