package me.qther.rubiksuhc.scenarios;

import me.qther.rubiksuhc.RubiksUHC;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

public class CutClean implements Listener {
    public static boolean enabled;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!VeinMiner.enabled && enabled && RubiksUHC.started && !RubiksUHC.ended) {
            if (event.getBlock().getType() == Material.GOLD_ORE &&
                    (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.IRON_PICKAXE ||
                            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE)) {
                event.getBlock().setType(Material.AIR);
                event.setCancelled(true);
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if (meta != null && ThreadLocalRandom.current().nextInt(1,100 + 1) < 100 / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1)) {
                    Damageable damageable = (Damageable) item.getItemMeta();
                    damageable.setDamage(damageable.getDamage() + 1);
                    item.setItemMeta((ItemMeta) damageable);
                    if (damageable.getDamage() > item.getType().getMaxDurability()) {
                        event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }
                //event.getPlayer().giveExp(1);
                RubiksUHC.overworld.spawn(event.getBlock().getLocation().add(0.5, 0, 0.5), ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(1));
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.GOLD_INGOT, 1));
            }
            if (event.getBlock().getType() == Material.IRON_ORE &&
                    (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.STONE_PICKAXE ||
                            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.IRON_PICKAXE ||
                            event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE)) {
                event.getBlock().setType(Material.AIR);
                event.setCancelled(true);
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if (meta != null && ThreadLocalRandom.current().nextInt(1,100 + 1) < 100 / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1)) {
                    Damageable damageable = (Damageable) item.getItemMeta();
                    damageable.setDamage(damageable.getDamage() + 1);
                    item.setItemMeta((ItemMeta) damageable);
                    if (damageable.getDamage() > item.getType().getMaxDurability()) {
                        event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }
                //event.getPlayer().giveExp(ThreadLocalRandom.current().nextInt(0, 10) + 1 <= 7 ? 1 : 0);
                if (ThreadLocalRandom.current().nextInt(1, 10 + 1) <= 7) RubiksUHC.overworld.spawn(event.getBlock().getLocation().add(0.5, 0, 0.5), ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(1));
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.IRON_INGOT, 1));
            }
        }
    }
}
