# TeachHub
## 整体架构
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tianji-system.jpg)
## teachub-learning 
>Tips:在分布式系统中，使用数据库自增ID容易造成性能瓶颈和ID冲突，因为多个节点同时生成ID需要依赖数据库集中控制。而雪花算法（Snowflake）能在不同节点上本地高效、唯一地生成ID，避免分布式锁和数据库竞争问题，具有高可用、无中心、趋势递增等优点。因此，分布式系统推荐采用雪花算法而非默认的自增ID。
### 表结构设计
---
#### 业务流程分析
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/drawio.png)

#### 页面开发规则
- 未学习，已购买课程还未开始学习，可以开始学习
- 已学习，已购买课程已开始学习，展示学习进度，可以继续学习
- 已学完，已购买课程已经学完，可以重新学习
- 已失效，已购买课程已过期，不可继续学习，只能删除课程操作

综上设计出以下接口
| 接口用途                               | 请求方式 | 请求路径                         | 备注说明                         |
|----------------------------------------|----------|----------------------------------|----------------------------------|
| 支付或报名课程后加入课表               | MQ通知   | -                                | 支付/报名后发送 MQ 消息，加入课表 |
| 分页查询我的课表                       | GET      | /lessons/page                    | 支持分页查询                     |
| 查询我最近正在学习的课程               | GET      | /lessons/now                     | 返回当前正在学习的课程           |
| 根据 ID 查询指定课程的学习状态         | GET      | /lessons/{courseId}              | 获取某课程在课表中的学习状态     |
| 删除课表中的某课程                     | DELETE   | /lessons/{courseId}              | 主动移除课表中某个课程           |
| 退款后移除课表中的课程                 | MQ通知   | -                                | 退款成功后通过 MQ 移除课程       |
| 校验指定课程是否是有效课表课程（Feign）| GET      | /lessons/{courseId}/valid        | Feign 接口，用于远程调用校验     |
| 统计课程学习人数（Feign）              | GET      | /lessons/{courseId}/count        | Feign 接口，用于远程统计人数     |
#### ER图
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/Unknown.png)
#### 字段分析
课表要记录的是用户的学习状态，所谓学习状态就是记录谁在学习哪个课程，学习的进度如何。
- 其中，谁在学习哪个课程，就是一种关系。也就是说课表就是用户和课程的中间关系表。因此一定要包含三个字段：
  - userId：用户id，也就是谁
  - courseId：课程id，也就是学的课程
  - id：唯一主键
- 而学习进度，则是一些附加的功能字段，页面需要哪些功能就添加哪些字段即可：
  - status：课程学习状态。0-未学习，1-学习中，2-已学完，3-已过期
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown.png)
  - planStatus：学习计划状态，0-没有计划，1-计划进行中
  - weekFreq：计划的学习频率
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown1.png)
  - learnedSections：已学习小节数量，注意，课程总小节数、课程名称、封面等可由课程id查询得出，无需重复记录
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown2.png)
  - latestSectionId：最近一次学习的小节id，方便根据id查询最近学习的课程正在学第几节
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown3.png)
  - latestLearnTime：最近一次学习时间，用于分页查询的排序：
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown4.png)
  - createTime和expireTime，也就是课程加入时间和过期时间
  ![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Unknown5.png)
---
### 日志问题
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/aa3934c2-0a59-4d3f-8027-91f2127dd657.png)
今天我在测试课程购买成功后通过mq异步将课程加入用户课程表时发现一个小问题，就是`当我将课程加入课程表后重复添加在控制台并没有输出错误消息`，我以为是我的数据库约束条件没做好，但我检查数据库发现数据并没有重复添加，而删除数据后发送请求是可以将数据成功加入到数据库的，这让我百思不得其解，不断更改日志级别还是没能将错误消息打印在控制台。最后我注意到控制台不断重复接收消息让我想到了`消费者重试机制`，这说明了程序肯定是报错了的，我检查mq后发现`error.queue`接收到了很多消息，里面正是我想要的报错`LearningLessonMapper.insert (batch index #1) failed. Cause: java.sql.BatchUpdateException: Duplicate entry '129-1549025085494521857' for key 'learning_lesson.idx_user_id'`
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/2d0762b3-9f7d-4634-b3d9-555d6c958e10.png)
**原因：异常被Spring AMQP给捕获进行处理** 因此不是`ERROR`级别的错误，不会在控制台中爆红
```java
@Bean
@ConditionalOnClass(MessageRecoverer.class)
@ConditionalOnMissingBean
public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
    // 消息处理失败后，发送到错误交换机：error.direct，RoutingKey默认是error.微服务名称
    return new RepublishMessageRecoverer(
            rabbitTemplate, ERROR_EXCHANGE, defaultErrorRoutingKey);
}
```
当然了出现这个问题主要还是我在本地只启动了这一个微服务(其他服务跑在服务器上)，导致错误消息队列中消息没被消费，我使用try catch后确实把错误消息打印出来了，记录一下这个小小发现叭~
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/0ab6c810-0904-404b-a704-0620ea2855c3.png)
