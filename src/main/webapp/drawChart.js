google.charts.load('current', {'packages':['corechart'],'language':language});
google.charts.setOnLoadCallback(drawChart);
document.getElementById("main-panel").setAttribute("style","margin-left:0px;padding-left: 0px");

var nuller = Number.MAX_SAFE_INTEGER;
var dataSet1 = {}

for(var m in dataSet2){
	var newArray = new Array(dataSet2[m].length);
	for (var i = 0; i < dataSet2[m].length; i++) {
		newArray[i] = new Array(dataSet2[m][i].length);
		for (var j = 0; j < dataSet2[m][i].length; j++) {
			if(dataSet2[m][i][j]==null){
				newArray[i][j] = nuller;
			}else if(dataSet2[m][i][j]=='inf'){
				newArray[i][j] = Number.POSITIVE_INFINITY;
			}else if(dataSet2[m][i][j]=='-inf'){
				newArray[i][j] = Number.NEGATIVE_INFINITY;
			}else
				newArray[i][j] = dataSet2[m][i][j];
		}
	}
	dataSet1[m] = newArray;
}

var metricNames = [];
for(var m in dataSet1){
	metricNames.push(m);
}

for(var counter=0;counter<metricNames.length;counter++){
	jQ('#charts_body').append('<tr id="'+metricNames[counter]+'"><td style="width:80%;"><div id="curve_chart'+counter+'"></div></td>'
			+'<td style="width:20%;position: relative;"><div id="curve_chart_stat'+counter+'" style="position:absolute;top:0;bottom:0;left:0;right:0;width:50%;height:30%;margin:auto;"></div></td></tr>');
}

sortTable("id","charts",null,"asc",true)

//--------------------------------------------------------------------------------------------Variables-----------------------------------------------------------------------//
var disabledColumns = [];
var charts = [];
var dataView = [];
var options = [];
var dataSet = [];
var unsuccesfullBuilds = [];
var allBuilds = [];
for (var i = 0; i < buildNr.length; i++) {
	allBuilds.push(i);
}
for (var i = 0; i < metricNames.length; i++) {
	dataSet[i] = dataSet1[metricNames[i]];
}
unsuccesfullBuilds = unsuccesfullBuildsNames.map(x=>buildNr.indexOf(x));
var succesfullBuilds = [];
for (var i = 0; i < buildNr.length; i++) {
	if(!unsuccesfullBuilds.includes(i))
		succesfullBuilds.push(i);
}

function createRow(text,value,value_min,value_max,unit,succesC,failureC,failureNumber=0){
	if(unit=='%'){
		var valueS;
		if (value == Number.POSITIVE_INFINITY){
			valueS = "+<sub><sub><font size='+2'>&infin;</font></sub></sub>"
		}else if (value == Number.NEGATIVE_INFINITY){
			valueS = "-<sub><sub><font size='+2'>&infin;</font></sub></sub>"
		}else{
			valueS = (value*100).toFixed(2);
		}
//		var valueS = (value*100).toFixed(2);
		var value_minS = value_min*100;
		var value_maxS = value_max*100;
	}else{
		var valueS = value.toFixed(2);
		var value_minS = value_min;
		var value_maxS = value_max;
	}
	var failure_min = value<value_min && value_min!=nuller && value != nuller;
	var failure_max = value>value_max && value_max!=nuller  && value != nuller;
	var failure = failure_min || failure_max;
	var color_minS = failure_min?'<font color="'+failureC+'">':'';
	var color_maxS = failure_max?'<font color="'+failureC+'">':'';
	var colorS = failure?'<font color="'+failureC+'">':'';
	var colorE = failure?'</font>':'';
	//no value
	if(value == nuller && value_min == nuller && value_max == nuller) return ""
	//front text
	var r = '<tr>'
	+'<td style="padding-right: 5px;"><font color="'+(failure?failureC:succesC)+'">'+text+':</font></td>'
	//min value
	if(value_min!=nuller)
		r+= '<td align="right">' + color_minS + value_minS.toFixed(2) + colorE + '</td><td>' + color_minS + unit + colorE + '</td>'
	else
		r += '<td align="right"></td><td></td>'
	//sign between min and value
	if(value!=nuller && value_min!=nuller)
		r += '<td style="padding:0px 5px 0px 5px">'+ color_minS +(failure_min?'&ge;':'&le;') + colorE + '</td>'
	else
		r += '<td></td>'
	//value
	if(value!=nuller){
		r+='<td align="right">' + colorS + valueS + colorE + '</td><td>' + colorS +unit  + colorE + '</td>'
	}
	else
		r += '<td width="10px"></td>'
	//sign between value and max
	if(value!=nuller && value_max!=nuller)
		r += '<td style="padding:0px 5px 0px 5px">'+ color_maxS +(failure_max?'&ge;':'&le;') + colorE + '</td>'
	else
		r += '<td></td>'		
	//min value
	if(value_max!=nuller)
		r+= '<td align="right">' + color_maxS + value_maxS.toFixed(2) + colorE + '</td><td>' + color_maxS + unit + colorE + '</td>'
	else
		r += '<td align="right"></td><td></td>'
	r+='</tr>';
	return r;
}

