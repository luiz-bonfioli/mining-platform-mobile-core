package br.com.mining.platform.core.service.messaging

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.*
import br.com.mining.platform.service.MqttService
import br.com.mining.platform.service.messaging.MqttConstants
import br.com.mining.platform.service.messaging.MqttResult
import br.com.mining.platform.service.messaging.listeners.MqttMessageListener
import java.nio.ByteBuffer

class CommunicationService : Service(), MqttMessageListener {

    private val mqttService: MqttService by lazy {
        MqttService(this)
    }
    private val messenger: Messenger by lazy {
        Messenger(IncomingHandler())
    }
    private var clientMessenger: Messenger? = null


    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    override fun onMessageArrived(message: ByteArray, topic: String) {
        try {
            val msgBundle = Message.obtain(null, MqttConstants.MESSAGE_ARRIVED)
            val bundle = Bundle()
            bundle.putCharArray(MqttConstants.TOPIC, topic.toCharArray())
            bundle.putByteArray(MqttConstants.PAYLOAD, message)
            msgBundle.data = bundle
            clientMessenger?.send(msgBundle)
        } catch (ex: TransactionTooLargeException) {
            ex.printStackTrace()
            sendError(message, topic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpdateState(mqttResult: MqttResult) {
        try {
            val msgBundle = Message.obtain(null, MqttConstants.MESSAGE_STATE)
            val bundle = Bundle()
            bundle.putString(MqttConstants.STATE, mqttResult.state.name)
            msgBundle.data = bundle
            clientMessenger?.send(msgBundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendError(message: ByteArray, topic: String) {
        try {
            val ERROR: Byte = 0x52
            val msg = "Error: TransactionTooLargeException"

            val payloadBuffer = ByteBuffer.wrap(message)
            val moduleId = payloadBuffer.get()
            val eventId = payloadBuffer.get()

            val payload = ByteBuffer.allocate(3 + msg.toByteArray().size)
            payload.put(moduleId)
            payload.put(eventId)
            payload.put(ERROR)
            payload.put(msg.toByteArray())
            val msgBundle = Message.obtain(null, MqttConstants.MESSAGE_ARRIVED)
            val bundle = Bundle()
            bundle.putCharArray(MqttConstants.TOPIC, topic.toCharArray())
            bundle.putByteArray(MqttConstants.PAYLOAD, payload.array())
            msgBundle.data = bundle
            clientMessenger?.send(msgBundle)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @SuppressLint("HandlerLeak")
    private inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MqttConstants.CONNECT -> {
                    val bundle = msg.data
                    val host = bundle.getCharArray(MqttConstants.HOST)?.let { String(it) } ?: ""
                    val port = bundle.getCharArray(MqttConstants.PORT)?.let { String(it) } ?: ""
                    val user = bundle.getCharArray(MqttConstants.USER)?.let { String(it) } ?: ""
                    val pass = bundle.getCharArray(MqttConstants.PASS)?.let { String(it) } ?: ""
                    val clientId =
                        bundle.getCharArray(MqttConstants.CLIENTID)?.let { String(it) } ?: ""
                    //set client messanger
                    clientMessenger = msg.replyTo
                    //connect mqtt
                    mqttService.start(clientId, host, port, user, pass)
                }
                MqttConstants.SUBSCRIBE -> {
                    val bundle = msg.data
                    val topic = bundle.getCharArray(MqttConstants.TOPIC)?.let { String(it) } ?: ""
                    mqttService.subscribe(topic)
                }
                MqttConstants.PUBLISH -> {
                    val bundle = msg.data
                    val topic = bundle.getCharArray(MqttConstants.TOPIC)?.let { String(it) } ?: ""
                    val payload = bundle.getByteArray(MqttConstants.PAYLOAD) ?: byteArrayOf()
                    mqttService.publish(payload, topic)
                }
                MqttConstants.UNSUBSCRIBE -> {
                    val bundle = msg.data
                    val topic = bundle.getCharArray(MqttConstants.TOPIC)?.let { String(it) } ?: ""
                    mqttService.unsubscribe(topic)
                }
                MqttConstants.RECONNECT ->
                    mqttService.reconnect()
                MqttConstants.CLOSE ->
                    mqttService.close()
            }
        }
    }
}




