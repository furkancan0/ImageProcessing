package com.ImageProcessing.service;

import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.EventDto;
import com.ImageProcessing.dto.ImageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = RabbitConfig.IMAGE_NOTIFICATION_QUEUE)
    public void sendNotifications(ImageDto dto) {
        try {
            logger.info("Processing notifications for post: {} by user: {}",
                    dto.getId(), dto.getName());

            sendPushNotification(dto.getUserId(), "Notificantion", "Image Notification");

            logger.info("Notifications sent for post: {} date {}", dto.getName(), dto.getImageDate());


        } catch (Exception e) {
            logger.error("Error sending notifications for post: {} - Error: {}",
                    dto.getId(), e.getMessage());
            throw new RuntimeException("Failed to send notifications for post: " + dto.getId(), e);
        }
    }

    private void sendPushNotification(Long userId, String title, String body) {
        logger.info("📱 PUSH NOTIFICATION - User: {}, Title: {}, Body: {}",
                userId, title, truncateText(body, 50));
    }

    private String truncateText(String text, int maxLength) {
        return text != null && text.length() > maxLength
                ? text.substring(0, maxLength) + "..."
                : text;
    }
}
