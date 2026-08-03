package com.shop.product.config;

import com.shop.common.mq.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductMQConfig {

    @Bean
    public Queue productCancelledQueue() {
        return new Queue(MQConstants.Q_PRODUCT_ORDER_CANCELLED, true);
    }

    @Bean
    public Binding productCancelledBinding(TopicExchange topicExchange) {
        return BindingBuilder.bind(productCancelledQueue())
                .to(topicExchange).with(MQConstants.KEY_ORDER_CANCELLED);
    }
}

