package baf.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

class Bafmod2Commands {
    init {
        createConfigFileIfNeeded()

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
        val configPath = "./config/Bafmod2Config.json"
        val configFile = File(configPath)

        if (!configFile.exists()) {
            println("Config file not found: $configPath")
            return
        }

        val configFileContents = configFile.readText()
        val configJson = Gson().fromJson(configFileContents, JsonObject::class.java)
        val apiUrl = configJson.getAsJsonPrimitive("apiUrl")?.asString

        if (apiUrl == null) {
            println("apiUrl not found in the config file")
            return
        }

        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val postData = """
        {
            "prompt": "$message",
            "stream": true
        }
    """.trimIndent()

        val postDataBytes = postData.toByteArray(StandardCharsets.UTF_8)
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Content-Length", postDataBytes.size.toString())

        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.write(postDataBytes)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseStringBuilder = StringBuilder()

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String? = reader.readLine()

            while (line != null) {
                if (line.startsWith("data: ")) {
                    val responseData = line.substring("data: ".length)
                    println("Received response data: $responseData")

                    if (responseData == "[DONE]") {
                        // Handle the completion of the response stream
                        break
                    }

                    try {
                        val jsonResponse = JsonParser.parseString(responseData).asJsonObject
                        val choicesArray = jsonResponse.getAsJsonArray("choices")

                        if (choicesArray != null && choicesArray.size() > 0) {
                            val choice = choicesArray[0].asJsonObject
                            val choiceText = choice.getAsJsonPrimitive("text").asString
                            responseStringBuilder.append(choiceText).append(" ")
                        }
                    } catch (e: Exception) {
                        println("Error processing the API response: ${e.message}")
                    }
                }

                line = reader.readLine()
            }

            reader.close()

            val responseString = responseStringBuilder.toString()
            println("Response: $responseString")

            val minecraftClient = MinecraftClient.getInstance()
            val chatText: Text = Text.of("[local.ai] $responseString")
            minecraftClient.inGameHud.chatHud.addMessage(chatText)

            println("Message successfully sent to the API.")
        } else {
            println("Failed to send the message. Response code: $responseCode")
            // Handle the error if needed
        }

        connection.disconnect()
    }

    private fun createConfigFileIfNeeded() {
        val configPath = "./config/Bafmod2Config.json"
        val configFile = File(configPath)

        if (!configFile.exists()) {
            val defaultConfigContent = """
                {
                  "apiUrl": "http://localhost:8000/completions"
                }
            """.trimIndent()

            try {
                FileWriter(configFile).use { writer ->
                    writer.write(defaultConfigContent)
                }
                println("Config file created: $configPath")
            } catch (e: Exception) {
                println("Failed to create the config file: $configPath")
                e.printStackTrace()
            }
        }
    }
}
