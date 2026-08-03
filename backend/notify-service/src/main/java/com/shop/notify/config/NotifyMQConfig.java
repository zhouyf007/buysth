package com.shop.notify.config;

import com.shop.common.mq.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifyMQConfig {

    @Bean
    public Queue notifySendQueue() {
        return new Queue(MQConstants.Q_NOTIFY_SEND, true);
    }

    @Bean
    public Binding notifyPaidBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(notifySendQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_PAID);
    }

    @Bean
    public Binding notifyCancelledBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(notifySendQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_CANCELLED);
    }

    @Bean
    public Binding notifyShippedBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(notifySendQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_SHIPPED);
    }

    @Bean
    public Binding notifySeckillBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(notifySendQueue())
                .to(topicExchange).with(MQConstants.KEY_SECKILL_ORDER_CREATED);
    }
}

