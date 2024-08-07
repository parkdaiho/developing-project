package me.parkdaiho.project.repository;

import me.parkdaiho.project.domain.article.Article;
import me.parkdaiho.project.domain.Comment;
import me.parkdaiho.project.domain.Notice;
import me.parkdaiho.project.domain.Post;
import me.parkdaiho.project.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByArticle(Pageable pageable, Article article);
    Page<Comment> findByPost(Pageable pageable, Post post);
    Page<Comment> findByNotice(Pageable pageable, Notice notice);
    Page<Comment> findByWriter(Pageable pageable, User user);
}
