<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" href="/css/css.css">
	<title>FIND-USERNAME</title>
</head>
<body>
	<div class="find-info-area">
		<div class="find-user-info-menu">
			<ul>
				<li><a class="menu-on" href="javascript: return false;" onclick="getFindUsernamePage();">FIND-USERNAME</a></li>
				<li><a href="javascript: return false;" onclick="getFindPasswordPage();">FIND-PASSWORD</a></li>
			</ul>
		</div>
		<div class="find-user-info-right-area">
			<div class="find-user-info-info">
				<h2>FIND USERNAME</h2>
				<p>Find your username through the mail you authenticated in the sign-up.</p>
			</div>
			<div class="find-user-info-input">
				<label>EMAIL</label>
				<input type="email" id="email">
			</div>
			<div class="find-user-info-loading" id="find-user-info-loading" style="display: none;">
				LOADING...
			</div>
			<div class="find-user-info-input" id="find-user-info-code" style="display: none;">
				<label>CODE</label>
				<input type="text" id="code">
			</div>
			<div class="find-username-btn-area">
				<button onclick="sendCodeForUsername();" id="find-username-send-btn">SEND</button>
				<button onclick="authenticateCodeForUsername();" id="find-username-auth-btn" style="display: none;">AUTHENTICATION</button>
			</div>
		</div>
	</div>
</body>
</html>
