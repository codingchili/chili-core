/**
 * @author Robin Duda
 *
 * Game.js
 */

var resources = {};

var game = {
    initialize: function (_canvas) {
        canvas = _canvas;
        context = canvas.getContext('2d');
        account = application.authentication.account;

        document.keydown(function (event) {
            console.log(event);
            keys[event.keyCode] = true;
        });

        document.keyup(function (event) {
            console.log(event);
            keys[event.keyCode] = false;
        });

        loop();
    }
};

var account = null;
var canvas = null;
var context = null;
var player = {x: 100.0, y: 100.0};
var keys = {};

function loop() {
    requestAnimationFrame(loop);
    update();
    draw();
}

function update() {
    if (keys[83])
        player.y = player.y + 1;
    if (keys[68])
        player.x = player.x + 1;
    if (keys[65])
        player.x = player.x - 1;
    if (keys[87])
        player.y = player.y - 1;
}

function draw() {
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.fillStyle = "#FF0000";
    context.fillRect(player.x, player.y, 24, 24);
    context.font = "16px Arial";
    context.fillText(account.username, player.x - 8, player.y - 8);
}