# Chat_Client-Server
>Server and Client with a GUI for a Chat application on desktop.

## Usage
### Compile
```bash
javac ChatClient.java ChatServer.java
```
### Execute
On one computer execute ChatServer with the port that the server should be listening:
```bash
java ChatServer 1337
```

In other computers (or same) execute ChatServer with the IP address and port that the server is listening:
```bash
java ChatClient 192.168.1.100 1337
```
