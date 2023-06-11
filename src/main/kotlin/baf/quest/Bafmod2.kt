package baf.quest

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.village.VillagerProfession

object Bafmod2 : ModInitializer {
    private val eventHandledPlayers = mutableSetOf<ServerPlayerEntity>()
    private val packetId = Identifier("bafmod2", "player_chat")
    private const val LISTEN_DURATION = 400
    private val villagerListenTicks = mutableMapOf<ServerPlayerEntity, Int>()

    override fun onInitialize() {
        // Register the event that triggers when a player joins the server
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, sender, _ ->
            val packetId = Identifier("bafmod2", "trigger_villager_greeting")
            ServerPlayNetworking.registerReceiver(handler, packetId) { _, _, _, _, _ ->
                val serverPlayer = handler.player
                val playerName = serverPlayer?.name?.string
                // Send a response to the player if their name is available and they are in the villagerListenTicks map
                if (serverPlayer != null && playerName != null && villagerListenTicks.containsKey(serverPlayer)) {
                    val response = Text.of("Yo! My man, $playerName")
                    serverPlayer.sendMessage(response, false)
                }
            }
        })

        // Register the event that triggers at the end of each server tick
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { _ ->
            // Decrement the listen duration for each player in villagerListenTicks map and remove players whose listen duration has expired
            val iterator = villagerListenTicks.entries.iterator()
            while (iterator.hasNext()) {
                val (player, ticks) = iterator.next()
                if (ticks > 0) {
                    villagerListenTicks[player] = ticks - 1
                } else {
                    iterator.remove()
                }
            }
            // Clear the set of eventHandledPlayers
            eventHandledPlayers.clear()
        })

        // Register the event that triggers when a player interacts with an entity
        UseEntityCallback.EVENT.register(UseEntityCallback { player, _, _, entity, _ ->
            // Handle the interaction if the entity is a Nitwit Villager and the player has not already been handled
            if (entity is VillagerEntity && entity.villagerData.profession == VillagerProfession.NITWIT && player is ServerPlayerEntity && !eventHandledPlayers.contains(player)) {
                val villagerName = entity.customName?.string
                val message = Text.of("[${villagerName}] Hi!")
                player.sendMessage(message, false)

                // Trigger the villager greeting by sending a packet to the player
                val packetId = Identifier("bafmod2", "trigger_villager_greeting")
                val packet = PacketByteBufs.empty()
                ServerPlayNetworking.send(player, packetId, packet)

                // Add the player to the eventHandledPlayers set and set the listen duration for the player
                eventHandledPlayers.add(player)
                villagerListenTicks[player] = LISTEN_DURATION

                return@UseEntityCallback ActionResult.SUCCESS
            }

            ActionResult.PASS
        })

        // Register the global network receiver for handling player chat packets
        ServerPlayNetworking.registerGlobalReceiver(packetId) { server, player, _, buf, _ ->
            // Read the message from the packet and the player's name
            val message = buf.readString()
            val playerName = player.name.string

            // Find the name of the first player in the eventHandledPlayers set
            val villagerName = eventHandledPlayers.firstOrNull()?.name?.string
            // Send a response to the player if the villager name is found in the message
            if (villagerName != null && message.contains(villagerName, ignoreCase = true)) {
                val response = Text.of("Yo! My man, $playerName")
                val responsePacket = PacketByteBufs.create()
                responsePacket.writeText(response)
                ServerPlayNetworking.send(player, packetId, responsePacket)
            }
        }
    }
}
