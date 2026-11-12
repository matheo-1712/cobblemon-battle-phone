package matheo1712.battlephone

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Who this mod is: its ID, its logger, and the two helpers every file reaches for.
 *
 * One ID on both loaders. NeoForge refuses a hyphen in a mod ID, Fabric accepts an underscore,
 * so the underscore form is the only one that can be the same everywhere - unlike Cobblemon
 * Trainers, which predates the NeoForge port and lives with two.
 *
 * The phones themselves keep their `cobblemon-trainers:` item IDs (see [BattlePhoneItems]):
 * that is the one place where this mod writes in another mod's namespace, and it is for the
 * phones already sitting in players' inventories.
 */
object BattlePhoneMod {

    const val MOD_ID = "cobblemon_battlephone"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    fun id(path: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

    /** A translatable component under this mod's prefix, from `assets/cobblemon_battlephone/lang/`. */
    fun lang(key: String, vararg args: Any): MutableComponent =
        Component.translatable("$MOD_ID.$key", *args)
}
