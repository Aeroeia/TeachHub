# TeachHub
## 整体架构
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tianji-system.jpg)
## Day1 
1. learning表结构设计
2. 雪花算法
3. 三接口设计、实现 添加课程到课表、分页查询课表及学习状态
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
