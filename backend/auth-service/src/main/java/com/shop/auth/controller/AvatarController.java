package com.shop.auth.controller;

import com.shop.common.result.Result;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@RestController
public class AvatarController {

    @Value("${shop.upload.path}")
    private String uploadPath;

    @PostMapping("/api/auth/avatar/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("无法创建上传目录");
            }
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase() : ".png";
            if (!ext.matches("\\.(png|jpg|jpeg|gif)$")) {
                return Result.fail(400, "仅支持 JPG/PNG/GIF 图片");
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            file.transferTo(new File(dir, filename));
            return Result.ok("/api/auth/avatar/files/" + filename);
        } catch (Exception e) {
            log.error("avatar upload failed", e);
            return Result.fail(500, "头像上传失败");
        }
    }

    @GetMapping("/api/auth/avatar/files/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        try {
            Path path = Path.of(uploadPath, filename).normalize();
            if (!path.startsWith(Path.of(uploadPath).normalize()) || !Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType == null ? "application/octet-stream" : contentType))
                    .body(new FileSystemResource(path));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

