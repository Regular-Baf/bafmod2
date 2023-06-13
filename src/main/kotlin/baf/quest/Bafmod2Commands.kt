package baf.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Bafmod2Commands {
    init {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { commandDispatcher: CommandDispatcher<ServerCommandSource>, commandRegistryAccess: CommandRegistryAccess, registrationEnvironment: CommandManager.RegistrationEnvironment ->
            commandDispatcher.register(CommandManager.literal("local.ai")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes { context ->
                        val message = StringArgumentType.getString(context, "message")
                        sendToAIChatbot(message)
                        1
                    }))
        })
    }

    private fun sendToAIChatbot(message: String) {
        val url = URL("http://localhost:8000/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val postData = "message=${URLEncoder.encode(message, "UTF-8")}"
        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.writeBytes(postData)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            println("Message sent successfully!")
            // Request successful
            // Handle the response if needed
        } else {
            println("Failed to send the message. Response code: $responseCode")
            // Request failed
            // Handle the error if needed
        }
    }
}
