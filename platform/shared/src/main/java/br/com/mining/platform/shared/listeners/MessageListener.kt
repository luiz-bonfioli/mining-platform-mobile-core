package br.com.mining.platform.shared.listeners


interface MessageListener {
    fun onMessageArrived(message: ByteArray, eventId: Byte, topic: String)
}