function createTooltip(data1,row){
	var error  = unsuccesfullBuildsNames.includes(data1.getValue(row,0));
	var value1 = data1.getValue(row,1);
	var value_min1 = data1.getValue(row,2);
	var value_max1 = data1.getValue(row,3);
	var value2 = data1.getValue(row,4);
	var value_min2 = data1.getValue(row,5);
	var value_max2 = data1.getValue(row,6);
	var underline = 'style="border-bottom:1px solid #525054;"';
	var tablehead = (buildNames[row] != null ? '<b><font size="2" '+ ((error)?'color="red"':'')  +'>' + buildNames[row] + '</font></b>' : '')
		+'<tr>'
		+'<td><b><font size="2" '+ ((error)?'color="red"':'')  +'>' + data1.getValue(row,0) + '</font></b></td>'
		//min
		+ ((value_min1!= nuller || value_min2!=nuller)?'<td colspan="3" '+underline +'>'+ statNames['min']+'</td>':'<td colspan="3" '+underline + '></td>')
		//value
		+ ((value1!= nuller || value2!=nuller)?'<td  colspan="2" align="center" '+underline +'>'+ columnsNames[1]+'</td>':'<td '+underline + '></td>')
		//max
		+ ((value_max1!= nuller || value_max2!=nuller)?'<td colspan="3" align="right" '+underline + '>'+statNames['max']+'</td>':'')
		+'</tr>'
	return '<div style="padding:5px 5px 5px 5px; border: '+ ((error)?'3':'1')  +'px solid '+ ((error)?'red':'black')  +'; border-radius: 10px; background-color: white;">'
	+'<table style="width:100%">'
	+ tablehead
	+createRow(columnsNames[1],value1,value_min1,value_max1,data1['unit'],'#00ff00','#ff0000')
	+createRow(columnsNames[4],value2,value_min2,value_max2,'%','#0000ff','#ff3300')
	+'</table>'
	+'</div>';
}

var functions = [
	{
		label: columnsNames[1],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,1);
			return (x==nuller)?null:x;
		}
	},
	{
		label: columnsNames[2],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,2);
			return (x==nuller)?null:x;
		}
	},
	{
		label: columnsNames[3],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,3);
			return (x==nuller)?null:x;
		}
	},
	{
		label: columnsNames[4],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,4);
			return (x==nuller || x==Number.POSITIVE_INFINITY || x == Number.NEGATIVE_INFINITY) ? null : x;
		}
	},
	{
		label: columnsNames[5],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,5);
			return (x==nuller)?null:x;
		}
	},
	{
		label: columnsNames[6],
		type: 'number',
		calc: function (a,b) {
			var x = a.getValue(b,6);
			return (x==nuller)?null:x;
		}
	}	
]

var columns = [
	0,
	//all
	{
		calc: function(data1, row) {
			return createTooltip(data1,row);
		},
		type: 'string',
		role: 'tooltip',
		properties: {
			html: true
		}
	},
	//value
	1,
	{
		calc: colorTime,
		type: 'string',
		role: 'style'
	},
	//min max
	2,3,
	//percent
	4,
	{
		calc: colorPercent,
		type: 'string',
		role: 'style'
	},
	//min max Percent
	5,6
	];

var columnDic = {}
for(var i=0;i<columns.length;i++){
	if(Number.isInteger(columns[i])){
		columnDic[i] = columns[i];
	}
}

