package domain

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import remote.RemoteService
import java.net.InetAddress

class MessageInteractor(private val remote : RemoteService) {

    private lateinit var me : User

    private val messages = mutableListOf<Message>()
    private lateinit var callback : (List<Message>) -> Unit
    private var connectedUser : User? = null

    fun init(name : String) {
        me = User(
            name,
            InetAddress.getLocalHost().hostAddress
        )
        CoroutineScope(Dispatchers.IO).launch {
            remote.start(me)
        }
        CoroutineScope(Dispatchers.Default).launch {
            remote.messages().consumeEach {
                messages.add(it)
                messages.sortBy(Message::number)
                callback(messages.toList())
            }
        }
    }

    fun waitForConnection() = runBlocking {
        connectedUser = withContext(Dispatchers.IO) {
            remote.getConnection()
        }
    }

    fun connect(to : String) = runBlocking {
        withContext(Dispatchers.IO) {
            remote.connect(to, me).also {
                connectedUser = it.data
            }
        }
    }

    fun disconnect() = runBlocking {
        remote.disconnect()
    }

    fun send(content : String) {
        require(connectedUser != null) {
            "You cant send messages before connection"
        }
        val message = Message(
            connectedUser!!,
            me,
            System.currentTimeMillis(),
            content
        )
        CoroutineScope(Dispatchers.Default).launch {
            proceedMessage(message, 5)
        }
    }

    private suspend fun proceedMessage(message : Message, limit : Int) {
        if (limit <= 0) {
            println("Resend limit reached")
            return
        }
        val res = withContext(Dispatchers.IO) {
            remote.send(message)
        }
        if (res.isSuccessful) {
            messages.add(message)
            messages.sortBy(Message::number)
            callback(messages)
        } else {
            println("Message error, retrying with limit: $limit")
            proceedMessage(message, limit - 1)
        }
    }


    fun subscribeMessageUpdates(callback : (List<Message>) -> Unit) {
        this.callback = callback
        callback(messages.toList())
    }

}