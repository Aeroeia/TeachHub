# TeachHub
## 整体架构
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tianji-system.jpg)
- 点赞 zset set
- xxl-job配置使用
- Interceptor顺序
- xxl-job集群分片
- xxl-job执行器 一个实例对应一个执行器 调度器分配
- redis del和unlink区别 同步/异步 立即释放内存
- 分表 动态表名
- 任务链 存在bug
- redis管道
- 兑换码 BitMap
- 异步生成兑换码 
- 思考如何解决兑换码、优惠券过期清理问题
- 乐观锁悲观锁 String.intern()
- 事务失效 锁问题
- 异步意义 削峰填谷
## 一、我的课表模块开发 
>Tips:在分布式系统中，使用数据库自增ID容易造成性能瓶颈和ID冲突，因为多个节点同时生成ID需要依赖数据库集中控制。而雪花算法（Snowflake）能在不同节点上本地高效、唯一地生成ID，避免分布式锁和数据库竞争问题，具有高可用、无中心、趋势递增等优点。因此，分布式系统推荐采用雪花算法而非默认的自增ID
---
### 业务流程分析
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/drawio.png)

#### 页面开发规则&接口设计
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
## 二、学习计划和进度模块开发
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
## 三、高并发优化
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
#### 延迟队列配置
**DelayTask**
```java
@Data
public class DelayTask<D> implements Delayed {
    private D data;
    private long deadlineNanos;

    public DelayTask(D data, Duration delayTime) {
        this.data = data;
        this.deadlineNanos = System.nanoTime() + delayTime.toNanos();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(0, deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long l = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        if(l > 0){
            return 1;
        }else if(l < 0){
            return -1;
        }else {
            return 0;
        }
    }
}
```
**延迟队列**
```java
private final DelayQueue<DelayTask<RecordTaskData>> queue = new DelayQueue<>();
@Data
    @NoArgsConstructor
    private static class RecordCacheData {
        private Long id;
        private Integer moment;
        private Boolean finished;

        public RecordCacheData(LearningRecord record) {
            this.id = record.getId();
            this.moment = record.getMoment();
            this.finished = record.getFinished();
        }
    }

    @Data
    @NoArgsConstructor
    private static class RecordTaskData {
        private Long lessonId;
        private Long sectionId;
        private Integer moment;

        public RecordTaskData(LearningRecord record) {
            this.lessonId = record.getLessonId();
            this.sectionId = record.getSectionId();
            this.moment = record.getMoment();
        }
    }
```
#### 多线程优化
**线程池配置**
```java
static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
        5,
        16,
        60,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<>(10)
);
```
**多线程获取延迟队列任务**
```java
private void handleDelayTask() {

    while(begin){
        try {
            // 1.尝试获取任务
            log.info("尝试获取任务");
            DelayTask<RecordTaskData> task = queue.take();
            log.debug("获取到要处理的播放记录任务");
            poolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    // 2.读取Redis缓存
                    RecordTaskData data = task.getData();
                    LearningRecord record = readRecordCache(data.getLessonId(), data.getSectionId());
                    if(record==null){
                        return;
                    }
                    // 3.比较数据
                    if (!Objects.equals(data.getMoment(), record.getMoment())) {
                        // 4.如果不一致，播放进度在变化，无需持久化
                        return;
                    }
                    // 5.如果一致，证明用户离开了视频，需要持久化
                    // 5.1.更新学习记录
                    record.setFinished(null);
                    recordMapper.updateById(record);
                    // 5.2.更新课表
                    LearningLesson lesson = new LearningLesson();
                    lesson.setId(data.getLessonId());
                    lesson.setLatestSectionId(data.getSectionId());
                    lesson.setLatestLearnTime(LocalDateTime.now());
                    lessonService.updateById(lesson);

                    log.debug("准备持久化学习记录信息");
                }
            });

        } catch (Exception e) {
            log.error("处理播放记录任务发生异常", e);
        }
    }

}
```
## 四、问答系统开发
### 产品原型
#### 1.课程详情页
在用户已经登录的情况下，如果用户购买了课程，在课程详情页可以看到一个互动问答的选项卡：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/1280X1280.PNG)
问答选项卡如下：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/2.PNG)
```
1. 问答列表
- 问答列表可以选择全部问题还是我的问题，选择我的问题则只展示我提问的问题。默认是全部
- 选择章节序号，根据章节号查看章节下对应问答。默认展示所有章节的问题
- 对于我提问的问题，可以做删除、修改操作
2. 跳转逻辑
- 点击提问按钮，进入问题编辑页面
- 点击问题标题，进入问题详情页
- 点击问题下的回答，进入回答表单
```

