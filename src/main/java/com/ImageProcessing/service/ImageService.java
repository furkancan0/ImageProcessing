package com.ImageProcessing.service;


import com.ImageProcessing.dto.Coordinates;

import com.ImageProcessing.entity.Image;
import com.ImageProcessing.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor()
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final RabbitTemplate rabbitTemplate;

    public static String[] allowedFormats={"JPG", "JPEG", "PNG", "BMP", "WBMP" , "GIF"};

    /* if(!Arrays.asList(allowedFormats).contains(format)){
        throw new ApiRequestException("Format not supported", HttpStatus.BAD_REQUEST);
    }*/

    public byte[] resizeImage(MultipartFile file, int targetSize) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.resize(image ,targetSize);

        return ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
    }

    public byte[] createThumbnail(Image image, Integer width){
        byte[] bytes = ImageUtils.decompressImage(image.getImageData());
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) throw new IllegalArgumentException("Original image invalid or unsupported format");
            BufferedImage bufferedImage = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
            return ImageUtils.conversionImage(bufferedImage, image.getType());
        } catch (IOException e) {
            throw new RuntimeException("Thumbnail creation failed", e);
        }
    }

    public String flipImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.flip(image);
        byte[] imageData = ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
        logger.info("image has flipped");

        return Base64.getEncoder().encodeToString(imageData);
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

    public String grayScale(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage bufferedImage = ImageUtils.grayScale(image);
        byte[] imageData = ImageUtils.conversionImage(bufferedImage, file.getContentType().split("/")[1]);
        return Base64.getEncoder().encodeToString(imageData);
    }
}