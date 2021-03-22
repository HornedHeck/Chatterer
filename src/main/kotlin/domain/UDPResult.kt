package domain

data class UDPResult<T>(
    val isSuccessful : Boolean,
    val data : T?
)

