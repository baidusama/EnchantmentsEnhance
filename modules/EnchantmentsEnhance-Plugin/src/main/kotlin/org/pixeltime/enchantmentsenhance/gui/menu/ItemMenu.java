package org.pixeltime.enchantmentsenhance.gui.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;
import org.pixeltime.enchantmentsenhance.Main;
import org.pixeltime.enchantmentsenhance.gui.GUIAbstract;
import org.pixeltime.enchantmentsenhance.gui.menu.icons.BackIcon;
import org.pixeltime.enchantmentsenhance.gui.menu.icons.GrindIcon;
import org.pixeltime.enchantmentsenhance.gui.menu.icons.ReblathIcon;
import org.pixeltime.enchantmentsenhance.manager.CompatibilityManager;
import org.pixeltime.enchantmentsenhance.manager.ItemManager;
import org.pixeltime.enchantmentsenhance.manager.MaterialManager;
import org.pixeltime.enchantmentsenhance.manager.SettingsManager;
import org.pixeltime.enchantmentsenhance.mysql.PlayerStat;
import org.pixeltime.enchantmentsenhance.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ItemMenu extends GUIAbstract {
    private static final int BUNDLE = 5;
    public static Map<String, Integer> clickedItem = new HashMap<>();
    private BackIcon back = new BackIcon();
    private ReblathIcon reblath = new ReblathIcon();
    private GrindIcon grind = new GrindIcon();

    public ItemMenu(Player p) {
        super(p, 54, SettingsManager.lang.getString("item.title"));
        update();
    }


    @Override
    public void update() {
        getInventory().clear();
        getActions().clear();

        Player player = Bukkit.getPlayer(getPlayerName());
        for (int i = 0; i < MaterialManager.stoneTypes.size(); i++) {
            final int stoneId = i;
            setItem(Util.getSlot((i % 9) + 1, (i / 9) + 1),
                    (clickedItem.containsKey(getPlayerName()) && stoneId == clickedItem.get(getPlayerName())
                            ? CompatibilityManager.glow.addGlow(MainMenu.stone.getItem(i, player))
                            : MainMenu.stone.getItem(i, player)),
                    (clickType) -> {
                        if (clickType == ClickType.LEFT) {
                            if (Main.getApi().getItem(getPlayerName(), stoneId) > 0) {
                                if (clickedItem.containsKey(getPlayerName()) && clickedItem.get(getPlayerName()) == stoneId) {
                                    clickedItem.remove(player.getName());
                                } else {
                                    clickedItem.put(player.getName(), stoneId);
                                }
                            }
                        }
                        if (clickType == ClickType.RIGHT && SettingsManager.config.getBoolean("enableItemMaterialization")) {
                            // If play has enough stones.
                            if (Main.getApi().getItem(getPlayerName(), stoneId) >= BUNDLE) {
                                if (!Util.invFull(player)) {
                                    player.getInventory().addItem(ItemManager.itemMaterialize(stoneId, BUNDLE));
                                    Main.getApi().addItem(getPlayerName(), stoneId, -BUNDLE);
                                } else {
                                    Util.sendMessage(SettingsManager.lang.getString("materialize.inventoryFull"), player);
                                }
                            } else {
                                Util.sendMessage(SettingsManager.lang.getString("materialize.notEnoughItem"), player);
                            }
                        }
                    });
        }

        setItem(back.getPosition(), back.getItem(getPlayerName()), (clickType) -> new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                new MainMenu(player).open();
            }
        }.runTaskLater(Main.getMain(), 2L));

        if (SettingsManager.config.getBoolean("enableGrinding")) {
            setItem(grind.getPosition(), grind.getItem(getPlayerName()), (clickType) -> {
                if (clickedItem.containsKey(player.getName())) {
                    // If player has item to failstack.
                    if (Main.getApi().getItem(player.getName(), clickedItem.get(player.getName())) > 0) {
                        int locked = 2;
                        if (PlayerStat.getPlayerStats(getPlayerName()) != null) {
                            locked = PlayerStat.getPlayerStats(getPlayerName()).getGrind();
                        }
                        Main.getApi().addItem(player.getName(), clickedItem.get(player.getName()), -1);
                        Random random = new Random();
                        double num = random.nextDouble();

                        if (num < (1.0 / locked)) {
                            // Reward
                            Util.sendMessage(SettingsManager.lang.getString("grind.success")
                                            .replace("%amount%", Integer.toString(locked))
                                    , player);
                            Main.getApi().addItem(player.getName(), clickedItem.get(player.getName()), locked);
                        } else {
                            // Fail
                            Util.sendMessage(SettingsManager.lang.getString("grind.failed"), player);
                        }
                    } else {
                        Util.sendMessage(SettingsManager.lang.getString("gui.noItem"), player);
                    }
                } else {
                    Util.sendMessage(SettingsManager.lang.getString("gui.missingItem"), player);
                }
            });
        }

        if (SettingsManager.config.getBoolean("enableReblathFailstacking")) {
            setItem(reblath.getPosition(), reblath.getItem(getPlayerName()), (clickType) ->
            {
                if (clickedItem.containsKey(player.getName())) {
                    // If player has item to failstack.
                    if (Main.getApi().getItem(player.getName(), clickedItem.get(player.getName())) > 0) {
                        // Roll.
                        if ((Math.random() * 100) > reblath.getChance()) {
                            int levelsToAdd = 1;
                            Main.getApi().addFailstack(player.getName(), levelsToAdd);
                            Main.getApi().addItem(player.getName(), clickedItem.get(player.getName()), -1);
                            Util.sendMessage(SettingsManager.lang.getString("gui.addFailstack")
                                    .replace("%level%", Integer.toString(levelsToAdd))
                                    .replace("%size%", Integer.toString(Main.getApi().getFailstack(player.getName()))), player);
                        } else {
                            Util.sendMessage(SettingsManager.lang.getString("gui.resetFailstack").replace("%level%", Integer.toString(Main.getApi().getFailstack(player.getName()))), player);
                            Main.getApi().resetFailstack(player.getName());
                        }
                    } else {
                        Util.sendMessage(SettingsManager.lang.getString("gui.noItem"), player);
                    }
                } else {
                    Util.sendMessage(SettingsManager.lang.getString("gui.missingItem"), player);
                }
            });
        }
    }
}
