package com.shop.order.config;

import com.shop.common.mq.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderMQConfig {

    @Bean
    public Queue orderSeckillQueue() {
        return new Queue(MQConstants.Q_ORDER_SECKILL, true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(MQConstants.Q_ORDER_PAID, true);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(MQConstants.Q_ORDER_TIMEOUT, true);
    }

    @Bean
    public Queue orderShippedQueue() {
        return new Queue(MQConstants.Q_ORDER_SHIPPED, true);
    }

    @Bean
    public Queue orderTimeoutTtlQueue() {
        return QueueBuilder.durable(MQConstants.Q_ORDER_TIMEOUT_TTL)
                .withArgument("x-message-ttl", MQConstants.ORDER_TIMEOUT_MS)
                .withArgument("x-dead-letter-exchange", MQConstants.TOPIC_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MQConstants.KEY_ORDER_TIMEOUT)
                .build();
    }

    @Bean
    public Binding seckillBinding(org.springframework.amqp.core.TopicExchange topicExchange) {
        return BindingBuilder.bind(orderSeckillQueue())
                .to(topicExchange).with(MQConstants.KEY_SECKILL_ORDER_CREATED);
    }

    @Bean
    public Binding paidBinding(org.springframework.amqp.core.TopicExchange topicExchange) {
        return BindingBuilder.bind(orderPaidQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_PAID);
    }

    @Bean
    public Binding timeoutBinding(org.springframework.amqp.core.TopicExchange topicExchange) {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_TIMEOUT);
    }

    @Bean
    public Binding shippedBinding(org.springframework.amqp.core.TopicExchange topicExchange) {
        return BindingBuilder.bind(orderShippedQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_SHIPPED);
    }

    @Bean
    public Binding ttlBinding(org.springframework.amqp.core.DirectExchange directExchange) {
        return BindingBuilder.bind(orderTimeoutTtlQueue())
                .to(directExchange).with(MQConstants.Q_ORDER_TIMEOUT_TTL);
    }
}

