package com.jianghu.ling.file;

import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorage {

    private final AppProperties appProperties;

    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件不能为空");
        }
        try {
            Path dir = Paths.get(appProperties.getUpload().getDir());
            Files.createDirectories(dir);
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String filename = fileId + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return Map.of(
                    "url", "/files/" + filename,
                    "fileId", fileId
            );
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL, "上传失败");
        }
    }
}
