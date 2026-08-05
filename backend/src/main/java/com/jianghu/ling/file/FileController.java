package com.jianghu.ling.file;

import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final LocalFileStorage localFileStorage;

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(localFileStorage.upload(file));
    }
}
