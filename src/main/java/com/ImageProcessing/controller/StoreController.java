package com.ImageProcessing.controller;

import com.ImageProcessing.dto.ImageDataDto;
import com.ImageProcessing.dto.SearchRequest;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.service.ImageService;
import com.ImageProcessing.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/store")
public class StoreController {

    @Autowired
    private StoreService service;

    @GetMapping
    public List<ImageProjection> getImages(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                           @RequestParam(name = "query", defaultValue = "") String query) {
        if(query == null || query.trim().isEmpty()) {
            return service.getImages(page).getContent();
        }
        return service.searchImages(query, page);
    }

    @GetMapping("/search")
    public List<ImageProjection> getImagesByFilter(@RequestBody SearchRequest searchRequest) {
        return service.searchImagesByFilter(searchRequest.getTypes(), searchRequest.getDateStart(), searchRequest.getDateEnd());
    }

    //public List<ImageProjection> getImagesByFilter(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime start)

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{imageId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long imageId) {
        ImageDataDto imageData=service.downloadImage(imageId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(imageData);
    }
}
