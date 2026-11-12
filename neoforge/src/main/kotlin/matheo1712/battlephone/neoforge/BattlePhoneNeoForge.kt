package matheo1712.battlephone.neoforge

import matheo1712.battlephone.BattlePhone
import matheo1712.battlephone.BattlePhoneMod
import matheo1712.battlephone.client.BattlePhoneClient
import net.minecraft.core.registries.Registries
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.RegisterEvent

/**
 * The phone registers through Cobblemon Trainers' platform, which queues every registration and
 * hands it to NeoForge from its own `RegisterEvent` listener. Two things follow:
 *
 * - **The init waits for the block registry event**, like Cobblemon Trainers' own: the phones
 *   are created eagerly, and an item made before NeoForge unfreezes the registries has no
 *   intrusive holder to land in.
 * - **It is queued before the item registry is handed out.** `RegisterEvent` goes through every
 *   mod for one registry before moving to the next, so whatever is queued while blocks are being
 *   registered is in the list by the time items are. `ordering = "AFTER"` on the dependency, in
 *   `neoforge.mods.toml`, keeps this mod behind Cobblemon Trainers within each of those passes.
 */
@Mod(BattlePhoneMod.MOD_ID)
class BattlePhoneNeoForge(bus: IEventBus) {
    init {
        bus.addListener { event: RegisterEvent ->
            if (event.registryKey == Registries.BLOCK) BattlePhone.onInitialize()
        }
    }
}

@Mod(value = BattlePhoneMod.MOD_ID, dist = [Dist.CLIENT])
class BattlePhoneNeoForgeClient(@Suppress("UNUSED_PARAMETER") bus: IEventBus) {
    init {
        BattlePhoneClient.onInitializeClient()
    }
}
