package com.shop.product.controller;

import com.shop.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    @Value("${shop.upload.path}")
    private String uploadPath;

    @PostMapping("/api/admin/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "product") String type) {
        try {
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("无法创建上传目录");
            }
            BufferedImage source = ImageIO.read(file.getInputStream());
            if (source == null) {
                return Result.fail(400, "仅支持 JPG/PNG 图片");
            }
            int size = "icon".equalsIgnoreCase(type) ? 128 : 512;
            BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = output.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int side = Math.min(source.getWidth(), source.getHeight());
            int sx = (source.getWidth() - side) / 2;
            int sy = (source.getHeight() - side) / 2;
            g.drawImage(source, 0, 0, size, size, sx, sy, sx + side, sy + side, null);
            g.dispose();
            String filename = UUID.randomUUID().toString().replace("-", "") + ".png";
            ImageIO.write(output, "png", new File(dir, filename));
            return Result.ok("/api/product/uploads/" + filename);
        } catch (Exception e) {
            log.error("Upload failed", e);
            return Result.fail(500, "图片上传失败");
        }
    }

    @GetMapping("/api/product/uploads/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        try {
            Path path = Path.of(uploadPath, filename).normalize();
            if (!path.startsWith(Path.of(uploadPath).normalize()) || !Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(path);
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType == null ? "application/octet-stream" : contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
