package com.jianghu.ling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String inviteBaseUrl = "http://localhost:5173/r/";
    private String mockSmsCode = "123456";
    private Upload upload = new Upload();

    @Data
    public static class Upload {
        private String dir = "./uploads";
    }
}
