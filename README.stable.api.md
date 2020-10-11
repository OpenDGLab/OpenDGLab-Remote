# OpenDGLab 稳定版协议库
## 控制端
实例化 `DGRemoteV1.Controller(Auth, String)` 类，第一个值为 Auth 类的实例，第二个值为连接串。一般来说使用一个可以显示二维码内容的条码扫描器软件即可获得二维码中的值。

`joinControl() : Structure.Request` 获取被控端信息。本接口将会返回 Request 对象。

`getNIMConnect() : Structure.NIMAuth` 获取 NIM 登录信息，包含 appKey、account 和 token。获取后直接连接网易云信 NIM 即可。

`connect() : Structure.NIMMessage` 通知被控端已连接。

`disconnect() : Structure.NIMMessage` 通知被控端已断开。

`canOnline() : Boolean` 返回是否已经可以发送控制数据了（在被控端回应连接请求后返回 true）。

`getLimit() : Pair<Int, Int>` 返回强度限制

`heartbeat() : Structure.NIMMessage` 心跳数据包，请每 2 秒发送一次。

`prepareSend(channel: Int, strength: Int, bytes: String)` 发送波形和强度数据。channel 1 为 A 通道，channel 2 为 B 通道。强度值超出对方限制时将会自动无效。请注意 bytes 中的波形数据需要为字符串形式。本方法不会立刻触发要求发送。

`forcePack()` 立刻执行一次波形强度数据打包，即使数据不足6条。在停止波形发送时使用。

`clearSend()` 清空所有未发送的数据。

`getSend() : Structure.NIMMessage?` 获取准备发送的信息，如果返回为 null 代表没有足够的数据可供打包。

`processNIM(String)` 处理 NIM 上的信息。

`process(String, Int)` 处理请求信息。

## 被控端
实例化 `DGRemoteV1.Controlled(Auth, Int, Int, Int, Int)` 类，第一个值为 Auth 类的实例，第二个和第三个值为 A B 通道初始强度，注意最低为 1，第四个和第五个值为强度限制。

`requestControl() : Structure.Request` 请求远程控制码。本接口将会返回 Request 对象。

`logoutControl() : Structure.Request` 请求无效化远程控制码。本接口将会返回 Request 对象。

`getQrUrl() : String?` 如果已准备好连接，返回二维码中的 Url 连接字符串地址，将其编码为二维码即可。如果返回 null 那么 joinControl 尚未完成。

`getNIMConnect() : Structure.NIMAuth` 获取 NIM 登录信息，包含 appKey、account 和 token。获取后直接连接网易云信 NIM 即可。

`isOnline()` 获取是否有控制端已经上线。

`getHeartbeatPassed() : Long` 获取距离上一次心跳数据包已经过了多少毫秒。超过 6 秒请强制断开当前连接。

`forceDropCurrent()` 抛弃当前控制用户。一般用于心跳超时。

`sendFeeling(Int, Int) : Structure.NIMMessage` 发送感受数据。第一个值为通道 channel 1 为 A 通道，channel 2 为 B 通道。第二个值为感受信息。有效值为 1-4，详见协议文档。

`processNIM(String)` 处理 NIM 上的信息。

`process(String, Int)` 处理请求信息。