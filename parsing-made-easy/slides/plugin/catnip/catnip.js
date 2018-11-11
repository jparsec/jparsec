(function() {

  var catnipFrame = null;
  var consoleBacklog = [];

  window.console = {
    log: function() {
      var command = "client-frame:" + JSON.stringify({
        console: {
          method: "log",
          arguments: Array.prototype.slice.call(arguments)
        }
      });
      if (catnipFrame)
        catnipFrame.postMessage(command, "*");
      else
        consoleBacklog.push(command);
    }
  };

  var lastUrl = window.location.href;

  window.setInterval(function() {
    var currentUrl = window.location.href;
    if (currentUrl != lastUrl) {
      lastUrl = currentUrl;
      if (catnipFrame) {
        var command = "client-frame:" + JSON.stringify({
          url: lastUrl
        });
        catnipFrame.postMessage(command, "*");
      }
    }
  }, 100);

  window.addEventListener("message", function(event) {
    if (event.data === "hello") {
      catnipFrame = event.source;
      consoleBacklog.forEach(function(item) {
        catnipFrame.postMessage(JSON.stringify(item), "*");
      });
      consoleBacklog = [];
    } else if (event.data === "nextSlide") {
      Reveal.navigateNext();
    } else if (event.data === "previousSlide") {
      Reveal.navigatePrevious();
    } else if (event.data === "slideLeft") {
      Reveal.navigateLeft();
    } else if (event.data === "slideRight") {
      Reveal.navigateRight();
    } else if (event.data === "slideUp") {
      Reveal.navigateUp();
    } else if (event.data === "slideDown") {
      Reveal.navigateDown();
    } else if (event.data === "escape") {
      Reveal.toggleOverview();
    }
  }, false);
}());
