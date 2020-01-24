var stompClient = null;

function setConnected(connected) {
   	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
    $("#name").prop("disabled", connected);
    if (connected) {
        $("#conversation").show();
        $("#sendForm").show();
    }
    else {
        $("#conversation").hide();
        $("#sendForm").hide();
    }
    $("#messages").html("");
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
        stompClient.subscribe('/topic/messages', function (message) {
            showMessage(JSON.parse(message.body));
        });
        sendName();
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
    $("#message").focus();
}

function sendMessage() {
    stompClient.send("/app/message", {}, JSON.stringify(
    		{'name': $("#name").val(), 'message': $("#message").val()}));
    $("#message").val('');
}

function showGreeting(message) {
    $("#messages").prepend("<tr><td>" + message + "</td></tr>");
}

function showMessage(message) {
    $("#messages").prepend("<tr><td><strong>" + message.name + ":</strong> " + message.message + "</td></tr>");
}

$(function () {
	$("#conversation").hide();
    $("#sendForm").hide();
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#name" ).keyup(function() { 
   		$("#connect").prop("disabled", !($("#name").val().trim()));
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#sendMessage" ).click(function() { sendMessage(); });
    $("#name").focus();
});