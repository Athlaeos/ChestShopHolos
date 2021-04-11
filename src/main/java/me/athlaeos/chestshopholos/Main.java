package me.athlaeos.chestshopholos;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.athlaeos.chestshopholos.listeners.ChestShopListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;

public final class Main extends JavaPlugin {
    private static Main plugin;
    private static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        plugin = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Plugin startup logic
        saveResource("config.yml", false);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChestShopListener(), this);
        new ToggleHoloPositionCommand();

        new BukkitRunnable(){
            @Override
            public void run() {
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
                                                if (type.equalsIgnoreCase("buy")) {
                                                    BigDecimal buyPrice = PriceUtil.getExactBuyPrice(s.getLine(2));
                                                    entity.setCustomName(Utils.chat(ChestShopListener.buyLine
                                                            .replace("%item%", Utils.getItemName(MaterialUtil.getItem(s.getLine(3))))
                                                            .replace("%amount%", s.getLine(1))
                                                            .replace("%cost%", String.format("%.2f", buyPrice))));
                                                } else if (type.equalsIgnoreCase("sell")) {
                                                    BigDecimal sellprice = PriceUtil.getExactSellPrice(s.getLine(2));
                                                    entity.setCustomName(Utils.chat(ChestShopListener.sellLine
                                                            .replace("%item%", Utils.getItemName(MaterialUtil.getItem(s.getLine(3))))
                                                            .replace("%amount%", s.getLine(1))
                                                            .replace("%cost%", String.format("%.2f", sellprice))));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 300L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getPlugin() {
        return plugin;
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
