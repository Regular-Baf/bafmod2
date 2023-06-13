package baf.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

class Bafmod2Commands {
    init {
        CommandRegistrationCallback.EVENT.register { commandDispatcher: CommandDispatcher<ServerCommandSource>, _: CommandRegistryAccess, _: CommandManager.RegistrationEnvironment ->
            commandDispatcher.register(
                CommandManager.literal("local.ai")
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes { context ->
                            val message = StringArgumentType.getString(context, "message")
                            sendToAIChatbot(message)
                            1
                        })
            )
        }
    }

    private fun sendToAIChatbot(message: String) {
        val url = URL("http://localhost:8000/completion")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val postData = "message=$message"
        val postDataBytes = postData.toByteArray(StandardCharsets.UTF_8)
        connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
        connection.setRequestProperty("Content-Length", postDataBytes.size.toString())

        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.write(postDataBytes)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.lines().collect(Collectors.joining())
            reader.close()

            println("Received response: $response")
            // Process the response if needed
        } else {
            println("Failed to send the message. Response code: $responseCode")
            // Handle the error if needed
        }

        connection.disconnect()
    }
}
