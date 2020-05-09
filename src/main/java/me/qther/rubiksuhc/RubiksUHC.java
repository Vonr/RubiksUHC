package me.qther.rubiksuhc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.MenuFunctionListener;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.type.ChestMenu;

public final class RubiksUHC extends JavaPlugin {

    public static Menu mainMenu = RubiksUHC.createMenu("RubiksUHC Menu");
    public static Menu scenarioMenu = RubiksUHC.createMenu("RubiksUHC Menu");

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("RubiksUHC has been initialized!");
        getCommand("ruhc").setExecutor(new UHCCommand());
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);



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
        Bukkit.broadcastMessage("Not implemented yet");
    }

}
