package domain


data class Message(
    val to : User,
    val from : User,
    val number : Long,
    val content : String
)