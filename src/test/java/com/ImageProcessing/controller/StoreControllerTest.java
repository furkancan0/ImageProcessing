package com.ImageProcessing.controller;

import com.ImageProcessing.dto.SearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithUserDetails("2331ef@gmail.com")
@SpringBootTest
@AutoConfigureMockMvc
public class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile mockImageFile;

    InputStream input = new FileInputStream("src/test/resources/test.jpg");

    public StoreControllerTest() throws FileNotFoundException {
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
    @DisplayName("[200] GET /api/v1/store/images - Get Images")
    void getImagesWithDefaultPageable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*]", hasSize(4)))
                .andExpect(jsonPath("$[0].type").value("image/jpeg"));
    }

    @Test
    @DisplayName("[200] GET /api/v1/store/images - Get Images Pageable")
    void getImagesWithCustomPageAndSize() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "imageDate,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("[200] GET /api/v1/store/images/search - Get Images by Query")
    void getImagesWithQueryAndPageable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images/search")
                        .param("query", "wall")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "imageDate,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("wallpaperflare.com_wallpaper (1).jpg"));
    }

    @Test
    @DisplayName("[200] POST /api/v1/store/images/filter - Get Images by Filter")
    void getImagesByFilter() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setTypes(List.of("png","jpeg"));
        searchRequest.setDateStart(LocalDateTime.now().minusDays(7));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/store/images/filter")
                        .content(objectMapper.writeValueAsString(searchRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(4)))
                .andExpect(jsonPath("$[0].type").value("image/jpeg"));
    }

    @Test
    @DisplayName("[200] GET /api/v1/store/{imageId} - Download Image")
    void downloadImage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("image/jpeg"))
                .andExpect(jsonPath("$.name").value("wallpaperflare.com_wallpaper (1).jpg"));
    }

    @Test
    @DisplayName("[404] GET /api/v1/store/{imageId} - Download Image Not found")
    public void downloadImageShouldReturnNotfound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_NOT_FOUND")));
    }

    @Test
    @DisplayName("[500] GET /api/v1/store/{imageId} - Download Image Bad Request")
    public void downloadImageShouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_DELETED")));
    }

    @Test
    @DisplayName("[200] POST /api/v1/store/{imageId} - Upload Image")
    public void uploadImage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/store")
                        .file(mockImageFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_UPLOAD_SUCCESSFUL")));
    }

    @Test
    @DisplayName("[200] GET /api/v1/store/images/{userId} - Get User Images")
    public void getImagesByUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(4)))
                .andExpect(jsonPath("$[0].type").value("image/jpeg"));
    }

    @Test
    @DisplayName("[404] GET /api/v1/store/{imageId} - Get User Images Not found")
    public void getImagesByUserIdShouldReturnNotfound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images/{userId}", 2L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", Matchers.is("PRIVATE_USER")));
    }

    @Test
    @DisplayName("[200] DELETE /api/v1/store/{imageId} - Delete Image")
    public void deleteImage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/store/{imageId}", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.is("YOUR_IMAGE_WAS_DELETED")));
    }

    @Test
    @DisplayName("[200] DELETE /api/v1/store/{imageId} - Delete Image Not found")
    public void deleteImageShouldReturnNotfound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/store/{imageId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_NOT_FOUND")));
    }
}
