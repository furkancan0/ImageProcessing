package com.ImageProcessing.service;

import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.util.AuthUtil;
import com.ImageProcessing.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {

    private final ImageRepository repository;
    private final AuthService authService;

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
    public ImageProjection downloadImage(Long imageId){
        ImageProjection imageProjection = repository.getImageById(imageId)
                .orElseThrow(() -> new ApiRequestException("IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND));
        if (imageProjection.getDeleted()) {
            throw new ApiRequestException("IMAGE_DELETED", HttpStatus.BAD_REQUEST);
        }

        return imageProjection;
    }


    @Transactional(readOnly = true)
    public List<ImageProjection> searchImagesByFilter(List<String> arr, LocalDateTime start, LocalDateTime end) {
        return repository.getImagesByFilterParams(arr, start, end);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        User user = authService.getCurrentUser();

        repository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(ImageUtils.compressImage(file.getBytes()))
                .imageDate(LocalDateTime.now())
                .deleted(false)
                .user(user).build());
        return "IMAGE_UPLOAD_SUCCESSFUL";
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
        image.setDeleted(true);
        return "YOUR_IMAGE_WAS_DELETED";
    }

}
