package com.ImageProcessing.service;

import com.ImageProcessing.dto.Coordinates;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    InputStream input = new FileInputStream("src/test/resources/test.jpg");

    ImageServiceTest() throws FileNotFoundException {
    }

    private MockMultipartFile mockImageFile;

    @BeforeEach
    void setUp() throws IOException {
        mockImageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                Objects.requireNonNull(getClass().getResourceAsStream("/test.jpg")).readAllBytes()
        );
    }

    @Test
    void testResizeImage_ReturnsResizedImage() throws IOException {
        byte[] resized = imageService.resizeImage(mockImageFile, 100);
        assertNotNull(resized);
    }

    @Test
    void testConversionImage_ValidFormat() throws IOException {
        byte[] result = imageService.conversionImage(mockImageFile, "PNG");
        assertNotNull(result);
    }

    @Test
    void testConversionImage_UnsupportedFormat_Throws() {
        MockMultipartFile badFile = new MockMultipartFile(
                "image", "bad.plg", "image/plg", new byte[]{1, 2}
        );

        ApiRequestException ex = assertThrows(ApiRequestException.class, () ->
                imageService.conversionImage(badFile, "PLG")
        );

        assertEquals("Format not supported", ex.getMessage());
    }

    @Test
    void testGrayScale_ReturnsBytes() throws IOException {
        byte[] result = imageService.grayScale(mockImageFile);
        assertNotNull(result);
    }

    @Test
    void testRotateImage_ReturnsBytes() throws IOException {
        byte[] rotated = imageService.rotateImage(mockImageFile, "90");

        assertNotNull(rotated);
        assertTrue(rotated.length > 0);
    }

    @Test
    void testFlipImage_ReturnsBytes() throws IOException {
        byte[] flipped = imageService.flipImage(mockImageFile);
        assertNotNull(flipped);
    }

    @Test
    void testCropImage_ValidCoordinates() throws IOException {
        Coordinates coordinates = new Coordinates(0, 0, 100, 100);
        byte[] cropped = imageService.cropImage(mockImageFile, coordinates);
        assertNotNull(cropped);
    }

    @Test
    void testMirrorImage_ReturnsBytes() throws IOException {
        byte[] mirrored = imageService.mirrorImage(mockImageFile);
        assertNotNull(mirrored);
    }
}