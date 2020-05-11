package me.qther.rubiksuhc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class RubiksUHC extends JavaPlugin {

    public static Menu mainMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu scenarioMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static boolean started = false;
    public static boolean ended = false;
    public static long timeStarted = 0;
    public static List<UUID> scattered = new ArrayList<>(Arrays.asList());
    public static List<UUID> dead = new ArrayList<>(Arrays.asList());

    static DecimalFormat df = new DecimalFormat("#.00");

    public static String overworldName;
    public static int borderSize;
    public static int borderTime;
    public static int gracePeriod;
    public static int scatterSize;
    public static boolean lateScatter;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveConfig();
        getCommand("ruhc").setExecutor(new UHCCommand());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        System.out.println("RubiksUHC has been initialized!");

        // Config magics
        overworldName = getConfig().getString("world.overworld.name") == null ? "world" : getConfig().getString("world.overworld.name");
        borderSize = getConfig().getInt("uhc.border.size") == 0 ? 5000 : getConfig().getInt("uhc.border.size");
        borderTime = getConfig().getInt("uhc.border.time");
        gracePeriod = getConfig().getInt("uhc.game.gracePeriod");
        scatterSize = getConfig().getInt("uhc.game.scatterSize") < 0.2 * borderSize ? borderSize - 200 : getConfig().getInt("uhc.game.scatterSize");
        lateScatter = getConfig().getBoolean("uhc.game.lateScatter");
        getConfig().set("world.overworld.name", overworldName);
        getConfig().set("uhc.border.size", borderSize);
        getConfig().set("uhc.border.time", borderTime);
        getConfig().set("uhc.game.gracePeriod", gracePeriod);
        getConfig().set("uhc.game.scatterSize", scatterSize);
        getConfig().set("uhc.game.lateScatter", lateScatter);
        saveConfig();

        // Main Menu
        ItemStack scenarioItem = createItemStack(Material.COMMAND_BLOCK, 1, "&r&6Scenarios");
        mainMenu.getSlot(0).setItem(scenarioItem);
        mainMenu.getSlot(0).setClickHandler((player, info) -> {
            displayMenu(player, scenarioMenu);
        });
        ItemStack startItem = createItemStack(Material.LIME_WOOL, 1, "&r&aStart");
        mainMenu.getSlot(4).setItem(startItem);
        mainMenu.getSlot(4).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.start")) {
                startUHC();
            } else {
                player.sendMessage("You do not have permission to start the UHC!");
            }
            mainMenu.close(player);
        });

        // Scenario Menu
        ItemStack backItem = createItemStack(Material.BARRIER, 1, "&r&cBack");
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
                dead.forEach(p -> Bukkit.getPlayer(p).setGameMode(GameMode.SPECTATOR));
                if (scattered.size() - dead.size() <= 1) {
                    Player winner = Bukkit.getPlayer(scattered.stream().filter(p -> {
                        for (UUID player : dead) {
                            if (p == player) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList()).get(0));
                    if (!winner.isOnline()) { winner = Bukkit.getPlayer(dead.get(dead.size() - 1)); }
                    if (!ended) {
                        winner.teleport(new Location(winner.getWorld(), 0, 300, 0));
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                        World world = Bukkit.getWorld("world");
                        WorldBorder border = world.getWorldBorder();
                        border.setCenter(0, 0);
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
        Bukkit.getWorld(overworldName).setTime(6000);
        Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setCenter(0, 0);
        if (borderTime > 0) {
            Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setSize(2 * borderSize);
            Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setSize(0.5, borderTime);
        }
        else Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setSize(2 * borderSize);
        Objects.requireNonNull(Bukkit.getWorld(overworldName)).getWorldBorder().setDamageAmount(2);
        if (borderTime > 0) Bukkit.broadcastMessage("The border has started to shrink from a size of " + borderSize * 2 + " square blocks towards x0 z0.");
        else Bukkit.broadcastMessage("The border has been created with a size of " + borderSize * 2 + " square blocks.");
        if (borderTime > 0) Bukkit.broadcastMessage("It will reach x0 z0 in " + borderTime + " seconds (" + df.format(borderTime / 60) + " minutes)");
        if (gracePeriod > 0) Bukkit.broadcastMessage("There is a grace (No PVP) period of " + gracePeriod + " seconds (" + df.format(gracePeriod / 60) + " minutes)");
        Bukkit.broadcastMessage("Good luck, have fun!");
        timeStarted = System.currentTimeMillis();
        started = true;
        Scatter((List<Player>) Bukkit.getOnlinePlayers(), scatterSize);
    }

    public static void Scatter(List<Player> players, int bounds) {
        players.forEach(player -> {
            if (started) Bukkit.broadcastMessage("Scattering " + player.getName() + "!");
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.teleport(new Location(player.getWorld(), ThreadLocalRandom.current().nextInt(-bounds, bounds + 1), 300, ThreadLocalRandom.current().nextInt(-bounds, bounds + 1)));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30,50));
            if (started && !scattered.contains(player.getUniqueId())) scattered.add(player.getUniqueId());
        });
    }

    public static ItemStack createItemStack(Material material, int amount, String name) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(name.replaceAll("&", "\u00a7"));
        stack.setItemMeta(stackMeta);
        return stack;
    }

}
