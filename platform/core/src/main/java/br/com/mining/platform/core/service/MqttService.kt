package br.com.mining.platform.service

import android.os.Environment
import br.com.mining.platform.service.messaging.MqttResult
import br.com.mining.platform.service.messaging.enums.MqttState
import br.com.mining.platform.service.messaging.listeners.MqttMessageListener
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import java.io.File

class MqttService(
    val mqttMessageCallback: MqttMessageListener
) : MqttCallbackExtended {

    companion object {
        private const val QOS_0 = 0
        private const val QOS_1 = 1
        private const val QOS_2 = 2
        private const val TIME_WAIT = 5000
    }

    private val FILE = "${File.separator}mining${File.separator}platform${File.separator}"
    private var mqttClient: MqttClient? = null

    fun start(clientId: String, host: String, port: String, userName: String, password: String) {
        connect(clientId, host, port, userName, password)
    }

    fun publish(message: ByteArray, topic: String) {
        try {
            if (mqttClient?.isConnected == true) {
                val mqttMessage = MqttMessage(message)
                mqttMessage.qos = QOS_1
                mqttMessage.isRetained = false

                // Wait until the method 'deliveryComplete' is called
                mqttClient?.publish(topic, mqttMessage)
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.PUBLISH))
            } else {
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.PUBLISH_ERROR))
            }
        } catch (ex: Exception) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.PUBLISH_ERROR, ex.message ?: ""))
        }
    }

    fun subscribe(topic: String?) {
        try {
            if (mqttClient?.isConnected == true) {
                mqttClient?.subscribe(topic, QOS_1)
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.SUBSCRIBE))
            } else {
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.SUBSCRIBE_ERROR))
            }
        } catch (ex: Exception) {
            mqttMessageCallback.onUpdateState(
                MqttResult(
                    MqttState.SUBSCRIBE_ERROR,
                    ex.message ?: ""
                )
            )
        }
    }

    fun unsubscribe(topic: String?) {
        try {
            mqttClient?.unsubscribe(topic)
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.UNSUBSCRIBE))
        } catch (ex: MqttException) {
            mqttMessageCallback.onUpdateState(
                MqttResult(
                    MqttState.UNSUBSCRIBE_ERROR,
                    ex.message ?: ""
                )
            )
        }
    }

    fun reconnect() {
        try {
            if (mqttClient?.isConnected == true) {
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.CONECTED))
            } else {
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.RECONNECT))
                mqttClient?.reconnect()
            }
        } catch (e: MqttException) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.DISCONNECTED, e.message ?: ""))
        }
    }

    fun close() {
        try {
            if (mqttClient != null) {
                disconnect()
                mqttClient?.close(true)
                mqttClient = null
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.CLOSE))
            }
        } catch (ex: Exception) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.ERROR, ex.message ?: ""))
        }
    }

    fun disconnect() {
        try {
            if (mqttClient != null) {
                mqttClient?.disconnect()
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.DISCONNECTED))
            }
        } catch (ex: Exception) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.ERROR, ex.message ?: ""))
        }
    }

    private fun configureClient(clientId: String, serverUri: String) {
        try {
            mqttClient = MqttClient(serverUri, clientId, createPersistence())
            mqttClient?.timeToWait = TIME_WAIT.toLong()
            mqttClient?.setCallback(this)
        } catch (ex: MqttException) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.ERROR, ex.message ?: ""))
        }
    }

    private fun createPersistence(): MqttClientPersistence {
        return MqttDefaultFilePersistence(
            Environment.getExternalStorageDirectory()
                .toString() + FILE
        )
    }

    private fun connect(
        clientId: String, host: String, port: String, userName: String,
        password: String
    ) {
        val serverUri = "tcp://$host:$port"
        try {
            if (mqttClient == null) {
                mqttMessageCallback.onUpdateState(MqttResult(MqttState.CONECTING))
                configureClient(clientId, serverUri)
                val mqttConnectOptions = MqttConnectOptions()
                mqttConnectOptions.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
                mqttConnectOptions.isAutomaticReconnect = true
                mqttConnectOptions.isCleanSession = false
                mqttConnectOptions.userName = userName
                mqttConnectOptions.password = password.toCharArray()
                mqttConnectOptions.keepAliveInterval = 20

                //connect
                mqttClient?.connect(mqttConnectOptions)
            }
        } catch (ex: MqttException) {
            mqttMessageCallback.onUpdateState(MqttResult(MqttState.DISCONNECTED, ex.message ?: ""))
        }
    }

    // Init MqttCallback
    override fun connectionLost(cause: Throwable) {
        mqttMessageCallback.onUpdateState(MqttResult(MqttState.DISCONNECTED))
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        mqttMessageCallback.onMessageArrived(message.payload, topic)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String) {
        mqttMessageCallback.onUpdateState(MqttResult(MqttState.CONECTED))
    }
    // Finish MqttCallback

}