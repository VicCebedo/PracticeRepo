<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!-- Left side column. contains the logo and sidebar -->
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<sec:authentication var="authUser" property="user"/>
<sec:authentication var="authStaff" property="staff"/>
<style>
.autocomplete-suggestions { background: #FFF; overflow: auto; }
.autocomplete-suggestion { padding: 5px 5px; white-space: nowrap; overflow: hidden;}
.autocomplete-no-suggestion { display: block; text-align:center; font-size: medium; background: #F0F0F0; }
.autocomplete-selected { background: #F0F0F0; }
.autocomplete-suggestions strong { font-weight: bold; color: #3c8dbc; }
.autocomplete-group strong { display: block; text-align:center; font-size: medium; background: #F0F0F0; }
</style>
<script src="<c:url value="/resources/lib/jquery.min.js" />"></script>
<script src="<c:url value="/resources/lib/jquery-ui.min.js" />"type="text/javascript"></script>
<script src="<c:url value="/resources/lib/jquery.autocomplete.min.js" />"></script>
<script type="text/javascript">
String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

var source = [ { value: "www.foo.com",
    label: "Spencer Kline"
  },
  { value: "www.example.com",
    label: "James Bond"
  }
];

function logout(){
	document.getElementById('logoutForm').submit();
}

$(document).ready(function() {
	$('#searchField').autocomplete({
		serviceUrl: '${contextPath}/search/',
		paramName: "searchInput",
		delimiter: ",",
		forceFixPosition: true,
		showNoSuggestionNotice: true,
		noSuggestionNotice: "<h5><i>No results</i></h5>",
 		minChars: 3,
		groupBy: 'objectName',
		transformResult: function(response) {
			return {
				// Must convert json to javascript object before process.
				suggestions: $.map($.parseJSON(response), function(item) {
					return { 
						value: item.text,
						href: '${contextPath}/' + item.objectName + '/edit/' + item.objectID,
						data: { objectName : item.objectName.capitalize(), id : item.id }
					};
				})
			};
		},
		onSelect: function (suggestion) {
	        window.location.href = suggestion.href;
	    }
	});
});
</script>
<aside class="left-side sidebar-offcanvas">
    <!-- sidebar: style can be found in sidebar.less -->
    <section class="sidebar">
        <!-- Sidebar user panel -->
        <div class="user-panel">
            <div class="pull-left image">
            	<c:choose>
				<c:when test="${!empty authStaff.thumbnailURL}">
					<img src="${contextPath}/image/display/staff/profile/?staff_id=${authStaff.id}" class="img-circle" alt="User Image" />
				</c:when>
				<c:when test="${empty authStaff.thumbnailURL}">
					<img src="<c:url value="/resources/img/avatar5.png" />" class="img-circle" alt="User Image" />
				</c:when>
				</c:choose>
            </div>
            <div class="pull-left info">
            	<c:choose>
            	 	<c:when test="${!empty authStaff}">
            	 		<c:set var="staffName" value="${authStaff.prefix} ${authStaff.firstName} ${authStaff.middleName} ${authStaff.lastName} ${authStaff.suffix}"/>
            	 		<p>Hello, ${staffName}</p>
            	 		<h6>${authStaff.companyPosition}</h6>
            	 	</c:when>
            	 	<c:when test="${empty authStaff}">
            	 		<p>Hello, ${authUser.username}</p>
            	 		<h6>No Staff for this User.</h6>
            	 	</c:when>
            	</c:choose>
            </div>
        </div>
        <!-- search form -->
        <form action="#" id="sidebar-search-form" method="post" class="sidebar-form">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <div class="input-group">
                <input type="text" id="searchField" name="search" class='form-control' placeholder="Search..."/>
                <span class="input-group-btn">
                    <button type='submit' name='search' id='search-btn' class="btn btn-flat"><i class="fa fa-search"></i></button>
                </span>
            </div>
        </form>
        <!-- /.search form -->
        <!-- sidebar menu: : style can be found in sidebar.less -->
        <ul class="sidebar-menu">
            <li class="active">
                <a href="index.html">
                    <i class="fa fa-dashboard"></i> <span>Dashboard</span>
                </a>
            </li>
            <sec:authorize access="hasRole('ACCESS_PROJECT')">
            <li>
                <a href="${contextPath}/project/list/">
                    <i class="fa fa-folder"></i> <span>Projects</span>
                </a>
            </li>
            </sec:authorize>
            <sec:authorize access="hasRole('ACCESS_PROJECTFILE')">
            <li>
                <a href="${contextPath}/projectfile/list/">
                    <i class="fa fa-file"></i> <span>Files</span>
                </a>
            </li>
            </sec:authorize>
            <sec:authorize access="hasRole('ACCESS_TASK')">
            <li>
                <a href="${contextPath}/task/list/">
                    <i class="fa fa-tasks"></i> <span>Tasks</span>
                </a>
            </li>
            </sec:authorize>
<!--             <li> -->
<!--                 <a href="pages/calendar.html"> -->
<!--                     <i class="fa fa-calendar"></i> <span>Calendar</span> -->
<!--                 </a> -->
<!--             </li> -->
			<sec:authorize access="hasRole('ACCESS_TEAM')">
            <li>
                <a href="${contextPath}/team/list/">
                    <i class="fa fa-users"></i> <span>Teams</span>
<!--                     <small class="badge pull-right bg-green">new</small> -->
                </a> 
            </li>
            </sec:authorize>
            <sec:authorize access="hasRole('ACCESS_STAFF')">
            <li>
                <a href="${contextPath}/staff/list/">
                    <i class="fa fa-user"></i> <span>Staff</span>
                </a>
            </li>
            </sec:authorize>
            <sec:authorize access="hasRole('ROLE_SYSTEMUSER_EDITOR')">
            <li>
                <a href="${contextPath}/systemuser/list/">
                    <i class="fa fa-male"></i> <span>Users</span>
                </a>
            </li>
            </sec:authorize>
            <c:if test="${authUser.superAdmin == true}">
            <li>
                <a href="${contextPath}/field/list/">
                    <i class="fa fa-list"></i> <span>Fields</span>
                </a>
            </li>
            </c:if>
            <c:if test="${authUser.superAdmin == true}">
            <li class="treeview">
                <a href="#">
                    <i class="fa fa-laptop"></i>
                    <span>Super User</span>
                    <i class="fa fa-angle-left pull-right"></i>
                </a>
                <ul class="treeview-menu">
                    <li><a href="${contextPath}/company/list/"><i class="fa fa-angle-double-right"></i> Companies</a></li>
                    <li><a href="${contextPath}/log/list"><i class="fa fa-angle-double-right"></i> Logs</a></li>
                    <li><a href="pages/UI/icons.html"><i class="fa fa-angle-double-right"></i> Licenses</a></li>
                    <li><a href="${contextPath}/config/list"><i class="fa fa-angle-double-right"></i> System Configuration</a></li>
                </ul>
            </li>
            </c:if>
        </ul>
    </section>
    <!-- /.sidebar -->
</aside>