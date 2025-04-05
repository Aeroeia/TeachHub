package com.teachub.common.constants;

public interface MqConstants {
    interface Topic {
        /*课程有关的交换机*/
        String COURSE_TOPIC = "course_topic";

        /*订单有关的交换机*/
        String ORDER_TOPIC = "order_topic";

        /*学习有关的交换机*/
        String LEARNING_TOPIC = "learning_topic";

        /*信息中心短信相关的交换机*/
        String SMS_TOPIC = "sms_topic";

        /*异常信息的交换机*/
        String ERROR_TOPIC = "error_topic";

        /*支付有关的交换机*/
        String PAY_TOPIC = "pay_topic";
        /*交易服务延迟任务交换机*/
        String TRADE_DELAY_TOPIC = "trade_delay_topic";

        /*点赞记录有关的交换机*/
        String LIKE_RECORD_TOPIC = "like_record_topic";

        //促销服务交换机
        String PROMOTION_TOPIC = "promotion_topic";
    }
    
    interface Tag {
        /*课程有关的 RoutingKey*/
        String COURSE_NEW = "course_new";
        String COURSE_UP = "course_up";
        String COURSE_DOWN = "course_down";
        String COURSE_EXPIRE = "course_expire";
        String COURSE_DELETE = "course_delete";

        /*订单有关的RoutingKey*/
        String ORDER_PAY = "order_pay";
        String ORDER_REFUND = "order_refund";

        /*积分相关RoutingKey*/
        /* 写回答 */
        String WRITE_REPLY = "reply_new";
        /* 签到 */
        String SIGN_IN = "sign_in";
        /* 学习视频 */
        String LEARN_SECTION = "section_learned";
        /* 写笔记 */
        String WRITE_NOTE = "note_new";
        /* 笔记被采集 */
        String NOTE_GATHERED = "note_gathered";

        /*点赞的RoutingKey*/
        String LIKED_TIMES_KEY_TEMPLATE = "{}_times_changed";
        /*问答*/
        String QA_LIKED_TIMES_KEY = "QA_times_changed";
        /*笔记*/
        String NOTE_LIKED_TIMES_KEY = "NOTE_times_changed";

        /*短信系统发送短信*/
        String SMS_MESSAGE = "sms_message";

        /*异常RoutingKey的前缀*/
        String ERROR_KEY_PREFIX = "error_";
        String DEFAULT_ERROR_TAG = "error_all";

        /*支付有关的key*/
        String PAY_SUCCESS = "pay_success";
        String REFUND_CHANGE = "refund_status_change";

        String ORDER_DELAY = "delay_order_query";

        //领取优惠券的key
        String COUPON_RECEIVE = "coupon_receive";
    }
}