点击提问或编辑按钮会进入问题编辑页面：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/3.png)
```
1. 表单内容
- 课程：问题一定关联提问时所在的课程，无需选择
- 章节：可以选择提问知识点对应的章节，也可以不选
- 问题标题：一个概括性描述
- 问题详情：详细问题信息，富文本
- 是否匿名：用户可以选择匿名提问，其它用户不可见提问者信息
```

点击某个问题，则会进入问题详情页面：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/4.png)
```
1. 页面内容
- 顶部展示问题相关详细信息
- 任何人都可以对问题做回复，也可以对他人的回答再次回复，无限叠楼。
- 也没渲染只分两层：
  - 对问题的一级回复，称为回答
  - 对回答的回复、对回复的回复，作为第二级，称为评论
- 问题详情页下面展示问题下的所有回答
- 点击回答下的详情才展示二级评论
- 可以对评论、回答点赞
```
#### 2.视频学习页
另外，在视频学习页面中同样可以看到互动问答功能：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/5.png)
这个页面与课程详情页功能类似，只不过是在观看视频的过程中操作。用户产生学习疑问是可以快速提问，不用退回到课程详情页，用户体验较好。
```
1. 页面逻辑
- 默认展示视频播放小节下的问答
- 用户可以在这里提问问题，自动与当前课程、当前视频对应章节关联。其它参数与课程详情页的问题表单类似。
- 问答列表默认只显示问题，点击后进入问题详情页才能查看具体答案
```
#### 3.管理端问答管理页
除了用户端以外，管理端也可以管理互动问答，首先是一个列表页：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/6.png)
```
1. 搜索
- 管理员可以搜索用户提出的所有问题
- 搜索结果可以基于页面过滤条件做过滤
  - 问题状态：已查看、未查看两种。标示是否已经被管理员查看过。每当学员在问题下评论，状态重置为未查看
  - 课程名称：由于问题是提问在课程下的，所以会跟课程关联。管理员输入课程名称，搜索该课程下的所有问题
  - 提问时间：提出问题的时间

2. 页面列表
- 默认按照提问时间倒序排列；点击回答数量时可以根据回答数量排序
- 课程分类：需要展示问题所属课程的三级分类的名称的拼接
- 课程所属章节：如果是在视频页面提问，则问题会与视频对应的章、节关联，则此处显示章名称、节名称。
- 课程名称：提问是针对某个课程的，因此此处显示对应的课程名称
- 回答数量：该问题下的一级回复，称为回答。此处显示问题下的回答的数量，其它评论不统计。
- 用户端状态：隐藏/显示。表示是否在用户端展示，对于一些敏感话题，管理员可以直接隐藏问题。

3. 操作
- 点击查看：会将该问题标记为已查看状态，并且跳转到问题详情页
- 点击隐藏或显示：控制该问题是否在用户端显示。隐藏问题，则问题下的所有回答和恢复都被隐藏
```
点击查看按钮，会进入一个问题详情页面：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/7.png)
```
1. 问题详情
- 页面顶部是问题详情，展示信息与问题列表页基本一致
- 点击评论，老师可以回答问题
- 点击隐藏/显示，可以隐藏或显示问题
2. 回答列表
- 分页展示问题下的回答（一级回复）
- 可以对回答点赞、评论、隐藏
- 点击查看，则进入回答详情页
```
继续点击查看更多按钮，可以进入回答详情页：
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/8.png)
```
1. 回答详情
- 页面顶部是回答详情，展示信息与回答列表页基本一致
- 点击我来评论，老师可以评论该回答
- 点击隐藏/显示，可以隐藏或显示该回答，该回答下的所有评论也都会被隐藏或显示
2. 评论列表
- 分页展示回答下的评论
- 可以对评论点赞、回复、隐藏
```
### 接口设计
| 编号 | 接口简述                              |
|------|-------------------------------------|
| **互动问题相关接口**                |                                     |
| 1    | 新增互动问题                        |
| 2    | 修改互动问题                        |
| 3    | 分页查询问题（用户端）              |
| 4    | 根据id查询问题详情（用户端）        |
| 5    | 删除我的问题                        |
| 6    | 分页查询问题（管理端）              |
| 7    | 根据id查询问题详情（管理端）        |
| 8    | 隐藏或显示指定问题（管理端）        |
| **回答及评论相关接口**              |                                     |
| 1    | 新增回答或评论                      |
| 2    | 分页查询回答或评论列表              |
| 4    | 隐藏或显示指定回答或评论（管理端）  |
---
### ER图
#### 1.问题的ER图
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/whiteboard_exported_image-3.png)
#### 2..回答、评论的ER图
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/whiteboard_exported_image-2.png)

