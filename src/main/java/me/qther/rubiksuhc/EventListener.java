package me.qther.rubiksuhc;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static me.qther.rubiksuhc.RubiksUHC.*;

public class EventListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!started) {
            event.getPlayer().sendMessage(pluginPrefix + "The UHC has not started!");
            event.setCancelled(true);
        }
        if (event.getBlock().getType() == Material.GRINDSTONE) {
            event.getPlayer().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
            event.setCancelled(true);
        }
        if (!event.isCancelled() &&
                (
                event.getBlock().getType() == Material.OAK_LEAVES ||
                event.getBlock().getType() == Material.DARK_OAK_LEAVES ||
                event.getBlock().getType() == Material.JUNGLE_LEAVES ||
                event.getBlock().getType() == Material.BIRCH_LEAVES ||
                event.getBlock().getType() == Material.ACACIA_LEAVES ||
                event.getBlock().getType() == Material.SPRUCE_LEAVES
                )
        ) {
            if (ThreadLocalRandom.current().nextInt(0, 10) + 1 <= 2) event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.APPLE, 1));
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!started || ended) {
            event.setCancelled(true);
        }
        if (!event.isCancelled()) {
            if (ThreadLocalRandom.current().nextInt(0, 10) + 1 <= 2) event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.APPLE, 1));
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
            event.getPlayer().sendMessage(pluginPrefix + "The UHC has not started!");
            event.setCancelled(true);
        }
        if (event.getBlock().getType() == Material.GRINDSTONE) {
            event.getPlayer().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!started || ended) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event) {
        if (!started || ended) {
            event.getPlayer().sendMessage(pluginPrefix + "The UHC has not started!");
            event.setCancelled(true);
            return;
        }
        try {
            if (Objects.requireNonNull(event.getClickedBlock()).getType() == Material.GRINDSTONE) {
                event.getPlayer().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
                event.setCancelled(true);
            }
        } catch (Throwable ignored) {}
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
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(new Vector(0, 0.1, 0)));
            event.setCancelled(true);
        }
        if (!started || ended || timeStarted + 20 * 1000 > System.currentTimeMillis()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(pluginPrefix + "Do /ruhc to access the RubiksUHC menu!");

        // Gamerules
        Objects.requireNonNull(RubiksUHC.overworld).setGameRule(GameRule.NATURAL_REGENERATION, false);
        Objects.requireNonNull(RubiksUHC.overworld).setGameRule(GameRule.MOB_GRIEFING, false);
        Objects.requireNonNull(RubiksUHC.overworld).setGameRule(GameRule.DISABLE_RAIDS, true);
        Objects.requireNonNull(RubiksUHC.overworld).setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        Objects.requireNonNull(RubiksUHC.overworld).setGameRule(GameRule.DO_INSOMNIA, false);
        if (!started) {
            event.getPlayer().setHealth(20);
            event.getPlayer().setFoodLevel(20);
            event.getPlayer().setSaturation(20);
            event.getPlayer().sendMessage(pluginPrefix + "Clearing your inventory, potion effects and XP!");
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            event.getPlayer().setLevel(0);
            event.getPlayer().setExp(0);
            PlayerInventory pinv = event.getPlayer().getInventory();
            pinv.clear();
            pinv.setHelmet(null);
            pinv.setChestplate(null);
            pinv.setLeggings(null);
            pinv.setBoots(null);
            event.getPlayer().getActivePotionEffects().forEach(e -> event.getPlayer().removePotionEffect(e.getType()));
            event.getPlayer().getActivePotionEffects().clear();
            event.getPlayer().teleport(new Location(overworld, 0, 300, 0));
            overworld.getWorldBorder().setCenter(0, 0);
            overworld.getWorldBorder().setSize(2 * 35);
            overworld.getWorldBorder().setDamageAmount(0);
            event.getPlayer().sendMessage(pluginPrefix + "Welcome to the UHC, " + event.getPlayer().getName() + "!");
        } else if (scattered.stream().filter(p -> p == event.getPlayer().getUniqueId()).collect(Collectors.toList()).isEmpty() && System.currentTimeMillis() < timeStarted + 3 * 60 * 1000) {
            event.getPlayer().sendMessage(pluginPrefix + "Clearing your inventory, potion effects and XP!");
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            event.getPlayer().setLevel(0);
            event.getPlayer().setExp(0);
            PlayerInventory pinv = event.getPlayer().getInventory();
            pinv.clear();
            pinv.setHelmet(null);
            pinv.setChestplate(null);
            pinv.setLeggings(null);
            pinv.setBoots(null);
            event.getPlayer().getActivePotionEffects().forEach(e -> event.getPlayer().removePotionEffect(e.getType()));
            event.getPlayer().getActivePotionEffects().clear();
            event.getPlayer().sendMessage(pluginPrefix + "You're late to the UHC, " + event.getPlayer().getName() + "!");
            if (lateScatter) {
                Bukkit.broadcastMessage(pluginPrefix + "Late scattering " + event.getPlayer().getName() + "!");
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
    public void onPortalCreate(PortalCreateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (started) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1f));
            dead.add(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.GRINDSTONE) {
            event.getWhoClicked().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
            event.setCancelled(true);
        }
    }
}
