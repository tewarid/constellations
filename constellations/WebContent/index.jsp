<html>
<head>
<title>:: Constelações - Recombo ::</title>
</head>
<%
String serverURL = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
%>
<body bgcolor="#000000">
	<div align="center">
		<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
				codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0" 
				width="880" height="660" NAME="constellations" ID="constellations">
			<param name="movie" value="<%=request.getContextPath()%>/flash/reCombo.swf">
			<param name="quality" value="high">
			<param name="wmode" value="transparent"/>
			<param name="flashvars" value="servidor=<%=serverURL%>"/>
			<embed src="<%=request.getContextPath()%>/flash/reCombo.swf" quality="high"
				pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" 
				width="880" height="660" NAME="constellations" SWLIVECONNECT="true"></embed>
		</object>	
	</div>
</body>
</html>
