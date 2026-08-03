package com.shop.common.mq;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    @Bean
    public TopicExchange shopTopicExchange() {
        return new TopicExchange(MQConstants.TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange shopDirectExchange() {
        return new DirectExchange(MQConstants.DIRECT_EXCHANGE, true, false);
    }
}

