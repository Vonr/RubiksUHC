package me.qther.rubiksuhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.qther.rubiksuhc.RubiksUHC.*;

public class EventListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!started) {
            event.getPlayer().sendMessage("The UHC has not started!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!started || ended) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobExplode(EntityExplodeEvent event) {
        if (!started || ended) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!started) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!started || ended) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        if (!started) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!started || ended || (event.getDamager() instanceof Player && event.getEntity() instanceof Player && System.currentTimeMillis() < timeStarted + gracePeriod)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (!started || ended || timeStarted + 20 * 100 > System.currentTimeMillis()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!started) {
            event.getPlayer().sendMessage("Clearing your inventory and potion effects!");
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            PlayerInventory pinv = event.getPlayer().getInventory();
            pinv.clear();
            pinv.setHelmet(null);
            pinv.setChestplate(null);
            pinv.setLeggings(null);
            pinv.setBoots(null);
            event.getPlayer().getActivePotionEffects().forEach(e -> event.getPlayer().removePotionEffect(e.getType()));
            event.getPlayer().getActivePotionEffects().clear();
            Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setCenter(0, 0);
            Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setSize(2 * 35);
            Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setDamageAmount(0);
            event.getPlayer().sendMessage("Welcome to the UHC, " + event.getPlayer().getName() + "!");
            Scatter(Collections.singletonList(event.getPlayer()), 30);
        } else if (scattered.stream().filter(p -> p == event.getPlayer().getUniqueId()).collect(Collectors.toList()).isEmpty() && System.currentTimeMillis() < timeStarted + 3 * 60 * 100) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            event.getPlayer().sendMessage("Clearing your inventory and potion effects!");
            PlayerInventory pinv = event.getPlayer().getInventory();
            pinv.clear();
            pinv.setHelmet(null);
            pinv.setChestplate(null);
            pinv.setLeggings(null);
            pinv.setBoots(null);
            event.getPlayer().getActivePotionEffects().forEach(e -> event.getPlayer().removePotionEffect(e.getType()));
            event.getPlayer().getActivePotionEffects().clear();
            event.getPlayer().sendMessage("You're late to the UHC, " + event.getPlayer().getName() + "!");
            if (lateScatter) {
                Bukkit.broadcastMessage("Late scattering " + event.getPlayer().getName() + "!");
                Scatter(Collections.singletonList(event.getPlayer()), 4800);
            } else {
                dead.add(event.getPlayer().getUniqueId());
                Bukkit.broadcastMessage(event.getPlayer().getName() + " was late to the UHC and will be spectating the game.");
            }
        }
        if (!dead.stream().filter(p -> p == event.getPlayer().getUniqueId()).collect(Collectors.toList()).isEmpty()) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler
    public void onPortalEvent(EntityPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (started) {
            dead.add(event.getEntity().getUniqueId());
        }
    }
}
