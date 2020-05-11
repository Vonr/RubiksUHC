package me.qther.rubiksuhc;

import me.qther.rubiksuhc.scenarios.CutClean;
import me.qther.rubiksuhc.scenarios.QuickTools;
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

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class RubiksUHC extends JavaPlugin {

    public static World overworld = null;

    public static Menu mainMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu scenarioMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static boolean started = false;
    public static boolean ended = false;
    public static long timeStarted = 0;
    public static List<UUID> scattered = new ArrayList<>(Arrays.asList());
    public static List<UUID> dead = new ArrayList<>(Arrays.asList());

    ItemStack cutCleanIndicatorItem;

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

        // Scenario
        getServer().getPluginManager().registerEvents(new CutClean(), this);
        getServer().getPluginManager().registerEvents(new QuickTools(), this);

        // Config magics
        overworldName = getConfig().getString("world.overworld.name") == null ? "world" : getConfig().getString("world.overworld.name");
        borderSize = getConfig().getInt("uhc.border.size") == 0 ? 5000 : getConfig().getInt("uhc.border.size");
        borderTime = getConfig().getInt("uhc.border.time");
        gracePeriod = getConfig().getInt("uhc.game.gracePeriod");
        scatterSize = getConfig().getInt("uhc.game.scatterSize") < 0.2 * borderSize ? borderSize - 200 : getConfig().getInt("uhc.game.scatterSize");
        lateScatter = getConfig().getBoolean("uhc.game.lateScatter");
        CutClean.enabled = getConfig().getBoolean("uhc.scenarios.cutClean");
        QuickTools.enabled = getConfig().getBoolean("uhc.scenarios.quickTools");
        getConfig().set("world.overworld.name", overworldName);
        getConfig().set("uhc.border.size", borderSize);
        getConfig().set("uhc.border.time", borderTime);
        getConfig().set("uhc.game.gracePeriod", gracePeriod);
        getConfig().set("uhc.game.scatterSize", scatterSize);
        getConfig().set("uhc.game.lateScatter", lateScatter);
        getConfig().set("uhc.scenarios.cutClean", CutClean.enabled);
        getConfig().set("uhc.scenarios.quickTools", QuickTools.enabled);
        saveConfig();

        // Scenario Indicators
        cutCleanIndicatorItem = CutClean.enabled ? new ItemStack(Material.LIME_STAINED_GLASS_PANE) : new ItemStack(Material.RED_STAINED_GLASS_PANE);

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
                if (!started) {
                    startUHC();
                } else {
                    player.sendMessage("The UHC has already begun!");
                }
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

        // CutClean
        ItemStack cutCleanItem = createItemStack(Material.IRON_INGOT, 1, "&r&6CutClean", "&r&cAutomatically smelts", "&r&6Gold &cand &7Iron",  "&r&cores when mined");
        scenarioMenu.getSlot(10).setItem(cutCleanItem);
        scenarioMenu.getSlot(10).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    CutClean.enabled = !CutClean.enabled;
                    getConfig().set("uhc.scenarios.cutClean", CutClean.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage("CutClean is now " + (CutClean.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    Bukkit.broadcastMessage("CutClean is " + (CutClean.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                Bukkit.broadcastMessage("CutClean is " + (CutClean.enabled ? "enabled!" : "disabled!"));
            }
        });

        // Quick Tools
        ItemStack quickToolsItem = createItemStack(Material.DIAMOND_PICKAXE, 1, "&r&6Quick Tools", "&r&cMakes crafted tools", "&r&chave &6Efficiency 3 &cand",  "&r&6Unbreaking 1");
        scenarioMenu.getSlot(11).setItem(quickToolsItem);
        scenarioMenu.getSlot(11).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    QuickTools.enabled = !QuickTools.enabled;
                    getConfig().set("uhc.scenarios.quickTools", QuickTools.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage("Quick Tools is now " + (QuickTools.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    Bukkit.broadcastMessage("Quick Tools is " + (QuickTools.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                Bukkit.broadcastMessage("Quick Tools is " + (QuickTools.enabled ? "enabled!" : "disabled!"));
            }
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
            /*if (started) {
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
            }*/
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
        overworld.setTime(6000);
        Objects.requireNonNull(overworld).getWorldBorder().setCenter(0, 0);
        if (borderTime > 0) {
            Objects.requireNonNull(overworld).getWorldBorder().setSize(2 * borderSize);
            Objects.requireNonNull(overworld).getWorldBorder().setSize(0.5, borderTime);
        }
        else Objects.requireNonNull(overworld).getWorldBorder().setSize(2 * borderSize);
        Objects.requireNonNull(overworld).getWorldBorder().setDamageAmount(0.2);
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

    public static ItemStack createItemStack(Material material, int amount, String name, String... lore) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(name.replaceAll("&", "\u00a7"));
        List<String> loreAsList = new ArrayList<>(Arrays.asList(lore));
        for (int i = 0; i < loreAsList.size(); i++) {
            loreAsList.set(i, loreAsList.get(i).replaceAll("&", "\u00a7"));
        }
        stackMeta.setLore(loreAsList);
        stack.setItemMeta(stackMeta);
        return stack;
    }

}
