<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Field ${action}</title>
	
	<style>
	  ul {         
	      padding:0 0 0 0;
	      margin:0 0 0 0;
	  }
	  ul li {     
	      list-style:none;
	      margin-bottom:25px;           
	  }
	  ul li img {
	      cursor: pointer;
	  }
	</style>
</head>
<body class="skin-blue">
	<c:import url="/resources/header.jsp" />
	<div class="wrapper row-offcanvas row-offcanvas-left">
		<c:import url="/resources/sidebar.jsp" />
		<aside class="right-side">
		<!-- Content Header (Page header) -->
	        <section class="content-header">
	            <h1>
	            	TODO
	                <small>${action} Field</small>
	            </h1>
	        </section>
	        <section class="content">
                <div class="row">
                    <div class="col-xs-12">
                        <!-- Custom Tabs -->
                        <div class="nav-tabs-custom">
                            <ul class="nav nav-tabs">
                                <li class="active"><a href="#tab_1" data-toggle="tab">Details</a></li>
                            </ul>
                            <div class="tab-content">
                                <div class="tab-pane active" id="tab_1">
                                	<div class="row">
                   						<div class="col-md-6">
                   							<div class="box box-default">
                   								<div class="box-body">
                   									<form:form modelAttribute="attendance"
														id="detailsForm"
														method="post"
														action="${contextPath}/staff/add/attendance">
				                                        <div class="form-group">
				                                        	<c:if test="${empty attendance.timestamp}">
				                                            <label>Date</label>
				                                            <form:input type="text" class="form-control" id="date-mask" path="timestamp"/><br/>
				                                        	</c:if>
				                                            <label>Status</label>
				                                            <form:input type="text" class="form-control" path="statusID"/><br/>
				                                            <c:if test="${!empty attendance.timestamp}">
				                                            <label>Salary</label>
				                                            <form:input type="text" class="form-control" path="wage"/><br/>
				                                            </c:if>
				                                        </div>
	                                            		<button class="btn btn-default btn-flat btn-sm" id="detailsButton">Submit TODO</button>
				                                    </form:form>
<%--                                             		<c:url var="urlDeleteField" value="/project/field/delete" /> --%>
<%--                                             		<a href="${urlDeleteField}"> --%>
<!-- 														<button class="btn btn-default btn-flat btn-sm">Remove This Field</button> -->
<!-- 													</a> -->
                   								</div>
                   							</div>
                   						</div>
              						</div>
                                </div><!-- /.tab-pane -->
                            </div><!-- /.tab-content -->
                        </div><!-- nav-tabs-custom -->
                    </div><!-- /.col -->
                </div> <!-- /.row -->
            </section><!-- /.content -->
        </aside>
	</div>
	
	<!-- InputMask -->
    <script src="${contextPath}/resources/js/plugins/input-mask/jquery.inputmask.js" type="text/javascript"></script>
    <script src="${contextPath}/resources/js/plugins/input-mask/jquery.inputmask.date.extensions.js" type="text/javascript"></script>
    <script src="${contextPath}/resources/js/plugins/input-mask/jquery.inputmask.extensions.js" type="text/javascript"></script>
	
	<script>
		$(document).ready(function() {
			$("#date-mask").inputmask("yyyy/mm/dd", {"placeholder": "yyyy/mm/dd"});
	    });	
	
		function submitForm(id) {
			$('#'+id).submit();
		}
	</script>
</body>
</html>