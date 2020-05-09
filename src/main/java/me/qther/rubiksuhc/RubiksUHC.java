package me.qther.rubiksuhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.MenuFunctionListener;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.type.ChestMenu;

import java.util.*;
import java.util.stream.Collectors;

public final class RubiksUHC extends JavaPlugin implements Listener {

    public static Menu mainMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu scenarioMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static boolean started = false;
    public static boolean ended = false;
    public static long timeStarted = 0;
    public static List<Player> scattered = new ArrayList<>(Arrays.asList());
    public static List<Player> dead = new ArrayList<>(Arrays.asList());

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("ruhc").setExecutor(new UHCCommand());
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        System.out.println("RubiksUHC has been initialized!");



        // Main Menu
        ItemStack scenarioItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta scenarioIM = scenarioItem.getItemMeta();
        scenarioIM.setDisplayName("\u00a7r\u00a76Scenarios");
        scenarioItem.setItemMeta(scenarioIM);
        mainMenu.getSlot(0).setItem(scenarioItem);
        mainMenu.getSlot(0).setClickHandler((player, info) -> {
            displayMenu(player, scenarioMenu);
        });
        ItemStack startItem = new ItemStack(Material.LIME_WOOL);
        ItemMeta startIM = startItem.getItemMeta();
        startIM.setDisplayName("\u00a7r\u00a7aStart");
        startItem.setItemMeta(startIM);
        mainMenu.getSlot(4).setItem(startItem);
        mainMenu.getSlot(4).setClickHandler((player, info) -> {
            startUHC();
            mainMenu.close(player);
        });

        // Scenario Menu
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backIM = backItem.getItemMeta();
        backIM.setDisplayName("\u00a7r\u00a7cBack");
        backItem.setItemMeta(backIM);
        scenarioMenu.getSlot(27).setItem(backItem);
        scenarioMenu.getSlot(27).setClickHandler((player, info) -> {
            displayMenu(player, mainMenu);
        });

        //Click Options
        ClickOptions options = ClickOptions.builder()
                .allow(ClickType.LEFT, ClickType.RIGHT)
                .build();
        for (int i = 0; i < 35; i++) {
            mainMenu.getSlot(i).setClickOptions(options);
        }



        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            if (started) {
                dead.forEach(p -> p.setGameMode(GameMode.SPECTATOR));
                if (Bukkit.getOnlinePlayers().size() - dead.size() <= 1) {
                    Player winner = Bukkit.getOnlinePlayers().stream().filter(p -> {
                        for (Player player : dead) {
                            if (p.getUniqueId() == player.getUniqueId()) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList()).get(0);
                    if (!ended) {
                        winner.teleport(new Location(winner.getWorld(), 0.5, 300, 0.5));
                        winner.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 50));
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        World world = Bukkit.getWorld("world");
                        WorldBorder border = world.getWorldBorder();
                        border.setCenter(0.5, 0.5);
                        border.setSize(2 * 35);
                        border.setDamageAmount(0);
                    }
                    ended = true;
                }
            }
        }, 0L, 1L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("RubiksUHC decided to play with Rubik's Cubes instead ;(");
    }

    public static Menu createMenu(String title) {
        return ChestMenu.builder(4)
                .title(title)
                .build();
    }

    public static void displayMenu(Player player, Menu menu) {
        menu.open(player);
    }

    public static void startUHC() {
        // TODO: Use config
        World world = Bukkit.getWorld("world");
        world.setTime(6000);
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.5, 0.5);
        border.setSize(2 * 5000, 1800);
        border.setDamageAmount(2);
        Bukkit.broadcastMessage("The border has started to shrink from 10k^2 towards x0 z0.");
        Bukkit.broadcastMessage("It will reach x0 z0 in 1800 seconds or 30 minutes.");
        Bukkit.broadcastMessage("There is a grace (No PVP) period of 5 minutes, good luck!");
        timeStarted = System.currentTimeMillis();
        started = true;
        Scatter((List<Player>) Bukkit.getOnlinePlayers(), 4800);
    }

    public static void Scatter(List<Player> players, int bounds) {
        players.forEach(player -> {
            Bukkit.broadcastMessage("Scattering " + player.getName() + "!");
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            Random rand = new Random(player.getUniqueId().getMostSignificantBits() * System.currentTimeMillis());
            Random rand2 = new Random(player.getUniqueId().getMostSignificantBits() + System.currentTimeMillis() * 777);
            player.teleport(new Location(player.getWorld(), rand.nextInt((bounds * 2) + 1) - bounds, 300, rand2.nextInt((bounds * 2) + 1) - bounds));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 20,50));
            scattered.add(player);
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!started) {
            event.getPlayer().sendMessage("The UHC has not started!");
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
    public void onPickUp(EntityPickupItemEvent event) {
        if (!started) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!started || ended || (event.getDamager() instanceof Player && event.getEntity() instanceof Player && System.currentTimeMillis() < timeStarted + 30000)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (!started || ended) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scattered.stream().filter(p -> p.getUniqueId() == event.getPlayer().getUniqueId()).forEach(p -> {
            p.sendMessage("Clearing your inventory and potion effects!");
            ItemStack[] pinv = p.getInventory().getContents();
            for (ItemStack i : pinv) {
                i.setType(Material.AIR);
                i.setAmount(0);
            }
            p.getInventory().setContents(pinv);
            p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
        });
        if (!started) {
            World world = Bukkit.getWorld("world");
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(2 * 35);
            border.setDamageAmount(0);
            event.getPlayer().sendMessage("Welcome to the UHC, " + event.getPlayer().getName() + "!");
            Scatter(Collections.singletonList(event.getPlayer()), 30);
        } else if (started && scattered.stream().filter(p -> p.getUniqueId() == event.getPlayer().getUniqueId()).collect(Collectors.toList()).isEmpty() && System.currentTimeMillis() < timeStarted + 3 * 60 * 100) {
            event.getPlayer().sendMessage("You're late to the UHC, " + event.getPlayer().getName() + "!");
            Bukkit.broadcastMessage("Late scattering " + event.getPlayer().getName() + "!");
            Scatter(Collections.singletonList(event.getPlayer()), 4800);
        }
        if (!dead.stream().filter(p -> p.getUniqueId() == event.getPlayer().getUniqueId()).collect(Collectors.toList()).isEmpty()) {
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
            dead.add(event.getEntity());
        }
    }
}
