import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.js.JsName


class DGRemoteV1 {
    class Controller(val auth: Auth, qrUrl: String) {
        val randomCode = qrUrl.split("#")[1]
        private var limitA = 0
        private var limitB = 0
        private var toID = ""
        private val sendArray = mutableListOf<Structure.NIM.V1.SendWaveAndStrength>()
        private val packedSendArray = mutableListOf<String>()

        @JsName("joinControl")
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

        @JsName("getNIMConnect")
        public fun getNIMConnect(): Structure.NIMAuth {
            return Structure.NIMAuth(
                    appKey = Structure.appKey,
                    account = auth.getUUID(),
                    token = auth.getToken()
            )
        }

        @JsName("connect")
        public fun connect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(
                            Structure.NIM.V1.Join(
                                code = 200,
                                content = Json.encodeToString(Structure.NIM.V1.JoinDetail(auth.getUUID(), 3)),
                                type = 1
                            )
                    )
            )
        }

        @JsName("disconnect")
        public fun disconnect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(
                            Structure.NIM.V1.Disconnect(
                                code = 200,
                                content = Json.encodeToString(Structure.NIM.V1.DisconnectDetail(2)),
                                type = 1
                            )
                    )
            )
        }

        @JsName("getLimit")
        public fun getLimit() : Pair<Int, Int> {
            return Pair(limitA, limitB)
        }

        @JsName("canOnline")
        public fun canOnline() = toID.isNotEmpty()

        @JsName("heartbeat")
        public fun heartbeat(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = "998"
            )
        }

        @JsName("prepareSend")
        public fun prepareSend(channel: Int, strength: Int, bytes: String) {
            if (strength < 0) return
            if (channel == 1 && strength > limitA) return
            if (channel == 2 && strength > limitB) return
            sendArray.add(Structure.NIM.V1.SendWaveAndStrength(channel, bytes, strength))
            sendPack()
        }

        @JsName("forcePack")
        public fun forcePack() {
            val msg = Json.encodeToString((0..6).mapNotNull {
                sendArray.removeFirstOrNull()
            })
            packedSendArray.add(Json.encodeToString(Structure.NIM.V1.WaveStrengthMsg(msg)))
        }

        public fun sendPack() {
            if (sendArray.size < 6) return
            val msg = Json.encodeToString((0..6).mapNotNull {
                sendArray.removeFirstOrNull()
            })
            packedSendArray.add(Json.encodeToString(Structure.NIM.V1.WaveStrengthMsg(msg)))
        }

        @JsName("clearSend")
        public fun clearSend() {
            sendArray.clear()
            packedSendArray.clear()
        }

        @JsName("getSend")
        public fun getSend() : Structure.NIMMessage? {
            val wave = packedSendArray.removeFirstOrNull() ?: return null
            return Structure.NIMMessage(
                    toID = toID,
                    msg = wave
            )
        }

        @JsName("processNIM")
        public fun processNIM(data: String) : Structure.FeelingMessage? {
            val jsonData = Json.decodeFromString<JsonElement>(data)
            val from = jsonData.jsonObject["from"]!!.jsonPrimitive.content
            if (toID.isNotEmpty() && from != toID) return null
            val text = jsonData.jsonObject["text"]!!.jsonPrimitive.content
            return when(text) {
                "a1" -> Structure.FeelingMessage(1, "再强点")
                "a2" -> Structure.FeelingMessage(1, "轻一点")
                "a3" -> Structure.FeelingMessage(1, "好舒服")
                "a4" -> Structure.FeelingMessage(1, "换一个")
                "b1" -> Structure.FeelingMessage(2, "再强点")
                "b2" -> Structure.FeelingMessage(2, "轻一点")
                "b3" -> Structure.FeelingMessage(2, "好舒服")
                "b4" -> Structure.FeelingMessage(2, "换一个")
                else -> null
            }
        }

        @JsName("process")
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
        private var lastHeartbeat = 0L

        @JsName("requestControl")
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

        @JsName("logoutControl")
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

        @JsName("getQrUrl")
        public fun getQrUrl(): String? {
            return if (randomCode.isNotEmpty()) {
                "${Structure.qrUrl}#$randomCode"
            } else {
                null
            }
        }

        @JsName("getNIMConnect")
        public fun getNIMConnect(): Structure.NIMAuth {
            return Structure.NIMAuth(
                    appKey = Structure.appKey,
                    account = auth.getUUID(),
                    token = auth.getToken()
            )
        }

        @JsName("isOnline")
        public fun isOnline() = toID.isNotEmpty()

        @JsName("getHeartbeatPassed")
        public fun getHeartbeatPassed() : Long {
            return Clock.System.now().toEpochMilliseconds() - lastHeartbeat
        }

        @JsName("forceDropCurrent")
        public fun forceDropCurrent() {
            toID = ""
        }

        @JsName("sendFeeling")
        public fun sendFeeling(channel: Int, feeling: Int) : Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = "${if(channel == 1) "a" else "b"}$feeling"
            )
        }

        @JsName("processNIM")
        public fun processNIM(data: String) : Array<Structure.NIM.V1.SendWaveAndStrength>? {
            val jsonData = Json.decodeFromString<JsonElement>(data)
            val from = jsonData.jsonObject["from"]!!.jsonPrimitive.content
            if (toID.isNotEmpty() && from != toID) return null
            val text = jsonData.jsonObject["text"]!!.jsonPrimitive.content
            if (text == "998") {
                lastHeartbeat = Clock.System.now().toEpochMilliseconds()
                return null
            }
            val textData = Json.decodeFromString<JsonElement>(text)
            if (textData.jsonObject.containsKey("msg")) {
                val wave = textData.jsonObject["msg"]!!.jsonPrimitive.content
                val array = Json.decodeFromString<Array<Structure.NIM.V1.SendWaveAndStrength>>(wave)
                return array.mapNotNull {
                    when (it.channel) {
                        1 -> {
                            if (it.strength <= limitA)
                                return@mapNotNull Structure.NIM.V1.SendWaveAndStrength(1, it.bytes, strengthA + it.strength)
                            null
                        }
                        2 -> {
                            if (it.strength <= limitB)
                                return@mapNotNull Structure.NIM.V1.SendWaveAndStrength(2, it.bytes, strengthB + it.strength)
                            null
                        }
                        else -> {
                            null
                        }
                    }
                }.toTypedArray()
            } else {
                val content = textData.jsonObject["content"]
                if (content != null) {
                    val parseContent = Json.decodeFromString<JsonElement>(content.jsonPrimitive.content)
                    val status = parseContent.jsonObject["status"]!!.jsonPrimitive.int
                    if (status == 3) {
                        toID = parseContent.jsonObject["id"]!!.jsonPrimitive.content
                    } else if (status == 2) {
                        toID = ""
                    }
                }
                return null
            }
        }
        @JsName("process")
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