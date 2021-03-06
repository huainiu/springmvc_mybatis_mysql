<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>  
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML>
<html>
  <head>
    <base href="<%=basePath%>">
    <title><%=basePath%></title>
    </head>
  
  <body>
    <h2>增加用户信息页面</h2> <br>
    <!-- 此时没有写action,直接提交会提交给/add -->
    <sf:form method="post" modelAttribute="user">
        姓名:<sf:input path="username"/>  <sf:errors path="username" /> <br/>
        密码:<sf:password path="password"/> <sf:errors path="password" /> <br/>
        邮箱:<sf:input path="email"/> <sf:errors path="email" /> <br/>
        <input type="submit" value="添加" />
    </sf:form>
  </body>
</html>