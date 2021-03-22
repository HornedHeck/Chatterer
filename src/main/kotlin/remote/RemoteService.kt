package remote

import domain.Message
import domain.UDPResult
import domain.User
import kotlinx.coroutines.channels.ReceiveChannel

interface RemoteService {

    suspend fun start(me : User)

    suspend fun send(message : Message) : UDPResult<Long>

    suspend fun connect(to : String, me : User) : UDPResult<User>

    suspend fun disconnect()

    suspend fun getConnection() : User

    fun messages() : ReceiveChannel<Message>

}