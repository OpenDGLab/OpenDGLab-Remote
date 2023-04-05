# DG-Lab 原版远程控制协议
DG-Lab 官方协议使用网易云信的 IM 通信功能 P2P 模式进行协议通信。具体参见 [网易云信接入指南](https://dev.yunxin.163.com/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/%E6%96%B0%E6%89%8B%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)

NIM AppKey 目前为 `9dac64d02f2e5f11aa5e887d809e911c`

本仓库旨在还原 DG-Lab 客户端原版通信协议，以此保证与原客户端的通信行为。

DG-Lab 目前分为2个通信版本，两者具有完全不同的协议。  

~~[DG-LAB 过时版](README.outdated.md) 现在没有任何版本在使用这个协议了~~

[DG-Lab 稳定版](README.stable.md) 目前的版本。

# 协议库
协议库仅帮助其他库处理信息，本身不具有网络访问能力。

所有网络访问接口将返回 Structure.Request 类以描述访问的相关信息。包含 headers 头、contentType 头、url 地址、method 访问方法、body 访问数据、requestCode 访问编号。

在按要求访问后将返回值的字符串传入对应类的 process 方法中并传入 requestCode，即可进行解析和自动状态维护。

所有 NIM 中传输的数据将返回 Structure.NIMMessage ，其中包含发送目标 toID 和消息内容 msg。请通过 sendText 发送字符串消息。所有 NIM 的返回值调用 processNIM 方法即可进行解析。 

本库需要 kotlinx 的序列化库和日期与时间库才能正常工作。Javascript 库用户请从编译时 build 文件夹中的 node_modules 中复制相关库文件。

[DG-Lab Auth](README.auth.md) DG-Lab 官方登录 API 库。

~~[DG-LAB 过时版](README.outdated.api.md) 基于过时版的 API 库。~~

[DG-LAB 稳定版](README.stable.api.md) 稳定版的 API 库。
