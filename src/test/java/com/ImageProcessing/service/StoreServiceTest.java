package com.ImageProcessing.service;

import com.ImageProcessing.ServiceTestHelper;
import com.ImageProcessing.config.RabbitConfig;
import com.ImageProcessing.dto.ImageDto;
import com.ImageProcessing.dto.UploadImageResponse;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.UserRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.util.ImageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.ImageProcessing.ServiceTestHelper.pageable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ImageUtils imageUtils;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private StoreService storeService;

    private User mockUser;

    private static final List<ImageProjection> imageProjections = Arrays.asList(
            ServiceTestHelper.createImageProjection(ImageProjection.class),
            ServiceTestHelper.createImageProjection(ImageProjection.class));
    private static final Page<ImageProjection> pageableImageProjections = new PageImpl<>(imageProjections, pageable, 10);
    private static final ImageProjection imageProjection = ServiceTestHelper.createImageProjection(ImageProjection.class);

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(2L);
        ServiceTestHelper.mockAuthenticatedUser();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getImages() {
        when(imageRepository.getAuthorImages(1L, pageable)).thenReturn(pageableImageProjections);
        assertEquals(pageableImageProjections.getContent(), storeService.getImages(pageable));
        verify(imageRepository, times(1)).getAuthorImages(1L, pageable);
    }

    @Test
    void searchImages() {
        String testQuery = "test";
        when(imageRepository.searchUserImages(1L,testQuery, pageable)).thenReturn(pageableImageProjections);
        assertEquals(pageableImageProjections.getContent(), storeService.searchImages(testQuery, pageable));
        verify(imageRepository, times(1)).searchUserImages(1L, testQuery, pageable);
    }

    @Test
    void downloadImage() {
        when(imageRepository.getImageById(10L)).thenReturn(Optional.of(imageProjection));
        assertEquals(imageProjection, storeService.downloadImage(10L));
        verify(imageRepository, times(1)).getImageById(10L);
    }

    @Test
    void downloadImage_ShouldImageNotFound() {
        when(imageRepository.getImageById(10L)).thenReturn(Optional.empty());
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> storeService.downloadImage(10L));
        assertEquals("IMAGE_NOT_FOUND", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }


    @Test
    void searchImagesByFilter() {
        List<String> tags = List.of("png");
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(5);
        when(imageRepository.getImagesByFilterParams(tags, start, end)).thenReturn(imageProjections);
        assertEquals(imageProjections.size(), storeService.searchImagesByFilter(tags, start, end).size());
        verify(imageRepository, times(1)).getImagesByFilterParams(tags, start, end);
    }

    @Test
    void uploadImage() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "testImage.jpg", "image/jpeg"
                , "image".getBytes());
        Image mockImage = new Image(1L, "testImage.jpg", "image/jpeg", "desc",
                100L, "image".getBytes(),"image".getBytes(), mockUser, LocalDateTime.now());

        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(imageRepository.save(any(Image.class))).thenReturn(mockImage);

        UploadImageResponse response = storeService.uploadImage(file);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.IMAGE_FANOUT_EXCHANGE), eq(""), any(ImageDto.class)
        );

        assertNotNull(response);
        assertEquals("Upload successful", response.getMessage());
        assertEquals(Long.valueOf(1), response.getId());
        verify(imageRepository, times(1)).save(any(Image.class));
        verify(authService, times(1)).getCurrentUser();
    }

    @Test
    void getImagesByUserId() {
        when(imageRepository.getUserImages(mockUser.getId(),pageable)).thenReturn(pageableImageProjections);
        assertEquals(pageableImageProjections.getContent(), storeService.getImagesByUserId(mockUser.getId(),pageable));
        verify(imageRepository, times(1)).getUserImages(mockUser.getId(),pageable);
    }

    @Test
    void getImagesByUserId_ShouldPrivateUser(){
        when(authService.isUserHavePrivateProfile(mockUser.getId())).thenReturn(true);
        ApiRequestException ex = assertThrows(ApiRequestException.class, () -> {
            storeService.getImagesByUserId(mockUser.getId(), pageable);
        });
        assertEquals("PRIVATE_USER", ex.getMessage());
        SecurityContextHolder.clearContext();
    }

}