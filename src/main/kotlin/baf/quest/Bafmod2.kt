package baf.quest

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.village.VillagerProfession
import net.minecraft.world.World

class Bafmod2 : ModInitializer {
    override fun onInitialize() {
        UseEntityCallback.EVENT.register(UseEntityCallback { player, world, hand, entity, hitResult ->
            if (player != null && hand == Hand.MAIN_HAND && entity is VillagerEntity) {
                if (entity.villagerData.profession == VillagerProfession.NITWIT) {
                    return@UseEntityCallback onInteractWithNitwitVillager(player, hand, world, entity, hitResult?.pos as? BlockPos ?: BlockPos.ORIGIN)
                }
            }
            ActionResult.PASS
        })

        // Create an instance of Bafmod2Commands
        val bafmod2Commands = Bafmod2Commands()
    }

    private fun onInteractWithNitwitVillager(player: PlayerEntity, hand: Hand, world: World, entity: VillagerEntity, pos: BlockPos): ActionResult {
        if (!world.isClient) {
            val screenHandlerFactory = object : NamedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, playerEntity: PlayerEntity): NitwitVillagerScreenHandler {
                    return NitwitVillagerScreenHandler(syncId, playerInventory, entity)
                }

                override fun getDisplayName(): Text {
                    return Text.of("Nitwit Villager")
                }
            }

            player.openHandledScreen(screenHandlerFactory)
        }

        return ActionResult.SUCCESS
    }
}
