let fullData = {};

$(document).ready(function () {
	$("#events").submit(function (event) {
		$("#btn-search").prop('disabled', true);
		let data = getData();
		localStorage.setItem("topicName", data.topic)
		localStorage.setItem("jsonPathPredicate", data.jsonPathPredicate)
		localStorage.setItem("messageId", data.messageId)
		$.ajax({
			type: "GET",
			url: "/read",
			data,
		}).done(function (data) {
			let html = data.length === 0 ? '<tr><td colspan="4" style="text-align: center">No data found!</td></tr>' : '';
			$.each(data, function (i, item){
				fullData[item.messageId] = item;
				html += '<tr onclick="showDetail(\'' + item.messageId + '\')"><td>' + item.messageId + '</td><td>' + moment(item.publishTime).format("yyyy-MM-DD HH:mm:ss") + '</td><td>' + item.producer + '</td><td>' +  item.payload.substr(0, 96) + '</td></tr>';
			});
			$('#tbody-events').html(html);
			$("#btn-search").prop('disabled', false);
		}).fail(function (data) {
			alert(JSON.stringify(data.responseJSON, null, 2));
			$("#btn-search").prop('disabled', false);
		});

		event.preventDefault();
	});
	let storageTopicName = localStorage.getItem("topicName");
	if(storageTopicName && storageTopicName !== "undefined"){
		$("#topic-name").val(storageTopicName);
	}
	let storageJsonPathPredicate = localStorage.getItem("jsonPathPredicate");
	if(storageJsonPathPredicate && storageJsonPathPredicate !== "undefined"){
		$("#json-path-predicate").val(storageJsonPathPredicate);
	}
	let storageMessageId = localStorage.getItem("messageId");
	if(storageMessageId && storageMessageId !== "undefined"){
		$("#message-id").val(storageMessageId);
	}
});

function getData(){
	if($("#message-id").val().length > 0 && $("#message-id").val() !== 'undefined'){
		return {
			topic: $("#topic-name").val(),
			messageId: $("#message-id").val()
		}
	}
	if($("#json-path-predicate").val().length > 0 && $("#json-path-predicate").val() !== 'undefined'){
		return {
			topic: $("#topic-name").val(),
			jsonPathPredicate: $("#json-path-predicate").val()
		}
	}
	return {
		topic: $("#topic-name").val()
	}
}

function showDetail(messageId){
	$('#event-modal-label').html('Message ID: ' + messageId);
	try {
		let payload = JSON.parse(fullData[messageId].payload);
		$('#event-modal-body').html('<pre id="json">' + JSON.stringify(payload, undefined, 2) + '</pre>');
	}
	catch(err) {
		$('#event-modal-body').html('<pre id="text">' + fullData[messageId].payload + '</pre>');
	}
	$('#event-modal').modal();
}