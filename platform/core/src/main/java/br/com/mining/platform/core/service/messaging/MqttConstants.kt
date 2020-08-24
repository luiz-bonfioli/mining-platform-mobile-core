package br.com.mining.platform.service.messaging

object MqttConstants {
    val CLIENTID = "CLIENTID"

    val HOST = "HOST"

    val PORT = "PORT"

    val USER = "USER"

    val PASS = "PASS"

    val PAYLOAD = "PAYLOAD"

    val TOPIC = "TOPIC"

    val STATE = "STATE"

    val CONNECT = 0

    val MESSAGE_ARRIVED = 1

    val SUBSCRIBE = 2

    val PUBLISH = 3

    val UNSUBSCRIBE = 4

    val RECONNECT = 5

    val MESSAGE_STATE = 6

    val CLOSE = 7
}