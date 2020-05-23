package me.qther.rubiksuhc;

import me.qther.rubiksuhc.scenarios.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.MenuFunctionListener;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.type.ChestMenu;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.time.DurationFormatUtils.formatDuration;

public final class RubiksUHC extends JavaPlugin {

    public static final boolean devMode = true;

    public static World overworld = null;
    public Scoreboard scoreboard;
    public static ScoreboardManager scoreboardMgr;
    public Objective objective;
    public Score sb_timeLeft;
    public Score sb_borderSize;
    public Score sb_untilPVP;
    public Score sb_untilFinalHeal;

    public static String pluginPrefix = "\u00a7r\u00a76RubiksUHC \u00a7c\u00bb \u00a77";
    public static Menu mainMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu scenarioMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu optionsMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu craftsMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static boolean started = false;
    public static boolean ended = false;
    public static long timeStarted = 0;
    public static List<UUID> scattered = new ArrayList<>(Arrays.asList());
    public static List<UUID> dead = new ArrayList<>(Arrays.asList());

    static DecimalFormat df = new DecimalFormat("#.00");

    public static String opt_overworldName;
    public static int opt_borderSize;
    public static int opt_borderTime;
    public static int opt_gracePeriod;
    public static int opt_scatterSize;
    public static int opt_finalHeal;
    public static boolean opt_lateScatter;
    public static boolean opt_goldenHeads;
    public static List<Material> hiddenRecipes = new ArrayList<>(Arrays.asList());

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveConfig();

        // Commands
        getCommand("ruhc").setExecutor(new UHCCommand());

        // Main
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        System.out.println("RubiksUHC has been initialized!");

        // Scenario
        List<Listener> scenarios = Arrays.asList(
                new CutClean(),
                new QuickTools(),
                new InfiniteEnchants(),
                //new VeinMiner(),
                new DoubleHealth(),
                new TreeCapitator()
        );
        scenarios.forEach(scenario -> getServer().getPluginManager().registerEvents(scenario, this));

        // Config magics
        opt_overworldName = getConfig().getString("world.overworld.name") == null ? "world" : getConfig().getString("world.overworld.name");
        opt_borderSize = getConfig().getInt("uhc.border.size") == 0 ? 5000 : getConfig().getInt("uhc.border.size");
        opt_borderTime = getConfig().getInt("uhc.border.time");
        opt_gracePeriod = getConfig().getInt("uhc.game.gracePeriod");
        opt_scatterSize = getConfig().getInt("uhc.game.scatterSize") < 0.1 * opt_borderSize ? opt_borderSize - 200 : getConfig().getInt("uhc.game.opt_scatterSize");
        opt_lateScatter = getConfig().getBoolean("uhc.game.lateScatter");
        opt_finalHeal = getConfig().getInt("uhc.game.finalHeal");
        CutClean.enabled = getConfig().getBoolean("uhc.scenarios.cutClean");
        QuickTools.enabled = getConfig().getBoolean("uhc.scenarios.quickTools");
        InfiniteEnchants.enabled = getConfig().getBoolean("uhc.scenarios.infiniteEnchants");
        //VeinMiner.enabled = getConfig().getBoolean("uhc.scenarios.veinMiner");
        DoubleHealth.enabled = getConfig().getBoolean("uhc.scenarios.doubleHealth");
        TreeCapitator.enabled = getConfig().getBoolean("uhc.scenarios.treeCapitator");
        opt_goldenHeads = getConfig().getBoolean("uhc.crafts.goldenHeads");
        VeinMiner.enabled = false;
        getConfig().set("world.overworld.name", opt_overworldName);
        getConfig().set("uhc.border.size", opt_borderSize);
        getConfig().set("uhc.border.time", opt_borderTime);
        getConfig().set("uhc.game.gracePeriod", opt_gracePeriod);
        getConfig().set("uhc.game.scatterSize", opt_scatterSize);
        getConfig().set("uhc.game.lateScatter", opt_lateScatter);
        getConfig().set("uhc.game.finalHeal", opt_finalHeal);
        getConfig().set("uhc.scenarios.cutClean", CutClean.enabled);
        getConfig().set("uhc.scenarios.quickTools", QuickTools.enabled);
        getConfig().set("uhc.scenarios.infiniteEnchants", InfiniteEnchants.enabled);
        //getConfig().set("uhc.scenarios.veinMiner", VeinMiner.enabled);
        getConfig().set("uhc.scenarios.doubleHealth", DoubleHealth.enabled);
        getConfig().set("uhc.scenarios.treeCapitator", TreeCapitator.enabled);
        getConfig().set("uhc.crafts.goldenHeads", opt_goldenHeads);
        saveConfig();

