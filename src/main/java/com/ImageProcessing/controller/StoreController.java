package com.ImageProcessing.controller;

import com.ImageProcessing.dto.ImageDataDto;
import com.ImageProcessing.dto.SearchRequest;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.service.ImageService;
import com.ImageProcessing.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService service;

    @GetMapping
    public List<ImageProjection> getImagesPage(@PageableDefault(size = 10, sort = "imageDate",
            direction = Sort.Direction.DESC) Pageable pageable) {
            //Get author images
            return service.getImages(pageable);
    }

    @GetMapping("/q")
    public List<ImageProjection> getImagesQuery(@PageableDefault(size = 10, sort = "imageDate", direction = Sort.Direction.DESC) Pageable pageable,
                                           @RequestParam(name = "query", defaultValue = "") String query) {
        return service.searchImages(query, pageable);
    }

    @GetMapping("/search")
    public List<ImageProjection> getImagesByFilter(@RequestBody SearchRequest searchRequest) {
        return service.searchImagesByFilter(searchRequest.getTypes(), searchRequest.getDateStart(), searchRequest.getDateEnd());
    }

    //public List<ImageProjection> getImagesByFilter(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime start)

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{imageId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long imageId) {
        ImageProjection imageProjection = service.downloadImage(imageId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageProjection);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        String message = service.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/{userId}")
    public List<ImageProjection> getImagesByUserId(@PageableDefault(size = 10, sort = "imageDate", direction = Sort.Direction.DESC) Pageable pageable,
                                                 @PathVariable("userId") Long userId) {
        return service.getImagesByUserId(userId, pageable);
    }

    @DeleteMapping(("/{imageId}"))
    public ResponseEntity<String> deleteTweet(@PathVariable("imageId") Long imageId) {
        return ResponseEntity.ok(service.deleteImage(imageId));
    }
}
