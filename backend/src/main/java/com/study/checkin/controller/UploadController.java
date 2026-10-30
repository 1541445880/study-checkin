package com.study.checkin.controller;

import com.study.checkin.common.Result;
import com.study.checkin.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传（头像）
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final List<String> ALLOWED = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/avatar")
    public Result<Map<String, String>> avatar(@RequestParam("file") MultipartFile file) {
        return upload(file, "avatar");
    }

    /** 通用图片上传（头像、背景图等共用） */
    @PostMapping("/image")
    public Result<Map<String, String>> image(@RequestParam("file") MultipartFile file) {
        return upload(file, "image");
    }

    private Result<Map<String, String>> upload(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片不能超过 5MB");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED.contains(ext)) {
            throw new BusinessException("仅支持 jpg/png/gif/webp/bmp 图片");
        }

        try {
            File baseDir = new File(uploadPath).getAbsoluteFile();
            File dir = new File(baseDir, subDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException("创建上传目录失败");
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);

            Map<String, String> map = new HashMap<>();
            map.put("url", "/uploads/" + subDir + "/" + filename);
            return Result.ok(map);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("上传失败：" + e.getMessage());
        }
    }
}
