package me.parkdaiho.project.dto.article;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SearchNaverNewsResponse {

    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<NewsItem> items;
}
