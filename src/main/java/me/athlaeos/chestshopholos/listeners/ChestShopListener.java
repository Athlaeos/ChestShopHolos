package me.athlaeos.chestshopholos.listeners;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import me.athlaeos.chestshopholos.Main;
import me.athlaeos.chestshopholos.Utils;
import me.athlaeos.chestshopholos.managers.HoloOptionManager;
import me.athlaeos.chestshopholos.managers.HologramDatabase;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
        Location signLocation = e.getSign().getLocation();
        if (HoloOptionManager.getInstance().placeHoloAboveBlock(e.getPlayer())){
            holoLocation = getSignSupportBlockLocation(e.getSign());
            if (holoLocation == null) holoLocation = e.getSign().getLocation();
        } else {
            holoLocation = e.getSign().getLocation();
        }
        holoLocation.add(0.5, 2.5, 0.5);

        if (!HoloOptionManager.getInstance().placeHoloItemIcon(e.getPlayer())){
            holoLocation.subtract(0, 1, 0); // Adjust hologram location if item is absent
        }

        boolean sell = e.getSignLines()[2].contains("S");
        boolean buy = e.getSignLines()[2].contains("B");

        ItemStack item = MaterialUtil.getItem(e.getSignLine((short) 3));
        if (sell && buy) holoLocation.add(0, 0.5, 0);

        NamedHologram holo = new NamedHologram(holoLocation, Utils.locationToString(signLocation));
        if (buy && buyLines.size() != 0){
            for (String line : buyLines){
                holo.appendTextLine(Utils.chat(line
                        .replace("%item%", Utils.getItemName(item))
                        .replace("%amount%", e.getSignLine((short)1))
                        .replace("%cost%", String.format("%.2f", PriceUtil.getExactBuyPrice(e.getSignLine((short) 2))))));
            }
            holo.appendTextLine("");
        }
        if (sell && sellLines.size() != 0){
            for (String line : sellLines){
                holo.appendTextLine(Utils.chat(line
                        .replace("%item%", Utils.getItemName(item))
                        .replace("%amount%", e.getSignLine((short)1))
                        .replace("%cost%", String.format("%.2f", PriceUtil.getExactSellPrice(e.getSignLine((short) 2))))));
            }
        }
        if (HoloOptionManager.getInstance().placeHoloItemIcon(e.getPlayer())){
            holo.appendItemLine(item);
        }
        NamedHologramManager.addHologram(holo);
        holo.refreshAll();

        HologramDatabase.saveHologram(holo);
        HologramDatabase.trySaveToDisk();
    }

    @EventHandler
    public void onPlayerShopDestroy(ShopDestroyedEvent e){
        Location signLocation = e.getSign().getLocation();

        NamedHologram holo = NamedHologramManager.getHologram(Utils.locationToString(signLocation));

        if (holo != null){
            holo.delete();
            NamedHologramManager.removeHologram(holo);
            HologramDatabase.deleteHologram(holo.getName());
            HologramDatabase.trySaveToDisk();
        }

//        for (Entity entity : e.getSign().getWorld().getEntitiesByClasses(Item.class, ItemFrame.class, ArmorStand.class)){
//            if (entity.getPersistentDataContainer().has(holoDisplaykey, PersistentDataType.STRING)){
//                String matchString = entity.getPersistentDataContainer().get(holoDisplaykey, PersistentDataType.STRING);
//                assert matchString != null;
//                Location assertedSignLocation = Utils.stringToSignLocation(matchString, signLocation.getWorld());
//                if (assertedSignLocation != null){
//                    if (Utils.areEqualEnough(assertedSignLocation.getX(), signLocation.getX())
//                    && Utils.areEqualEnough(assertedSignLocation.getY(), signLocation.getY())
//                    && Utils.areEqualEnough(assertedSignLocation.getZ(), signLocation.getZ())){
//                        entity.remove();
//                    }
//                }
//            }
//        }
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
