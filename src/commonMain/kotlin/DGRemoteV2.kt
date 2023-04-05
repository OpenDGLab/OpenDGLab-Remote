import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.js.JsName

class DGRemoteV2 {
    companion object {
        private val json: Json
            get() = Json { ignoreUnknownKeys = true }
    }
    class Controller(val auth: Auth, val qrUrl: String) {
        val toID = qrUrl.split("#")[1].removeSuffix("2020")
        var limitA = 0
        var limitB = 0
        var startA = 0
        var startB = 0
        var aIncrease = 0
        var bIncrease = 0
        val sendArray = mutableListOf<Structure.NIM.V2.WaveAndStrength>()
        val packedSendArray = mutableListOf<String>()
        var isConnected = false
        var shouldDoConfig = false
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
                    msg = Json.encodeToString(Structure.NIM.V2.Connect(
                            msgType = 0,
                            pwd = "2020",
                            conOrDiscon = 0
                    ))
            )
        }

        @JsName("disconnect")
        fun disconnect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(Structure.NIM.V2.Connect(
                            msgType = 0,
                            conOrDiscon = 2,
                            pwd = "2020"
                    ))
            )
        }

        @JsName("isConnect")
        public fun isConnect() : Boolean {
            return isConnected
        }

        @JsName("getLimit")
        public fun getLimit() : Pair<Int, Int> {
            return Pair(limitA, limitB)
        }

        @JsName("shouldConfig")
        public fun shouldConfig() : Boolean {
            if (shouldDoConfig) {
                shouldDoConfig = false
                return true
            }
            return false
        }

        @JsName("prepareSend")
        public fun prepareSend(channel: Int, strength: Int, bytes: String) {
            if (strength < 0) return
            if (channel == 1 && strength > limitA) return
            if (channel == 2 && strength > limitB) return
            sendArray.add(Structure.NIM.V2.WaveAndStrength(bytes, channel, strength - 1))
            sendPack()
        }

        @JsName("forcePack")
        public fun forcePack() {
            val msg = (0..8).mapNotNull {
                sendArray.removeFirstOrNull()
            }
            packedSendArray.add(Json.encodeToString(Structure.NIM.V2.Wave(
                    msgType = 3,
                    pluseData = msg.toTypedArray(),
                    dataMsg = Json.encodeToString(msg.toTypedArray())
            )))
        }

        public fun sendPack() {
            if (sendArray.size < 8) return
            val msg = (0..8).mapNotNull {
                sendArray.removeFirstOrNull()
            }
            packedSendArray.add(Json.encodeToString(Structure.NIM.V2.Wave(
                    msgType = 3,
                    pluseData = msg.toTypedArray(),
                    dataMsg = Json.encodeToString(msg.toTypedArray())
            )))
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
            val content = Json.decodeFromString<JsonElement>(text)
            return when (content.jsonObject["msgType"]!!.jsonPrimitive.int) {
                0 -> {
                    val c = json.decodeFromJsonElement<Structure.NIM.V2.Config>(content)
                    if (c.conOrDiscon == 2) {
                        // Logout
                        isConnected = false
                        shouldDoConfig = false
                        limitA = 0
                        limitB = 0
                        startA = 0
                        startB = 0
                        aIncrease = 0
                        bIncrease = 0
                    } else {
                        // Login
                        limitA = c.aStrengthRangeMax
                        limitB = c.bStrengthRangeMax
                        startA = c.realStrengthA - 9
                        startB = c.realStrengthB - 9
                        aIncrease = 0
                        bIncrease = 0
                        isConnected = true
                        shouldDoConfig = true
                    }
                    null
                }
                4 -> {
                    val c = json.decodeFromJsonElement<Structure.NIM.V2.UpdateStrength>(content)
                    startA = c.realStrengthA - 9 - aIncrease
                    startB = c.realStrengthB - 9 - bIncrease
                    null
                }
                1 -> {
                    val c = json.decodeFromJsonElement<Structure.NIM.V2.Feeling>(content)
                    when (c.feelIndex) {
                        0 -> Structure.FeelingMessage(1, "再强点")
                        1 -> Structure.FeelingMessage(1, "轻一点")
                        2 -> Structure.FeelingMessage(1, "好舒服")
                        3 -> Structure.FeelingMessage(1, "换一个")
                        4 -> Structure.FeelingMessage(2, "再强点")
                        5 -> Structure.FeelingMessage(2, "轻一点")
                        6 -> Structure.FeelingMessage(2, "好舒服")
                        7 -> Structure.FeelingMessage(2, "换一个")
                        else -> null
                    }
                }
                else -> null
            }
        }
    }
    class Controlled(val auth: Auth, var strengthA:Int, var strengthB: Int , val limitA: Int, val limitB: Int) {
        private var toID = ""
        private var ack = mutableListOf<String>()
        @JsName("getNIMConnect")
        public fun getNIMConnect(): Structure.NIMAuth {
            return Structure.NIMAuth(
                    appKey = Structure.appKey,
                    account = auth.getUUID(),
                    token = auth.getToken()
            )
        }

        @JsName("getQrUrl")
        public fun getQrUrl() : String {
            return "${Structure.qrUrl}#${auth.getUUID()}2020"
        }

        @JsName("setBaseStrength")
        public fun setBaseStrength(a: Int, b: Int) : Structure.NIMMessage {
            strengthA = a
            strengthB = b
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(Structure.NIM.V2.UpdateStrength(
                            msgType = 4,
                            realStrengthA = strengthA + 9,
                            realStrengthB = strengthB + 9
                    ))
            )
        }

        @JsName("canConnect")
        public fun canConnect() : Boolean {
            return toID.isNotEmpty()
        }

        @JsName("needAck")
        public fun needAck(): Structure.NIMMessage? {
            val msg = ack.removeFirstOrNull() ?: return null
            return Structure.NIMMessage(
                    toID = toID,
                    msg = msg
            )
        }

        @JsName("disconnect")
        fun disconnect(): Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(Structure.NIM.V2.Connect(
                            msgType = 0,
                            conOrDiscon = 2,
                            pwd = "2020"
                    ))
            )
        }

        @JsName("sendFeeling")
        public fun sendFeeling(channel: Int, feeling: Int) : Structure.NIMMessage {
            return Structure.NIMMessage(
                    toID = toID,
                    msg = Json.encodeToString(Structure.NIM.V2.Feeling(
                            msgType = 1,
                            realStrengthA = strengthA + 9,
                            realStrengthB = strengthB + 9,
                            feelIndex = when(channel) {
                                1 -> {
                                    feeling
                                }
                                2 -> {
                                    feeling + 4
                                }
                                else -> 0
                            }
                    ))
            )
        }

        @JsName("processNIM")
        public fun processNIM(data: String): Array<Structure.NIM.V2.WaveAndStrength>? {
            val jsonData = Json.decodeFromString<JsonElement>(data)
            val from = jsonData.jsonObject["from"]!!.jsonPrimitive.content
            if (toID.isNotEmpty() && from != toID) return null
            val text = jsonData.jsonObject["text"]!!.jsonPrimitive.content
            val content = Json.decodeFromString<JsonElement>(text)
            return when (content.jsonObject["msgType"]!!.jsonPrimitive.int) {
                0 -> {
                    val c = json.decodeFromJsonElement<Structure.NIM.V2.Config>(content)
                    if (c.conOrDiscon == 2 || c.conOrDiscon == 1) {
                        // Logout
                        toID = ""
                        ack.clear()
                    } else {
                        // Login
                        toID = from
                        ack.add(
                                Json.encodeToString(
                                        Structure.NIM.V2.Config(
                                                msgType = 0,
                                                realStrengthA = strengthA + 9,
                                                realStrengthB = strengthB + 9,
                                                conOrDiscon = 0,
                                                pwd = "2020",
                                                aStrengthRangeMax = limitA,
                                                bStrengthRangeMax = limitB
                                        )
                                )
                        )
                    }
                    null
                }
                3 -> {
                    val msg = json.decodeFromJsonElement<Structure.NIM.V2.Wave>(content)
                    msg.pluseData.mapNotNull {
                        when(it.channel) {
                            1 -> Structure.NIM.V2.WaveAndStrength(it.bytes, 1, it.strength + strengthA)
                            2 -> Structure.NIM.V2.WaveAndStrength(it.bytes, 2, it.strength + strengthB)
                            else -> null
                        }
                    }.toTypedArray()
                }
                else -> null
            }
        }
    }
}