//From https://www.w3schools.com/howto/howto_js_sort_table.asp
var lastSortbyName = null;
var lastSortDir = null;
var lastSortFunction = null;

function sortTable(name,tablename,f,dir="asc",forceAsc=false) {
	if(dir==null) dir = "asc";
	lastSortbyName = name;
	lastSortFunction = ""+f;
	var table, rows, switching, i, x, y, shouldSwitch, switchcount = 0;
	table = document.getElementById(tablename);
	if(name==="id"){
		var n = "id";
	}else{
		var n = jQ("#"+tablename).find('thead > tr > th').get().map(p=>p.id).indexOf(name);
	}
	switching = true;
	while (switching) {
		switching = false;
		rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("TR");
		for(i = 0; i<(rows.length - 1); i++){
			shouldSwitch = false;
			if(n==="id"){
				t1 = rows[i].id;
				t2 = rows[i + 1].id;
			}else{
				x = rows[i].getElementsByTagName("TD")[n];
				y = rows[i + 1].getElementsByTagName("TD")[n];
				t1 = f(x);
				t2 = f(y);
			}
			if((t1=="--"&&t2=="--") || (t1!="--"&&t2=="--")){
				shouldSwitch = false;
			}else if(t1=="--"&&t2!="--"){
				shouldSwitch = true;
				break;
			}else{
				if (dir == "asc") {
					if ( t1 > t2 ) {
						shouldSwitch = true;
						break;
					}
				} else if (dir == "desc") {
					if ( t1!=t2 && t1<t2 ) {
						shouldSwitch = true;
						break;
					}
				}
			}
		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
			switchcount ++;
		} else {
			if (switchcount == 0 && dir == "asc" && !forceAsc) {
				dir = "desc";
				switching = true;
			}
		}
	}
	lastSortDir = dir;
}