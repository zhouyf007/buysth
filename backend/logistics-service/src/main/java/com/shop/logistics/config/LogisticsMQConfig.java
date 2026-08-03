package com.shop.logistics.config;

import com.shop.common.mq.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogisticsMQConfig {

    @Bean
    public Queue logisticsPaidQueue() {
        return new Queue(MQConstants.Q_LOGISTICS_ORDER_PAID, true);
    }

    @Bean
    public Binding logisticsPaidBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(logisticsPaidQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_PAID);
    }
}

