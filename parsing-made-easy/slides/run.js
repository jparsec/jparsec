#!/usr/bin/env node

var fs = require("fs");
var path = require("path");
var express = require("express");

var app = express(),
    server = require("http").createServer(app),
    io = require("socket.io").listen(server);

io.set("log level", 1);

io.of("/notes")
  .on('connection', function(socket) {
    socket.on('slidechanged', function(slideData) {
      socket.broadcast.emit('slidedata', slideData);
    });
    socket.on('fragmentchanged', function(fragmentData) {
      socket.broadcast.emit('fragmentdata', fragmentData);
    });
  });

app.configure(function() {
  ['m', 'css', 'js', 'images', 'plugin', 'lib'].forEach(function(dir) {
    app.use('/' + dir, express.static(path.join(__dirname, dir)));
  });
});

app.get("/", function(req, res) {
  res.writeHead(200, {'Content-Type': 'text/html'});
  fs.createReadStream(path.join(__dirname, 'index.html')).pipe(res);
});

app.get("/notes/:socketId", function(req, res) {
  fs.readFile(path.join(__dirname, 'plugin/notes-server/notes.html'),
              function(err, data) {
                res.send(require("mustache").to_html(data.toString(), {
                  socketId: req.params.socketId
                }));
              });
});

server.listen(1337);

var brown = '\033[33m',
    green = '\033[32m',
    reset = '\033[0m';

var slidesLocation = "http://localhost:1337/";

console.log( brown + "reveal.js - Speaker Notes" + reset );
console.log( "1. Open the slides at " + green + slidesLocation + reset );
console.log( "2. Click on the link your JS console to go to the notes page" );
console.log( "3. Advance through your slides and your notes will advance automatically" );

server.listen(1337);
