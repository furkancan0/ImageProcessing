package com.ImageProcessing.controller;

import com.ImageProcessing.dto.Coordinates;
import com.ImageProcessing.dto.ImageDataDto;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/image")
public class ImageController {

    @Autowired
    private ImageService service;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("image")MultipartFile file) throws IOException {
        service.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body("Image uploaded");
    }


    @PostMapping("/resize")
    public ResponseEntity<?> resizeImage(@RequestParam("image") MultipartFile file, @RequestParam(value = "size") String size) throws IOException {
        byte[] imageData = service.resizeImage(file, Integer.parseInt(size));
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/conversion")
    public ResponseEntity<?> conversionImage(@RequestParam("image") MultipartFile file, @RequestParam(value = "format") String format) throws IOException {
        byte[] imageData = service.conversionImage(file, format);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/flip")
    public ResponseEntity<?> flipImage(@RequestParam("image") MultipartFile file) throws IOException {
        byte[] imageData = service.flipImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/mirror")
    public ResponseEntity<?> mirrorImage(@RequestParam("image") MultipartFile file) throws IOException {
        byte[] imageData = service.mirrorImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/rotate")
    public ResponseEntity<?> rotateImage(@RequestParam("image") MultipartFile file, @RequestParam(value = "degree", required = false) String degree) throws IOException {
        byte[] imageData = service.rotateImage(file, degree);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

    @PostMapping("/grayScale")
    public ResponseEntity<?> grayScale(@RequestParam("image") MultipartFile file) throws IOException {
        byte[] imageData = service.grayScale(file);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }
    @PostMapping("/crop")
    public ResponseEntity<?> cropImage(@RequestParam("image") MultipartFile file, @RequestParam(value = "jsonStr") String jsonStr) throws IOException {
        @Valid
        Coordinates coordinates = objectMapper.readValue(jsonStr, Coordinates.class);
        byte[] imageData = service.cropImage(file, coordinates);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(imageData);
    }

}
