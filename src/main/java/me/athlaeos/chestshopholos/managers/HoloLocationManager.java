package me.athlaeos.chestshopholos.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoloLocationManager {
    private static HoloLocationManager manager = null;
    private final Map<UUID, Boolean> holoLocationOptions = new HashMap<>();

    public boolean placeHoloAboveBlock(UUID uuid){
        if (holoLocationOptions.containsKey(uuid)){
            return holoLocationOptions.get(uuid);
        }
        return true;
    }

    public void toggleHolosAboveBlock(UUID uuid){
        holoLocationOptions.put(uuid, !placeHoloAboveBlock(uuid));
    }

    public static HoloLocationManager getInstance(){
        if (manager == null) manager = new HoloLocationManager();
        return manager;
    }
}
