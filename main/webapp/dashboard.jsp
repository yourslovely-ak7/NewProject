<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>DashBoard</title>
</head>
<body>
	<div id="userDetails">
	</div>
	<button onclick="tryAccess()">Access Resource</button>
	<button onclick="logoutUser()">LOG OUT</button>
	<br/>
	<br/>
	<button onclick="triggerRefresh()">Force Refresh!</button>
	<script src="dashboard.js"></script>
</body>
</html>