package remote

data class DatagramHeader(
    val from : String,
    val type : String,
    val content : String
) {

    fun isMessage() = type == MESSAGE

    fun isConnect() = type == CONNECT

    fun isConnectResult() = type == CONNECT_RES

    fun isMessageResult() = type == MESSAGE_RES

}

const val MESSAGE = "Message"
const val MESSAGE_RES = "MessageResult"
const val CONNECT = "Connect"
const val CONNECT_RES = "ConnectResult"
