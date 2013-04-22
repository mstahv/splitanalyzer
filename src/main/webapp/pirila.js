(function() {
	window.onload = function() {
		var v = document.createElement("BUTTON");
		v.innerHTML = "SplitAnalyzer";
		var f = function() {
			var script = document.createElement('SCRIPT');
			script.type = 'text/javascript';
			script.src = 'http://v3.tahvonen.fi/splitanalyzer/splits/splits.nocache.js';
			document.getElementsByTagName('head')[0].appendChild(script);
			window.splitanalyzeself = true;
			v.style.display = "none";
		};
		v.onclick = f;
		var h = document.getElementsByTagName("h2")[0];

		h.parentNode.insertBefore(v, h.nextSibling);
	};
})();
