package org.pixeltime.enchantmentsenhance.event.enchantment.gear

import com.sk89q.worldguard.bukkit.WGBukkit
import com.sk89q.worldguard.protection.flags.DefaultFlag
import com.sk89q.worldguard.protection.flags.StateFlag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.pixeltime.enchantmentsenhance.listener.EnchantmentListener
import org.pixeltime.enchantmentsenhance.manager.SettingsManager

class Purge : EnchantmentListener() {
    override fun desc(): Array<String> {
        return arrayOf("A chance to call down a strike of lightning upon your enemy, removing all positive potion effects they have and damages them", "攻击别人有几率降下闪电并清除他的所有增益效果")
    }

    override fun lang(): Array<String> {
        return arrayOf("净化")
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onDamage(entityDamageByEntityEvent: EntityDamageByEntityEvent) {

        if (entityDamageByEntityEvent.damager is Player && entityDamageByEntityEvent.entity is Player) {

            val player = entityDamageByEntityEvent.damager as Player
            val player2 = entityDamageByEntityEvent.entity as Player
            if (entityDamageByEntityEvent.isCancelled) {
                return
            }
            if (SettingsManager.enchant.getBoolean("allow-worldguard") && WGBukkit.getRegionManager(player2.world).getApplicableRegions(player2.location).queryState(null, DefaultFlag.PVP) == StateFlag.State.DENY) {
                return
            }
            try {
                val level = getLevel(player)
                if (level > 0 && (roll(level))) {
                    player2.world.strikeLightningEffect(player2.location)
                    val iterator = player2.activePotionEffects.iterator()
                    while (iterator.hasNext()) {
                        player2.removePotionEffect((iterator.next() as PotionEffect).type)
                        player2.damage(2.0)
                    }
                }
            } catch (ex: Exception) {
            }

        }
    }
}
