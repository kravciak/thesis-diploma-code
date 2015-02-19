jQuery ->
	$('.thumbnail').hover(
	    -> $(@).find('a img').css("opacity", 0.9)
	    -> $(@).find('a img').css("opacity", 1)
	)