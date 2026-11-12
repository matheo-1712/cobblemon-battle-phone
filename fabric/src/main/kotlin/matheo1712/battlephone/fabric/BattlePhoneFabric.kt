package matheo1712.battlephone.fabric

import matheo1712.battlephone.BattlePhone
import matheo1712.battlephone.client.BattlePhoneClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer

/**
 * Fabric registers immediately, through Cobblemon Trainers' platform, so the order against that
 * mod's own initializer does not matter: everything here only has to happen before the
 * registries freeze.
 */
class BattlePhoneFabric : ModInitializer {
    override fun onInitialize() = BattlePhone.onInitialize()
}

class BattlePhoneFabricClient : ClientModInitializer {
    override fun onInitializeClient() = BattlePhoneClient.onInitializeClient()
}
