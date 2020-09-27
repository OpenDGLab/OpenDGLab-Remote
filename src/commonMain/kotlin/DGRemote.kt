import kotlin.js.JsName
import kotlin.jvm.JvmStatic

class DGRemote {
    companion object {
        @JvmStatic
        @JsName("identifyProtocolVersion")
        public fun identifyProtocolVersion(data: String) : String {
            return if (data.contains("msgType")) {
                "V2"
            } else {
                "V1"
            }
        }
    }
}