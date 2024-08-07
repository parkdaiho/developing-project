<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<title>ARTICLES</title>
	<link rel="stylesheet" href="/css/css.css">
</head>
<body>
<header>
	<jsp:include page="header.jsp"></jsp:include>
</header>
<main>
	<section class="page-info-area">
		<h1>ARTICLES</h1>
		<p>Read the articles people interested in!</p>
	</section>
	<section class="search-area">
		<jsp:include page="article-search-area.jsp"></jsp:include>
	</section>
	<section class="articles-area">
		<div class="article-list-area">
			<div class="article-list-info">
				<c:choose>
					<c:when test="${order == 'popularity'}">
						<h2>MOST-POPULAR ARTICLES</h2>
						<select onchange="getArticlesByOrder(this.value);">
							<option value="views">MOST-VIEWED</option>
							<option value="popularity" selected>MOST-POPULAR</option>
							<option value="comments">MOST-COMMENTS</option>
						</select>
					</c:when>
					<c:when test="${order == 'comments'}">
						<h2>MOST-COMMENTS ARTICLES</h2>
						<select onchange="getArticlesByOrder(this.value)">
							<option value="views">MOST-VIEWED</option>
							<option value="popularity">MOST-POPULAR</option>
							<option value="comments" selected>MOST-COMMENTS</option>
						</select>
					</c:when>
					<c:otherwise>
						<h2>MOST-VIEWED ARTICLES</h2>
						<select onchange="getArticlesByOrder(this.value)">
							<option value="views" selected>MOST-VIEWED</option>
							<option value="popularity">MOST-POPULAR</option>
							<option value="comments">MOST-COMMENTS</option>
						</select>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="article-list">
				<c:forEach var="article" items="${articles}" varStatus="status">
					<div class="article-in-list" onclick="showArticle(${article.id});">
						<div class="article-rank-in-articles">
								${status.count}
						</div>
						<div class="article-summary">
							<div class="article-summary-title">
									${article.title}
							</div>
							<div class="article-summary-description">
									${article.description}
							</div>
							<div class="article-summary-figures">
								<div class="article-summary-pubdate">
										${article.pubDate}
								</div>
								<div class="article-summary-figure">
									<b>조회수</b>&nbsp;&nbsp;${article.views}
								</div>
								<div class="article-summary-figure">
									<b>좋아요</b>&nbsp;&nbsp;${article.good}
								</div>
								<div class="article-summary-figure">
									<b>댓글수</b>&nbsp;&nbsp;${article.commentsSize}
								</div>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
		</div>
	</section>
</main>
<footer>
	<jsp:include page="footer.jsp"></jsp:include>
</footer>

<script src="/js/articles.js"></script>

</body>
</html>
