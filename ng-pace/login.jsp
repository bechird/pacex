<!doctype html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html class="ng-scope" ng-class="MODE =='pacex'?'login-content-pacex':'login-content-pacea'">
<head>
<meta charset="utf-8">
<title>PACEX Login</title>
<meta name="description" content="">
<meta name="viewport" content="width=device-width">
<!-- Place favicon.ico and apple-touch-icon.png in the root directory -->
<!-- build:css(.) styles/vendor.css -->
<!-- bower:css -->
<link rel="stylesheet" href="bower_components/Ionicons/css/ionicons.css">
<!-- endbower -->
<!-- endbuild -->
<!-- build:css(.tmp) styles/main.css -->

<link rel="stylesheet" href="styles/iphone.css">
<link rel="stylesheet" href="styles/login-page.css">
<link rel="stylesheet" href="styles/body.css">


<link rel="stylesheet"
	href="bower_components/material-design-iconic-font/dist/css/material-design-iconic-font.css" />

<!-- endbuild -->
</head>

<body class="login-content">
    <div class="lc-block toggled visible-hard-screen"  id="l-login">
    		
            <form method="post">
              <c:if test="${param.error != null}">
                            <div class="alert alert-danger alert-dismissable" role="alert">
                                Invalid username or password !
                            </div>
            </c:if>
        <div class="input-group m-b-20">
            <span class="input-group-addon"><i class="zmdi zmdi-account"></i></span>
            <div class="fg-line">
                 <input type="text" name="username" id="username"  class="form-control" placeholder="Email">
            </div>
        </div>

        <div class="input-group m-b-20">
            <span class="input-group-addon"><i class="zmdi zmdi-lock"></i></span>
            <div class="fg-line">
              <input type="password" name="password" id="password"  class="form-control" placeholder="Password">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </div>
        </div>
			
        <div class="clearfix"></div>

        <button type="submit" class="btn btn-login bgm-indigo btn-float waves-effect waves-circle"><i class="zmdi zmdi-arrow-forward"></i></button>
	</form>
        
    </div>
    
   
</body>
</html>

<!-- uiView:  -->
