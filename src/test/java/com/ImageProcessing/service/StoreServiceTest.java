package com.ImageProcessing.service;

import com.ImageProcessing.ServiceTestHelper;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.UserRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks
    private StoreService storeService;

    private User mockUser;

    private static final List<ImageProjection> imageProjections = Arrays.asList(
            ServiceTestHelper.createImageProjection(false, ImageProjection.class),
            ServiceTestHelper.createImageProjection(false, ImageProjection.class));
    private static final Page<ImageProjection> pageableImageProjections = new PageImpl<>(imageProjections, pageable, 10);
    private static final ImageProjection imageProjection = ServiceTestHelper.createImageProjection(false, ImageProjection.class);

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
    void downloadImage_ShouldImageDeleted() {
        ImageProjection imageProjection = ServiceTestHelper.createImageProjection(true, ImageProjection.class);
        when(imageRepository.getImageById(10L)).thenReturn(Optional.of(imageProjection));
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> storeService.downloadImage(10L));
        assertEquals("IMAGE_DELETED", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
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
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg",
                "image/jpeg", new byte[]{1, 2, 3});
        assertEquals("IMAGE_UPLOAD_SUCCESSFUL", storeService.uploadImage(file));
        verify(imageRepository, times(1)).save(any(Image.class));
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

    @Test
    void deleteTweet() {
        Image mockImage = ServiceTestHelper.createImage(false);
        when(imageRepository.getImageByUserId(1L, 10L)).thenReturn(Optional.of(mockImage));
        assertEquals("YOUR_IMAGE_WAS_DELETED", storeService.deleteImage(mockImage.getId()));
        assertTrue(mockImage.isDeleted());
        verify(imageRepository, times(1)).getImageByUserId(1L,mockImage.getId());
    }

}