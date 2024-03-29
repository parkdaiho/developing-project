package me.parkdaiho.project.service.article;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.parkdaiho.project.config.properties.PaginationProperties;
import me.parkdaiho.project.domain.Domain;
import me.parkdaiho.project.domain.Sort;
import me.parkdaiho.project.domain.Article;
import me.parkdaiho.project.dto.IndexViewResponse;
import me.parkdaiho.project.dto.article.*;
import me.parkdaiho.project.repository.ArticleRepository;
import me.parkdaiho.project.util.CookieUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final NaverNewsCrawler naverNewsCrawler;

    private final PaginationProperties paginationProperties;

    public SearchNaverNewsResponse getSearchResult(SearchNaverNewsRequest dto) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromUriString("http://localhost:8080")
                .path("/api/naver-news")
                .build().toUri();

        RequestEntity<SearchNaverNewsRequest> request = RequestEntity.post(uri)
                .body(dto);

        ResponseEntity<SearchNaverNewsResponse> response = restTemplate.exchange(request, SearchNaverNewsResponse.class);
        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("fait to search news");
        }

        return response.getBody();
    }

    private Article findArticleByLink(ArticleViewRequest dto) throws IOException {
        Article article = articleRepository.findByLink(dto.getLink())
                .orElse(dto.toEntity());

        if(article.getId() != null) {
            return article;
        }

        if(!article.getLink().equals(article.getOriginalLink())) {
            String contents = getContentsByLink(article.getLink());

            article.setContents(contents);
        }

        return articleRepository.save(article);
    }

    private String getContentsByLink(String link) throws IOException {
        return naverNewsCrawler.getContents(link);
    }

    public Long getArticleId(ArticleViewRequest dto) throws IOException {
        Article article = findArticleByLink(dto);

        return article.getId();
    }

    @Transactional
    public ArticleViewResponse getArticleView(Long id, HttpServletRequest request, HttpServletResponse response) {
        Article article = findArticleById(id);

        if(!CookieUtils.checkView(request, response, Domain.ARTICLE, id)) article.addViews();

        return new ArticleViewResponse(article);
    }

    public Article findArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected articleId: " + id));
    }

    public List<IndexViewResponse> getArticlesForIndex(Sort sort) {
        Pageable pageable = getPageable(paginationProperties.getIndexViews(), 1, sort);
        Page<Article> articles = articleRepository.findAll(pageable);

        switch (sort) {
            case POPULARITY -> {
                return articles.stream()
                        .map(entity -> IndexViewResponse.builder()
                                .title(entity.getTitle())
                                .figure(entity.getGood())
                                .build()).toList();
            }
            case VIEWS -> {
                return articles.stream()
                        .map(entity -> IndexViewResponse.builder()
                                .title(entity.getTitle())
                                .figure(entity.getViews())
                                .build()).toList();
            }

            default -> throw new IllegalArgumentException("Unexpected sort: " + sort.getValue());
        }
    }

    private Pageable getPageable(int size, int page, Sort sort) {
        org.springframework.data.domain.Sort pageableSort = null;
        switch (sort) {
            case LATEST, POPULARITY, VIEWS -> pageableSort = org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, sort.getProperty());
            case EARLIEST -> pageableSort = org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, sort.getProperty());

            default -> throw new IllegalArgumentException("Unexpected sort:" + sort.getValue());
        }

        return PageRequest.of(page - 1, size, pageableSort);
    }
}
