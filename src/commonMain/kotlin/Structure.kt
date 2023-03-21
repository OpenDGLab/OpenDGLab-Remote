import kotlinx.serialization.Serializable

class Structure {
    companion object {
        const val baseUrl = "https://dungeon-server.com:8445"
        const val qrUrl = "http://dungeon-lab.cn/appdownload.html"
        const val appKey = "9dac64d02f2e5f11aa5e887d809e911c"
    }
    data class Header(val key: String, val value: String)
    data class Request(val headers: Array<Header>, val contentType: String, val url: String, val method: String, val body: String, val requestCode: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Request

            if (!headers.contentEquals(other.headers)) return false
            if (contentType != other.contentType) return false
            if (url != other.url) return false
            if (method != other.method) return false
            if (body != other.body) return false
            if (requestCode != other.requestCode) return false

            return true
        }

        override fun hashCode(): Int {
            var result = headers.contentHashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + url.hashCode()
            result = 31 * result + method.hashCode()
            result = 31 * result + body.hashCode()
            result = 31 * result + requestCode
            return result
        }
    }
    data class NIMAuth(val appKey: String, val account: String, val token: String)
    data class NIMMessage(val toID: String, val msg: String)
    data class FeelingMessage(val channel: Int, val msg: String)
    class Response {
        @Serializable
        data class Common(val code: Int, val msg: String)
        @Serializable
        data class Login(val code: Int, val msg: String, val uuid: String, val token: String)
        @Serializable
        data class TokenVerify(val code: Int, val msg: String, val uuid: String)
        @Serializable
        data class IMConnectCode(val code: Int, val msg: String, val accstatus: Int, val appkey: String, val randomcode: String, val uuid: String)
        @Serializable
        data class JoinControl(val code: Int, val msg: String, val accstatus: Int, val appkey: String, val devicetype: Int, val fromID: String, val strengthA: Int, val strengthB: Int, val toID: String)
    }
    class NIM {
        class V1 {
            @Serializable
            data class Join(val code: Int, val content: String, val type: Int)
            @Serializable
            data class JoinDetail(val id: String, val status: Int)
            @Serializable
            data class Disconnect(val code: Int, val content: String, val type: Int)
            @Serializable
            data class DisconnectDetail(val status: Int)
            @Serializable
            data class SendWaveAndStrength(val channel: Int, val bytes: String, val strength: Int)
            @Serializable
            data class WaveStrengthMsg(val msg: String)
        }
        class V2 {
            @Serializable
            data class Connect(val msgType: Int, val conOrDiscon: Int, val pwd: String)
            //msgType = 0
            @Serializable
            data class Config(val msgType: Int, val aStrengthRangeMax: Int, val bStrengthRangeMax: Int, val conOrDiscon: Int, val pwd: String, val realStrengthA: Int, val realStrengthB: Int)
            //msgType = 4
            @Serializable
            data class UpdateStrength(val msgType: Int, val realStrengthA: Int, val realStrengthB: Int)
            //msgType = 1
            @Serializable
            data class Feeling(val msgType: Int, val realStrengthA: Int, val realStrengthB: Int, val feelIndex: Int)
            //msgType = 3
            @Serializable
            data class Wave(val msgType: Int, val dataMsg: String, val pluseData: Array<WaveAndStrength>) {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other == null || this::class != other::class) return false

                    other as Wave

                    if (msgType != other.msgType) return false
                    if (dataMsg != other.dataMsg) return false
                    if (!pluseData.contentEquals(other.pluseData)) return false

                    return true
                }

                override fun hashCode(): Int {
                    var result = msgType
                    result = 31 * result + dataMsg.hashCode()
                    result = 31 * result + pluseData.contentHashCode()
                    return result
                }
            }
            @Serializable
            data class WaveAndStrength(val bytes: String, val channel: Int, val strength: Int)
        }
    }
}
