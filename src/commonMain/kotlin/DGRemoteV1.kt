import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class DGRemoteV1 {
    companion object {
        private val msgMap = mapOf("1" to "再强点", "b" to "轻一点", "c" to "好舒服", "d" to "换一个")
    }
    class Controller(val auth: Auth, qrUrl: String) {
        val randomCode = qrUrl.split("#")[1]
        private var limitA = 0
        private var limitB = 0
        private var toID = ""
        private val sendArray = mutableListOf<Structure.NIM.V1.SendWaveAndStrength>()
        private val packedSendArray = mutableListOf<String>()
        private val msgArray = mutableListOf<Structure.FeelingMessage>()
        public fun joinControl(): Structure.Request {
            return Structure.Request(
                    headers = arrayOf(
                            Structure.Header("device", "android"),
                            Structure.Header("token", auth.getToken()),
                            Structure.Header("la", "0")
                    ),
                    url = "${Structure.baseUrl}/joinControl",
                    method = "POST",
                    body = "randomCode=$randomCode",
                    contentType = "application/x-www-form-urlencoded",
                    requestCode = 0
            )
        }

        public fun getNIMConnect(): Structure.NIMAuth {
            return Structure.NIMAuth(
                    appKey = Structure.appKey,
                    account = auth.getUUID(),
                    token = auth.getToken()
            )
        }

        public fun connect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(
                            Structure.NIM.V1.Join(
                                    content = Structure.NIM.V1.JoinDetail(auth.getUUID())
                            )
                    )
            )
        }

        public fun disconnect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(
                            Structure.NIM.V1.Disconnect(
                              content = Structure.NIM.V1.DisconnectDetail()
                            )
                    )
            )
        }

        public fun heartbeat(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = "998"
            )
        }

        public fun prepareSend(channel: Int, strength: Int, bytes: String) {
            if (strength < 0) return
            if (channel == 1 && strength > limitA) return
            if (channel == 2 && strength > limitB) return
            sendArray.add(Structure.NIM.V1.SendWaveAndStrength(channel, bytes, strength))
        }

        public fun sendTick() {
            val msg = Json.encodeToString((0..6).map {
                sendArray.removeFirstOrNull()
            }.filterNotNull())
            packedSendArray.add(Json.encodeToString(Structure.NIM.V1.WaveStrengthMsg(msg)))
        }

        public fun clearSend() {
            sendArray.clear()
            packedSendArray.clear()
        }

        public fun getSend() : Structure.NIMMessage? {
            val wave = packedSendArray.removeFirstOrNull() ?: return null
            return Structure.NIMMessage(
                    toID = toID,
                    msg = wave
            )
        }

        public fun clearMessage() {
            msgArray.clear()
        }

        public fun countMessage() = msgArray.size

        public fun getMessage() : Structure.FeelingMessage? {
            return msgArray.removeFirstOrNull()
        }

        public fun processNIM(data: String) : Structure.FeelingMessage? {
            return when(data) {
                "a1", "a2", "a3", "a4", "b1", "b2", "b3", "b4" -> {
                    val channel = if (data.startsWith("a")) 1 else 2
                    val msg = msgMap[data.last().toString()] ?: error("")
                    Structure.FeelingMessage(channel, msg)
                }
                else -> null
            }
        }

        public fun process(data: String, requestCode: Int) {
            when (requestCode) {
                0 -> {
                    val join = Json.decodeFromString<Structure.Response.JoinControl>(data)
                    if (join.code == 200) {
                        limitA = join.strengthA
                        limitB = join.strengthB
                        toID = join.toID
                    }
                }
            }
        }
    }

    class Controlled(val auth: Auth, var strengthA: Int, var strengthB: Int, val limitA: Int, val limitB: Int) {
        private var randomCode = ""
        private var toID = ""
        public fun requestControl(): Structure.Request {
            return Structure.Request(
                    headers = arrayOf(
                            Structure.Header("device", "android"),
                            Structure.Header("token", auth.getToken()),
                            Structure.Header("la", "0")
                    ),
                    url = "${Structure.baseUrl}/getIMConnectCode",
                    method = "POST",
                    body = "type=1&strengthA=$limitA&strengthB=$limitB&limited=0",
                    contentType = "application/x-www-form-urlencoded",
                    requestCode = 0
            )
        }

        public fun logoutControl(): Structure.Request {
            return Structure.Request(
                    headers = arrayOf(
                            Structure.Header("device", "android"),
                            Structure.Header("token", auth.getToken()),
                            Structure.Header("la", "0")
                    ),
                    url = "${Structure.baseUrl}/accidentalIMLogOut",
                    method = "POST",
                    body = "a=0",
                    contentType = "application/x-www-form-urlencoded",
                    requestCode = 1
            )
        }

        public fun getQrUrl(): String? {
            return if (randomCode.isNotEmpty()) {
                "${Structure.qrUrl}#$randomCode"
            } else {
                null
            }
        }

        public fun getNIMConnect(): Structure.NIMAuth {
            return Structure.NIMAuth(
                    appKey = Structure.appKey,
                    account = auth.getUUID(),
                    token = auth.getToken()
            )
        }

        public fun isOnline() = toID.isNotEmpty()

        public fun sendFeeling(channel: Int, feeling: Int) : Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = "${if(channel == 1) "a" else "b"}$feeling"
            )
        }

        public fun processNIM(data: String) : Array<Structure.NIM.V1.SendWaveAndStrength>? {
            val jsonData = Json.decodeFromString<JsonElement>(data)
            if (jsonData.jsonObject.containsKey("msg")) {
                val wave = jsonData.jsonObject["msg"]!!.jsonPrimitive.content
                val array = Json.decodeFromString<Array<Structure.NIM.V1.SendWaveAndStrength>>(wave)
                return array.mapNotNull {
                    when (it.channel) {
                        1 -> {
                            if (it.strength <= limitA)
                                Structure.NIM.V1.SendWaveAndStrength(1, it.bytes, strengthA + it.strength)
                            null
                        }
                        2 -> {
                            if (it.strength <= limitB)
                                Structure.NIM.V1.SendWaveAndStrength(2, it.bytes, strengthB + it.strength)
                            null
                        }
                        else -> {
                            null
                        }
                    }
                }.toTypedArray()
            } else {
                if (jsonData.jsonPrimitive.isString) {
                    if (jsonData.jsonPrimitive.content == "998") {
                        return null
                    }
                }
                val content = jsonData.jsonObject["content"]
                if (content?.jsonObject?.get("status")?.jsonPrimitive?.int == 2) {
                    toID = ""
                } else if (content?.jsonObject?.get("status")?.jsonPrimitive?.int == 3) {
                    toID = content.jsonObject["id"]!!.jsonPrimitive.content
                }
                return null
            }
        }

        public fun process(data: String, requestCode: Int) {
            when (requestCode) {
                0 -> {
                    val code = Json.decodeFromString<Structure.Response.IMConnectCode>(data)
                    if (code.code == 200) {
                        this.randomCode = code.randomcode
                    }
                }
                1 -> {
                    randomCode = ""
                }
            }
        }
    }
}