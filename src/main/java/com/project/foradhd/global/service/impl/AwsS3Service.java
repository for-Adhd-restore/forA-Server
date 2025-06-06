package com.project.foradhd.global.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import com.project.foradhd.global.image.web.enums.ImagePathPrefix;
import com.project.foradhd.global.service.FileStorageService;
import com.project.foradhd.global.util.ImageUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AwsS3Service implements FileStorageService {

    private static final String FILE_EXTENSION_SEPARATOR = ".";
    private static final List<String> ALLOWED_IMAGE_FILE_EXTENSION_LIST = Arrays.asList("jpg", "jpeg", "png", "gif",
            "heic", "heif");
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public List<String> uploadImages(ImagePathPrefix imagePathPrefix, List<MultipartFile> images) {
        return images.stream()
                .map(image -> uploadImage(imagePathPrefix, image))
                .toList();
    }

    @Override
    public void deleteImages(List<String> imagePaths) {
        imagePaths.forEach(this::deleteImage);
    }

    private String uploadImage(ImagePathPrefix imagePathPrefix, MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }
        validateImageFileExtension(image.getOriginalFilename());
        return uploadImageToS3(imagePathPrefix, image);
    }

    private void deleteImage(String imagePath) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, imagePath);
        amazonS3.deleteObject(deleteObjectRequest);
    }

    private void validateImageFileExtension(String imageFilename) {
        String fileExtension = getFileExtension(imageFilename);
        boolean isAllowedExtension = ALLOWED_IMAGE_FILE_EXTENSION_LIST.stream()
                .anyMatch(allowedImageFileExtension -> allowedImageFileExtension.equalsIgnoreCase(fileExtension));
        if (!isAllowedExtension) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String uploadImageToS3(ImagePathPrefix imagePathPrefix, MultipartFile image) {
        String randomImageFilename = generateRandomImageFilename(image);
        String imagePath = imagePathPrefix.getPath() + randomImageFilename;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(image.getInputStream())
                    .size(1080, 1080)       // 최대 가로/세로 크기(리사이징)
                    .outputFormat("jpg")                 // 포맷 통일
                    .outputQuality(0.75f)                // 0.0 ~ 1.0 (압축률 조절)
                    .toOutputStream(os);

            byte[] compressedImageBytes = os.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(compressedImageBytes);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            metadata.setContentLength(compressedImageBytes.length);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, imagePath, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);

            return imagePath;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    private String getFileExtension(String filename) {
        int fileExtensionIndex = filename.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        if (fileExtensionIndex == -1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_FILE_EXTENSION);
        }
        return filename.substring(fileExtensionIndex + 1);
    }

    private String generateRandomImageFilename(MultipartFile image) {
        String originalImageFilename = image.getOriginalFilename();
        String fileExtension = getFileExtension(originalImageFilename);
        int[] imageDimensions = ImageUtil.getImageDimensions(image);
        return UUID.randomUUID().toString().replace("-", "")
                + "_" + imageDimensions[0] + "x" + imageDimensions[1]
                + FILE_EXTENSION_SEPARATOR + fileExtension;
    }
}
