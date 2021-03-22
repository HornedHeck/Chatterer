import domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import remote.RemoteService
import remote.RemoteServiceImpl


class RemoteTests {

    private val remote1 : RemoteService = RemoteServiceImpl(8000, 9000)

    private val remote2 : RemoteService = RemoteServiceImpl(9000, 8000)

    @Test
    fun connectionTest() {
        val user1 = User("U1", "127.0.0.1")
        val user2 = User("U2", "127.0.0.1")


        CoroutineScope(Dispatchers.IO).launch {
            remote1.start(user1)
        }
        CoroutineScope(Dispatchers.IO).launch {
            remote2.start(user2)
        }
        runBlocking {
            val result = remote1.connect(user2.address, user1)
            assert(result.isSuccessful)
            assertNotNull(remote2.getConnection())
        }

    }

    @Test
    fun singleConnectionOnlyTest() {
        val user1 = User("U1", "127.0.0.1")
        val user2 = User("U2", "127.0.0.1")


        CoroutineScope(Dispatchers.IO).launch {
            remote1.start(user1)
        }
        CoroutineScope(Dispatchers.IO).launch {
            remote2.start(user2)
        }
        runBlocking {
            val result = remote1.connect(user2.address, user1)
            val result2 = remote1.connect(user2.address, user1)
            assert(result.isSuccessful)
            assert(!result2.isSuccessful)
            assertNotNull(remote2.getConnection())
        }
    }

}