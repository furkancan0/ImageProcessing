package com.ImageProcessing.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }

    public static final String IMAGE_FANOUT_EXCHANGE = "image.fanout.exchange";

    public static final String IMAGE_THUMBNAIL_QUEUE = "image.thumbnail.queue";
    public static final String IMAGE_THUMBNAIL_RETRY_QUEUE = "image.thumbnail.retry.queue";
    public static final String IMAGE_THUMBNAIL_DLQ = "image.thumbnail.dlq";

    public static final String IMAGE_NOTIFICATION_QUEUE = "image.notification.queue";
    public static final String IMAGE_NOTIFICATION_DLQ = "image.notification.dlq";

    public static final String IMAGE_ANALYTICS_QUEUE = "image.analytics.queue";
    public static final String IMAGE_ANALYTICS_DLQ = "image.analytics.dlq";

    // -------------------- Exchange --------------------
    @Bean
    public FanoutExchange imageFanoutExchange() {
        return new FanoutExchange(IMAGE_FANOUT_EXCHANGE);
    }

    // -------------------- Thumbnail Queues --------------------
    @Bean
    public Queue imageThumbnailQueue() {
        return QueueBuilder.durable(IMAGE_THUMBNAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", IMAGE_THUMBNAIL_DLQ)
                .build();
    }

    @Bean
    public Queue imageThumbnailRetryQueue() {
        return QueueBuilder.durable(IMAGE_THUMBNAIL_RETRY_QUEUE)
                .withArgument("x-message-ttl", 10000) // 10 seconds delay before requeue
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", IMAGE_THUMBNAIL_QUEUE)
                .build();
    }

    @Bean
    public Queue imageThumbnailDLQ() {
        return QueueBuilder.durable(IMAGE_THUMBNAIL_DLQ).build();
    }

    @Bean
    public Binding imageThumbnailBinding(FanoutExchange imageFanoutExchange) {
        return BindingBuilder.bind(imageThumbnailQueue()).to(imageFanoutExchange);
    }

    // -------------------- Notification Queues --------------------
    @Bean
    public Queue imageNotificationQueue() {
        return QueueBuilder.durable(IMAGE_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", IMAGE_NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue imageNotificationDLQ() {
        return QueueBuilder.durable(IMAGE_NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding imageNotificationBinding(FanoutExchange imageFanoutExchange) {
        return BindingBuilder.bind(imageNotificationQueue()).to(imageFanoutExchange);
    }

    // -------------------- Analytics Queues --------------------
    @Bean
    public Queue imageAnalyticsQueue() {
        return QueueBuilder.durable(IMAGE_ANALYTICS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", IMAGE_ANALYTICS_DLQ)
                .build();
    }

    @Bean
    public Queue imageAnalyticsDLQ() {
        return QueueBuilder.durable(IMAGE_ANALYTICS_DLQ).build();
    }

    @Bean
    public Binding imageAnalyticsBinding(FanoutExchange imageFanoutExchange) {
        return BindingBuilder.bind(imageAnalyticsQueue()).to(imageFanoutExchange);
    }

}
