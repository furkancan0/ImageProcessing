package com.ImageProcessing.service;

import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.ImageDto;
import com.ImageProcessing.dto.UploadImageResponse;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.util.AuthUtil;
import com.ImageProcessing.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final ImageRepository repository;
    private final AuthService authService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public List<ImageProjection> getImages(Pageable pageable){
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        return repository.getAuthorImages(currentUserId,pageable).getContent();
    }

    @Transactional(readOnly = true)
    public List<ImageProjection> searchImages(String query, Pageable pageable) {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        return repository.searchUserImages(currentUserId,query,pageable).getContent();
    }

    @Transactional(readOnly = true)
    public List<ImageDto> searchImagesAdvanced(String query) {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();

        return repository.search(query)
                .stream()
                .map(img -> new ImageDto(img.getId(), img.getName(), img.getType(), currentUserId, img.getImageDate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ImageProjection downloadImage(Long imageId){
        return repository.getImageById(imageId)
                .orElseThrow(() -> new ApiRequestException("IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ImageProjection> searchImagesByFilter(List<String> arr, LocalDateTime start, LocalDateTime end) {
        return repository.getImagesByFilterParams(arr, start, end);
    }

    public UploadImageResponse uploadImage(MultipartFile file) throws IOException {
        User user = authService.getCurrentUser();

        Image image = repository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .fileSize(file.getSize())
                .imageData(ImageUtils.compressImage(file.getBytes()))
                .imageDate(LocalDateTime.now())
                .user(user).build());

        ImageDto eventDto = new ImageDto(
                image.getId(), image.getName(), image.getType(), user.getId(), image.getImageDate());

        try {
            rabbitTemplate.convertAndSend(RabbitConfig.IMAGE_FANOUT_EXCHANGE, "", eventDto);

            logger.info("Published image {} to fanout exchange {}", eventDto.getId(), RabbitConfig.IMAGE_FANOUT_EXCHANGE);
        } catch (Exception e) {
            logger.error("Failed to publish post to queue: {}", e.getMessage());
            throw new RuntimeException("Failed to publish image", e);
        }

        logger.info("User {} uploaded image: {} ({} bytes)", user.getUsername(), file.getOriginalFilename(), file.getSize());
        logger.info("Image {} saved and queued for processing", image.getId());

        return new UploadImageResponse(image.getId(), "Upload successful");
    }

    @Transactional(readOnly = true)
    public List<ImageProjection> getImagesByUserId(Long userId, Pageable pageable){
        Long authenticatedUserId = AuthUtil.getAuthenticatedUserId();
        if(!authenticatedUserId.equals(userId)){
            if (authService.isUserHavePrivateProfile(userId)) {
                throw new ApiRequestException("PRIVATE_USER", HttpStatus.NOT_FOUND);
            }
        }
        return repository.getUserImages(userId,pageable).getContent();
    }

    @Transactional
    public String deleteImage(Long imageId) {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        Image image = repository.getImageByUserId(currentUserId, imageId)
                .orElseThrow(() -> new ApiRequestException("IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND));
        //image.setDeleted(true);
        return "YOUR_IMAGE_WAS_DELETED";
    }

    public Image findImageById(Long imageId) {
        return repository.findById(imageId).orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }

    public List<ImageDto> getAll() throws InterruptedException {
        return repository.findAll().stream().map(image ->
                        new ImageDto(image.getId(), image.getName(), image.getType(), image.getUser().getId(), image.getImageDate()))
                .toList();
    }
}
