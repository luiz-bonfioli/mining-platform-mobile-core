package br.com.mining.platform.service.messaging.enums

enum class MqttState {
    CONECTED, CONECTING, DISCONNECTED, ERROR, SUBSCRIBE_ERROR, SUBSCRIBE,  PUBLISH_ERROR, PUBLISH,
    UNSUBSCRIBE, UNSUBSCRIBE_ERROR, RECONNECT, CLOSE;
}