var series = {
		0: {targetAxisIndex: 0,
			lineWidth: 2
		},
		1: {targetAxisIndex: 0,
			lineWidth: 1
		},
		2: {targetAxisIndex: 0,
			lineWidth: 1
		},
		3: {targetAxisIndex: 1,
			lineDashStyle: [4, 1],
			lineWidth: 2
		},
		4: {targetAxisIndex: 1,
			lineDashStyle: [4, 1],
			lineWidth: 1
		},
		5: {targetAxisIndex: 1,
			lineDashStyle: [4, 1],
			lineWidth: 1
		}
}

//--------------------------------------------------------------------------------------------Variables-----------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------Optic------------------------------------------------------------------------//
var cookL = null;
var cookR = null;

function save(){
	var d = {left:left,right:(right==MAX)?'MAX':right,onlySucces:document.getElementById("onlySucces").checked,disabledColumns:disabledColumns,search:document.getElementById("searchField").value}
	sessionStorage.setItem(window.location.pathname+"_Store",JSON.stringify(d));
}

function load(){
	var d = JSON.parse(sessionStorage.getItem(window.location.pathname+"_Store"))
	if(d!=undefined){
		cookL = d.left;
		cookR = d.right=='MAX'?MAX:d.right;
		disabledColumns = JSON.parse(d.disabledColumns);
		document.getElementById("onlySucces").checked = d.onlySucces;
		document.getElementById("searchField").value = d.search;
	}
}

function calculateMax(){
	if(document.getElementById("onlySucces").checked){
		return Number.isInteger(buildNr[buildNr.length-1])?buildNr[buildNr.length-1]:buildNr.length-unsuccesfullBuilds.length;
	}else
		return Number.isInteger(buildNr[buildNr.length-1])?buildNr[buildNr.length-1]:buildNr.length;
}

const maxZoom = 3;
const MIN = Number.isInteger(buildNr[0])?buildNr[0]:0;
var MAX = calculateMax();
var left = MIN;
var right = MAX;
onStart(load);

if(!isNaN(cookL)){
	left = (cookL<=MIN || cookL > MAX-maxZoom)?MIN:cookL;
}
if(!isNaN(cookR)){
	right = (cookR>=MAX || cookR-maxZoom < left)?MAX:cookR;
}

function stayTop(){
	try{
		document.getElementById("controller").style.top=document.getElementById("breadcrumbs").getBoundingClientRect().bottom+"px";
	} catch (error){
		console.log("Can not find breadcrumbs, please inform the developer!")
	}
}

function showButtons(){
	if(left == MIN){
		document.getElementById("leftButton").disabled = true; 
	}else{
		document.getElementById("leftButton").disabled = false; 
	}
	if(right == MAX){
		document.getElementById("rightButton").disabled = true; 
	}else{
		document.getElementById("rightButton").disabled = false;
	}
	if(right == MAX && left == MIN){
		document.getElementById("zoomOutButton").disabled = true; 
	}else{
		document.getElementById("zoomOutButton").disabled = false;
	}
	if(right - left <= maxZoom){
		document.getElementById("zoomInButton").disabled = true;
	}else{
		document.getElementById("zoomInButton").disabled = false; 
	}
}

function zoom(zoom = true){
	if(zoom && right - left > maxZoom){
		left++;
		if(right - left > maxZoom)
			right--;
	}else if(!zoom){
		right = (right==MAX)?MAX:right+1;
		left = (left==MIN)?MIN:left-1;
	}else 
		return;
	setLeftAndRight(left,right);
}

function go(steps){
	if(steps!=0 && (left+steps)>=MIN && (right+steps)<=MAX){
		left += steps;
		right += steps;
		setLeftAndRight(left,right);
	}
}

function setLeftAndRight(left,right){
	showButtons();
	for (var i = 0; i < options.length; i++) {
		options[i].hAxis.viewWindow.min = left;
		options[i].hAxis.viewWindow.max = right;
	}
	resize(500);
}

function indexOrHigher(array,l){
	for (var i = 0; i < array.length; i++) {
		if(array[i]>=l){
			if(i>=MAX) return MAX-1;
			else return i;
		}
	}
	return MAX-1;
}

function indexOrLower(array,l){
	var o = 0;
	for (var i = 0; i < array.length; i++) {
		if(array[i]<=l){
			o = i;
		}else
			return o;
	}
	return o;
}

