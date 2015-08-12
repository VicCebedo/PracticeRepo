
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Task ${action}</title>
	
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
	            	<c:choose>
	            		<c:when test="${task.id == 0}">New Task</c:when>
	            		<c:when test="${task.id > 0}">${task.content}</c:when>
	            	</c:choose>
	                <small>${action} Task</small>
	            </h1>
	        </section>
	        <section class="content">
                <div class="row">
                    <div class="col-md-12">
                    	${uiParamAlert}
                        <!-- Custom Tabs -->
                        <div class="nav-tabs-custom">
                            <ul class="nav nav-tabs">
                                <li class="active"><a href="#tab_1" data-toggle="tab">Details</a></li>
                                <li><a href="#tab_assigned_staff" data-toggle="tab">Staff</a></li>
                            </ul>
                            <div class="tab-content">
                                <div class="tab-pane active" id="tab_1">
                                	<div class="row">
                   						<div class="col-md-6">
                   							<div class="box box-body box-default">
                   								<div class="box-body">
                   									<form:form modelAttribute="task" role="form" name="detailsForm" id="detailsForm" method="post" action="${contextPath}/project/create/task">
				                                        <div class="form-group">
				                                        	<label>Status</label>
				                                            <form:select class="form-control" id="task_status" path="status">
						                                    	<form:option value="0" label="New"/>
						                                    	<form:option value="1" label="Ongoing"/>
						                                    	<form:option value="2" label="Completed"/>
						                                    	<form:option value="3" label="Failed"/>
						                                    	<form:option value="4" label="Cancelled"/>
				                                            </form:select><br/>
				                                            <label>Title</label>
				                                            <form:input type="text" class="form-control" path="title"/><br/>
				                                            
				                                            <label>Content</label>
				                                            <form:input type="text" class="form-control" path="content"/><br/>
				                                            
				                                            <label>Start</label>
					                                        <div class="input-group">
					                                            <div class="input-group-addon">
					                                                <i class="fa fa-calendar"></i>
					                                            </div>
<%-- 					                                            <form:input type="text" id="date-mask" class="form-control" path="dateStart" data-inputmask="'alias': 'yyyy/mm/dd'" data-mask/> --%>
					                                            <form:input type="text" id="date-mask" class="form-control" path="dateStart"/>
					                                        </div>
					                                        <br/>
					                                        <label>Duration (Man Days)</label>
				                                            <form:input type="text" class="form-control" path="duration"/>
				                                        </div>
				                                    </form:form>
				                                    <c:choose>
		                                            	<c:when test="${task.id == 0}">
		                                            		<button class="btn btn-default btn-flat btn-sm" id="detailsButton" onclick="submitForm('detailsForm')">Create</button>
		                                            	</c:when>
		                                            	<c:when test="${task.id > 0}">
		                                            		<button class="btn btn-default btn-flat btn-sm" id="detailsButton" onclick="submitForm('detailsForm')">Update</button>
		                                            		<a href="${contextPath}/task/delete/${task.id}">
																<button class="btn btn-default btn-flat btn-sm">Delete This Task</button>
															</a>
		                                            	</c:when>
		                                            </c:choose>
                   								</div>
                   							</div>
                   						</div>
              						</div>
                                </div><!-- /.tab-pane -->
                                <div class="tab-pane" id="tab_assigned_staff">
                                	<div class="box">
		                                <div class="box-body table-responsive">
		                                	<table>
		                                    	<tr>
		                                    		<td>
		                                    			<c:url var="urlCreateStaff" value="/staff/edit/0/from/task/${task.id}"/>
		                                    			<a href="${urlCreateStaff}">
				                                    	<button class="btn btn-default btn-flat btn-sm">Create Staff</button>
		                                    			</a>
		                                    		</td>
		                                    		<td>
		                                    			&nbsp;
		                                    		</td>
		                                    		<c:if test="${!empty staffList}">
 		                                    		<form:form 
 		                                    		modelAttribute="staffAssignment"  
 		                                    		method="post" 
 		                                    		action="${contextPath}/task/assign/staff"> 
 		                                    			<td>
 		                                    			<form:select class="form-control" path="staffID"> 
                                     						<c:forEach items="${staffList}" var="staff"> 
                                     							<form:option value="${staff.id}" label="${staff.getFullName()}"/> 
                                     						</c:forEach> 
 		                                    			</form:select> 
 		                                    			</td>
 		                                    			<td>
 		                                    				&nbsp;
 		                                    			</td>
 														<td>
 														<button class="btn btn-default btn-flat btn-sm">Assign</button>
 		                                    			</td> 
 		                                    		</form:form> 
		                                    		</c:if>
		                                    		<td>
		                                    			&nbsp;
		                                    		</td>
		                                    		<c:if test="${!empty task.staff}">
		                                    		<td>
               											<c:url var="urlTaskUnassignStaffAll" value="/task/unassign/staff/all"/>
		                                    			<a href="${urlTaskUnassignStaffAll}">
                											<button class="btn btn-default btn-flat btn-sm">Unassign All</button>
		                                    			</a>
		                                    		</td>
		                                    		</c:if>
		                                    	</tr>
		                                    </table>
		                                    <table id="staff-table" class="table table-bordered table-striped">
		                                    	<thead>
		                                            <tr>
		                                            	<th>&nbsp;</th>
		                                                <th>Photo</th>
		                                                <th>Full Name</th>
		                                                <th>Position</th>
		                                                <th>E-Mail</th>
		                                                <th>Contact Number</th>
		                                            </tr>
                                        		</thead>
		                                        <tbody>
			                                		<c:forEach items="${task.staff}" var="staffAssign">
			                                            <tr>
			                                            	<td>
			                                            		<center>
			                                            			<c:url var="urlViewStaff" value="/staff/edit/${staffAssign.id}/from/task/${task.id}" />
			                                            			<a href="${urlViewStaff}">
							                                    	<button class="btn btn-default btn-flat btn-sm">View</button>
			                                            			</a>
	                   												<c:url var="urlUnassignStaff" value="/task/unassign/staff/${staffAssign.id}"/>
	                   												<a href="${urlUnassignStaff}">
																		<button class="btn btn-default btn-flat btn-sm">Unassign</button>
	                   												</a>
																</center>
															</td>
			                                                <td>
			                                                	<div class="user-panel">
													            <div class="pull-left image">
													            	TODO
													            </div>
														        </div>
			                                                </td>
			                                                <td>${staffAssign.getFullName()}</td>
			                                                <td>${staffAssign.companyPosition}</td>
			                                                <td>${staffAssign.email}</td>
			                                                <td>${staffAssign.contactNumber}</td>
			                                            </tr>
		                                            </c:forEach>
			                                    </tbody>
			                                </table>
		                                </div><!-- /.box-body -->
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
		function submitForm(id) {
			$('#'+id).submit();
		}
		
		function submitAjax(id) {
			var formObj = $('#'+id);
			var serializedData = formObj.serialize();
			$.ajax({
				type: "POST",
				url: '${contextPath}/field/update/assigned/task',
				data: serializedData,
				success: function(response){
					location.reload();
				}
			});
		}
	
		$(document).ready(function() {
			$("#date-mask").inputmask("yyyy/mm/dd", {"placeholder": "yyyy/mm/dd"});
			$("#task_status").val("${task.status}");
			$("#staff-table").dataTable();
			$("#teams-table").dataTable();
	    });
	</script>
</body>
</html>