package me.qther.rubiksuhc;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static me.qther.rubiksuhc.RubiksUHC.*;

public class EventListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!RubiksUHC.devMode && !started) {
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
        if (!RubiksUHC.devMode && (!started || ended)) {
            event.setCancelled(true);
        }
        if (!event.isCancelled()) {
            if (ThreadLocalRandom.current().nextInt(0, 10) + 1 <= 2) event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.APPLE, 1));
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!RubiksUHC.devMode && (!started || ended)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobExplode(EntityExplodeEvent event) {
        if (!RubiksUHC.devMode && (!started || ended)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!RubiksUHC.devMode && !started) {
            event.getPlayer().sendMessage(pluginPrefix + "The UHC has not started!");
            event.setCancelled(true);
        }
        if (event.getItemInHand() == RubiksUHC.createNewHead(1, "LegendaryJulien", "&r&6Golden Head", "&r&cHeals you on right click.", "&aRegen 2 (0:10)", "&aAbsorption 1 (2:00)")) {
            event.setCancelled(true);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, true));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0, true, true));
            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() == 0) event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);
            return;
        }
        if (event.getBlock().getType() == Material.GRINDSTONE) {
            event.getPlayer().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!RubiksUHC.devMode && (!started || ended)) {
            event.setCancelled(true);
            return;
        }
        if (((Player) event.getRightClicked()).getInventory().getItemInMainHand() == RubiksUHC.createNewHead(1, "LegendaryJulien", "&r&6Golden Head", "&r&cHeals you on right click.", "&aRegen 2 (0:10)", "&aAbsorption 1 (2:00)")) {
            event.setCancelled(true);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, true));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0, true, true));
            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() == 0) event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event) {
        if (!RubiksUHC.devMode && (!started || ended)) {
            event.getPlayer().sendMessage(pluginPrefix + "The UHC has not started!");
            event.setCancelled(true);
            return;
        }
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.PLAYER_HEAD && !event.getItem().getItemMeta().getLore().isEmpty()) {
            event.setCancelled(true);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, true));
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0, true, true));
            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() == 0) event.getPlayer().getInventory().getItemInMainHand().setType(Material.AIR);
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
        if (!RubiksUHC.devMode && !started) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!RubiksUHC.devMode && (!started || ended || (event.getDamager() instanceof Player && event.getEntity() instanceof Player && System.currentTimeMillis() < timeStarted + opt_gracePeriod))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(new Vector(0, 0.1, 0)));
            event.setCancelled(true);
        }
        if (!RubiksUHC.devMode && (!started || ended || timeStarted + 20 * 1000 > System.currentTimeMillis())) event.setCancelled(true);
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
            if (opt_lateScatter) {
                Bukkit.broadcastMessage(pluginPrefix + "Late scattering " + event.getPlayer().getName() + "!");
                RubiksUHC.Scatter(Collections.singletonList(event.getPlayer()), 4800);
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
        if (started || RubiksUHC.devMode) {
            Player player = event.getEntity();
            if (opt_goldenHeads) {
                player.getWorld().dropItemNaturally(player.getLocation(), RubiksUHC.createNewHead(1, player.getUniqueId(), player.getName() + "'s Head"));
            }
            Bukkit.getOnlinePlayers().forEach(_player -> _player.playSound(_player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1f));
            dead.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.GRINDSTONE) {
            event.getWhoClicked().sendMessage(pluginPrefix + "Grindstones are disabled! Use /ruhc and click the Grindstone to disenchant your item.");
            event.setCancelled(true);
            return;
        }
        if (hiddenRecipes.contains(event.getRecipe().getResult().getType())) {
            event.getWhoClicked().sendMessage(pluginPrefix + "This recipe has been disabled.");
            event.setCancelled(true);
            return;
        }
    }
}
