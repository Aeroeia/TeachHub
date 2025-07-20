# TeachHub
## 整体架构
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tianji-system.jpg)
## 我的课表模块开发 
>Tips:在分布式系统中，使用数据库自增ID容易造成性能瓶颈和ID冲突，因为多个节点同时生成ID需要依赖数据库集中控制。而雪花算法（Snowflake）能在不同节点上本地高效、唯一地生成ID，避免分布式锁和数据库竞争问题，具有高可用、无中心、趋势递增等优点。因此，分布式系统推荐采用雪花算法而非默认的自增ID
---
### 业务流程分析
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/drawio.png)

#### 页面开发规则&接口设计
- 未学习，已购买课程还未开始学习，可以开始学习
- 已学习，已购买课程已开始学习，展示学习进度，可以继续学习
- 已学完，已购买课程已经学完，可以重新学习
- 已失效，已购买课程已过期，不可继续学习，只能删除课程操作
- 
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
### 表结构设计
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
## 学习计划和进度模块开发
### 业务流程分析
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/a1.png)
#### 接口设计
1. 创建学习计划
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/1.1.png)
2. 查询学习记录
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/1.2.png)
3. 提交学习记录
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/1.3.png)
4. 查询我的学习计划
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/1.4.png)
#### 数据库设计
数据表的设计要满足学习计划、学习进度的功能需求。学习计划信息在learning_lesson表中已经设计，因此我们关键是设计学习进度记录表即可。

按照之前的分析，用户学习的课程包含多个小节，小节的类型包含两种：
- 视频：视频播放进度超过50%就算当节学完
- 考试：考完就算一节学完
学习进度除了要记录哪些小节学完，还要记录学过的小节、每小节的播放的进度（方便续播）。因此，需要记录的数据就包含以下部分：
- 学过的小节的基础信息
  - 小节id
  - 小节对应的lessonId
  - 用户id：学习课程的人
- 小节的播放进度信息
  - 视频播放进度：也就是播放到了第几秒
  - 是否已经学完：播放进度有没有超过50%
  - 第一次学完的时间：用户可能重复学习，第一次从未学完到学完的时间要记录下来

再加上一些表基础字段，整张表结构就出来了：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/sql.png)
### 循环依赖问题 
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {
    private final ILearningRecordService learningRecordService;
}


@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {
    private final ILearningLessonService learningLessonService;
}

```
会产生报错
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/304218eb-f79c-4e6c-8901-5ec72f1987c8.png)

**解决方法：**
1. 采用懒注入 @Lazy
```java
@Lazy
@Autowired
private  ILearningRecordService learningRecordService;
```
2. 注入Mapper
## 高并发优化
### 流程分析
播放进度记录业务较为复杂，但是我们认真思考一下整个业务分支：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/learning.png)
- 考试：每章只能考一次，还不能重复考试。因此属于低频行为，可以忽略
- 视频进度：前端每隔15秒就提交一次请求。在一个视频播放的过程中，可能有数十次请求，但完播（进度超50%）的请求只会有一次。因此多数情况下都是更新一下播放进度即可。

也就是说，95%的请求都是在更新`learning_record`表中的`moment`字段，以及`learning_lesson`表中的正在学习的小节id和时间。  

而播放进度信息，不管更新多少次，下一次续播肯定是从最后的一次播放进度开始续播。也就是说我们只需要记住最后一次即可。因此可以采用合并写方案来降低数据库写的次数和频率，而异步写做不到。  
综上，提交播放进度业务虽然看起来复杂，但大多数请求的处理很简单，就是更新播放进度。并且播放进度数据是可以合并的（覆盖之前旧数据）。我们建议采用合并写请求方案：

![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/redis.png)
---
### Redis数据结构设计
这条业务支线的流程如下：
- 查询播放记录，判断是否存在
  - 如果不存在，新增一条记录
  - 如果存在，则更新学习记录
- 判断当前进度是否是第一次学完
  - 播放进度要超过50%
  - 原本的记录状态是未学完
- 更新课表中最近学习小节id、学习时间

这里有多次数据库操作，例如：
- 查询播放记录：需要知道播放记录是否存在、播放记录当前的完成状态
- 更新播放记录：更新播放进度
- 更新最近学习小节id、时间

一方面我们要缓存写数据，减少写数据库频率；另一方面我们要缓存播放记录，减少查询数据库。因此，缓存中至少要包含3个字段：
- 记录id：id，用于根据id更新数据库
- 播放进度：moment，用于缓存播放进度
- 播放状态（是否学完）：finished，用于判断是否是第一次学完

既然一个课程包含多个小节，我们完全可以把一个课程的多个小节作为一个KEY来缓存

**Redis结构设计如下**
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/efd41a70-e24b-43bd-b893-63d41757c5fd.png)
这样做有两个好处：
- 可以大大减少需要创建的KEY的数量，减少内存占用。
- 一个课程创建一个缓存，当用户在多个视频间跳转时，整个缓存的有效期都会被延续，不会频繁的创建和销毁缓存数据

---
### 持久化思路
对于合并写请求方案，一定有一个步骤就是持久化缓存数据到数据库。一般采用的是定时任务持久化：
但是定时任务的持久化方式在播放进度记录业务中存在一些问题，主要就是时效性问题。我们的产品要求视频续播的时间误差不能超过30秒。
- 假如定时任务间隔较短，例如20秒一次，对数据库的更新频率太高，压力太大
- 假如定时任务间隔较长，例如2分钟一次，更新频率较低，续播误差可能超过2分钟，不满足需求

那么问题来了，有什么办法能够在不增加数据库压力的情况下，保证时间误差较低吗？

假如一个视频时长为20分钟，我们从头播放至15分钟关闭，每隔15秒提交一次播放进度，大概需要提交60次请求。
但是下一次我们再次打开该视频续播的时候，肯定是从最后一次提交的播放进度来续播。也就是说续播进度之前的N次播放进度都是没有意义的，都会被覆盖。
既然如此，我们完全没有必要定期把这些播放进度写到数据库，只需要将用户最后一次提交的播放进度写入数据库即可。    

但问题来了，我们怎么知道哪一次提交是最后一次提交呢？
>只要用户一直在提交记录，Redis中的播放进度就会一直变化。如果Redis中的播放进度不变，肯定是停止了播放，是最后一次提交。

因此，我们只要能判断Redis中的播放进度是否变化即可。怎么判断呢？
每当前端提交播放记录时，我们可以设置一个延迟任务并保存这次提交的进度。等待20秒后（因为前端每15秒提交一次，20秒就是等待下一次提交），检查Redis中的缓存的进度与任务中的进度是否一致。
- 不一致：说明持续在提交，无需处理
- 一致：说明是最后一次提交，更新学习记录、更新课表最近学习小节和时间到数据库中

**流程如下**
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/op.png)
## 问答系统
### 三级缓存
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/Caffeine_Redis.png)