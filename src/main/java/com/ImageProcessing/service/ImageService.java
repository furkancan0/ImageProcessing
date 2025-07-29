package com.ImageProcessing.service;

import com.ImageProcessing.dto.Coordinates;
import com.ImageProcessing.dto.ImageDataDto;
import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.repository.projection.ImageProjection;
import com.ImageProcessing.util.ImageUtils;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ImageService {

    @Autowired
    private ImageRepository repository;
    @Autowired
    private AuthService authService;

    public static String[] allowedFormats={"JPG", "JPEG", "PNG", "BMP", "WBMP" , "GIF"};

    public void uploadImage(MultipartFile file) throws IOException {
        User user = authService.getCurrentUser();

        repository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(ImageUtils.compressImage(file.getBytes()))
                .imageDate(LocalDateTime.now())
                .user(user).build());
    }

    public byte[] resizeImage(MultipartFile file, int targetSize) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.resize(image ,targetSize);

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] conversionImage(MultipartFile file, String format) throws IOException {
        if(!Arrays.asList(allowedFormats).contains(format)){
            throw new ApiRequestException("Format not supported", HttpStatus.BAD_REQUEST);
        }
        BufferedImage image = ImageIO.read(file.getInputStream());

        return ImageUtils.conversionImage(image, format);
    }

    public byte[] flipImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.flip(image);
        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] mirrorImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.mirror(image);
        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] cropImage(MultipartFile file, Coordinates coordinates) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.crop(image, coordinates.getX(), coordinates.getY()
                , coordinates.getWidth(), coordinates.getHeight());

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] rotateImage(MultipartFile file, String rotateDeg) throws IOException {
        if(Objects.equals(rotateDeg, "")){
            return file.getBytes();
        }
        Scalr.Rotation type = null;

        type = switch (rotateDeg) {
            case "90" -> Scalr.Rotation.CW_90;
            case "180" -> Scalr.Rotation.CW_180;
            case "270" -> Scalr.Rotation.CW_270;
            default -> type;
        };
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.rotate(image , type);

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] grayScale(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.grayScale(image);

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

}