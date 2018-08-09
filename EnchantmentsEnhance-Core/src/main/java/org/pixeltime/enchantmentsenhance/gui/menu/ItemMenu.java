/*
 *     Copyright (C) 2017-Present HealPot
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
import org.pixeltime.enchantmentsenhance.manager.MM;
import org.pixeltime.enchantmentsenhance.manager.SettingsManager;
import org.pixeltime.enchantmentsenhance.mysql.PlayerStat;
import org.pixeltime.enchantmentsenhance.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author HealPotion
 * @version Feb 9, 2018
 */
public class ItemMenu extends GUIAbstract {
    private static final int BUNDLE = 5;
    public static Map<String, Integer> clickedItem = new HashMap<>();
    private BackIcon back = new BackIcon();
    private ReblathIcon reblath = new ReblathIcon();
    private GrindIcon grind = new GrindIcon();

    public ItemMenu(Player p) {
        super(p, 54, SettingsManager.lang.getString("Item.title"));
        update();
    }


    @Override
    public void update() {
        getInventory().clear();
        getActions().clear();

        Player player = Bukkit.getPlayer(playerName);
        for (int i = 0; i < MM.stoneTypes.size(); i++) {
            final int stoneId = i;
            setItem(Util.getSlot((i % 9) + 1, (i / 9) + 1),
                    (clickedItem.containsKey(playerName) && stoneId == clickedItem.get(playerName)
                            ? CompatibilityManager.glow.addGlow(MainMenu.stone.getItem(i, player))
                            : MainMenu.stone.getItem(i, player)),
                    (clickType) -> {
                        if (clickType == ClickType.LEFT) {
                            if (Main.getAPI().getItem(playerName, stoneId) > 0) {
                                if (clickedItem.containsKey(playerName) && clickedItem.get(playerName) == stoneId) {
                                    clickedItem.remove(player.getName());
                                } else {
                                    clickedItem.put(player.getName(), stoneId);
                                }
                            }
                        }
                        if (clickType == ClickType.RIGHT) {
                            // If play has enough stones.
                            if (Main.getAPI().getItem(playerName, stoneId) > BUNDLE) {
                                if (!Util.invFull(player)) {
                                    player.getInventory().addItem(ItemManager.itemMaterialize(stoneId, BUNDLE));
                                    Main.getAPI().addItem(playerName, stoneId, -BUNDLE);
                                } else {
                                    Util.sendMessage(SettingsManager.lang.getString("Materialize.inventoryFull"), player);
                                }
                            } else {
                                Util.sendMessage(SettingsManager.lang.getString("Materialize.notEnoughItem"), player);
                            }
                        }
                    });
        }

        setItem(back.getPosition(), back.getItem(playerName), (clickType) -> new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                new MainMenu(player).open();
            }
        }.runTaskLaterAsynchronously(Main.getMain(), 2L));

        setItem(grind.getPosition(), grind.getItem(playerName), (clickType) -> {
            if (clickedItem.containsKey(player.getName())) {
                // If player has item to failstack.
                if (Main.getAPI().getItem(player.getName(), clickedItem.get(player.getName())) > 0) {
                    int locked = 2;
                    if (PlayerStat.getPlayerStats(playerName) != null) {
                        locked = PlayerStat.getPlayerStats(playerName).getGrind();
                    }
                    Main.getAPI().addItem(player.getName(), clickedItem.get(player.getName()), -1);
                    Random random = new Random();
                    double num = random.nextDouble();
                    if (num < 1 / locked) {
                        // Reward
                        Util.sendMessage(SettingsManager.lang.getString("Grind.success")
                                        .replace("%amount%", Integer.toString(locked))
                                , player);
                        Main.getAPI().addItem(player.getName(), clickedItem.get(player.getName()), locked);
                    } else {
                        // Fail
                        Util.sendMessage(SettingsManager.lang.getString("Grind.failed"), player);
                    }
                } else {
                    Util.sendMessage(SettingsManager.lang.getString("Gui.noItem"), player);
                }
            } else {
                Util.sendMessage(SettingsManager.lang.getString("Gui.missingItem"), player);
            }
        });

        setItem(reblath.getPosition(), reblath.getItem(playerName), (clickType) ->
        {
            if (clickedItem.containsKey(player.getName())) {
                // If player has item to failstack.
                if (Main.getAPI().getItem(player.getName(), clickedItem.get(player.getName())) > 0) {
                    // Roll.
                    if ((Math.random() * 100) > reblath.getChance()) {
                        int levelsToAdd = 1;
                        Main.getAPI().addFailstack(player.getName(), levelsToAdd);
                        Main.getAPI().addItem(player.getName(), clickedItem.get(player.getName()), -1);
                        Util.sendMessage(SettingsManager.lang.getString("Gui.addFailstack")
                                .replace("%level%", Integer.toString(levelsToAdd))
                                .replace("%size%", Integer.toString(Main.getAPI().getFailstack(player.getName()))), player);
                    } else {
                        Util.sendMessage(SettingsManager.lang.getString("Gui.resetFailstack").replace("%level%", Integer.toString(Main.getAPI().getFailstack(player.getName()))), player);
                        Main.getAPI().resetFailstack(player.getName());
                    }
                } else {
                    Util.sendMessage(SettingsManager.lang.getString("Gui.noItem"), player);
                }
            } else {
                Util.sendMessage(SettingsManager.lang.getString("Gui.missingItem"), player);
            }
        });
    }
}
