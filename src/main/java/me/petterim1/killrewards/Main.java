package me.petterim1.killrewards;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.plugin.PluginBase;
import angga7togk.economyapi.database.EconomyDB;

import java.util.HashMap;
import java.util.Map;

public class Main extends PluginBase implements Listener {

    private Map<String, Double> killRewards;
    private String message;
    private boolean popupMessage;

    private boolean eapi;
    private boolean leco;

    public void onEnable() {
        eapi = getServer().getPluginManager().getPlugin("EconomyAPI") != null;
        leco = getServer().getPluginManager().getPlugin("LlamaEconomy") != null;
        if (!eapi && !leco) {
            getLogger().warning("No economy plugins not found!");
        }
        saveDefaultConfig();
        killRewards = (Map<String, Double>) getConfig().get("killRewards", new HashMap());
        message = getConfig().getString("message");
        popupMessage = getConfig().getBoolean("popupMessage");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        System.out.println(e.getEntity().getName() + " is died");
        if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent dmgCause = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
            if (dmgCause.getDamager() instanceof Player) {
                Double reward = killRewards.get(String.valueOf(e.getEntity().getNetworkId()));
                if (reward != null) {
                    if (giveMoney((Player) dmgCause.getDamager(), reward)) {
                        System.out.println("Player claim reward");
                        if (!message.isEmpty()) {
                            if (popupMessage) {
                                ((Player) dmgCause.getDamager()).sendPopup(message.replace("%amount%", reward.toString()).replace("%entity%", e.getEntity().getName()));
                            } else {
                                ((Player) dmgCause.getDamager()).sendMessage(message.replace("%amount%", reward.toString()).replace("%entity%", e.getEntity().getName()));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent dmgCause = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
            if (dmgCause.getDamager() instanceof Player) {
                Double reward = killRewards.get(-1);
                if (reward != null) {
                    if (giveMoney((Player) dmgCause.getDamager(), reward)) {
                        if (!message.isEmpty()) {
                            if (popupMessage) {
                                ((Player) dmgCause.getDamager()).sendPopup(message.replace("%amount%", reward.toString()).replace("%entity%", e.getEntity().getName()));
                            } else {
                                ((Player) dmgCause.getDamager()).sendMessage(message.replace("%amount%", reward.toString()).replace("%entity%", e.getEntity().getName()));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean giveMoney(Player damager, double reward) {
        if (eapi) {
            return EconomyDB.addMoney(damager, (int) reward);
        }
        return false;
    }
}
