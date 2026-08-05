package com.jianghu.ling.bounty.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmitRequest {
    private String summary;
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        private String itemCode;
        private Boolean done;
        private String text;
        private List<String> mediaUrls;
    }
}
