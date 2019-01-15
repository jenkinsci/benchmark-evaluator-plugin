var old = null;
function resetPosition(saveFunction=null){
	var o = JSON.parse(sessionStorage.getItem(window.location.pathname+"Scroll"));
	if(document.referrer === "" && o!=null)window.scrollTo(o.x, o.y); 
	sessionStorage.removeItem(window.location.pathname+"Scroll")
	old = window.onbeforeunload;
	window.onbeforeunload = function(){
		if(old!=null) old();
		if(saveFunction!=null) saveFunction();
		sessionStorage.setItem(window.location.pathname+"Scroll",JSON.stringify({x:window.pageXOffset,y:window.pageYOffset}));
	}
}
function onStart(loadFunction=null,onNewLoad=null){
	if(document.referrer === "" && loadFunction!=null){ loadFunction() }else if(onNewLoad!=null) onNewLoad();
}