        // Get world
        overworld = Bukkit.getWorld(opt_overworldName);

        // Recipes
        NamespacedKey key1 = new NamespacedKey(this, "golden_head");
        ItemStack goldenHeadItem = createNewHead(1, "LegendaryJulien", "&r&6Golden Head", "&r&cHeals you on right click.", "&aRegen 2 (0:10)", "&aAbsorption 1 (2:00)");
        ShapedRecipe recipe1 = new ShapedRecipe(key1, goldenHeadItem);
        recipe1.shape("GGG", "GHG", "GGG");
        recipe1.setIngredient('G', Material.GOLD_INGOT);
        recipe1.setIngredient('H', Material.PLAYER_HEAD);
        Bukkit.addRecipe(recipe1);
        if (opt_goldenHeads) {
            hiddenRecipes.remove(goldenHeadItem.getType());
        } else {
            hiddenRecipes.add(goldenHeadItem.getType());
        }

        // Main Menu
        ItemStack scenarioItem = createItemStack(Material.COMMAND_BLOCK, 1, "&r&6Scenarios");
        mainMenu.getSlot(0).setItem(scenarioItem);
        mainMenu.getSlot(0).setClickHandler((player, info) -> {
            displayMenu(player, scenarioMenu);
        });
        ItemStack craftsItem = createItemStack(Material.CRAFTING_TABLE, 1, "&r&6Custom Crafts");
        mainMenu.getSlot(8).setItem(craftsItem);
        mainMenu.getSlot(8).setClickHandler((player, info) -> {
            displayMenu(player, craftsMenu);
        });
        /*ItemStack optionsItem = createItemStack(Material.STONECUTTER, 1, "&r&6Options");
        mainMenu.getSlot(13).setItem(optionsItem);
        mainMenu.getSlot(13).setClickHandler((player, info) -> {
            displayMenu(player, optionsMenu);
        });*/
        ItemStack startItem = createItemStack(Material.LIME_WOOL, 1, "&r&aStart");
        mainMenu.getSlot(4).setItem(startItem);
        mainMenu.getSlot(4).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.start")) {
                if (!started) {
                    startUHC();
                } else {
                    player.sendMessage(pluginPrefix + "The UHC has already begun!");
                }
            } else {
                player.sendMessage(pluginPrefix + "You do not have permission to start the UHC!");
            }
            mainMenu.close(player);
        });
        ItemStack disenchantItem = createItemStack(Material.GRINDSTONE, 1, "&r&cDisenchant Held Item");
        mainMenu.getSlot(31).setItem(disenchantItem);
        mainMenu.getSlot(31).setClickHandler((player, info) -> {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                player.sendMessage(pluginPrefix + "You are not holding an item with enchantments.");
            } else {
                PlayerInventory inventory = player.getInventory();
                ItemStack handItem = inventory.getItemInMainHand();
                ItemMeta handItemMeta = handItem.getItemMeta();
                if (Objects.requireNonNull(handItemMeta).getEnchants().isEmpty()) {
                    player.sendMessage(pluginPrefix + "You are not holding an item with enchantments.");
                } else {
                    handItemMeta.getEnchants().forEach((enchant, level) -> handItemMeta.removeEnchant(enchant));
                    handItem.setItemMeta(handItemMeta);
                    inventory.setItemInMainHand(handItem);
                    player.sendMessage(pluginPrefix + "Disenchanted your held \"" + handItemMeta.getDisplayName() + "\".");
                }
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
                    Bukkit.broadcastMessage(pluginPrefix + "CutClean is now " + (CutClean.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "CutClean is " + (CutClean.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "CutClean is " + (CutClean.enabled ? "enabled!" : "disabled!"));
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
                    Bukkit.broadcastMessage(pluginPrefix + "Quick Tools is now " + (QuickTools.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "Quick Tools is " + (QuickTools.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "Quick Tools is " + (QuickTools.enabled ? "enabled!" : "disabled!"));
            }
        });

        // Infinite Enchants
        ItemStack infiniteEnchantsItem = createItemStack(Material.ENCHANTED_BOOK, 1, "&r&6Infinite Enchants", "&r&cGives you &aInfinite Levels &cto enchant", "&r&cand a &6Enchantment Kit&c.");
        scenarioMenu.getSlot(12).setItem(infiniteEnchantsItem);
        scenarioMenu.getSlot(12).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    InfiniteEnchants.enabled = !InfiniteEnchants.enabled;
                    getConfig().set("uhc.scenarios.infiniteEnchants", InfiniteEnchants.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage(pluginPrefix + "Infinite Enchants is now " + (InfiniteEnchants.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "Infinite Enchants is " + (InfiniteEnchants.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "Infinite Enchants is " + (InfiniteEnchants.enabled ? "enabled!" : "disabled!"));
            }
        });

        // Double Health
        ItemStack doubleHealthItem = createItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1, "&r&6Double Health", "&r&cSpawns you with &aDouble Health&c.");
        scenarioMenu.getSlot(13).setItem(doubleHealthItem);
        scenarioMenu.getSlot(13).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    DoubleHealth.enabled = !DoubleHealth.enabled;
                    getConfig().set("uhc.scenarios.doubleHealth", DoubleHealth.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage(pluginPrefix + "Double Health is now " + (DoubleHealth.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "Double Health is " + (DoubleHealth.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "Double Health is " + (DoubleHealth.enabled ? "enabled!" : "disabled!"));
            }
        });

        // TreeCapitator
        ItemStack treeCapitatorItem = createItemStack(Material.IRON_AXE, 1, "&r&6TreeCapitator", "&r&cChops down logs directly connected vertically&c.");
        scenarioMenu.getSlot(14).setItem(treeCapitatorItem);
        scenarioMenu.getSlot(14).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    TreeCapitator.enabled = !TreeCapitator.enabled;
                    getConfig().set("uhc.scenarios.treeCapitator", TreeCapitator.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage(pluginPrefix + "TreeCapitator is now " + (TreeCapitator.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "TreeCapitator is " + (TreeCapitator.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "TreeCapitator is " + (TreeCapitator.enabled ? "enabled!" : "disabled!"));
            }
        });

        /*
        // Vein Miner
        ItemStack veinMinerItem = createItemStack(Material.IRON_ORE, 1, "&r&6Vein Miner", "&r&cMines entire veins upon break.", "&r&cWorks with &5Fortune!");
        scenarioMenu.getSlot(13).setItem(veinMinerItem);
        scenarioMenu.getSlot(13).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.uhc.changeScenarios")) {
                if (!started) {
                    VeinMiner.enabled = !VeinMiner.enabled;
                    getConfig().set("uhc.scenarios.veinMiner", VeinMiner.enabled);
                    saveConfig();
                    Bukkit.broadcastMessage(pluginPrefix + "Vein Miner is now " + (VeinMiner.enabled ? "enabled!" : "disabled!"));
                    displayMenu(player, scenarioMenu);
                } else {
                    player.sendMessage(pluginPrefix + "Vein Miner is " + (VeinMiner.enabled ? "enabled!" : "disabled!"));
                }
            } else {
                player.sendMessage(pluginPrefix + "Vein Miner is " + (InfiniteEnchants.enabled ? "enabled!" : "disabled!"));
            }
        });
        */

        // Crafts Menu
        craftsMenu.getSlot(27).setItem(backItem);
        craftsMenu.getSlot(27).setClickHandler((player, info) -> {
            displayMenu(player, mainMenu);
        });

        // Golden Heads
        craftsMenu.getSlot(10).setItem(goldenHeadItem);
        craftsMenu.getSlot(10).setClickHandler((player, info) -> {
            if (player.hasPermission("rubiksuhc.crafts.toggleCrafts")) {
                if (!started) {
                    opt_goldenHeads = !opt_goldenHeads;
                    getConfig().set("uhc.crafts.goldenHeads", opt_goldenHeads);
                    saveConfig();
                    Bukkit.broadcastMessage(pluginPrefix + "Golden Heads are now " + (opt_goldenHeads ? "enabled!" : "disabled!"));
                    displayMenu(player, craftsMenu);
                } else {
                    player.sendMessage(pluginPrefix + "Golden Heads are " + (opt_goldenHeads ? "enabled!" : "disabled!"));
                }
                if (opt_goldenHeads) {
                    hiddenRecipes.remove(goldenHeadItem.getType());
                } else {
                    hiddenRecipes.add(goldenHeadItem.getType());
                }
            } else {
                player.sendMessage(pluginPrefix + "Golden Heads are " + (opt_goldenHeads ? "enabled!" : "disabled!"));
            }
        });

        // Options Menu
        optionsMenu.getSlot(27).setItem(backItem);
        optionsMenu.getSlot(27).setClickHandler((player, info) -> {
            displayMenu(player, optionsMenu);
        });

        // Click Options
        ClickOptions options = ClickOptions.builder()
                .allow(ClickType.LEFT, ClickType.RIGHT)
                .build();
        for (int i = 0; i < 35; i++) {
            mainMenu.getSlot(i).setClickOptions(options);
            scenarioMenu.getSlot(i).setClickOptions(options);
            craftsMenu.getSlot(i).setClickOptions(options);
            //optionsMenu.getSlot(i).setClickOptions(options);
        }



        // Scoreboard
        scoreboardMgr = Bukkit.getServer().getScoreboardManager();

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            if (started) {
                World world = Bukkit.getWorld(opt_overworldName);
                WorldBorder border = world.getWorldBorder();


                // Scoreboard
                scoreboard = scoreboardMgr.getNewScoreboard();
                objective = scoreboard.registerNewObjective("rubiksuhc", "dummy", "\u00a7r\u00a76\u00a7lRubiksUHC", RenderType.INTEGER);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                sb_timeLeft = objective.getScore("\u00a7aTime Left \u00a7c\u00bb \u00a76" + (ended ? 0 : formatDuration(Math.max(0, timeStarted + opt_borderTime * 1000 - System.currentTimeMillis()), "mm:ss")));
                sb_borderSize = objective.getScore("\u00a7aBorder Inradius \u00a7c\u00bb \u00a76" + (int) border.getSize() / 2);
                sb_timeLeft.setScore(opt_gracePeriod > 0 ? 4 : 3);
                sb_borderSize.setScore(opt_gracePeriod > 0 ? 3 : 2);
                if (!ended && opt_gracePeriod > 0) {
                    if ((int) Math.max(0, timeStarted + opt_gracePeriod * 1000 - System.currentTimeMillis()) / 1000 > 0) {
                        sb_untilPVP = objective.getScore("\u00a7aPvP in \u00a7c\u00bb \u00a76" + formatDuration(Math.max(0, timeStarted + opt_gracePeriod * 1000 - System.currentTimeMillis()), "mm:ss"));
                    } else {
                        sb_untilPVP = objective.getScore("\u00a7aPvP in \u00a7c\u00bb \u00a76Now");
                    }

                    sb_untilPVP.setScore(2);
                }
                if (!ended && opt_finalHeal > 0) {
                    if ((int) Math.max(0, timeStarted + opt_finalHeal * 1000 - System.currentTimeMillis()) / 1000 > 0) {
                        sb_untilFinalHeal = objective.getScore("\u00a7aFinal Heal in \u00a7c\u00bb \u00a76" + formatDuration(Math.max(0, timeStarted + opt_finalHeal * 1000 - System.currentTimeMillis()), "mm:ss"));
                    } else {
                        sb_untilFinalHeal = objective.getScore("\u00a7aFinal Heal in \u00a7c\u00bb \u00a76Over");
                    }

                    sb_untilFinalHeal.setScore(1);
                }
                Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(scoreboard));

                if (InfiniteEnchants.enabled) {
                    scattered.forEach(uuid -> {
                        Player player = Bukkit.getPlayer(uuid);
                        Objects.requireNonNull(player).setLevel(20000);
                        Objects.requireNonNull(player).setExp(0);
                    });
                }
                if (!devMode) {
                    dead.forEach(p -> Objects.requireNonNull(Bukkit.getPlayer(p)).setGameMode(GameMode.SPECTATOR));
                    if (scattered.size() - dead.size() <= 1) {
                        Player winner = Bukkit.getPlayer(scattered.stream().filter(p -> {
                            for (UUID player : dead) {
                                if (p == player) {
                                    return false;
                                }
                            }
                            return true;
                        }).collect(Collectors.toList()).get(0));
                        if (!Objects.requireNonNull(winner).isOnline()) {
                            winner = Bukkit.getPlayer(dead.get(dead.size() - 1));
                        }
                        if (!ended) {
                            Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                            Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                            Bukkit.broadcastMessage(winner.getName() + " is our WINNER!");
                            border.setCenter(0, 0);
                            border.setSize(border.getSize());
                            border.setDamageAmount(0);
                            Player finalWinner = winner;
                            Bukkit.getOnlinePlayers().forEach(online -> online.teleport(finalWinner.getLocation()));
                        }
                        ended = true;
                    }
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

    public void startUHC() {
        if (opt_finalHeal > 0) {
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<?> finalHeal = scheduler.schedule(() -> {
                scattered.forEach(p -> {
                    if (Bukkit.getPlayer(p) != null) {
                        if (DoubleHealth.enabled) {
                            Objects.requireNonNull(Bukkit.getPlayer(p)).setHealth(40.0);
                        } else {
                            Objects.requireNonNull(Bukkit.getPlayer(p)).setHealth(20.0);
                        }
                    }
                });
            }, opt_finalHeal, TimeUnit.SECONDS);
        }
        overworld.setTime(6000);
        Objects.requireNonNull(overworld).getWorldBorder().setCenter(0, 0);
        if (opt_borderTime > 0) {
            Objects.requireNonNull(overworld).getWorldBorder().setSize(2 * opt_borderSize);
            Objects.requireNonNull(overworld).getWorldBorder().setSize(0.5, opt_borderTime);
        }
        else Objects.requireNonNull(overworld).getWorldBorder().setSize(2 * opt_borderSize);
        Objects.requireNonNull(overworld).getWorldBorder().setDamageAmount(0.2);
        if (opt_borderTime > 0) Bukkit.broadcastMessage(pluginPrefix + "The border has started to shrink from a size of " + opt_borderSize * 2 + " square blocks towards x0 z0.");
        else Bukkit.broadcastMessage(pluginPrefix + "The border has been created with a size of " + opt_borderSize * 2 + " square blocks.");
        if (opt_borderTime > 0) Bukkit.broadcastMessage(pluginPrefix + "It will reach x0 z0 in " + opt_borderTime + " seconds (" + df.format(opt_borderTime / 60) + " minutes)");
        if (opt_gracePeriod > 0) Bukkit.broadcastMessage(pluginPrefix + "There is a grace (No PVP) period of " + opt_gracePeriod + " seconds (" + df.format(opt_gracePeriod / 60) + " minutes)");
        Bukkit.broadcastMessage(pluginPrefix + "Good luck, have fun!");
        timeStarted = System.currentTimeMillis();
        started = true;
        Scatter((List<Player>) Bukkit.getOnlinePlayers(), opt_scatterSize);
    }

    public static void Scatter(List<Player> players, int bounds) {
        players.forEach(player -> {
            if (started) Bukkit.broadcastMessage(pluginPrefix + "Scattering " + player.getName() + "!");
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.teleport(new Location(player.getWorld(), ThreadLocalRandom.current().nextInt(-bounds, bounds + 1), 300, ThreadLocalRandom.current().nextInt(-bounds, bounds + 1)));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30,50));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 30,50));
            if (opt_gracePeriod > 0 ) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (int) (opt_gracePeriod * 20 - (System.currentTimeMillis() - timeStarted) / 1000 * 20), 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (opt_gracePeriod * 20 - (System.currentTimeMillis() - timeStarted) / 1000 * 20), 3));
            }
            if (started && !scattered.contains(player.getUniqueId())) scattered.add(player.getUniqueId());

            // Infinite Enchants
            if (InfiniteEnchants.enabled) {
                PlayerInventory inventory = player.getInventory();
                List<ItemStack> infiniteEnchantsKit = Arrays.asList(
                        new ItemStack(Material.ENCHANTING_TABLE, 16),
                        new ItemStack(Material.LAPIS_BLOCK, 32),
                        new ItemStack(Material.BOOKSHELF, 64),
                        new ItemStack(Material.BOOKSHELF, 64),
                        new ItemStack(Material.BOOKSHELF, 64)
                );
                infiniteEnchantsKit.forEach(inventory::addItem);
                player.setLevel(20000);
            }
        });
    }

    public ItemStack createItemStack(Material material, int amount, String name) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(name.replaceAll("&", "\u00a7"));
        stack.setItemMeta(stackMeta);
        return stack;
    }

    public ItemStack createItemStack(Material material, int amount, String name, String... lore) {
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

    public static ItemStack createNewHead(int amount, String owner, String name, String... lore) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta stackMeta = (SkullMeta) stack.getItemMeta();
        stackMeta.setOwner(owner);
        stackMeta.setDisplayName("\u00a7r" + name.replaceAll("&", "\u00a7"));
        List<String> loreAsList = new ArrayList<>(Arrays.asList(lore));
        for (int i = 0; i < loreAsList.size(); i++) {
            loreAsList.set(i, loreAsList.get(i).replaceAll("&", "\u00a7"));
        }
        stackMeta.setLore(loreAsList);
        stack.setItemMeta(stackMeta);
        return stack;
    }

    public static ItemStack createNewHead(int amount, UUID owner, String name, String... lore) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta stackMeta = (SkullMeta) stack.getItemMeta();
        stackMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        stackMeta.setDisplayName("\u00a7r" + name.replaceAll("&", "\u00a7"));
        List<String> loreAsList = new ArrayList<>(Arrays.asList(lore));
        for (int i = 0; i < loreAsList.size(); i++) {
            loreAsList.set(i, loreAsList.get(i).replaceAll("&", "\u00a7"));
        }
        stackMeta.setLore(loreAsList);
        stack.setItemMeta(stackMeta);
        return stack;
    }
}
