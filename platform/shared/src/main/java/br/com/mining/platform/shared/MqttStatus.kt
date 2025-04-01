package br.com.mining.platform.shared

import java.util.*

enum class MqttStatus {
    INIT, CONECTED, CONECTING, DISCONNECTED, ERROR, SUBSCRIBE_ERROR, SUBSCRIBE, PUBLISH_ERROR,
    PUBLISH, UNSUBSCRIBE, UNSUBSCRIBE_ERROR, RECONNECT, CLOSE;

    fun isError(status: MqttStatus): Boolean {
        return Arrays.asList(
            DISCONNECTED,
            ERROR,
            SUBSCRIBE_ERROR,
            PUBLISH_ERROR,
            UNSUBSCRIBE_ERROR,
            CLOSE
        ).contains(status)
    }

    fun isOnline(status: MqttStatus): Boolean {
        return Arrays.asList(
            CONECTED,
            SUBSCRIBE,
            PUBLISH,
            UNSUBSCRIBE
        ).contains(status)
    }
}