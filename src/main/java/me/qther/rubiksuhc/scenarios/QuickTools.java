package me.qther.rubiksuhc.scenarios;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuickTools implements Listener {
    public static boolean enabled;
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        Material material = result.getType();
        if (material == Material.WOODEN_PICKAXE || material == Material.STONE_PICKAXE || material == Material.GOLDEN_PICKAXE || material == Material.IRON_PICKAXE || material == Material.DIAMOND_PICKAXE ||
                material == Material.WOODEN_AXE || material == Material.STONE_AXE || material == Material.GOLDEN_AXE || material == Material.IRON_AXE || material == Material.DIAMOND_AXE ||
                material == Material.WOODEN_SHOVEL || material == Material.STONE_SHOVEL || material == Material.GOLDEN_SHOVEL || material == Material.IRON_SHOVEL || material == Material.DIAMOND_SHOVEL) {
            HumanEntity crafter = event.getWhoClicked();
            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.addEnchant(Enchantment.DIG_SPEED, 3, false);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }

    }
}
