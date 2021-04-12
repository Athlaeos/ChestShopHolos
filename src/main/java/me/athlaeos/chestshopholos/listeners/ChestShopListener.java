package me.athlaeos.chestshopholos.listeners;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import me.athlaeos.chestshopholos.Main;
import me.athlaeos.chestshopholos.Utils;
import me.athlaeos.chestshopholos.managers.HoloOptionManager;
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

import java.util.List;
import java.util.ListIterator;

public class ChestShopListener implements Listener {
    public static List<String> buyLines;
    public static List<String> sellLines;
    public final static NamespacedKey holoDisplaykey = new NamespacedKey(Main.getPlugin(), "chestshop_hologram");
    public final static NamespacedKey holoTypeKey = new NamespacedKey(Main.getPlugin(), "chestshop_holo_type");

    public ChestShopListener(){
        buyLines = Main.getPlugin().getConfig().getStringList("buy_line");
        sellLines = Main.getPlugin().getConfig().getStringList("sell_line");
    }

    @EventHandler
    public void onPlayerShopCreate(ShopCreatedEvent e){
        if (!HoloOptionManager.getInstance().placeHolo(e.getPlayer())) return;
        Location holoLocation;
        Location itemLocation;
        Location signLocation = e.getSign().getLocation();
        if (HoloOptionManager.getInstance().placeHoloAboveBlock(e.getPlayer())){
            holoLocation = getSignSupportBlockLocation(e.getSign());
            if (holoLocation == null) holoLocation = e.getSign().getLocation();
        } else {
            holoLocation = e.getSign().getLocation();
        }
        itemLocation = holoLocation.clone();
        holoLocation.add(0.5, 1.8, 0.5);
        itemLocation.add(0.5, 1, 0.5);

        if (!HoloOptionManager.getInstance().placeHoloItemIcon(e.getPlayer())){
            holoLocation.subtract(0, 1, 0); // Adjust hologram location if item is absent
        }

        boolean sell = e.getSignLines()[2].contains("S");
        boolean buy = e.getSignLines()[2].contains("B");

        ItemStack item = MaterialUtil.getItem(e.getSignLine((short) 3));

        if (buy && buyLines.size() != 0){
            ListIterator<String> reversebuyLines = buyLines.listIterator(buyLines.size());
            while (reversebuyLines.hasPrevious()){
                ArmorStand buyHolo = (ArmorStand) e.getSign().getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);

                buyHolo.setBasePlate(false);
                buyHolo.setVisible(false);
                buyHolo.setGravity(false);
                buyHolo.setAI(false);
                buyHolo.setInvulnerable(true);
                buyHolo.setCollidable(false);
                buyHolo.setSilent(true);
                buyHolo.setMarker(true);
                buyHolo.setPersistent(true);
                buyHolo.setSmall(true);
                buyHolo.setCustomNameVisible(true);
                String line = reversebuyLines.previous();
                buyHolo.setCustomName(Utils.chat(line
                        .replace("%item%", Utils.getItemName(item))
                        .replace("%amount%", e.getSignLine((short)1))
                        .replace("%cost%", String.format("%.2f", PriceUtil.getExactBuyPrice(e.getSignLine((short) 2))))));
                buyHolo.getPersistentDataContainer().set(holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, signLocation));
                buyHolo.getPersistentDataContainer().set(holoTypeKey, PersistentDataType.STRING, "buy");
                holoLocation = holoLocation.add(0, 0.25, 0);
            }

            holoLocation = holoLocation.add(0, 0.4, 0);
        }
        if (sell && sellLines.size() != 0){
            ListIterator<String> reverseSellLines = sellLines.listIterator(sellLines.size());
            while (reverseSellLines.hasPrevious()){
                ArmorStand sellHolo = (ArmorStand) e.getSign().getWorld().spawnEntity(holoLocation, EntityType.ARMOR_STAND);
                sellHolo.setBasePlate(false);
                sellHolo.setVisible(false);
                sellHolo.setGravity(false);
                sellHolo.setAI(false);
                sellHolo.setMarker(true);
                sellHolo.setSilent(true);
                sellHolo.setCollidable(false);
                sellHolo.setInvulnerable(true);
                sellHolo.setPersistent(true);
                sellHolo.setSmall(true);
                sellHolo.setCustomNameVisible(true);
                String line = reverseSellLines.previous();
                sellHolo.setCustomName(Utils.chat(line
                        .replace("%item%", Utils.getItemName(item))
                        .replace("%amount%", e.getSignLine((short)1))
                        .replace("%cost%", String.format("%.2f", PriceUtil.getExactSellPrice(e.getSignLine((short) 2))))));
                sellHolo.getPersistentDataContainer().set(holoDisplaykey, PersistentDataType.STRING, Utils.holoLocationToString(holoLocation, signLocation));
                sellHolo.getPersistentDataContainer().set(holoTypeKey, PersistentDataType.STRING, "sell");
                holoLocation = holoLocation.add(0, 0.25, 0);
            }

        }
        if (HoloOptionManager.getInstance().placeHoloItemIcon(e.getPlayer())){
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
