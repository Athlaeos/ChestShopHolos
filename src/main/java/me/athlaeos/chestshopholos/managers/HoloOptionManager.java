package me.athlaeos.chestshopholos.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloOptionManager {
    private static HoloOptionManager manager = null;
    private final Map<UUID, Boolean> holoLocationOptions = new HashMap<>();
    private final Map<UUID, Boolean> holoEnabledOptions = new HashMap<>();
    private final Map<UUID, Boolean> holoItemsEnabledOptions = new HashMap<>();

    public boolean placeHoloAboveBlock(Player player){
        if (holoLocationOptions.containsKey(player.getUniqueId())){
            return holoLocationOptions.get(player.getUniqueId());
        }
        return player.hasPermission("shopholo.toggle") || player.hasPermission("shopholo.toggle.placement");
    }

    public boolean placeHolo(Player player){
        if (holoEnabledOptions.containsKey(player.getUniqueId())){
            return holoEnabledOptions.get(player.getUniqueId());
        }
        return player.hasPermission("shopholo.toggle") || player.hasPermission("shopholo.toggle.holograms");
    }

    public boolean placeHoloItemIcon(Player player){
        if (holoItemsEnabledOptions.containsKey(player.getUniqueId())){
            return holoItemsEnabledOptions.get(player.getUniqueId());
        }
        return player.hasPermission("shopholo.toggle") || player.hasPermission("shopholo.toggle.icons");
    }

    public void toggleHolosAboveBlock(Player player){
        holoLocationOptions.put(player.getUniqueId(), !placeHoloAboveBlock(player));
    }

    public void toggleHolosEnabled(Player player){
        holoEnabledOptions.put(player.getUniqueId(), !placeHolo(player));
    }

    public void toggleHoloItemIcons(Player player){
        holoItemsEnabledOptions.put(player.getUniqueId(), !placeHoloItemIcon(player));
    }

    public static HoloOptionManager getInstance(){
        if (manager == null) manager = new HoloOptionManager();
        return manager;
    }
}
