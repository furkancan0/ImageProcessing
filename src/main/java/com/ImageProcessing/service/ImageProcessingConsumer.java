package com.ImageProcessing.service;

import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.ImageDto;
import com.ImageProcessing.dto.ImageProcessingRequest;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.repository.ImageRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ImageProcessingConsumer {
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private static final int MAX_RETRY_COUNT = 3;
    private final RabbitTemplate rabbitTemplate;

    private final ImageRepository imageRepository;
    private final ImageService imageService;


    @RabbitListener(queues = RabbitConfig.IMAGE_THUMBNAIL_QUEUE)
    @Transactional
    public void consumeImageProcessing(ImageDto dto, Channel channel, Message message) throws IOException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            log.info("Processing image {} with operation {}", dto.getId(), "thumbnail");
            Image image = imageRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Image not found: " + dto.getId()));

            byte[] thumbnail = imageService.createThumbnail(image, 150);
            image.setThumbnail(thumbnail);
            image.setDescription("Description "+ image.getId());

            imageRepository.save(image);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("✅ Image thumbnail for id {} processed successfully", dto.getId());

        } catch (Exception e) {
            int retryCount = getRetryCount(message);
            if (retryCount < MAX_RETRY_COUNT) {
                log.warn("Retry {}/{} for message {}", retryCount, MAX_RETRY_COUNT, body);
                incrementRetryCountAndSendToRetryQueue(message, RabbitConfig.IMAGE_THUMBNAIL_RETRY_QUEUE);
            } else {
                log.error("Max retries reached for {}. Sending to DLQ.", body);
                rabbitTemplate.send(RabbitConfig.IMAGE_THUMBNAIL_DLQ, message);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private int getRetryCount(Message message) {
        Object header = message.getMessageProperties().getHeaders().get("x-retry-count");
        return header == null ? 0 : (int) header;
    }

    private void incrementRetryCountAndSendToRetryQueue(Message message, String retryQueue) {
        int retryCount = getRetryCount(message) + 1;
        message.getMessageProperties().getHeaders().put("x-retry-count", retryCount);
        rabbitTemplate.send(retryQueue, message);
    }
}
