## Http Adapter

提供基于轮询的 http 接口

### 配置文件

```yaml
adapterSettings:
  http:
    ## http server 监听的本地地址
    ## 一般为 localhost 即可, 如果多网卡等情况，自定设置
    host: localhost

    ## http server 监听的端口
    ## 与 websocket server 可以重复, 由于协议与路径不同, 不会产生冲突
    port: 8080

    ## 配置跨域, 默认允许来自所有域名
    cors: [*]
```

### 接口一览

#### 专有接口

专有接口为该 `adapter` 特有的接口

+ **[认证与会话](#认证与会话)**
  + [认证](#认证)
  + [绑定](#绑定)
  + [释放](#释放)
  + [传递(重要)](#传递(重要))
+ **[接收消息与事件](#接收消息与事件)**
  + [查看队列大小](#查看队列大小)
  + [获取队列头部](#获取队列头部)
  + [获取队列尾部](#获取队列尾部)
  + [查看队列头部](#查看队列头部)
  + [查看队列尾部](#查看队列尾部)
  + [消息队列操作接口请求与响应](#消息队列操作接口请求与响应)
+ **[多媒体内容上传](#多媒体内容上传)**
  * [图片文件上传](#图片文件上传)
  * [语音文件上传](#语音文件上传)
  * [群文件上传](#群文件上传)
-------

#### 通用接口

通用接口为所有 `built-in adapter` 公用的数据规范, 该文档定义了不同 `adapter` 的具体调用方式
+ **[获取插件信息](#获取插件信息)**
+ **[缓存操作]()**
  + [通过messageId获取消息](#通过messageId获取消息)
+ **[获取账号信息](#获取账号信息)**
  + [获取好友列表](#获取好友列表)
  + [获取群列表](#获取群列表)
  + [获取群成员列表](#获取群成员列表)
  + [获取Bot资料](#获取Bot资料)
  + [获取好友资料](#获取好友资料)
  + [获取群成员资料](#获取群成员资料)
+ **[消息发送与撤回](#消息发送与撤回)**
  + [发送好友消息](#发送好友消息)
  + [发送群消息](#发送群消息)
  + [发送临时会话消息](#发送临时会话消息)
  + [发送头像戳一戳消息](#发送头像戳一戳消息)
  + [撤回消息](#撤回消息)
+ **[文件操作](#文件操作)**
  + [查看文件列表](#查看文件列表)
  + [获取文件信息](#获取文件信息)
  + [创建文件夹](#创建文件夹)
  + [删除文件](#删除文件)
  + [移动文件](#移动文件)
  + [重命名文件](#重命名文件)
+ **[账号管理](#账号管理)**
  + [删除好友](#删除好友)
+ **[群管理](#群管理)**
  + [禁言群成员](#禁言群成员)
  + [解除群成员禁言](#解除群成员禁言)
  + [移除群成员](#移除群成员)
  + [退出群聊](#退出群聊)
  + [全体禁言](#全体禁言)
  + [解除全体禁言](#解除全体禁言)
  + [设置群精华消息](#设置群精华消息)
  + [获取群设置](#获取群设置)
  + [修改群设置](#修改群设置)
  + [获取群员资料](#获取群员设置)
  + [修改群员资料](#修改群员设置)
+ **[事件处理](#事件处理)**
  + [添加好友申请](#添加好友申请)
  + [用户入群申请](#用户入群申请)
  + [Bot被邀请入群申请](#Bot被邀请入群申请)

## 认证与会话

### 认证

#### 接口名称
```
[POST] /verify
```
使用此方法验证你的身份，并返回一个会话

#### 请求:

```json5
{
    "verifyKey": "U9HSaDXl39ksd918273hU"
}
```

| 名字    | 类型   | 可选  | 举例                    | 说明                                                       |
| ------- | ------ | ----- | ----------------------- | ---------------------------------------------------------- |
| verifyKey | String | false | "U9HSaDXl39ksd918273hU" | 创建Mirai-Http-Server时生成的key，可在启动时指定或随机生成 |

#### 响应: 

```json5
{
    "code": 0,
    "session": "UnVerifiedSession"
}
```

| 名字    | 类型   | 举例                | 说明            |
| ------- | ------ | ------------------- | --------------- |
| code    | Int    | 0                   | 返回[状态码](../api/API.md#状态码)      |
| session | String | "UnVerifiedSession" | 你的session key |


 session key 是使用以下方法必须携带的
 session key 使用前必须进行校验和绑定指定的Bot，**每个Session只能绑定一个Bot，但一个Bot可有多个Session**
 session Key 在未进行校验的情况下，一定时间后将会被自动释放


### 绑定

```
[POST] /bind
```

使用此方法校验并激活你的Session，同时将Session与一个**已登录**的Bot绑定

#### 请求:

```json5
{
    "sessionKey": "UnVerifiedSession",
    "qq": 123456789
}
```

| 名字       | 类型   | 可选  | 举例                | 说明                       |
| ---------- | ------ | ----- | ------------------- | -------------------------- |
| sessionKey | String | false | "UnVerifiedSession" | 你的session key            |
| qq         | Long   | false | 123456789           | Session将要绑定的Bot的qq号 |

#### 响应: 

```json5
{
    "code": 0,
    "msg": "success"
}
```


### 释放

```
[POST] /release
```

使用此方式释放session及其相关资源（Bot不会被释放）
**不使用的Session应当被释放，长时间（30分钟）未使用的Session将自动释放，否则Session持续保存Bot收到的消息，将会导致内存泄露(开启websocket后将不会自动释放)**

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "qq": 123456789
}
```

| 名字       | 类型   | 可选  | 举例             | 说明                       |
| ---------- | ------ | ----- | -----------------| -------------------------- |
| sessionKey | String | false | "YourSessionKey" | 你的session key            |
| qq         | Long   | false | 123456789        | 与该Session绑定Bot的QQ号码 |

#### 响应: 

```json5
{
    "code": 0,
    "msg": "success"
}
```
> SessionKey与Bot 对应错误时将会返回状态码2：指定的Bot不存在

### 传递(重要)

`sessionKey` 作为会话的唯一标识, 它对应着服务器缓存及其上下文. 在 `adapter` 允许的情况下, 复用 `sessionKey` 创建多个连接会共享上下文

> `HttpAdapter` 允许复用 `sessionKey` 创建多个连接

在 `singleMode` 模式下: 所有需要 `sessionKey` 参数的接口可忽略 `sessionKey`

在非 `singleMode` 模式下 `sessionKey` 可通过以下方式传递:
1. 设置请求头 `sessionKey: YourSessionKey`, 大小写敏感
2. 设置请求头 `Authorization: session YourSessionKey`, `Authorization` 大小写敏感, `session` 大小写不敏感
3. 设置请求头 `Authorization: sessionKey YourSessionKey`, `Authorization` 大小写敏感, `sessionKey` 大小写不敏感
4. 通过 url参数(对于GET请求)或 json 参数(对于POST请求)填入 `sessionKey` 字段, 大小写敏感

## 接收消息与事件

`http adapter` 基于缓存队列保存 `session` 的"未读消息", 消息与事件的接收，等同于对该队列的操作

`http adatper` 提供以下队列操作

### 查看队列大小

使用此方法获取 session 未读缓存消息的数量

```
[GET] /countMessage?sessionKey=YourSessionKey
```

#### 请求:

| 名字       | 可选  | 举例           | 说明                 |
| ---------- | ----- | -------------- | -------------------- |
| sessionKey | false | YourSessionKey | 你的session key      |

#### 响应: 

```json5
{
    "code": 0,
    "msg": "",
    "data": 1024,   
}
```


### 获取队列头部

即按时间顺序获取消息，获取消息后从队列中移除

```
[GET] /fetchMessage?sessionKey=YourSessionKey&count=10
```

### 获取队列尾部

即获取最新的消息，获取消息后从队列中移除

```
[GET] /fetchLatestMessage?sessionKey=YourSessionKey&count=10
```

### 查看队列头部

即按时间顺序查看消息，查看消息后**不**从队列中移除

```
[GET] /peekMessage?sessionKey=YourSessionKey&count=10
```

### 查看队列尾部

即查看最新的消息，查看消息后**不**从队列中移除

```
[GET] /peekLatestMessage?sessionKey=YourSessionKey&count=10
```

### 消息队列操作接口请求与响应

#### 请求:

| 名字       | 可选  | 举例           | 说明                 |
| ---------- | ----- | -------------- | -------------------- |
| sessionKey | false | YourSessionKey | 你的session key      |
| count      | false | 10             | 获取(查看)消息和事件的数量 |

#### 响应:

```json5
{
  "code": 0,
  "msg": "",
  "data": [] // 消息、事件列表
}
```

相关阅读: 
+ [消息类型一览](../api/MessageType.md)
+ [事件类型一览](../api/EventType.md)

## 获取插件信息

使用此方法获取插件的信息，如版本号

```
[GET] /about
```

通用接口定义: [通过messageId获取消息](../api/API.md#获取插件信息)

## 缓存操作

### 通过messageId获取消息

此方法通过 `messageId` 获取历史消息, 历史消息的缓存有容量大小, 在配置文件中设置

```
[GET] /messageFromId?sessionKey=YourSessionKey&id=1234567890
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [通过messageId获取消息](../api/API.md#通过messageId获取消息)

## 获取账号信息

### 获取好友列表

使用此方法获取bot的好友列表

```
[GET] /friendList?sessionKey=YourSessionKey
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [通过messageId获取消息](../api/API.md#获取好友列表)

### 获取群列表

使用此方法获取bot的群列表

```
[GET] /groupList?sessionKey=YourSessionKey
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [通过messageId获取消息](../api/API.md#获取群列表)

### 获取群成员列表

使用此方法获取bot指定群中的成员列表

```
[GET] /memberList?sessionKey=YourSessionKey&target=123456789
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [通过messageId获取消息](../api/API.md#获取群成员列表)

### 获取Bot资料

此接口获取 session 绑定 bot 的详细资料

```
[GET] /botProfile
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [获取Bot资料](../api/API.md#获取Bot资料)

### 获取好友资料

此接口获取好友的详细资料

```
[GET] /friendProfile
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [获取好友资料](../api/API.md#获取好友资料)

### 获取群成员资料

此接口获取群成员的消息资料

```
[GET] /memberProfile
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [获取群成员资料](../api/API.md#获取群成员资料)

## 消息发送与撤回

### 发送好友消息

使用此方法向指定好友发送消息

```
[POST] /sendFriendMessage
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [发送好友消息](../api/API.md#发送好友消息)

### 发送群消息

```
[POST] /sendGroupMessage
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [发送群消息](../api/API.md#发送群消息)

### 发送临时会话消息

```
[POST] /sendTempMessage
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [发送临时会话消息](../api/API.md#发送临时会话消息)

### 发送头像戳一戳消息

```
[POST] /sendNudge
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [发送头像戳一戳消息](../api/API.md#发送头像戳一戳消息)

### 撤回消息

```
[POST] /recall
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [撤回消息](../api/API.md#撤回消息)

## 文件操作

### 查看文件列表

```
[GET] /file/list
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [查看文件列表](../api/API.md#查看文件列表)

### 获取文件信息

```
[GET] /file/info
```

**本接口为[GET]请求, 参数格式为url参数**

通用接口定义: [获取文件信息](../api/API.md#获取文件信息)

### 创建文件夹

```
[POST] /file/mkdir
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [创建文件夹](../api/API.md#创建文件夹)

### 删除文件

```
[POST] /file/delete
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [删除文件](../api/API.md#删除文件)

### 移动文件

```
[POST] /file/move
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [移动文件](../api/API.md#移动文件)

### 重命名文件

```
[POST] /file/rename
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [重命名文件](../api/API.md#重命名文件)

## 多媒体内容上传

**如果发送错误的请求,API将不会返回任何数据,也不会断开连接.** 请确保发送了正确的`multipart`请求.

### 图片文件上传

使用此方法上传图片文件至服务器并返回ImageId

```
[POST] /uploadImage
```

**本接口为[POST]请求, 参数格式为`multipart/form-data`**

#### 请求:

| 名字         | 类型   | 可选  | 举例        | 说明                               |
| ------------ | ------ | ----- | ----------- | ---------------------------------- |
| sessionKey   | String | true | YourSession | 已经激活的Session                  |
| type         | String | false | "friend"    | "friend" 或 "group" 或 "temp"        |
| img          | File   | false | -           | 图片文件                           |

#### 响应: 

```json5
{
  "imageId": "{XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}.mirai",
  "url": "xxxxxxxxxxxxxxxxxxxx"
}
```

### 语音文件上传

使用此方法上传语音文件至服务器并返回VoiceId

```
[POST] /uploadVoice
```

**本接口为[POST]请求, 参数格式为`multipart/form-data`**

#### 请求:

| 名字         | 类型   | 可选  | 举例        | 说明                               |
| ------------ | ------ | ----- | ----------- | ---------------------------------- |
| sessionKey   | String | true  | YourSession | 已经激活的Session                  |
| type         | String | false | "group"     | 当前仅支持 "group"                   |
| voice        | File   | false | -           | 语音文件                           |

#### 响应: 

```json5
{
  "voiceId":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.amr", //语音的VoiceId
  "url":"xxxxxxxxxxxxxxxxxxxx"
}
```

### 群文件上传

**本接口为[POST]请求, 参数格式为`multipart/form-data`**

#### 请求:

| 名字         | 类型   | 可选  | 举例        | 说明                               |
| ------------ | ------ | ----- | ----------- | ---------------------------------- |
| sessionKey   | String | true  | YourSession | 已经激活的Session                  |
| type         | String | false | "group"     | 当前仅支持 "group"                   |
| path         | String | false | ""          | 上传目录的id, 空串为上传到根目录        |
| file         | File   | false | -           | 上传的文件                           |

#### 响应:

```json5
{
  "name":"setu.png",
  "id":"/12314d-1wf13-a98ffa",
  "path":"/setu.png",
  "parent":null,
  "contact":{
    "id":123123,
    "name":"setu qun",
    "permission":"OWNER"
  },
  "isFile":true,
  "isDictionary":false
}
```

> 返回上传文件的信息

## 账号管理

### 删除好友

使用此方法删除指定好友

```
[POST] /deleteFriend
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [删除好友](../api/API.md#删除好友)

## 群管理

### 禁言群成员

使用此方法指定群禁言指定群员（需要有相关限权）

```
[POST] /mute
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [禁言群成员](../api/API.md#禁言群成员)

### 解除群成员禁言

使用此方法指定群解除群成员禁言（需要有相关限权）

```
[POST] /unmute
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [解除群成员禁言](../api/API.md#解除群成员禁言)

### 移除群成员

使用此方法移除指定群成员（需要有相关限权）

```
[POST] /kick
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [移除群成员](../api/API.md#移除群成员)

### 退出群聊

使用此方法使Bot退出群聊

```
[POST] /quit
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [退出群聊](../api/API.md#退出群聊)

### 全体禁言

使用此方法令指定群进行全体禁言（需要有相关限权）

```
[POST] /muteAll
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [全体禁言](../api/API.md#全体禁言)

### 解除全体禁言

使用此方法令指定群解除全体禁言（需要有相关限权）

```
[POST] /unmuteAll
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [解除全体禁言](../api/API.md#解除全体禁言)

### 设置群精华消息

使用此方法添加一条消息为精华消息（需要有相关限权）

```
[POST] /setEssence
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [设置群精华消息](../api/API.md#设置群精华消息)

### 获取群设置

使用此方法获取群设置

```
[GET] /groupConfig
```

**本接口为[GET]请求, 参数格式为URL参数**

通用接口定义: [获取群设置](../api/API.md#获取群设置)

### 修改群设置

使用此方法修改群设置（需要有相关限权）

```
[POST] /groupConfig
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [修改群设置](../api/API.md#修改群设置)

### 获取群员设置

使用此方法获取群员设置

```
[GET] /memberInfo
```

**本接口为[GET]请求, 参数格式为URL参数**

通用接口定义: [获取群员设置](../api/API.md#获取群员设置)

### 修改群员设置

使用此方法修改群员设置（需要有相关限权）

```
[POST] /memberInfo
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [修改群员设置](../api/API.md#修改群员设置)

## 事件处理

### 添加好友申请

使用此方法处理添加好友申请

```
[POST] /resp/newFriendRequestEvent
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [添加好友申请](../api/API.md#添加好友申请)

### 用户入群申请

使用此方法处理用户入群申请

```
[POST] /resp/memberJoinRequestEvent
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [用户入群申请](../api/API.md#用户入群申请)

### Bot被邀请入群申请

使用此方法处理Bot被邀请入群申请

```
[POST] /resp/botInvitedJoinGroupRequestEvent
```

**本接口为[POST]请求, 参数格式为`application/json`**

通用接口定义: [Bot被邀请入群申请](../api/API.md#Bot被邀请入群申请)
