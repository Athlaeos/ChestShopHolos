package me.athlaeos.chestshopholos;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import me.athlaeos.chestshopholos.listeners.ChestShopListener;
import me.athlaeos.chestshopholos.managers.HoloOptionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public final class Main extends JavaPlugin {
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Plugin startup logic
        saveResource("config.yml", false);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChestShopListener(), this);
        new ToggleHoloOptionsCommand();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static void resetShopHolos(){
        Map<Sign, List<Entity>> shopHolos = new HashMap<>();
        for (World w : Main.getPlugin().getServer().getWorlds()){
            for (Entity entity : w.getEntitiesByClasses(Item.class, ArmorStand.class)){
                if (entity.getPersistentDataContainer().has(ChestShopListener.holoDisplaykey, PersistentDataType.STRING)) {
                    Location teleportTo = Utils.stringToPlacementLocation(entity.getPersistentDataContainer().get(ChestShopListener.holoDisplaykey, PersistentDataType.STRING), entity.getWorld());
                    if (teleportTo != null) {
                        entity.teleport(teleportTo);
                        Location signLocation = Utils.stringToSignLocation(entity.getPersistentDataContainer().get(ChestShopListener.holoDisplaykey, PersistentDataType.STRING), entity.getWorld());
                        if (signLocation != null) {
                            BlockState state = entity.getWorld().getBlockAt(signLocation).getState();
                            if (state instanceof Sign) {
                                Sign s = (Sign) state;

                                if (Utils.isShopSign(s)) {
                                    String type = entity.getPersistentDataContainer().get(ChestShopListener.holoTypeKey, PersistentDataType.STRING);
                                    if (type != null) {
                                        List<Entity> holos;
                                        if (shopHolos.containsKey(s)) {
                                            holos = shopHolos.get(s);
                                        } else {
                                            holos = new ArrayList<>();
                                        }
                                        holos.add(entity);
                                        shopHolos.put(s, holos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Sign sign : shopHolos.keySet()){
            List<ArmorStand> buyHolos = new ArrayList<>();
            List<ArmorStand> sellHolos = new ArrayList<>();
            Item itemHolo = null;

            for (Entity holo : shopHolos.get(sign)){
                if (holo.getType() == EntityType.ARMOR_STAND || holo.getType() == EntityType.DROPPED_ITEM){
                    if (holo.getPersistentDataContainer().has(ChestShopListener.holoTypeKey, PersistentDataType.STRING)){
                        String type = holo.getPersistentDataContainer().get(ChestShopListener.holoTypeKey, PersistentDataType.STRING);
                        if (type != null){
                            if (type.equalsIgnoreCase("buy")) {
                                if (holo instanceof ArmorStand){
                                    buyHolos.add((ArmorStand) holo);
                                    holo.remove();
                                }
                            } else if (type.equalsIgnoreCase("sell")) {
                                if (holo instanceof ArmorStand){
                                    sellHolos.add((ArmorStand) holo);
                                    holo.remove();
                                }
                            } else if (type.equalsIgnoreCase("item")){
                                if (holo instanceof Item){
                                    itemHolo = (Item) holo;
                                    holo.remove();
                                }
                            }
                        }
                    }
                }
            }

            Location approximateLocation = null;
            // The purpose of this is to know the X and Z coordinates for the hologram placement, the Y coordinate is the
            // only different variable
            if (buyHolos.size() > 0) approximateLocation = buyHolos.get(0).getLocation();
            if (sellHolos.size() > 0) approximateLocation = sellHolos.get(0).getLocation();
            if (itemHolo != null) approximateLocation = itemHolo.getLocation();
            if (approximateLocation != null){
                Location holoLocation = approximateLocation.clone();
                Location itemLocation = approximateLocation.clone();
                holoLocation.setY(sign.getLocation().getY());
                itemLocation.setY(sign.getLocation().getY());
                holoLocation.add(0, 2.4, 0);
                itemLocation.add(0, 1.6, 0);
                if (itemHolo == null) {
                    holoLocation.subtract(0, 1, 0);
                }

                boolean sell = sellHolos.size() > 0;
                boolean buy = buyHolos.size() > 0;
                if (sell && buy) holoLocation.add(0, 0.5, 0);

                NamedHologram holo = new NamedHologram(holoLocation, Utils.locationToString(sign.getLocation()));
                if (buy && ChestShopListener.buyLines.size() != 0){
                    for (String line : ChestShopListener.buyLines){
                        holo.appendTextLine(Utils.chat(line
                                .replace("%item%", Utils.getItemName(MaterialUtil.getItem(sign.getLine((short) 3))))
                                .replace("%amount%", sign.getLine((short)1))
                                .replace("%cost%", String.format("%.2f", PriceUtil.getExactBuyPrice(sign.getLine((short) 2))))));
                    }
                    holo.appendTextLine("");
                }
                if (sell && ChestShopListener.sellLines.size() != 0){
                    for (String line : ChestShopListener.sellLines){
                        holo.appendTextLine(Utils.chat(line
                                .replace("%item%", Utils.getItemName(MaterialUtil.getItem(sign.getLine((short) 3))))
                                .replace("%amount%", sign.getLine((short)1))
                                .replace("%cost%", String.format("%.2f", PriceUtil.getExactSellPrice(sign.getLine((short) 2))))));
                    }
                }
                if (itemHolo != null){
                    holo.appendItemLine(itemHolo.getItemStack());
                }
                NamedHologramManager.addHologram(holo);
                holo.refreshAll();

                HologramDatabase.saveHologram(holo);
                HologramDatabase.trySaveToDisk();
            }
        }
    }
}
