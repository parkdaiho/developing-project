package me.parkdaiho.project.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties("naver.search")
public class NaverSearchProperties {

    private String baseUrl;
    private String newsSearchPath;
    private String clientIdHeaderName;
    private String clientSecretHeaderName;
    private String clientId;
    private String clientSecret;
    private String display;
}
