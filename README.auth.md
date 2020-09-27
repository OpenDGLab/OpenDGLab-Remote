# OpenDGLab 登录到 DG-Lab
Auth 类将帮助您登录到 DG-Lab 官方服务器的认证中心。

实例化 Auth 类后，通过下面的 API 可以使用不同的登录方式。

`loginWithEmail(email: String) : Structure.Request` 传入 E-Mail 地址，使用邮箱登录。本接口将会返回 Request 对象。

`loginCode(psw: String) : Structure.Request` 使用邮箱登录时传入用户邮箱中的验证码完成登录。本接口将会返回 Request 对象。

`loginWithToken(token: String) : Structure.Request` 使用 Token 登录。本接口将会返回 Request 对象。

`isReady() : Boolean` 返回是否完成登录。

`getToken() : String` 返回登录 Token 可以用于以后的登录。

`getUUID() : String` 返回登录 UUID。

`process(String, Int)` 处理返回值。第一个值为返回内容，第二个值为 Request 对象中的 requestCode。