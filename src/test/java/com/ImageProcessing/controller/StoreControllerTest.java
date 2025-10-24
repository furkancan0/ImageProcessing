package com.ImageProcessing.controller;

import com.ImageProcessing.dto.SearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithUserDetails("test@gmail.com")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StoreControllerTest extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[200] GET /api/v1/store/images - Get Images")
    void getImagesWithDefaultPageable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*]", hasSize(5)))
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
                        .param("query", "image")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "imageDate,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("image1.jpg"));
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
                .andExpect(jsonPath("$[*]", hasSize(4)));
    }

    @Test
    @DisplayName("[200] GET /api/v1/store/{imageId} - Download Image")
    void downloadImage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("image/jpeg"))
                .andExpect(jsonPath("$.name").value("image1.jpg"));
    }

    @Test
    @DisplayName("[404] GET /api/v1/store/{imageId} - Download Image Not found")
    public void downloadImageShouldReturnNotfound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_NOT_FOUND")));
    }

    @Test
    @DisplayName("[400] GET /api/v1/store/{imageId} - Download Image Bad Request")
    public void downloadImageShouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/{imageId}", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_DELETED")));
    }


    @Test
    @DisplayName("[200] GET /api/v1/store/images/{userId} - Get User Images")
    public void getImagesByUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/store/images/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(5)))
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
    @DisplayName("[404] DELETE /api/v1/store/{imageId} - Delete Image Not found")
    public void deleteImageShouldReturnNotfound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/store/{imageId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", Matchers.is("IMAGE_NOT_FOUND")));
    }
}
