import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.js.JsName

class Auth {
    private var uuid = ""
    private var token = ""
    private var email = ""

    @JsName("loginWithEmail")
    public fun loginWithEmail(email: String) : Structure.Request {
        this.email = email
        return Structure.Request(
            headers = arrayOf(
                Structure.Header("device", "android"),
                Structure.Header("la", "0")
            ),
            url = "${Structure.baseUrl}/emailCodeSend",
            method = "POST",
            body = "email=$email",
            contentType = "application/x-www-form-urlencoded",
            requestCode = 0
        )
    }

    @JsName("loginCode")
    public fun loginCode(code: String) : Structure.Request {
        return Structure.Request(
            headers = arrayOf(
                Structure.Header("device", "android"),
                Structure.Header("la", "0")
            ),
            url = "${Structure.baseUrl}/emailRegVerify",
            method = "POST",
            body = "email=$email&psw=$code",
            contentType = "application/x-www-form-urlencoded",
            requestCode = 1
        )
    }

    @JsName("loginWithToken")
    public fun loginWithToken(token: String) : Structure.Request {
        this.token = token
        return Structure.Request(
            headers = arrayOf(
                Structure.Header("device", "android"),
                Structure.Header("token", token),
                Structure.Header("la", "0")
            ),
            url = "${Structure.baseUrl}/tokenVerify",
            method = "POST",
            body = "",
            contentType = "application/x-www-form-urlencoded",
            requestCode = 2
        )
    }

    @JsName("isReady")
    public fun isReady() : Boolean {
        return token.isNotEmpty() && uuid.isNotEmpty()
    }

    @JsName("getToken")
    public fun getToken() : String {
        return token
    }

    @JsName("getUUID")
    public fun getUUID() : String {
        return uuid
    }

    @JsName("process")
    public fun process(data: String, requestCode: Int) {
        when (requestCode) {
            0 -> {
                Json.decodeFromString<Structure.Response.Common>(data)
            }
            1 -> {
                val login = Json.decodeFromString<Structure.Response.Login>(data)
                if (login.code == 200) {
                    this.token = login.token
                    this.uuid = login.uuid
                }
            }
            2 -> {
                val login = Json.decodeFromString<Structure.Response.TokenVerify>(data)
                if (login.code == 200) {
                    this.uuid = login.uuid
                }
            }
        }
    }

}