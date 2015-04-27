# Place all the behaviors and hooks related to the matching controller here.
# All this logic will automatically be available in application.js.
# You can use CoffeeScript in this file: http://coffeescript.org/

jQuery ->
	# DatePicker initialize
	$('.dtpicker').datetimepicker
		format:'YYYY-MM-DD HH:mm:ss'
		showTodayButton:true
		sideBySide:true

	# Replay count
	$("#counter").click ->
		$('#counter').html '?'
		id = $("input[name=statement_id]").val()
		$.get '/statements/' + id + '/results/count',
			$("#replayf").serialize()
			(res, status, xhr) -> $('#counter').html res


# $.ajax '/yourUrlHere',
# 	data :
# 		key : 'value'
# 	success  : (res, status, xhr) ->
# 		alert "ok"
#	error    : (xhr, status, err) ->
#	complete : (xhr, status) ->