package com.ImageProcessing.service;

import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.ImageDto;
import com.ImageProcessing.entity.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    //this service hashmaps data will live jvm heap all the time because service is a bean.Not cleared by GC.
    //map.clear(); //System.gc();
    private final ConcurrentHashMap<Long, AtomicLong> userImageCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> imageTypeCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalImages = new AtomicLong(0);

    @Transactional
    @RabbitListener(queues = RabbitConfig.IMAGE_ANALYTICS_QUEUE)
    public void analyzeImage(ImageDto imageDto){
        try {
            logger.info("Analyzing image id: {} by user id: {}", imageDto.getId(), imageDto.getUserId());

            totalImages.incrementAndGet();

            userImageCounts.computeIfAbsent(imageDto.getUserId(), k -> new AtomicLong(0))
                    .incrementAndGet();

            imageTypeCounts.computeIfAbsent(imageDto.getType(), k -> new AtomicLong(0))
                    .incrementAndGet();
        }catch (Exception e){
            logger.error("Error analyzing image: {} - Error: {}", imageDto.getId(), e.getMessage());
            throw new RuntimeException("Failed to analyze image id: " + imageDto.getId(), e);
        }
    }

    public long getTotalPosts() {
        return totalImages.get();
    }

    public long getPostCountForUser(Long userId) {
        return userImageCounts.getOrDefault(userId, new AtomicLong(0)).get();
    }

    public long getTypeCount(String imageType) {
        return imageTypeCounts.getOrDefault(imageType, new AtomicLong(0)).get();
    }
}
