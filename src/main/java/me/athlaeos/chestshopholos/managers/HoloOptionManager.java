package me.athlaeos.chestshopholos.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloOptionManager {
    private static HoloOptionManager manager = null;
    private final Map<UUID, Boolean> holoLocationOptions = new HashMap<>();
    private final Map<UUID, Boolean> holoEnabledOptions = new HashMap<>();
    private final Map<UUID, Boolean> holoItemsEnabledOptions = new HashMap<>();

    public boolean placeHoloAboveBlock(UUID uuid){
        if (holoLocationOptions.containsKey(uuid)){
            return holoLocationOptions.get(uuid);
        }
        return true;
    }

    public boolean placeHolo(UUID uuid){
        if (holoEnabledOptions.containsKey(uuid)){
            return holoEnabledOptions.get(uuid);
        }
        return true;
    }

    public boolean placeHoloItemIcon(UUID uuid){
        if (holoItemsEnabledOptions.containsKey(uuid)){
            return holoItemsEnabledOptions.get(uuid);
        }
        return true;
    }

    public void toggleHolosAboveBlock(UUID uuid){
        holoLocationOptions.put(uuid, !placeHoloAboveBlock(uuid));
    }

    public void toggleHolosEnabled(UUID uuid){
        holoEnabledOptions.put(uuid, !placeHolo(uuid));
    }

    public void toggleHoloItemIcons(UUID uuid){
        holoItemsEnabledOptions.put(uuid, !placeHoloItemIcon(uuid));
    }

    public static HoloOptionManager getInstance(){
        if (manager == null) manager = new HoloOptionManager();
        return manager;
    }
}
