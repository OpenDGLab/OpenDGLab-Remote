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
`appkey` NIM AppKey 一般也没用

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

## 生成二维码
二维码内为网址

`http://dungeon-lab.cn/appdownload.html#` 后面加登录时获得的 UUID 后面再加上 2020。2020 为连接密码，目前固定。

## 建立云信连接
建立连接时使用以下参数
`appKey` getIMConnectCode 中获取的 appkey  
`account` getIMConnectCode 中获取的 uuid 加 2020  
`token` 登录时返回的 token  

## 通讯过程
扫码后，将 # 后面的值取出来，前32位为被控端 ID，后面为连接密码。目前固定为 2020。
建立连接后，控制端向被控端 ID 发出**连接请求**。  
然后被控端验证密码（其实并没有）并返回**配置信息**。里面记载有允许的增量，目前的强度值。是否连接等信息。  
此时，被控端进入受控模式。  
被控端可以发出**当前值更新信息**通知控制端已修改可调整的范围值。  
被控端也可以发出**感受信息**在控制端上显示一些信息。  
控制端可以发出**波形与强度指令**进入工作模式。
退出时退出方发出**退出信息**后断开连接。

## 共享的信息
### 退出信息
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":2,"feelIndex":0,"msgType":0,"pwd":"2020","realStrengthA":0,"realStrengthB":0}
```
msgType 为 0，conOrDiscon 为 2，发送后断开连接。

## 被控端
### 收到的信息
#### 连接请求
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":0,"pwd":"2020","realStrengthA":0,"realStrengthB":0}
```
收到消息后验证 pwd 是否为自己要的 pwd （目前固定为 2020），如果正确，回复**配置信息**

#### 波形与强度指令
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"dataMsg":"[{\"bytes\":\"000010000000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0}]","feelIndex":0,"msgType":3,"pluseData":[{"bytes":"000010000000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0}],"realStrengthA":0,"realStrengthB":0}
```
msgType 为 3  
收到此消息后建议将消息压入先进先出队列，每100毫秒对 channel 为 1 和 2 分别做一次读取操作。  
dataMsg 为 pluseData 的字符串模式。一般无视即可。  
pluseData 中含有传输过来的波形和强度信息，信息在每个消息中每通道最多4条。  
channel 为通道 1 为 A 通道，2 为 B 通道。bytes 为要发送的波形数组。strength 为强度增量。  
变更强度时，将 strength 和当前基础值相加后为目标强度值。    
收到后将 bytes 通过 KDataUtils 中的 convertStringToByteArray 方法转换为字节数组就可以发送给对应通道了。

### 发出的信息
#### 配置信息
```json
{"aStrengthRangeMax":30,"bStrengthRangeMax":35,"conOrDiscon":0,"feelIndex":0,"msgType":0,"pwd":"2020","realStrengthA":10,"realStrengthB":10}
```
msgType 为 0，realStrength 为当前对应通道的强度加9，pwd 为之前的密码。StrengthRangeMax 为强度增量限制。

#### 当前值更新信息
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":4,"realStrengthA":10,"realStrengthB":10}
```
msgType 为 4，realStrength 更新为当前值。此方法在被控端修改自己的基础值时调用。

#### 感受信息
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":1,"realStrengthA":10,"realStrengthB":10}
```
msgType 为 1，feelIndex 为要发送的感受信息。
| feelIndex | 通道 | 消息 |
|:-- |:-- |:-- |
| 0 | A | 再强点 |
| 1 | A | 轻一点 |
| 2 | A | 好舒服 |
| 3 | A | 换一个 |
| 4 | B | 再强点 |
| 5 | B | 轻一点 |
| 6 | B | 好舒服 |
| 7 | B | 换一个 |

## 控制端
### 收到的信息
#### 配置信息
```json
{"aStrengthRangeMax":30,"bStrengthRangeMax":35,"conOrDiscon":0,"feelIndex":0,"msgType":0,"pwd":"2020","realStrengthA":10,"realStrengthB":10}
```
msgType 为 0 时  
realStrengthA 和 realStrengthB 为 A 和 B 通道的当前值，此数字减去 9 为当前通道强度值。aStrengthRangeMax 和 bStrengthRangeMax 为强度增量限制。

#### 当前值更新信息
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":4,"realStrengthA":10,"realStrengthB":10}
```
msgType 为 4，realStrength 更新为当前值。此方法在被控端修改自己的基础值时调用。请注意收到后与原有值进行计算以重新规约最大最小值。

#### 感受信息
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":1,"realStrengthA":10,"realStrengthB":10}
```
见被控端

### 发出的信息
#### 连接请求
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"feelIndex":0,"msgType":0,"pwd":"2020","realStrengthA":0,"realStrengthB":0}
```


#### 波形与强度指令
```json
{"aStrengthRangeMax":0,"bStrengthRangeMax":0,"conOrDiscon":0,"dataMsg":"[{\"bytes\":\"000010000000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0},{\"bytes\":\"000010100000000100100001\",\"channel\":1,\"strength\":0}]","feelIndex":0,"msgType":3,"pluseData":[{"bytes":"000010000000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0},{"bytes":"000010100000000100100001","channel":1,"strength":0}],"realStrengthA":0,"realStrengthB":0}
```
msgType 为 3  
主要见被控端**波形与强度指令**章节，请注意，strength 是强度增量。