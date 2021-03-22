import domain.Message
import domain.MessageInteractor
import remote.RemoteServiceImpl
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

fun updateMessages(messages : List<Message>) {
    clearScreen()
    messages.forEach {
        val instant = Instant.ofEpochMilli(it.number)
        val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        println("${date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} ${it.from.name} -> ${it.to.name}:")
        println(it.content)
        println()
    }
}


fun main() {
    println("Enter ports")
    val p1 = readLine()!!.toInt()
    val p2 = readLine()!!.toInt()
    val remote = RemoteServiceImpl(p1, p2)
    val interactor = MessageInteractor(remote)

    println("Enter name")
    val name = readLine() ?: "Name"
    interactor.init(name)
    interactor.subscribeMessageUpdates(::updateMessages)

    connectSeq(interactor)

    while (true) {

        val message = readLine()
        if (message == "EXIT") {
            exitProcess(0)
        }
        if (message == "DISCONNECT") {
            interactor.disconnect()
            connectSeq(interactor)
        }

        message?.let(interactor::send)
    }

}

private fun connectSeq(interactor : MessageInteractor) {
    println("Enter address to connect or space to wait connection")
    val address = readLine()?.trim()

    if (address.isNullOrBlank()) {
        println("Waiting for connection ... ")
        interactor.waitForConnection()
    } else {
        interactor.connect(address)
    }
    println("Connected")
}

fun clearScreen() {
    print("\u001b[H\u001b[2J")
    System.out.flush()
}