### 管理端分页查询问题
#### Es使用
**ip配置**
**引入依赖**
```xml
<properties>
  <elasticsearch.version>7.12.1</elasticsearch.version>
<properties>

<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
</dependency>
```
```yaml
elasticsearch:
  uris: http://192.168.150.101:9200
```
**根据课程名查询课程id**
```java
@Autowired
private RestHighLevelClient restClient;

@Override
public List<Long> queryCoursesIdByName(String keyword) {
    // 1.创建Request
    SearchRequest request = new SearchRequest(CourseRepository.INDEX_NAME);
    // 2.构建DSL
    request.source()
            .query(QueryBuilders.matchPhraseQuery(CourseRepository.DEFAULT_QUERY_NAME, keyword))
            .fetchSource(new String[]{"id"}, null);
    // 3.查询
    SearchResponse response;
    try {
        response = restClient.search(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
        throw new CommonException(SearchErrorInfo.QUERY_COURSE_ERROR, e);
    }
    // 4.解析
    SearchHits searchHits = response.getHits();
    // 4.1.获取hits
    SearchHit[] hits = searchHits.getHits();
    if (hits.length == 0) {
        return CollUtils.emptyList();
    }
    // 4.2.获取id
    return Arrays.stream(hits)
            .map(SearchHit::getId)
            .map(Long::valueOf)
            .collect(Collectors.toList());
}
```
#### 三级缓存
在管理端分页查询问题的时候，需要查询课程的分类信息，而课程的分类有三级
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/cata.png)
每一个课程都与第三级分类关联，因此向上级追溯，也有对应的二级、一级分类。在课程微服务提供的查询课程的接口中，可以看到返回的课程信息中就包含了关联的一级、二级、三级分类。因此，只要我们查询到了问题所属的课程，就能知道课程关联的三级分类id。  
这里有一个值得思考的点：课程分类数据在很多业务中都需要查询，这样的数据如此频繁的查询，就需要用到缓存来提高性能

