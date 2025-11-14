package com.ImageProcessing.controller;

import com.ImageProcessing.dto.ImageDto;
import com.ImageProcessing.dto.ImageProcessingRequest;
import com.ImageProcessing.dto.SearchRequest;
import com.ImageProcessing.dto.UploadImageResponse;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.service.StoreService;
import com.ImageProcessing.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    @Autowired
    private StoreService service;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/images")
    public List<ImageProjection> getImagesPage(@PageableDefault(size = 10, sort = "imageDate",
            direction = Sort.Direction.DESC) Pageable pageable) {
        //Get author images
        return service.getImages(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/images/search")
    public List<ImageProjection> getImagesQuery(@PageableDefault(size = 10, sort = "imageDate", direction = Sort.Direction.DESC) Pageable pageable,
                                           @RequestParam(name = "query", defaultValue = "") String query) {
        return service.searchImages(query, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/images/advancesearch")
    public List<ImageDto> getImagesQuerySearch(@RequestParam(name = "query", defaultValue = "") String query) {
        return service.searchImagesAdvanced(query);
    }


    @PostMapping("/images/filter")
    public List<ImageProjection> getImagesByFilter(@RequestBody SearchRequest searchRequest) {
        return service.searchImagesByFilter(searchRequest.getTypes(), searchRequest.getDateStart(), searchRequest.getDateEnd());
    }

    //public List<ImageProjection> getImagesByFilter(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") LocalDateTime start)

    @GetMapping("/{imageId}")
    public ResponseEntity<?> downloadImage(@PathVariable Long imageId) {
        ImageProjection imageProjection = service.downloadImage(imageId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(imageProjection);
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        UploadImageResponse response = service.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/images/{userId}")
    public List<ImageProjection> getImagesByUserId(@PageableDefault(size = 10, sort = "imageDate", direction = Sort.Direction.DESC) Pageable pageable,
                                                 @PathVariable("userId") Long userId) {
        return service.getImagesByUserId(userId, pageable);
    }

    @DeleteMapping(("/{imageId}"))
    public ResponseEntity<String> deleteImage(@PathVariable("imageId") Long imageId) {
        return ResponseEntity.ok(service.deleteImage(imageId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ImageDto>> getAll() throws InterruptedException {
        return ResponseEntity.ok(service.getAll());
    }
}
