document.getElementById("main-panel").setAttribute("style","margin-left:0px;padding-left: 5%");

function save(){
	var d = {n:lastSortbyName,d:lastSortDir,f:lastSortFunction}
	sessionStorage.setItem(window.location.pathname+"_Store",JSON.stringify(d));
}
function load(){
	var d = JSON.parse(sessionStorage.getItem(window.location.pathname+"_Store"))
	sessionStorage.removeItem(window.location.pathname+"_Store");
	if(d!=undefined){
		lastSortbyName = (d.n==null)?"metric":d.n;
		lastSortDir = (d.d==null)?"asc":d.d;
		lastSortFunction = (d.f==null)?a=>a.innerHTML.toLowerCase():eval(d.f);
		sortTable(lastSortbyName,'configTable',lastSortFunction,lastSortDir);
	}else{
		sortTable('id','configTable',null,"asc",forceAsc=true);
	}
}

onStart(load,function(){sortTable('id','configTable',null,"asc",forceAsc=true);});
resetPosition(save);


function setMetricMinValue(metric){
	var minValue = document.getElementById(metric+"_minValue")
	var maxValue = document.getElementById(metric+"_maxValue")
	if(minValue.value != ""){
		if(parseFloat(minValue.value) > parseFloat(minValue.max)){
			minValue.value  = parseFloat(minValue.max);
		}
	}
	maxValue.min = parseFloat(minValue.value);
	backend.setMetricMinValue(metric, minValue.value, function(t) {});
}

function setMetricMaxValue(metric){
	var minValue = document.getElementById(metric+"_minValue")
	var maxValue = document.getElementById(metric+"_maxValue")
	if(maxValue.value != ""){
		if(parseFloat(maxValue.value) < parseFloat(maxValue.min)){
				maxValue.value = parseFloat(maxValue.min);
		}
	}
	minValue.max = parseFloat(maxValue.value);
	backend.setMetricMaxValue(metric, maxValue.value, function(t) {});
}

function setMinPercent(metric){
	var minPercent = document.getElementById(metric+"_minPercent")
	var maxPercent = document.getElementById(metric+"_maxPercent")
	if(minPercent.value != ""){
		if(parseFloat(minPercent.value) > parseFloat(minPercent.max)){
			minPercent.value  = parseFloat(minPercent.max);
		}
	}
	maxPercent.min = parseFloat(minPercent.value);
	backend.setMetricMinPercent(metric, minPercent.value, function(t) {});
}

function setMaxPercent(metric){
	var minPercent = document.getElementById(metric+"_minPercent")
	var maxPercent = document.getElementById(metric+"_maxPercent")
	if(maxPercent.value != ""){
		if(parseFloat(maxPercent.value) < parseFloat(maxPercent.min)){
				maxPercent.value = parseFloat(maxPercent.min);
		}
	}
	minPercent.max = parseFloat(maxPercent.value);
	backend.setMetricMaxPercent(metric, maxPercent.value, function(t) {});
}

function setMetricUnit(metric,unit){
	var unit = document.getElementById(metric+"_unit");
	backend.setMetricUnit(metric,unit.value, function(t) {});
}

function createMetric(name){
	backend.createMetric(name,function(t) {
		if(t.responseObject()){
			location.reload();
		}
	});	
}

function deleteMetric(name){
	backend.deleteMetric(name,function(t) {
		if(t.responseObject()){
			location.reload();
		}
	});	
}

jQ(document).ready(function(){
	var table = document.getElementById('configTable');
	var rows = table.getElementsByTagName("TR");
	for(i = 1; i<(rows.length); i++){
		var metric = rows[i].id;
		var minPercent = document.getElementById(metric+"_minPercent")
		var maxPercent = document.getElementById(metric+"_maxPercent")
		var minValue = document.getElementById(metric+"_minValue")
		var maxValue = document.getElementById(metric+"_maxValue")
		if(minPercent.value != ""){
			maxPercent.min = minPercent.value;
		}
		if(maxPercent.value != ""){
			minPercent.max = maxPercent.value;
		}
		if(minValue.value != ""){
			maxValue.min = minValue.value;
		}
		if(maxValue.value != ""){
			minValue.max = maxValue.value;
		}
	}
});