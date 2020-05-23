package me.qther.rubiksuhc.scenarios;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

public class TreeCapitator implements Listener {
    public static boolean enabled;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (enabled) {
            Material type = event.getBlock().getType();
            if (type == Material.ACACIA_LOG || type == Material.BIRCH_LOG || type == Material.DARK_OAK_LOG || type == Material.JUNGLE_LOG || type == Material.OAK_LOG || type == Material.SPRUCE_LOG) {
                Player player = event.getPlayer();
                Material itemType = player.getInventory().getItemInMainHand().getType();
                if (itemType == Material.WOODEN_AXE || itemType == Material.GOLDEN_AXE || itemType == Material.STONE_AXE || itemType == Material.IRON_AXE || itemType == Material.DIAMOND_AXE) {
                    breakLogs(player, event.getBlock().getLocation(), type);
                }
            }
        }
    }

    public void breakLogs(Player breaker, Location position, Material type) {
        World world = breaker.getWorld();
        for (int i = 1; world.getBlockAt(position.getBlockX(), i + position.getBlockY(), position.getBlockZ()).getType() == type; i++) {
            Location newPos = new Location(position.getWorld(), position.getBlockX(), i + position.getBlockY(), position.getBlockZ());
            world.getBlockAt(newPos).setType(Material.AIR);
            world.dropItemNaturally(newPos, new ItemStack(type));
            ItemStack item = breaker.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && ThreadLocalRandom.current().nextInt(1,100 + 1) < 100 / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1)) {
                Damageable damageable = (Damageable) item.getItemMeta();
                damageable.setDamage(damageable.getDamage() + 1);
                item.setItemMeta((ItemMeta) damageable);
                if (damageable.getDamage() > item.getType().getMaxDurability()) {
                    breaker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }
        }
        for (int i = -1; world.getBlockAt(position.getBlockX(), i + position.getBlockY(), position.getBlockZ()).getType() == type; i--) {
            Location newPos = new Location(position.getWorld(), position.getBlockX(), i + position.getBlockY(), position.getBlockZ());
            world.getBlockAt(newPos).setType(Material.AIR);
            world.dropItemNaturally(newPos, new ItemStack(type));
            ItemStack item = breaker.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && ThreadLocalRandom.current().nextInt(1,100 + 1) < 100 / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1)) {
                Damageable damageable = (Damageable) item.getItemMeta();
                damageable.setDamage(damageable.getDamage() + 1);
                item.setItemMeta((ItemMeta) damageable);
                if (damageable.getDamage() > item.getType().getMaxDurability()) {
                    breaker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }
        }
    }
}
