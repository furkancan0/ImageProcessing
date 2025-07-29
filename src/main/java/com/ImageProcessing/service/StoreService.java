package com.ImageProcessing.service;

import com.ImageProcessing.dto.ImageDataDto;
import com.ImageProcessing.dto.SearchRequest;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StoreService {

    @Autowired
    private ImageRepository repository;
    @Autowired
    private AuthService authService;

    @Transactional(readOnly = true)
    public Page<ImageProjection> getImages(Integer page){
        User user = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "imageDate");
        return repository.getUserImages(user,pageable);
    }

    @Transactional(readOnly = true)
    public List<ImageProjection> searchImages(String query, Integer page) {
        User user = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, "imageDate");
        return repository.searchUserImages(user,query,pageable).getContent();
    }

    @Transactional(readOnly = true)
    public ImageDataDto downloadImage(Long imageId){
        Image image = repository.getImageById(imageId)
                .orElseThrow(() -> new ApiRequestException("Image Not found", HttpStatus.NOT_FOUND));
        ImageDataDto imageDataDto = new ImageDataDto();
        imageDataDto.setImageData(ImageUtils.decompressImage(image.getImageData()));
        return imageDataDto;
    }

    @Transactional(readOnly = true)
    public List<ImageProjection> searchImagesByFilter(List<String> arr, LocalDateTime start, LocalDateTime end) {
        List<ImageProjection> imageProjections = repository.getImagesByFilterParams(arr, start, end);

        System.out.println(imageProjections);

        return imageProjections;
    }

}
