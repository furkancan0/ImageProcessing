package com.ImageProcessing.service;

import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.ImageProcessingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DeadLetterConsumer {
    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = RabbitConfig.IMAGE_THUMBNAIL_DLQ)
    public void handleThumbnailDlq(Message failedMessage) {
        String payload = new String(failedMessage.getBody(), StandardCharsets.UTF_8);
        log.error("💀 [DLQ] Thumbnail queue failed message: {}", payload);
        // TODO: optionally retry or send alert
    }

    @RabbitListener(queues = RabbitConfig.IMAGE_NOTIFICATION_DLQ)
    public void handleNotificationDlq(Message failedMessage) {
        String payload = new String(failedMessage.getBody(), StandardCharsets.UTF_8);
        log.error("💀 [DLQ] Notification queue failed message: {}", payload);
        // TODO: optionally retry or send alert
    }
}
