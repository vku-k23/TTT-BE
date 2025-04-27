package com.ttt.cinevibe.dto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "cinevibe")
@Getter
@Setter
public class AppContactDto {
    private String message;
    private Map<String, String> contactInfo;
    private List<String> onCallSupport;
}