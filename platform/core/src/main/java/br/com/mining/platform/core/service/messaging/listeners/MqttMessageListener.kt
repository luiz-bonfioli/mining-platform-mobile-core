package br.com.mining.platform.service.messaging.listeners

import br.com.mining.platform.service.messaging.MqttResult

interface MqttMessageListener {
    fun onMessageArrived(message: ByteArray, topic: String)

    fun onUpdateState(mqttResult: MqttResult)
}