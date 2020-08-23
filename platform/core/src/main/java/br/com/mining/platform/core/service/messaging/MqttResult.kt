package br.com.mining.platform.service.messaging

import br.com.mining.platform.service.messaging.enums.MqttState

data class MqttResult(
    var state: MqttState,
    var message: String = ""
)