function onlySuccesClicked(){
	var MAXO = MAX
	MAX = calculateMax();
	var oldL,oldR;
	var b = buildNr
	var a = buildNr.filter(x=>!unsuccesfullBuildsNames.includes(x))
	var c = []
	for (var i = 0; i < a.length; i++) {
		c[i] = b.indexOf(a[i])
	}
	c[a.length] = b.length;
	if(document.getElementById("onlySucces").checked){
		right = indexOrHigher(c,right-1)+1;
		left = indexOrLower(c,left);
	}else{
		left = c[left]
		right = c[right-1]+1;
	}
	while(right-left<maxZoom){
		if(left>MIN){
			left--;
		}else if(right<MAX){
			right++
		}else
			break;
	}
	setLeftAndRight(left,right);	
}

function orderBar(){
	var sizeLeft = document.getElementById("leftButton").getBoundingClientRect().width;
	var sizeRight = document.getElementById("rightButton").getBoundingClientRect().width;
	var sizeZoomIn = document.getElementById("zoomInButton").getBoundingClientRect().width;
	var sizeZoomOut = document.getElementById("zoomOutButton").getBoundingClientRect().width;
	var sizeSearch = document.getElementById("searchField").getBoundingClientRect().width;
	var leftSize = document.getElementById("controller").getBoundingClientRect().width*0.8 - sizeLeft - sizeRight - sizeZoomIn - sizeZoomOut - sizeSearch - 2;
	document.getElementById("rightButton").style.marginLeft = leftSize+"px";
}

function filter(start=false){
	var input = document.getElementById("searchField").value.toLowerCase();
	if(start && input === "") return;
	var rows = document.getElementById("charts").getElementsByTagName("TR");
	for(i = 0; i<rows.length; i++){
		var id = rows[i].id;
		if(id!=""){
			if(input==="" || id.toLowerCase().includes(input)){
				jQ(rows[i]).fadeIn(800);
			}else{
				if(start)
					jQ(rows[i]).fadeOut(0);
				else
					jQ(rows[i]).fadeOut();
			}
		}
	}
}

//on Zoom, windows size change
function resize (duration = 0) {
	stayTop();
	orderBar();
	for (var c in columnDic) {
		var col = parseInt(c);
		if(col==0)continue;
		if (disabledColumns.includes(col)) {
			// hide the data series
			columns[col] = {
					label: dataView[0].getColumnLabel(col),
					type: 'number',
					calc: function (a,b) {
						return null;
					}
			};
			// grey out the legend entry
			series[columnDic[col] - 1].color = '#A9A9A9';
		}
		else {
			// show the data series
			columns[col] = functions[columnDic[col]-1]
			series[columnDic[col] - 1].color = null;
		}
	}
	
	for (var i = 0; i < charts.length; i++) {
		var view = dataView[i];
		view.setColumns(columns);
		if(document.getElementById("onlySucces").checked){
			view.hideRows(unsuccesfullBuilds)
			showAllStats(succesfullBuilds.slice(left,right))
		}else{
			view.setRows(allBuilds)
			showAllStats(allBuilds.slice(left,right))
		}
		options[i]['animation']['duration'] = duration;
		options[i]['height'] = screen.height / 3;
		charts[i].draw(view, options[i]);
	}
}


//-----------------------------------------------------------------------------------------------Optic------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------Start------------------------------------------------------------------------//
stayTop();
orderBar();
showButtons();
//-----------------------------------------------------------------------------------------------Start------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------Chart------------------------------------------------------------------------//
function colorTime(data1, row) {
	if((
			(data1.getValue(row, 1)<data1.getValue(row, 2) && data1.getValue(row, 2)!=nuller) 
			||  
			(data1.getValue(row, 1)>data1.getValue(row, 3)  && data1.getValue(row, 3)!=nuller)
		))
		return '#ff0000';
	else return '#00ff00';
}

function colorPercent(data1, row) {
	if(
			(data1.getValue(row, 4)<data1.getValue(row,5) && data1.getValue(row, 5)!=nuller)
			||
			(data1.getValue(row, 4)>data1.getValue(row,6) && data1.getValue(row, 6)!=nuller)
		)
		return '#ff3300';
	else return '#0000ff';
}