像这样的数据，除了建立Redis缓存以外，还非常适合做本地缓存（Local Cache）。这样就可以形成多级缓存机制：
- 数据查询时优先查询本地缓存
- 本地缓存不存在，再查询Redis缓存
- Redis不存在，再去查询数据库。
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day05/whiteboard_exported_image.png)
#### Caffeine
**配置**
```java
@Configuration
public class CategoryCacheConfig {
    /**
     * 课程分类的caffeine缓存
     */
    @Bean
    public Cache<String, Map<Long, CategoryBasicDTO>> categoryCaches(){
        return Caffeine.newBuilder()
                .initialCapacity(1) // 容量限制
                .maximumSize(10_000) // 最大内存限制
                .expireAfterWrite(Duration.ofMinutes(30)) // 有效期
                .build();
    }
    /**
     * 课程分类的缓存工具类
     */
    @Bean
    public CategoryCache categoryCache(
            Cache<String, Map<Long, CategoryBasicDTO>> categoryCaches, CategoryClient categoryClient){
        return new CategoryCache(categoryCaches, categoryClient);
    }
}
```
**缓存结果**
```java
    public Map<Long, CategoryBasicDTO> getCategoryMap() {
        return categoryCaches.get(Constant.CAFFEINE_CACHE_NAME, key -> {
            // 1.从CategoryClient查询
            List<CategoryBasicDTO> list = categoryClient.getAllOfOneLevel();
            if (list == null || list.isEmpty()) {
                return CollUtils.emptyMap();
            }
            // 2.转换数据
            return list.stream().collect(Collectors.toMap(CategoryBasicDTO::getId, Function.identity()));
        });
    }

    public String getCategoryNames(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return "";
        }
        // 1.读取分类缓存
        Map<Long, CategoryBasicDTO> map = getCategoryMap();
        // 2.根据id查询分类名称并组装
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            sb.append(map.get(id).getName()).append("/");
        }
        // 3.返回结果
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
```
## 五、点赞系统
### 业务流程
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-remark/whiteboard_exported_image.png)
该业务可以采用以下思路进行实现:
1. 用户点赞后查询Redis是否存在该用户点赞记录(set)，若存在则直接返回，不存在则在redis新增点赞记录(zset)，采用定时任务，定期将数据通过mq发送到对应业务微服务更新点赞数量，同时清除zset中的数据  
2. 查询用户是否点赞远程微服务通过feign接口调用remark服务，使用redis管道连接功能提高遍历效率
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-remark/redisopt.png)
### ER图
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-remark/whiteboard_exported_image-2.png)
### Mq问题
我在调试用户点赞后更新远程微服务点赞数量的时候出现了以下报错
```
message_id:	121fc91d-0753-49c9-8da8-3a15aa91b5ce
priority:	0
delivery_mode:	2
headers:	
__ContentTypeId__:	java.lang.Object
__TypeId__:	java.util.ArrayList
requestId:	dfce9a6002c544499792d8e5c1e453bd
x-exception-message:	Failed to convert Message content
x-exception-stacktrace:	org.springframework.amqp.rabbit.support.ListenerExecutionFailedException: Failed to convert message
at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:156)
at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1670)
at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1589)
at jdk.internal.reflect.GeneratedMethodAccessor197.invoke(Unknown Source)
at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
at java.base/java.lang.reflect.Method.invoke(Unknown Source)
at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:344)
at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:198)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
at org.springframework.retry.interceptor.RetryOperationsInterceptor$1.doWithRetry(RetryOperationsInterceptor.java:97)
at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:329)
at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:225)
at org.springframework.retry.interceptor.RetryOperationsInterceptor.invoke(RetryOperationsInterceptor.java:122)
at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:215)
at org.springframework.amqp.rabbit.listener.$Proxy204.invokeListener(Unknown Source)
at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1577)
at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1568)
at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1512)
at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:993)
at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:940)
at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.access$1600(SimpleMessageListenerContainer.java:84)
at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1317)
at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1223)
at java.base/java.lang.Thread.run(Unknown Source)
Caused by: org.springframework.amqp.support.converter.MessageConversionException: Failed to convert Message content
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.doFromMessage(AbstractJackson2MessageConverter.java:350)
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.fromMessage(AbstractJackson2MessageConverter.java:309)
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.fromMessage(AbstractJackson2MessageConverter.java:292)
at org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener.extractMessage(AbstractAdaptableMessageListener.java:342)
at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter$MessagingMessageConverterAdapter.extractPayload(MessagingMessageListenerAdapter.java:366)
at org.springframework.amqp.support.converter.MessagingMessageConverter.fromMessage(MessagingMessageConverter.java:132)
at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.toMessagingMessage(MessagingMessageListenerAdapter.java:243)
at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:146)
... 24 more
Caused by: com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize value of type `com.tianji.api.dto.remark.LikedTimesDTO` from Array value (token `JsonToken.START_ARRAY`)
at [Source: (String)"[{"bizId":"1588103282121805825","likedTimes":1}]"; line: 1, column: 1]
at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59)
at com.fasterxml.jackson.databind.DeserializationContext.reportInputMismatch(DeserializationContext.java:1741)
at com.fasterxml.jackson.databind.DeserializationContext.handleUnexpectedToken(DeserializationContext.java:1515)
at com.fasterxml.jackson.databind.DeserializationContext.handleUnexpectedToken(DeserializationContext.java:1462)
at com.fasterxml.jackson.databind.deser.BeanDeserializer._deserializeFromArray(BeanDeserializer.java:638)
at com.fasterxml.jackson.databind.deser.BeanDeserializer._deserializeOther(BeanDeserializer.java:210)
at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:186)
at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:323)
at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4674)
at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3629)
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.convertBytesToObject(AbstractJackson2MessageConverter.java:411)
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.convertContent(AbstractJackson2MessageConverter.java:378)
at org.springframework.amqp.support.converter.AbstractJackson2MessageConverter.doFromMessage(AbstractJackson2MessageConverter.java:347)
... 31 more
x-original-exchange:	like.record.topic
x-original-routingKey:	QA.times.changed
```
调试的时候我发现一个很奇异的现象：发送MQ请求无法更新点赞数量，但是偶然又能成功更新数据库，让我百思不得其解  
起初我查看error消息队列有新增异常的时候返回来看idea控制台learning服务并没有打印任何东西，我以为类型转换错误不会打印消息会直接走`MessageRecoverer`，思来想去半个多小时突然想到**MQ好像不依赖nacos**，所以出现这个问题的原因是我本地写的代码没有推送到服务器上更新服务器上的服务，**即使我在nacos让服务下线，但该服务的MQ还是能正常进行消费**，这也解释了为什么前面偶然能成功更新而有时候又不行，是因为部分走了服务器上的消费者而部分走了本地服务  
*我还一直以为是我序列化有问题🤦 记录一下这半个多小时的折腾吧哈哈*
## 六、积分系统
### 接口设计
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/1.png)
### 数据库ER图
#### 签到记录
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/whiteboard_exported_image.png)
#### 积分记录
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/whiteboard_exported_image-2.png)
#### 排行榜
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/whiteboard_exported_image-3.png)
### 签到功能实现--BitMap
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/sign.png)
我们知道二进制是计算机底层最基础的存储方式了，其中的每一位数字就是计算机信息量的最小单位了，称之为bit，一个月最多也就 31 天，因此一个月的签到记录最多也就使用 31 bit 就能保存了，还不到 4 个字节。
而如果用到我们前面讲的数据库方式来保存相同数据，则要使用数百字节，是这种方式的上百倍都不止。
可见，这种用二进制位保存签到记录的方式，是不是非常高效啊！

