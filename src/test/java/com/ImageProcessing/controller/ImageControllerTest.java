package com.ImageProcessing.controller;

import com.ImageProcessing.dto.Coordinates;
import com.ImageProcessing.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private MockMultipartFile mockImageFile;

    InputStream input = new FileInputStream("src/test/resources/test.jpg");

    public ImageControllerTest() throws FileNotFoundException {
    }

    @BeforeEach
    void setUp() throws IOException {
        mockImageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
    }
    @Test
    @DisplayName("[200] POST /api/v1/image/resize - Resize Image")
    public void resizeImageShouldReturnOk() throws Exception {
        String size = "100";

        mockMvc.perform(multipart("/api/v1/image/resize?size="+size)
                .file(mockImageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("[200] POST /api/v1/image/flip - Flip Image")
    public void flipImageShouldReturnOk() throws Exception {

        mockMvc.perform(multipart("/api/v1/image/flip")
                        .file(mockImageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("[200] POST /api/v1/image/mirror - Mirror Image")
    public void mirrorImageShouldReturnOk() throws Exception {

        mockMvc.perform(multipart("/api/v1/image/mirror")
                        .file(mockImageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("[200] POST /api/v1/image/rotate - Rotate Image")
    public void rotateImageShouldReturnOk() throws Exception {
        String degree = "";

        mockMvc.perform(multipart("/api/v1/image/rotate?degree="+degree)
                        .file(mockImageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("[400] POST /api/v1/image/rotate - Rotate Image")
    public void rotateImageShouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/image/rotate")
                        .file(mockImageFile)
                        .param("degree", "45")) // unsupported degree
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", Matchers.is("Unsupported rotation degree: 45")));
    }

    @Test
    @DisplayName("[200] POST /api/v1/image/grayScale - GrayScale Image")
    public void grayScaleImageShouldReturnOk() throws Exception {

        mockMvc.perform(multipart("/api/v1/image/grayScale")
                        .file(mockImageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("[200] POST /api/v1/image/crop - Crop Image")
    public void cropImageShouldReturnOk() throws Exception {
        Coordinates coordinates = new Coordinates(10, 20, 100, 100);
        String jsonStr = objectMapper.writeValueAsString(coordinates);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/image/crop")
                        .file(mockImageFile)
                        .param("jsonStr", jsonStr))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
}
