# LogCatLogTracker
This project creates a service which runs on background thread. This thread reads logs from logcat and send it to server through socket connection.
The server is in node JS (https://github.com/AggarwalAnkit/SocketServer.git). 

#TODOS:
1. use batching to send logs
2. create a ui to show which apps can be tracked and on select of that app pass the process id of that app to the service to track
