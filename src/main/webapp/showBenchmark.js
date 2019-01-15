var dangerCSS = { "background-color" : "#ff3333" };

function save(){
	var d = {n:lastSortbyName,d:lastSortDir,f:lastSortFunction}
	sessionStorage.setItem(window.location.pathname+"_Store",JSON.stringify(d));
}
function load(){
	var d = JSON.parse(sessionStorage.getItem(window.location.pathname+"_Store"))
	sessionStorage.removeItem(window.location.pathname+"_Store");
	if(d!=undefined){
		lastSortbyName = (d.n==null)?"metrik":d.n;
		lastSortDir = (d.d==null)?"asc":d.d;
		lastSortFunction = (d.f==null)?a=>a.innerHTML.toLowerCase():eval(d.f);
		sortTable(lastSortbyName,"resultTable",lastSortFunction,lastSortDir);
	}else{
		sortTable("metrik","resultTable",a=>a.innerHTML.toLowerCase(),"asc",forceAsc=true);
	}
}

build();
onStart(load,function(){sortTable("metrik","resultTable",a=>a.innerHTML.toLowerCase(),"asc",forceAsc=true);});
resetPosition(save);

function build(){
	var lastE = typeof(lastResult) !== "undefined";
	var firstE = typeof(firstResult) !== "undefined";
	var lastS = typeof(lastStableResult) !== "undefined";
	if(!lastE){
		jQ("#lastBuild").remove()
	}
	if(!firstE){
		jQ("#firstBuild").remove()
		jQ("#lastStableBuild").remove()
	}
	var metriks = Object.keys(results);
	var body = jQ("#resultTable tbody")
	//TODO order metrics by name
	for(var i = 0;i<metriks.length;i++){
		var find = metriks[i]
		var row = jQ("<tr>");
		var metrik = jQ("<td>").html(find)
		row.append(metrik);
		var curValue = results[find]
		var value = jQ("<td>")
		var error = false;
		var unit = units[find]==null?"":" "+units[find];
		var optional = ""
			if(typeof(config[find]) !== "undefined"){
				if(config[find]["maxValue"]<curValue){
					optional = " &gt; "+config[find]["maxValue"].toFixed(2)+unit
					error=true;
				}else if(config[find]["minValue"]>curValue){
					optional = " &lt; "+config[find]["minValue"].toFixed(2)+unit
					error=true;
				}
				if(error) value.css(dangerCSS);
			}
		value.html(curValue.toFixed(2)+unit+optional);

		row.append(value);

		if(lastE){
			error = error | add(row,lastResult,find,curValue,true);
		}
		if(lastS){
			error = error | add(row,lastStableResult,find,curValue,true);
		}
		if(firstE){
			add(row,firstResult,find,curValue);
		}

		if(error){
			metrik.css(dangerCSS);
		}
		body.append(row);
	}
}

function add(row,obj,find,curValue,show=false){
	var e=false;
	var x = obj[find];
	var p;
	var b="";
	var optional = "";
	var td = jQ("<td>");
	if(typeof(x) === "undefined"){
		p = "--";
	}else{
		p = calculateChange(x,curValue)*100;
		if(show){
			if(typeof(config[find]) !== "undefined"){
				if(config[find]["maxPercent"]<p){
					optional = " &gt; "+config[find]["maxPercent"].toFixed(2)+"%"
					e=true;
				}else if(config[find]["minPercent"]>p){
					optional = " &lt; "+config[find]["minPercent"].toFixed(2)+"%"
					e=true;
				}
			}
			if(e){
				td.css(dangerCSS);
			}else{
				if(p>0){
					var css = { "background-color" : "#ffd2a8" };
					td.css(css);
				}else if(p<0){
					td.addClass("success");
				}
			}
				
		}
		b = p>0?"+":"";
		var inf = "<sub><sub><font size='+2'>&infin;</font></sub></sub>";
		p = p===Number.NEGATIVE_INFINITY ? "-"+inf : ( p === Number.POSITIVE_INFINITY ? inf : p.toFixed(2));
		p+= "%";
	}
	td.html(b + p + optional);
	row.append(td);
	return e;
}

function calculateChange(prev, now){
	if(prev == 0){
		if(now > 0){
			return Number.POSITIVE_INFINITY;
		}else if(now < 0){
			return Number.NEGATIVE_INFINITY;
		}else return 0;
	}
	var r = ((now/prev)-1);
	if((now > prev && r > 0) || (now == prev && r == 0) || (now < prev && r < 0)){
		return r;
	}else{
		return r * -1;
	}
}
