package me.qther.rubiksuhc.scenarios;

import me.qther.rubiksuhc.RubiksUHC;
import org.bukkit.Bukkit;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VeinMiner implements Listener {

    public static boolean enabled;

    private static List<Location> checked = new ArrayList<>(Arrays.asList());

    private static final List<Vector> offsets = new ArrayList<>(Arrays.asList(
            new Vector(-1, -1, -1),
            new Vector(-1, -1, 0),
            new Vector(-1, -1, 1),
            new Vector(-1, 0, -1),
            new Vector(-1, 0, 0),
            new Vector(-1, 0, 1),
            new Vector(-1, 1, -1),
            new Vector(-1, 1, 0),
            new Vector(-1, 1, 1),
            new Vector(0, -1, -1),
            new Vector(0, -1, 0),
            new Vector(0, -1, 1),
            new Vector(0, 0, -1),
            new Vector(0, 0, 0),
            new Vector(0, 0, 1),
            new Vector(0, 1, -1),
            new Vector(0, 1, 0),
            new Vector(0, 1, 1),
            new Vector(1, -1, -1),
            new Vector(1, -1, 0),
            new Vector(1, -1, 1),
            new Vector(1, 0, -1),
            new Vector(1, 0, 0),
            new Vector(1, 0, 1),
            new Vector(1, 1, -1),
            new Vector(1, 1, 0),
            new Vector(1, 1, 1)
    ));

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (enabled && RubiksUHC.started && !RubiksUHC.ended) {
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
            int toolStrength = 0;
            switch (tool.getType()) {
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
            Material blockType = block.getType();
            Location blockLoc = block.getLocation();
            switch (blockType) {
                case COAL_ORE:
                case NETHER_QUARTZ_ORE:
                    if (toolStrength >= 1) {
                        lookForOres(blockType, blockLoc, tool);
                        event.setCancelled(true);
                    }
                    break;
                case GOLD_ORE:
                case EMERALD_ORE:
                case REDSTONE_ORE:
                case DIAMOND_ORE:
                    if (toolStrength >= 3) {
                        lookForOres(blockType, blockLoc, tool);
                        event.setCancelled(true);
                    }
                    break;
                case IRON_ORE:
                case LAPIS_ORE:
                    if (toolStrength >= 2) {
                        lookForOres(blockType, blockLoc, tool);
                        event.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void lookForOres(Material oreType, Location blockLoc, ItemStack tool) {
        if (!checked.contains(blockLoc)) {
            checked.add(blockLoc);
            offsets.forEach(offset -> {
                Location locationStore = blockLoc;
                Location toCheck = locationStore.add(offset);
                if (toCheck.getBlock().getType() == oreType) {
                    mineOre(oreType, toCheck, tool);
                    lookForOres(oreType, toCheck, tool);
                }
            });
        }
    }

    public void mineOre(Material oreType, Location location, ItemStack tool) {
        checked.remove(location);
        Bukkit.broadcastMessage(location.toString());
        /*int baseAmount = 0;
        int amount = 0;
        int xp = 0;
        Material drop = Material.AIR;
        switch (oreType) {
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
        location.getBlock().setType(Material.AIR);

        if (drop == Material.AIR) {
            return;
        }
        if (amount == 0) {
            amount = fortuneLevel > 0 ? baseAmount + (ThreadLocalRandom.current().nextInt(1, 100 + 1) >= 100 / (fortuneLevel + 2) * 2 ? 1 : ThreadLocalRandom.current().nextInt(2, fortuneLevel + 1)) : baseAmount;
        }
        if (amount > 0) {
            RubiksUHC.overworld.dropItemNaturally(location, new ItemStack(drop, amount));
        }
        if (xp > 0) {
            int finalXp = xp;
            RubiksUHC.overworld.spawn(location, ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(finalXp));
        }*/
        location.getBlock().breakNaturally();
    }
}
