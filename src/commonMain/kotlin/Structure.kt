import kotlinx.serialization.Serializable

class Structure {
    companion object {
        const val baseUrl = "http://dungeon-server.top:8888"
        const val qrUrl = "http://dungeon-lab.cn/appdownload.html#"
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
        data class JoinControl(val code: Int, val msg: String, val accstatus: String, val appkey: String, val devicetype: String, val fromID: String, val strengthA: Int, val strengthB: Int, val toID: String)
    }
    class NIM {
        class V1 {
            @Serializable
            data class Join(val code: String = "200", val content: JoinDetail, val type: Int = 1)
            @Serializable
            data class JoinDetail(val id: String, val status: Int = 3)
            @Serializable
            data class Disconnect(val code: String = "200", val content: DisconnectDetail, val type: Int = 1)
            @Serializable
            data class DisconnectDetail(val status: Int = 2)
            @Serializable
            data class SendWaveAndStrength(val channel: Int, val bytes: String, val strength: Int)
            @Serializable
            data class WaveStrengthMsg(val msg: String)
        }
    }
}
