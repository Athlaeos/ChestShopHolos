package me.athlaeos.chestshopholos.listeners;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import me.athlaeos.chestshopholos.Main;
import me.athlaeos.chestshopholos.Utils;
import me.athlaeos.chestshopholos.managers.HoloLocationManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ChestShopListener implements Listener {
    public static String buyLine;
    public static String sellLine;
    public final static NamespacedKey holoDisplaykey = new NamespacedKey(Main.getPlugin(), "chestshop_hologram");
    public final static NamespacedKey holoTypeKey = new NamespacedKey(Main.getPlugin(), "chestshop_holo_type");

    public ChestShopListener(){
        buyLine = Main.getPlugin().getConfig().getString("buy_line");
        sellLine = Main.getPlugin().getConfig().getString("sell_line");
    }

    @EventHandler
    public void onPlayerShopCreate(ShopCreatedEvent e){
        Location holoLocation;
        Location itemLocation;
        Location signLocation = e.getSign().getLocation();
        if (HoloLocationManager.getInstance().placeHoloAboveBlock(e.getPlayer().getUniqueId())){
            holoLocation = getSignSupportBlockLocation(e.getSign());
            if (holoLocation == null) holoLocation = e.getSign().getLocation();
        } else {
            holoLocation = e.getSign().getLocation();
        }
        itemLocation = holoLocation.clone();
        holoLocation.add(0.5, 1.8, 0.5);
        itemLocation.add(0.5, 1, 0.5);

        boolean sell = e.getSignLines()[2].contains("S");
        boolean buy = e.getSignLines()[2].contains("B");

        ItemStack item = MaterialUtil.getItem(e.getSignLine((short) 3));

        if (buy && buyLine != null){
            ArmorStand buyHolo = (ArmorStand) e.getSign().getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);

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
            buyHolo.setCustomName(Utils.chat(buyLine
                    .replace("%item%", Utils.getItemName(item))
                    .replace("%amount%", e.getSignLine((short)1))
                    .replace("%cost%", String.format("%.2f", PriceUtil.getExactBuyPrice(e.getSignLine((short) 2))))));
            buyHolo.getPersistentDataContainer().set(holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, signLocation));
            buyHolo.getPersistentDataContainer().set(holoTypeKey, PersistentDataType.STRING, "buy");

            holoLocation = holoLocation.add(0, 0.4, 0);
        }
        if (sell && sellLine != null){
            ArmorStand sellHolo = (ArmorStand) e.getSign().getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);
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
            sellHolo.setCustomName(Utils.chat(sellLine
                    .replace("%item%", Utils.getItemName(item))
                    .replace("%amount%", e.getSignLine((short)1))
                    .replace("%cost%", String.format("%.2f", PriceUtil.getExactSellPrice(e.getSignLine((short) 2))))));
            sellHolo.getPersistentDataContainer().set(holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, signLocation));
            sellHolo.getPersistentDataContainer().set(holoTypeKey, PersistentDataType.STRING, "sell");

        }
        Item itemHolo = (Item) e.getSign().getWorld().spawnEntity(itemLocation, EntityType.DROPPED_ITEM);
        itemHolo.setItemStack(item);
        itemHolo.setPickupDelay(Integer.MAX_VALUE);
        itemHolo.setGravity(false);
        itemHolo.setInvulnerable(true);
        itemHolo.setPersistent(true);
        itemHolo.getPersistentDataContainer().set(holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(itemLocation, signLocation));
        itemHolo.getPersistentDataContainer().set(holoTypeKey, PersistentDataType.STRING, "item");

        new BukkitRunnable(){
            int timerLimiter = 0;
            @Override
            public void run() {
                if (timerLimiter >= 5) {
                    cancel();
                    return;
                }
                itemHolo.setVelocity(new Vector(0, 0, 0));
                itemHolo.teleport(itemLocation);
                timerLimiter++;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 10L);
    }

    @EventHandler
    public void onPlayerShopDestroy(ShopDestroyedEvent e){
        Location signLocation = e.getSign().getLocation();

        for (Entity entity : e.getSign().getWorld().getEntitiesByClasses(Item.class, ArmorStand.class)){
            if (entity.getPersistentDataContainer().has(holoDisplaykey, PersistentDataType.STRING)){
                String matchString = entity.getPersistentDataContainer().get(holoDisplaykey, PersistentDataType.STRING);
                assert matchString != null;
                Location assertedSignLocation = Utils.stringToSignLocation(matchString, signLocation.getWorld());
                if (assertedSignLocation != null){
                    if (Utils.areEqualEnough(assertedSignLocation.getX(), signLocation.getX())
                    && Utils.areEqualEnough(assertedSignLocation.getY(), signLocation.getY())
                    && Utils.areEqualEnough(assertedSignLocation.getZ(), signLocation.getZ())){
                        entity.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e){
        if (e.getEntity().getPersistentDataContainer().has(holoDisplaykey, PersistentDataType.STRING)){
            e.setCancelled(true);
        }
    }

    private Location getSignSupportBlockLocation(Sign s){
        if (s != null && s.getBlock().getState() instanceof Sign)
        {
            BlockData data = s.getBlock().getBlockData();
            if (data instanceof Directional)
            {
                Directional directional = (Directional)data;
                return s.getBlock().getRelative(directional.getFacing().getOppositeFace()).getLocation();
            }
        }
        return null;
    }
}
