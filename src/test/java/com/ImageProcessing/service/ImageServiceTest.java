package com.ImageProcessing.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageServiceTest {
    //test
    MockMultipartFile firstFile = new MockMultipartFile("image", "image.txt", "text/plain", "some xml".getBytes());
    @Test
    void uploadImage() {
    }
}
