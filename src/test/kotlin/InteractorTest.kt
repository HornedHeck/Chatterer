import domain.MessageInteractor
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import remote.RemoteServiceImpl

class InteractorTest {

    val remote1 = RemoteServiceImpl(8000, 9000)
    val remote2 = RemoteServiceImpl(9000, 8000)


    @Test
    fun connectionTest() {
        val interactor1 = MessageInteractor(remote1)
        interactor1.init("Name-1")
        val interactor2 = MessageInteractor(remote2)
        interactor2.init("Name-2")
        val waitJob = CoroutineScope(Dispatchers.IO).launch {
            interactor1.waitForConnection().let {
                println("Connected: $it")
            }
            cancel()
        }

        runBlocking {
            assert(interactor2.connect("127.0.0.1").isSuccessful)
            waitJob.join()
        }
    }

}