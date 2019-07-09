package org.pixeltime.enchantmentsenhance.event.enchantment.gear

import com.sk89q.worldguard.bukkit.WGBukkit
import com.sk89q.worldguard.protection.flags.DefaultFlag
import com.sk89q.worldguard.protection.flags.StateFlag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.pixeltime.enchantmentsenhance.listener.EnchantmentListener
import org.pixeltime.enchantmentsenhance.manager.SettingsManager

class Mischief : EnchantmentListener() {
    override fun desc(): Array<String> {
        return arrayOf("Inflicts nausea to your opponent", "攻击别人造成他反胃")
    }

    override fun lang(): Array<String> {
        return arrayOf("戏耍")
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
                    player2.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, SettingsManager.enchant.getInt("mischief.$level.duration") * 20, SettingsManager.enchant.getInt("mischief.$level.potion_lvl") - 1))
                }
            } catch (ex: Exception) {
            }
        }
    }
}
