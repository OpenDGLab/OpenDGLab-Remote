# DG-Lab 稳定版远程 API
URL 基础 `http://dungeon-server.top:8888`

所有发送类型均为 `application/x-www-form-urlencoded`

DG-Lab 服务器没有传统意义上的账号系统。所有行为均以邮箱 + 验证码实现。
## 通用 HTTP 请求头
| 头名称 | 参数 |
|:-- |:-- |
| device | 固定为 android 即可 |
| token | 自己的 token 有些接口这里留空即可 |
| la | 固定为 0 |

## 通用返回
返回值为 JSON  

| 头名称 | 参数 |
|:-- |:-- |
| code | 接口调用状态码 |
| msg | 如果有错误就是错误信息 |

## API
### 验证 token 是否还有效
POST /tokenVerify

#### 参数
`uuid` 不知道为什么有这个参数，但是没用。

#### 返回值
`uuid` 该 token 的 UUID

### 发送验证码
POST /emailCodeSend

此接口无需 token 头
#### 参数
`email` 要验证的邮箱

#### 返回值
无特殊返回值

### 验证验证码
POST /emailRegVerify

此接口无需 token 头
#### 参数
`email` 要验证的邮箱  
`psw` 验证码  

#### 返回值
`token` 需要保存下来其他接口用

### 创建远程连接
POST /getIMConnectCode

#### 参数
`type` 固定为 1  
`strengthA` 限制 A 通道强度增量  
`strengthB` 限制 B 通道强度增量  
`limited` 固定为 0 即可  

#### 返回值
`accstatus` 在线信息 可以无视  
`appkey` NIM AppKey 创建云信 NIM 连接时用  
`randomcode` 连接码 创建二维码用  
`uuid` 自己的用户 ID 连接 NIM 用

### 加入控制
POST /joinControl

#### 参数
`randomCode` 连接码 二维码扫描后 # 后的数字

#### 返回值
`accstatus` 在线信息 可以无视  
`appkey` NIM AppKey 创建云信 NIM 连接时用  
`devicetype` 设备类型 目前可以无视  
`fromID` 自己的 UUID  
`strengthA` A 通道增量限制  
`strengthB` B 通道增量限制  
`toID` 受控端 UUID  

### 退出远程连接
POST /accidentalIMLogOut

#### 参数
`a` 固定为 空  

#### 返回值
无特殊返回值

## 生成二维码
二维码内为网址

`http://dungeon-lab.cn/appdownload.html#` 后面加连接码即可。

## 建立云信连接
建立连接时使用以下参数
`appKey` getIMConnectCode 中获取的 appkey  
`account` getIMConnectCode 中获取的 uuid  
`token` 登录时返回的 token  

## 通讯过程
受控端的ID为上方加入控制的接口中返回的 toID。    
控制端发送**上线信息**后，被控端正式进入受控模式。  
然后被控端开始每2秒发送**心跳消息**。  
控制端开始发出**波形与强度指令**进入工作模式。  
被控端可以发出**通道消息**让控制端显示一些信息。  
由控制端断开连接时，被控端发出**断开消息**后离线。
由被控端断开连接时，直接关闭云信连接即可。

## 被控端
### 发送的消息
#### 心跳消息
被控端建立连接后，需要每2秒发送纯文本信息 `998` 进行心跳。不发送此消息会导致控制端出现被控端已离线消息并离开控制。

#### 通道消息
发送纯文本消息 `a1` `a2` `a3` `a4` 会使控制端在 A 通道上显示被控端 A 通道上可以发送的4个消息。同理 `b1` `b2` `b3` `b4`。

| 消息 | 文字 |
|:--  |:--  |
| a1 b1 | 再强点 |
| a2 b2 | 轻一点 |
| a3 b3 | 好舒服 |
| a4 b4 | 换一个 |

### 接收的消息
接收的消息均为 JSON 字符串。

收到消息后先将 JSON 字符串转为您可以处理的格式。
#### 控制端已连接
控制端已连接后会发送下列信息

```json
{"code": 200, "content": {"id": "控制端的 ID", "status":3}, "type": 1}
```
只需要判断 content 中的 status 为 3 即可。

收到此消息后被控端会进入受控模式并关闭等待连接对话框。记下控制端的 ID 之后即可后续向控制端发送消息。

#### 控制端断开连接
控制端断开时会发送
```json
{"code": 200, "content": {"status":2}, "type": 1}
```
只需要判断 content 中的 status 为 2 即可。

#### 波形与强度指令
在收到波形与强度指令时，msg 字段中的内容为数组，数组中的每一项为如下样式。 
```json
{ "channel": 1, "bytes": "000000000000000000000000", "strength": 5 }
```
收到此消息后建议将消息压入先进先出队列，每100毫秒对 channel 为 1 和 2 分别做一次读取操作。

channel 1 为 A 通道。channel 2 为 B 通道。

bytes 直接通过 KDataUtils 中的 convertStringToByteArray 方法转换为字节数组就可以发送给对应通道了。

strength 是控制端发过来的**增量值**，在本地与本地的基础值相加后发送给设备即可。

## 控制端
发送的消息全部为 JSON 字符串。

### 接收的消息
#### 心跳消息
会收到字符串 `998`，6秒内没收到即为掉线，一般无视即可。
#### 通道消息
会收到字符串 `a1` `a2` `a3` `a4` `b1` `b2` `b3` `b4` ，对应关系见上方表格。

### 发送的消息
#### 上线信息
控制端连接后，向 被控端ID 发送上线信息。
```json
{"code": 200, "content": {"id": "自己的 ID", "status":3}, "type": 1}
```
被控端收到此消息后会进入被控界面。

#### 发送波形
```json
{"code": 200, "msg": [{ "channel": 1, "bytes": "000000000000000000000000", "strength": 5 }]}
```
content 中的数组最少为1个，最大为4个。

channel 为通道 1 为 A 通道，2 为 B 通道。bytes 为要发送的波形数组。strength 为强度增量。

#### 断开消息
```json
{"code": 200, "content": {"status":2}, "type": 1}
```

发送后直接关闭 NIM 连接即可。