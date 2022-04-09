let fullData = {};
const momentFormat = "YYYY-MM-DD HH:mm:ss";
$(document).ready(function () {
	$("#events").submit(function (event) {
		$("#btn-search").prop('disabled', true);
		let data = getData();
		localStorage.setItem("topicName", data.topic)
		localStorage.setItem("key", data.key)
		localStorage.setItem("jsonPathPredicate", data.jsonPathPredicate)
		localStorage.setItem("messageId", data.messageId)
		localStorage.setItem("from", data.from)
		localStorage.setItem("to", data.to)
		$('#tbody-events').html('<tr><td colspan="5" style="text-align: center">Loading...</td></tr>');
		$.ajax({
			type: "GET",
			url: "/read",
			data,
		}).done(function (data) {
			let html = data.messages.length === 0 ? '<tr><td colspan="6" style="text-align: center">No data found!</td></tr>' : '';
			$.each(data.messages, function (i, item){
				fullData[item.messageId] = item;
				html += '<tr><td>' + (i + 1) + '</td><td>' + item.messageId + '</td><td>' + moment(item.publishTime).format("yyyy-MM-DD HH:mm:ss") + '</td><td>' + item.topic + '</td><td>' + item.producer + '</td><td>' + keyToString(item.key) + '</td><td onclick="showDetail(\'' + item.messageId + '\')">' +  item.payload.substr(0, 96) + '</td></tr>';
			});
			$('#tbody-events').html(html);
			if(data.errorMessage) {
				$('#msg-info').html('Total messages: ' + data.messages.length + '&nbsp;&nbsp;&nbsp;' + data.errorMessage);
			} else {
				$('#msg-info').html('Total messages: ' + data.messages.length);
			}
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
	let storageKey = localStorage.getItem("key");
	if(storageKey && storageKey !== "undefined"){
		$("#key").val(storageKey);
	}
	let storageJsonPathPredicate = localStorage.getItem("jsonPathPredicate");
	if(storageJsonPathPredicate && storageJsonPathPredicate !== "undefined"){
		$("#json-path-predicate").val(storageJsonPathPredicate);
	}
	let storageMessageId = localStorage.getItem("messageId");
	if(storageMessageId && storageMessageId !== "undefined"){
		$("#message-id").val(storageMessageId);
	}

	let from = localStorage.getItem("from");
	if(from && from !== "undefined"){
		$("#from").val(moment.unix(from).format(momentFormat));
	}else{
		$("#from").val(moment().subtract(1, 'hour').format(momentFormat));
	}
	let to = localStorage.getItem("to");
	if(to && to !== "undefined"){
		$("#to").val(moment.unix(to).format(momentFormat));
	}else{
		$('#to').attr("placeholder", moment().format(momentFormat));
	}
});

function getData(){
	let data = { topic: $("#topic-name").val() };
	if($("#message-id").val().length > 0 && $("#message-id").val() !== 'undefined'){
		data["messageId"] = $("#message-id").val();
	}
	if($("#json-path-predicate").val().length > 0 && $("#json-path-predicate").val() !== 'undefined'){
		data["jsonPathPredicate"] = $("#json-path-predicate").val();
	}
	if($("#key").val().length > 0 && $("#key").val() !== 'undefined'){
		data["key"] = $("#key").val();
	}
	if($("#from").val().length > 0 && $("#from").val() !== 'undefined'){
		data["from"] = moment($("#from").val(), momentFormat).unix();
	}
	if($("#to").val().length > 0 && $("#to").val() !== 'undefined'){
		data["to"] = moment($("#to").val(), momentFormat).unix();
	}

	return data;
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

function keyToString(key){
	if (key === undefined || key === null){
		return '';
	}
	return key;
}
$(document).ready(function () {
	$.ajax({
		type: "GET",
		url: "/info",
	}).done(function (data) {
		$('#version').html(data.version);
	}).fail(function (data) {
		$('#version').html("unknown");
	});
});
