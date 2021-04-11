package me.athlaeos.chestshopholos;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import me.athlaeos.chestshopholos.listeners.ChestShopListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
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

        new BukkitRunnable(){
            @Override
            public void run() {
                Map<Sign, List<Entity>> shopHolos = new HashMap<>();
                for (World w : getServer().getWorlds()){
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

                                        if (isShopSign(s)) {
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
                resetShopHolos(shopHolos);
            }
        }.runTaskTimer(this, 0L, this.getConfig().getInt("hologram_correction_rate"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getPlugin() {
        return plugin;
    }

    private void resetShopHolos(Map<Sign, List<Entity>> holos){
        for (Sign sign : holos.keySet()){
            List<ArmorStand> buyHolos = new ArrayList<>();
            List<ArmorStand> sellHolos = new ArrayList<>();
            Item itemHolo = null;

            for (Entity holo : holos.get(sign)){
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
                holoLocation.add(0, 1.8, 0);
                itemLocation.add(0, 1, 0);
                if (itemHolo == null) {
                    holoLocation.subtract(0, 1, 0);
                }

                boolean sell = sellHolos.size() > 0;
                boolean buy = buyHolos.size() > 0;

                if (buy && ChestShopListener.buyLines.size() != 0){
                    ListIterator<String> reversebuyLines = ChestShopListener.buyLines.listIterator(ChestShopListener.buyLines.size());
                    while (reversebuyLines.hasPrevious()){
                        ArmorStand buyHolo = (ArmorStand) sign.getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);

                        buyHolo.setBasePlate(false);
                        buyHolo.setVisible(false);
                        buyHolo.setGravity(false);
                        buyHolo.setAI(false);
                        buyHolo.setInvulnerable(true);
                        buyHolo.setCollidable(false);
                        buyHolo.setMarker(true);
                        buyHolo.setPersistent(true);
                        buyHolo.setSmall(true);
                        buyHolo.setCustomNameVisible(true);
                        String line = reversebuyLines.previous();
                        buyHolo.setCustomName(Utils.chat(line
                                .replace("%item%", Utils.getItemName(MaterialUtil.getItem(sign.getLine(3))))
                                .replace("%amount%", sign.getLine(1))
                                .replace("%cost%", String.format("%.2f", PriceUtil.getExactBuyPrice(sign.getLine(2))))));
                        buyHolo.getPersistentDataContainer().set(ChestShopListener.holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, sign.getLocation()));
                        buyHolo.getPersistentDataContainer().set(ChestShopListener.holoTypeKey, PersistentDataType.STRING, "buy");
                        holoLocation = holoLocation.add(0, 0.25, 0);
                    }

                    holoLocation = holoLocation.add(0, 0.4, 0);
                }
                if (sell && ChestShopListener.sellLines.size() != 0){
                    ListIterator<String> reverseSellLines = ChestShopListener.sellLines.listIterator(ChestShopListener.sellLines.size());
                    while (reverseSellLines.hasPrevious()){
                        ArmorStand sellHolo = (ArmorStand) sign.getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);
                        sellHolo.setBasePlate(false);
                        sellHolo.setVisible(false);
                        sellHolo.setGravity(false);
                        sellHolo.setAI(false);
                        sellHolo.setMarker(true);
                        sellHolo.setCollidable(false);
                        sellHolo.setInvulnerable(true);
                        sellHolo.setPersistent(true);
                        sellHolo.setSmall(true);
                        sellHolo.setCustomNameVisible(true);
                        String line = reverseSellLines.previous();
                        sellHolo.setCustomName(Utils.chat(line
                                .replace("%item%", Utils.getItemName(MaterialUtil.getItem(sign.getLine(3))))
                                .replace("%amount%", sign.getLine(1))
                                .replace("%cost%", String.format("%.2f", PriceUtil.getExactSellPrice(sign.getLine(2))))));
                        sellHolo.getPersistentDataContainer().set(ChestShopListener.holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, sign.getLocation()));
                        sellHolo.getPersistentDataContainer().set(ChestShopListener.holoTypeKey, PersistentDataType.STRING, "sell");
                        holoLocation = holoLocation.add(0, 0.25, 0);
                    }

                }
                if (itemHolo != null){
                    Item finalItemHolo = itemHolo;
                    new BukkitRunnable(){
                        int timerLimiter = 0;
                        @Override
                        public void run() {
                            if (timerLimiter >= 5) {
                                cancel();
                                return;
                            }
                            finalItemHolo.setVelocity(new Vector(0, 0, 0));
                            finalItemHolo.teleport(itemLocation);
                            timerLimiter++;
                        }
                    }.runTaskTimer(Main.getPlugin(), 0L, 10L);
                }
            }
        }
    }

    private boolean isShopSign(Sign s){
        try {
            Integer.parseInt(s.getLine(1));
            if (s.getLine(2).contains("B") || s.getLine(2).contains("S")){
                return MaterialUtil.getItem(s.getLine(3)) != null;
            } else {
                return false;
            }
        } catch (IllegalArgumentException ignored){
            return false;
        }
    }
}
