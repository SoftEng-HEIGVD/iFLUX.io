var shiftWindow = function () {
	scrollBy(0, -65)
};
window.addEventListener("hashchange", shiftWindow);
function load() {
	if (window.location.hash) shiftWindow();
}

var allToggled = false;
var toggles = {};

console.log(toggles);

$('.toggle').on('click', function(event) {
	var ref = $(event.currentTarget).attr('data-ref');

	toggles[ref] = !toggles[ref];

	$(ref).collapse(toggles[ref] ? 'show' : 'hide');
});

$('.panel-collapse')
	.on('hidden.bs.collapse', function(event) {
		var ref = $(event.currentTarget).attr('data-ref');

		if (ref) {
			toggles[ref] = false;
		}
	});

$('.toggle-all').on('click', function(event) {
	var ref = $(event.currentTarget).attr('data-ref');

	allToggled = !allToggled;

	$(ref).collapse(allToggled ? 'show' : 'hide');

	$(event.currentTarget).find('.glyphicon-minus').toggle(allToggled);
	$(event.currentTarget).find('.glyphicon-plus').toggle(!allToggled);

	for (var key in toggles) {
		toggles[key] = allToggled;
	}
});