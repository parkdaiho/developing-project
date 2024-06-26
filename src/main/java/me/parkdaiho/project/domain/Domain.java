package me.parkdaiho.project.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Domain {
    USER("user", "users"), ARTICLE("article", "articles"),
    POST("post", "posts"), NOTICE("notice", "notice"),
    COMMENT("comment", "comments");

    public static Domain getDomainByDomainPl(String domainPl) {
        for(Domain domain : Domain.values()) {
            if(domainPl.equals(domain.getDomainPl())) {
                return domain;
            }
        }

        throw new IllegalArgumentException("Unexpected domainPl: " + domainPl);
    }

    private final String domain;
    private final String domainPl;

    public static String getDomainPl(String domain) {
        return Domain.valueOf(domain.toUpperCase()).getDomainPl();
    }
}