像这种把每一个二进制位，与某些业务数据一一映射（本例中是与一个月的每一天映射），然后用二进制位上的数字0和1来标识业务状态的思路，称为位图。也叫做BitMap.

这种数据统计的方式非常节省空间，因此经常用来做各种数据统计。比如大名鼎鼎的布隆过滤器就是基于BitMap来实现的。

OK，那么利用BitMap我们就能直接实现签到功能，并且非常节省内存，还很高效。所以就无需通过数据库来操作了。
>BitMap用法合集:https://redis.io/docs/latest/commands/ 

**BitMap基本用法**
```java
 @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    //bitset
    void bitSet(){
        Boolean b = redisTemplate.opsForValue().setBit("test", 1, true);
        System.out.println(b);
    }

    @Test
    //bitget
    void bitGet(){
        //返回bitfield集合
        List<Long> longs = redisTemplate.opsForValue().bitField("test",
                BitFieldSubCommands.create().get(BitFieldSubCommands.
                //获取位数
                BitFieldType.unsigned(3))
                //从0索引开始
                .valueAt(0));
        Long l = longs.get(0);
        System.out.println(Long.toBinaryString(l));
    }
```
>Redis最基础的数据类型只有5种：String、List、Set、SortedSet、Hash，其它特殊数据结构大多都是基于以上5这种数据类型。
BitMap也不例外，它是基于String结构的。因为Redis的String类型底层是SDS，也会存在一个字节数组用来保存数据。而Redis就提供了几个按位操作这个数组中数据的命令，实现了BitMap效果。  
由于String类型的最大空间是512MB，也就是2的31次幂个bit，因此可以保存的数据量级是十分恐怖的
---
### 积分功能
由积分规则可知，获取积分的行为多种多样，而且每一种行为都有自己的独立业务。而这些行为产生的时候需要保存一条积分明细到数据库。
我们显然不能要求其它业务的开发者在开发时帮我们新增一条积分记录，这样会导致原有业务与积分业务耦合。因此必须采用异步方式，将原有业务与积分业务解耦。
如果有必要，甚至可以将积分业务抽离，作为独立微服务。

**该业务我们使用MQ进行解耦**
![](https://jiangdata.oss-cn-guangzhou.aliyuncs.com/tjxt/tj-learning/day07/whiteboard_exported_image-4.png)
## 七、排行榜
这个模块主要涉及两个功能的实现
1. 实时排行榜
实时榜需要快速获得
2. 历史排行榜


###  实时排行榜
