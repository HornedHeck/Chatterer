package remote

import com.google.gson.Gson
import domain.Message
import domain.UDPResult
import domain.User
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


private const val SIZE = 65000

class RemoteServiceImpl(private val port : Int = 8000, private val toPort : Int = port) : RemoteService {

    private val socket = DatagramSocket(port)

    private val gson = Gson()

    private val connectionChannel = Channel<User>(10) {
//        No action
    }

    private val messageChannel = Channel<Message>(Channel.UNLIMITED) {
//        No action
    }

    private val connectResultChannel = Channel<UDPResult<User>>(10) {
//        No action
    }

    private val messageResultChannel = Channel<UDPResult<Long>>(10) {
//        No action
    }

    override suspend fun start(me : User) {
        val data = ByteArray(SIZE)
        val packet = DatagramPacket(data, data.size)
        while (true) {
            socket.receive(packet)
            val header = gson.fromJson(String(packet.data, 0, packet.length), DatagramHeader::class.java)
            when {
                header.isMessage() -> {
                    val message = gson.fromJson(header.content, Message::class.java)
                    messageChannel.send(message)
                    val header = DatagramHeader(
                        me.address, CONNECT_RES, gson.toJson(
                            UDPResult(
                                true,
                                message.number
                            )
                        )
                    )
                    val bytes = gson.toJson(header).toByteArray()
                    socket.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName(header.from), toPort))
                }
                header.isConnect() -> {
                    val sendHeader = DatagramHeader(me.address, CONNECT_RES, gson.toJson(me))
                    val user = gson.fromJson(header.content, User::class.java)
                    socket.connect(InetAddress.getByName(header.from), toPort)

                    val bytes = gson.toJson(sendHeader).toByteArray()
                    socket.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName(header.from), toPort))
                    connectionChannel.send(user)
                    println("Internal: connection established")
                }
                header.isConnectResult() -> {
                    val user = gson.fromJson(header.content, User::class.java)
                    connectResultChannel.send(
                        UDPResult(true, user)
                    )
                }
                header.isMessageResult() -> {
                    messageResultChannel.send(gson.fromJson(header.content, UDPResult::class.java) as UDPResult<Long>)
                }
            }
        }
    }

    override suspend fun send(message : Message) : UDPResult<Long> {
        val header = DatagramHeader(message.from.address, MESSAGE, gson.toJson(message))
        val bytes = gson.toJson(header).toByteArray()
        socket.send(
            DatagramPacket(
                bytes, bytes.size, InetAddress.getByName(message.to.address), toPort
            )
        )
        return messageResultChannel
            .receiveAsFlow()
            .first { it.data == message.number }
    }

    override suspend fun connect(to : String, me : User) : UDPResult<User> = try {
        withTimeout(10000000) {
            val header = DatagramHeader(
                me.address,
                CONNECT,
                gson.toJson(me)
            )
            val bytes = gson.toJson(header).toByteArray()
            socket.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName(to), toPort))
            connectResultChannel.receive()
        }
    } catch (e : TimeoutCancellationException) {
        UDPResult(false, null)
    }

    override suspend fun getConnection() = connectionChannel.receive()

    override fun messages() : ReceiveChannel<Message> = messageChannel

    override suspend fun disconnect() {
        socket.disconnect()
    }
}