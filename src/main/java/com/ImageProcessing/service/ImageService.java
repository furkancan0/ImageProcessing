package com.ImageProcessing.service;

import com.ImageProcessing.dto.Coordinates;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.repository.ImageRepository;
import com.ImageProcessing.util.ImageUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

@Service
@Transactional
public class ImageService {

    @Autowired
    private ImageRepository repository;
    @Autowired
    private AuthService authService;

    public static String[] allowedFormats={"JPG", "JPEG", "PNG", "BMP", "WBMP" , "GIF"};


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
        // Return original if no rotation specified
        if (rotateDeg == null || rotateDeg.isBlank()) {
            return file.getBytes();
        }
        Scalr.Rotation rotation;

        switch (rotateDeg) {
            case "90" -> rotation = Scalr.Rotation.CW_90;
            case "180" -> rotation = Scalr.Rotation.CW_180;
            case "270" -> rotation = Scalr.Rotation.CW_270;
            default -> throw new IllegalArgumentException("Unsupported rotation degree: " + rotateDeg);
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.rotate(image , rotation);

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] grayScale(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.grayScale(image);
        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

}