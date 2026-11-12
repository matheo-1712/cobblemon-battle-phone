package matheo1712.battlephone

import matheo1712.cobbletrainers.block.TrainerBlocks

/**
 * Cobblemon BattlePhone: the progress tracker of Cobblemon Trainers, as a mod built on it.
 *
 * Everything the phone shows comes from Cobblemon Trainers - the trainers, the victories, the
 * requirements, the rewards, the skins. This mod only owns the item, the screen and the packets
 * that carry a listing to it.
 *
 * It goes through Cobblemon Trainers' own platform layer
 * ([matheo1712.cobbletrainers.platform.Platform]) for registries and packets, so the code below
 * is the same on both loaders; the loader entrypoints only decide *when* it runs.
 */
object BattlePhone {

    /** Items, packets and the creative tab entries. */
    fun onInitialize() {
        BattlePhoneItems.register()
        BattlePhoneNetworking.register()
        // Listed before the spawner in Cobblemon Trainers' tab, where they have always been.
        TrainerBlocks.addToTab(0, BattlePhoneItems.BATTLE_PHONES)
        BattlePhoneMod.LOGGER.info("Cobblemon BattlePhone initialized")
    }
}
