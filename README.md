# Chat_Client-Server
>Server and Client with a GUI for a Chat application on desktop.

## Usage
### Compile
```
javac ChatClient.java ChatServer.java
```
### Execute
On one computer execute ChatServer with the port that the server should be listening:
```
java ChatServer port
```

In other computers (or same) execute ChatServer with the IP address and port that the server is listening:
```
java ChatClient host port
```

### Commands
* /nick name: Used to choose or change a username in the chat
* /join room: Used to enter in a room or to change to another. If the room does not exist, it is created
* /leave: Leave the room.
* /bye: Close the chat.
