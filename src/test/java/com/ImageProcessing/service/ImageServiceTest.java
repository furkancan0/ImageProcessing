package com.ImageProcessing.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {
    MockMultipartFile firstFile = new MockMultipartFile("image", "image.txt", "text/plain", "some xml".getBytes());
    @Test
    void uploadImage() {
    }
}