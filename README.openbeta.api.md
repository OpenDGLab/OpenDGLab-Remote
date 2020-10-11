# OpenDGLab OpenBeta 版协议库
## 控制端
实例化 `DGRemoteV2.Controller(Auth, String)` 类，第一个值为 Auth 类的实例，第二个值为连接串。一般来说使用一个可以显示二维码内容的条码扫描器软件即可获得二维码中的值。

第二代协议无需访问网络 HTTP 请求，直接与 NIM 通信即可。

`getNIMConnect() : Structure.NIMAuth` 获取 NIM 登录信息，包含 appKey、account 和 token。获取后直接连接网易云信 NIM 即可。

`connect() : Structure.NIMMessage` 通知被控端已连接。

`disconnect() : Structure.NIMMessage` 通知被控端已断开。

`isConnect() : Boolean` 返回是否已经连接了。

`shouldConfig() : Boolean` 返回是否需要发送配置数据包，当 isConnect() 和 shouldConfig() 均为 true 时需要发送 connect() 的数据包。

`prepareSend(channel: Int, strength: Int, bytes: String)` 发送波形和强度数据。channel 1 为 A 通道，channel 2 为 B 通道。强度值超出对方限制时将会自动无效。请注意 bytes 中的波形数据需要为字符串形式。本方法不会立刻触发要求发送。

`forcePack()` 立刻执行一次波形强度数据打包，即使数据不足6条。在停止波形发送时使用。

`clearSend()` 清空所有未发送的数据。

`getSend() : Structure.NIMMessage?` 获取准备发送的信息，如果返回为 null 代表没有足够的数据可供打包。

`processNIM(String)` 处理 NIM 上的信息。

## 被控端
实例化 `DGRemoteV2.Controlled(Auth, Int, Int, Int, Int)` 类，第一个值为 Auth 类的实例，第二个和第三个值为 A B 通道初始强度，注意最低为 1，第四个和第五个值为强度限制。

`getQrUrl() : String` 返回二维码中的 Url 连接字符串地址，将其编码为二维码即可。

`getNIMConnect() : Structure.NIMAuth` 获取 NIM 登录信息，包含 appKey、account 和 token。获取后直接连接网易云信 NIM 即可。

`setBaseStrength(a: Int, b: Int) : Structure.NIMMessage` 更改 A B 通道基础值。

`canConnect() : Boolean` 是否已接受控制端连接。（是否准备好发送数据）

`getLimit() : Pair<Int, Int>` 返回强度限制

`needAck() : Structure.NIMMessage?` 需要回复的数据。如果返回 null 就没有数据需要返回，一般调用 processNIM 后需要检查下。

`disconnect() : Structure.NIMMessage` 通知控制端已断开。

`sendFeeling(Int, Int) : Structure.NIMMessage` 发送感受数据。第一个值为通道 channel 1 为 A 通道，channel 2 为 B 通道。第二个值为感受信息。有效值为 1-4，详见协议文档。

`processNIM(String)` 处理 NIM 上的信息。