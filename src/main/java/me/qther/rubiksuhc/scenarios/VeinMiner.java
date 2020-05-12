package me.qther.rubiksuhc.scenarios;

import me.qther.rubiksuhc.RubiksUHC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class VeinMiner implements Listener {

    public static boolean enabled;

    public static Map<Location, Location> ores = new ConcurrentHashMap<>();
    public static Map<Location, Integer> fortune = new ConcurrentHashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (enabled) {
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
            Material toolType = tool.getType();
            int toolStrength = 0;
            switch (toolType) {
                case WOODEN_PICKAXE:
                case GOLDEN_PICKAXE:
                    toolStrength = 1;
                    break;
                case STONE_PICKAXE:
                    toolStrength = 2;
                    break;
                case IRON_PICKAXE:
                    toolStrength = 3;
                    break;
                case DIAMOND_PICKAXE:
                    toolStrength = 4;
                    break;
                default:
                    break;
            }

            Block block = event.getBlock();
            Location blockLoc = block.getLocation();
            switch (block.getType()) {
                case COAL_ORE:
                case NETHER_QUARTZ_ORE:
                    event.setCancelled(true);
                    if (toolStrength >= 1) new Thread(() -> lookForOres(blockLoc, blockLoc)).start();;
                    new Thread(() -> lookForOres(blockLoc, blockLoc)).start();
                    break;
                case GOLD_ORE:
                case EMERALD_ORE:
                case REDSTONE_ORE:
                case DIAMOND_ORE:
                    event.setCancelled(true);
                    if (toolStrength >= 3) new Thread(() -> lookForOres(blockLoc, blockLoc)).start();;
                    new Thread(() -> lookForOres(blockLoc, blockLoc)).start();
                    break;
                case IRON_ORE:
                case LAPIS_ORE:
                    event.setCancelled(true);
                    if (toolStrength >= 2) new Thread(() -> lookForOres(blockLoc, blockLoc)).start();;
                    break;
                default:
                    break;
            }
            ItemMeta toolMeta = tool.getItemMeta();
            fortune.put(blockLoc, toolMeta == null ? 0 : toolMeta.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS));
        }
    }

    public static void lookForOres(Location origin, Location blockLoc) {
        for (int x = -1; x < 1; x++) {
            for (int y = -1; y < 1; y++) {
                for (int z = -1; z < 1; z++) {
                    Location toCheck = blockLoc.add(x, y, z);
                    if (!ores.containsValue(toCheck) && toCheck.getBlock().getType() == origin.getBlock().getType()) {
                        ores.put(origin, toCheck);
                    }
                }
            }
        }
        for (Map.Entry<Location, Location> entry : ores.entrySet()) {
            lookForOres(entry.getKey(), entry.getValue());
        }
    }

    public static void mineOres(Location key) {
        for (Map.Entry<Location, Location> entry : ores.entrySet()) {
            if (key == entry.getKey()) {
                int baseAmount = 0;
                int amount = 0;
                int xp = 0;
                Material drop = Material.AIR;
                switch (entry.getValue().getBlock().getType()) {
                    case COAL_ORE:
                        baseAmount = 1;
                        drop = Material.COAL;
                        xp = ThreadLocalRandom.current().nextInt(0, 2 + 1);
                        break;
                    case NETHER_QUARTZ_ORE:
                        baseAmount = 1;
                        drop = Material.QUARTZ;
                        xp = ThreadLocalRandom.current().nextInt(2, 5 + 1);
                        break;
                    case DIAMOND_ORE:
                        baseAmount = 1;
                        drop = Material.DIAMOND;
                        xp = ThreadLocalRandom.current().nextInt(3, 7 + 1);
                        break;
                    case EMERALD_ORE:
                        baseAmount = 1;
                        drop = Material.EMERALD;
                        xp = ThreadLocalRandom.current().nextInt(0, 3 + 7);
                        break;
                    case GOLD_ORE:
                        amount = 1;
                        drop = CutClean.enabled ? Material.GOLD_INGOT : Material.GOLD_ORE;
                        xp = CutClean.enabled ? 1 : 0;
                        break;
                    case IRON_ORE:
                        amount = 1;
                        drop = CutClean.enabled ? Material.IRON_INGOT : Material.IRON_ORE;
                        xp = CutClean.enabled ? ThreadLocalRandom.current().nextInt(1, 100 + 1) >= 70 ? 1 : 0 : 0;
                        break;
                    case LAPIS_ORE:
                        baseAmount = ThreadLocalRandom.current().nextInt(4, 9 + 1);
                        drop = Material.LAPIS_LAZULI;
                        xp = ThreadLocalRandom.current().nextInt(2, 5 + 1);
                        break;
                    case REDSTONE_ORE:
                        baseAmount = ThreadLocalRandom.current().nextInt(4,5 + 1);
                        drop = Material.REDSTONE;
                        xp = ThreadLocalRandom.current().nextInt(1, 5 + 1);
                        break;
                    default:
                        break;
                }
                int fortuneLevel = baseAmount + (fortune.get(entry.getKey()) == null ? 0 : fortune.get(entry.getKey()));
                if (amount != 0) amount = baseAmount + (ThreadLocalRandom.current().nextInt(1, 100 + 1) >= 100 / (fortuneLevel + 2) * 2 ? 1 : ThreadLocalRandom.current().nextInt(2, fortuneLevel + 1));
                RubiksUHC.overworld.dropItemNaturally(key.add(0.5, 0, 0.5), new ItemStack(drop, amount));
                int finalXp = xp;
                RubiksUHC.overworld.spawn(key.add(0.5, 0, 0.5), ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(finalXp));
                entry.getValue().getBlock().setType(Material.AIR);
            }
        }
    }
}