//https://stackoverflow.com/questions/17444586/show-hide-lines-data-in-google-chart
function select() {
	var chart;
	for (var i = 0; i < charts.length; i++) {
		if(charts[i].getSelection().length>0){
			chart = charts[i];
		}
	}
	if(chart === null || chart === undefined){
		return;
	}
	var sel = chart.getSelection();
	// if selection length is 0, we deselected an element
	// if row is undefined, we clicked on the legend
	if (sel[0].row === null) {
		var col = sel[0].column
		if(!disabledColumns.includes(col)){
			if(disabledColumns.length==5) return;
			disabledColumns.push(col);
		}else{
			disabledColumns.splice(disabledColumns.indexOf(col),1);
		}
		resize(500);
	}
}

function concatVert(a,b){
	var c = [];
	for (var i = 0; i < a.length; i++) {
		c[i] = [a[i]].concat(b[i]);
	}
	return c;
}

function drawChart() {

	for(counter=0;counter<metricNames.length;counter++){
		//read data
		var d = new google.visualization.DataTable();
		d.addColumn('string', columnsNames[0]);
		for (var i = 1; i < columnsNames.length; i++) {
			d.addColumn('number', columnsNames[i]);
		}
		d.addRows(concatVert(buildNr,dataSet[counter]));
		var unit = metricInfo[metricNames[counter]];
		unit = unit===undefined?'':unit;
		d['unit'] = unit;
		//options
		options[counter] = {
				interpolateNulls: false,
				tooltip: {isHtml: true},
				title:metricNames[counter],
				//X-Axis
				hAxis: {
					title: columnsNames[0],
					//format:"##",
					viewWindow: {min:left, max:right}
				},
				series: series,
				vAxes: {
					0: {format: '#.#'+unit},
					1: {title: axisNames[0],format:'#.#%'}
				},
				legend: { position: 'bottom',maxLines: 2},
				colors: ['#00ff00', '#66ff33', '#66ff33', '#0000ff', '#3399ff', '#3399ff'],
				animation : {
					duration: 500,
					easing: 'linear',
					startup : false
				},
				focusTarget: 'category'
		};

		dataView[counter] = new google.visualization.DataView(d);
		dataView[counter].setColumns(columns);
		charts[counter] = new google.visualization.LineChart(document.getElementById('curve_chart'+counter));
		google.visualization.events.addListener(charts[counter], 'select', select);
	}
	window.onload = resize;
	window.onresize = resize;
	window.onscroll = stayTop;
	resize();
	//else the view jumps
	resize();
	filter(true);
	resetPosition(save);
}

//-----------------------------------------------------------------------------------------------Chart------------------------------------------------------------------------//
//-----------------------------------------------------------------------------------------------Stats------------------------------------------------------------------------//

function showAllStats(builds){
	for(counter=0;counter<metricNames.length;counter++){
		drawStats(counter,builds);
	}
}

function drawStats(metricNr,builds){
	var name = metricNames[metricNr];
	var stats = calculateStats(name,builds);
	if(stats['avg']!=null){
		var result = '';
		var unit = metricInfo[name];
		unit = unit===undefined?'':unit;
		result += statNames['avg'] + ': ' + stats['avg'].toFixed(2)+unit+'<br>';
		result += statNames['min'] + ': ' + stats['min'].toFixed(2)+unit+'<br>';
		result += statNames['max'] + ': ' + stats['max'].toFixed(2)+unit+'<br>';
		result += statNames['var'] + ': ' + stats['var'].toFixed(2)+unit+(unit===''?'':'<sup>2</sup>')+'<br>';
	}
	jQ('#curve_chart_stat'+metricNr).html(result);
}

function calculateStats(metricname,builds){
	var result = {};
	var array = dataSet2[metricname];
	
	result['avg'] = null;
	result['var'] = null;
	result['min'] = null;
	result['max'] = null;
	var counter = 0;
	for (var a = 0; a < builds.length; a++) {
		var i = builds[a];
		var x = null;
		if((x=array[i][0])!=null){
			result['avg'] = result['avg'] + x;
			if(result['min']==null || result['min']>x){
				result['min'] = x;
			}
			if(result['max']==null || result['max']<x){
				
				result['max'] = x;
			}
			counter++;
		}
	}
	if(result['avg']!=null){
		result['avg'] /= counter;
		for (var a = 0; a < builds.length; a++) {
			var i = builds[a];
			if(array[i][0]!=null){
				result['var'] = result['var'] + Math.pow(array[i][0]-result['avg'],2);
			}
		}
		result['var'] /= counter;
	}
	
	return result;
}