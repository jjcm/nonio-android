import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.*

class WebSocketHelper(
    private val url: String,
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val messageChannel = Channel<String>()

    suspend fun connect() {
        val request = Request.Builder().url(url).build()

        val listener =
            object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    println("WebSocket connected")
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String,
                ) {
                    println("Received: $text")
                    runBlocking {
                        messageChannel.send(text)
                    }
                }

                override fun onClosing(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    println("WebSocket closing: $reason")
                    webSocket.close(1000, null)
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?,
                ) {
                    println("WebSocket failure: ${t.message}")
                }
            }

        withContext(Dispatchers.IO) {
            webSocket = client.newWebSocket(request, listener)
        }
    }

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.IO) {
            webSocket?.send(message)
        }
    }

    suspend fun waitForCompletion(onProgress: (String) -> Unit): String {
        var lastMessage = ""
        for (message in messageChannel) {
            lastMessage = message
            onProgress(message) // 每次收到消息时回调
            if (message.contains("source:100")) {
                break
            }
        }
        return lastMessage
    }

    fun close() {
        webSocket?.cancel()
        //  webSocket?.close(1000, "Client closed connection")
        client.dispatcher.executorService.shutdown()
        messageChannel.cancel()
    }
}

fun main() =
    runBlocking {
        val webSocketHelper = WebSocketHelper("wss://your-websocket-url")

        // Connect to WebSocket
        webSocketHelper.connect()

        // Send a message to initiate progress (if needed)
        webSocketHelper.sendMessage("Start progress")

        // Wait for completion with progress callback
        val finalMessage =
            webSocketHelper.waitForCompletion { progress ->
                println("Progress: $progress")
            }

        println("Final message: $finalMessage")
        println("Progress complete. Continuing with other operations...")

        // Close the WebSocket
        webSocketHelper.close()
    }
