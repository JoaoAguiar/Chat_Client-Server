import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer {
  // Um buffer pre-alocado para os dados que vamos receber
  static private final ByteBuffer buffer = ByteBuffer.allocate(16384);

  // Codificador e Descodificador de mensagens
  static private final Charset charset = Charset.forName("UTF8");
  static private final CharsetDecoder decoder = charset.newDecoder();
  static private final CharsetEncoder encoder = charset.newEncoder();

  // Informação do user
  static private HashMap<SocketChannel, ChatUser> userCH = new HashMap<SocketChannel, ChatUser>();
  static private HashMap<String, ChatUser> userNAME = new HashMap<String, ChatUser>();
  static private HashMap<String, ChatRoom> rooms = new HashMap<String, ChatRoom>();

  static public void main(String args[]) throws Exception {
    int port = Integer.parseInt(args[0]);

    // Descodificador para o user
    try {
      // Instead of creating a ServerSocket, create a ServerSocketChannel
      ServerSocketChannel server_socket_chanel = ServerSocketChannel.open();
      // Define-a como: sem bloqueio, para que possamos usar select
      server_socket_chanel.configureBlocking(false);
      // Conectar a socket ao canal e ligua-lo à porta
      ServerSocket server_socket = server_socket_chanel.socket();
      InetSocketAddress inet_socket_address = new InetSocketAddress(port);
      server_socket.bind(inet_socket_address);
      // Criar um novo Selector para selecionar
      Selector selector = Selector.open();
      // Registar o ServerSocketChannel, para que possa-mos ouvir as conexões de input
      server_socket_chanel.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("Chat listening on port " + port);

      while(true) {
        // Verifica se temos atividade (conexões de input ou dados de uma conexão existente) 
        // Se nao tever atividade (0) volta a repetir o loop
        if(selector.select() == 0) {
          continue;
        }

        // Obtem as chaves correspondentes à atividade detectada e processe-as uma por uma
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = keys.iterator();

        while(keyIterator.hasNext()) {
          // Obtem a chave representando um dos bits da atividade de I/O
          SelectionKey key = keyIterator.next();

          if((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
            // Conexão de input
            // Regista-mos o socket com o seletor para poder ouvir o input dele
            Socket socket = server_socket.accept();
            System.out.println("Got connection from " + socket);
            // Certifica-se de torná-lo sem bloqueio, para que possamos usar um selector nele
            SocketChannel socket_channel = socket.getChannel();
            socket_channel.configureBlocking(false);

            // Registra-o com o selector, para leitura
            socket_channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            ChatUser user = new ChatUser(socket_channel);
            userCH.put(socket_channel, user);
            user.setState(State.INIT);
          }
          else if((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
            SocketChannel socket_channel = null;

            try {
              // Dados a chegar da conexão, vamos processa-los
              socket_channel = (SocketChannel)key.channel();
              boolean ok = processInput(socket_channel);

              // Se a conexão estiver "morta", remove-mos e fechamos este canal do selector
              if(!ok) {
                key.cancel();
                Socket socket = null;

                try {
                  socket = socket_channel.socket();
                  System.out.println("Closing connection to " + socket);
                  socket.close();
                } 
                catch(IOException ie) {
                  System.err.println("Error closing socket " + socket + ": " + ie);
                }
              }

            } 
            catch(IOException ie) {
              // Se tiver uma excepção, remove-mos e fechamos este canal do selector
              key.cancel();

              try {
                socket_channel.close();
              } 
              catch(IOException ie2) { 
                System.out.println(ie2); 
              }

              System.out.println("Closed " + socket_channel);
            }
          }
        }

        // We remove the selected keys, because we've dealt with them.
        keys.clear();
      }
    } 
    catch(IOException ie) {
      System.err.println(ie);
    }
  }

  static private void clientSendMessage(ChatUser sender, SocketChannel socket_channel, String msg) throws IOException{
    String username = sender.getUsername();
    ChatRoom room = sender.getRoom();
    ChatUser[] userList = room.getUsers();

    for(ChatUser user : userList) {
      String message = String.format("MESSAGE %s %s", username, msg);
      SocketChannel socket_channel_output = user.getSocket();
      ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message.toString()));
      socket_channel_output.write(bufferAux);
    }
  }

  static private void serverSendMessage(SocketChannel socket_channel, String message) throws IOException {
      ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message.toString()));
      socket_channel.write(bufferAux);
  }

  // Lê a mensagem da socket e manda para o STDOUT
  static private boolean processInput(SocketChannel socket_channel) throws IOException {
    // Lê a mensagem para o buffer
    buffer.clear();
    socket_channel.read(buffer);
    buffer.flip();

    // Se não tiver dados, fecha a conexão
    if(buffer.limit() == 0) { 
      return false; 
    }

    // Decode and print the message to stdout
    String message = decoder.decode(buffer).toString().trim();
    ChatUser sender = (ChatUser)userCH.get(socket_channel);

    // Comandos
    if(message.startsWith("/")) {
      String subMessage = message.substring(1);
      String command = subMessage.split(" ")[0];

      if(command.startsWith("nick")) { 
        commandNick(sender, socket_channel, subMessage.split(" ")[1]); 
      }
      else if(command.startsWith("join")) {
        String room = subMessage.split(" ")[1];

        commandJoinRoom(sender, room, socket_channel);
      }
      else if(command.startsWith("leave")) { 
        commandLeaveRoom(sender, socket_channel); 
      }
      else if(command.startsWith("bye")) { 
        commandBye(sender, socket_channel); 
      }
      else { 
        clientSendMessage(sender, socket_channel, subMessage); 
      }
    }
    // Mensagem
    else {
      if(sender.getState() == State.INSIDE) { 
        clientSendMessage(sender, socket_channel, message); 
      }
      else { 
        String error_message = "ERROR_MESSAGE_OUTSIDE" + sender.getUsername() + "\n";
        serverSendMessage(socket_channel, error_message); 
      }
    }

    return true;
  }

  static private void commandNick(ChatUser sender, SocketChannel socket_channel, String nick) throws IOException {
    nick = nick.replace("\n", "").replace("\r", "");
      
    if(!userNAME.containsKey(nick)) {
      if(sender.getState() == State.INIT) {
          sender.setState(State.OUTSIDE);
          sender.setUsername(nick);
          String message = "NICK " + nick + '\n';
          serverSendMessage(socket_channel, message);
      }
      else if(sender.getState() == State.INSIDE) {
        ChatRoom room = sender.getRoom();
        ChatUser[] userList = room.getUsers();
          
        for(ChatUser user : userList) {
          if(user != sender) {
            String message = "NEWNICK " + sender.getUsername() + " " + nick + '\n';
            SocketChannel socket_channel_output = user.getSocket();
            ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message));
            socket_channel_output.write(bufferAux);
          }
        }
        
        sender.setUsername(nick);
      }
      else if(sender.getState() == State.OUTSIDE) {
          String message = "NEWNICK " + sender.getUsername() + " " + nick + '\n';
          sender.setUsername(nick);
          serverSendMessage(socket_channel, message);
      }
    }
    else { 
      serverSendMessage(socket_channel, "ERROR\n"); 
    }
  }

  static private void commandJoinRoom(ChatUser sender, String room, SocketChannel socket_channel) throws IOException {
      if(!rooms.containsKey(room)) {
        ChatRoom newRoom = new ChatRoom(room);
        rooms.put(room, newRoom);
      }
      if(sender.getState() == State.OUTSIDE) {
          ChatRoom entry = rooms.get(room);

          entry.joinRoom(sender);
          sender.setRoom(entry);
          sender.setState(State.INSIDE);

          String message = "JOIN_ROOM " + sender.getUsername() + " " + room + "\n";
          serverSendMessage(socket_channel, message);
          
          ChatUser[] userList = entry.getUsers();
          message = "JOINED " + sender.getUsername() + room + "\n";
          
          for(ChatUser user : userList) {
            if(user != sender) {
              SocketChannel socket_channel_output = user.getSocket();
              ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message));
              socket_channel_output.write(bufferAux);
            }
          }
      }
      else if(sender.getState() == State.INSIDE) {
        ChatRoom cur = sender.getRoom();
        ChatUser[] userList = cur.getUsers();

        String message = "LEFT_JOIN_ROOM " + sender.getUsername() + " " + cur.getRoom() + " " + room + "\n";
        serverSendMessage(socket_channel, message);
        message = "LEFT " + sender.getUsername() +'\n';
        
        for(ChatUser user : userList) {
          if(user != sender){
            SocketChannel socket_channel_output = user.getSocket();
            ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message));
            socket_channel_output.write(bufferAux);
          }
        }

        ChatRoom entry = rooms.get(room);

        cur.leaveRoom(sender);
        entry.joinRoom(sender);
        sender.setRoom(entry);
        userList = entry.getUsers();
        message = "JOINED " + sender.getUsername() +'\n';
        
        for(ChatUser user : userList) {
          if(user != sender) {
            SocketChannel socket_channel_output = user.getSocket();
            ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message));
            socket_channel_output.write(bufferAux);
          }
        }
      }
      else { 
        serverSendMessage(socket_channel, "ERROR\n"); 
      }
  }

  static private void commandLeaveRoom(ChatUser sender, SocketChannel socket_channel) throws IOException {
    if(sender.getState() == State.INSIDE) {
      ChatRoom leave = (ChatRoom)sender.getRoom();
      String username = sender.getUsername();
      
      leave.leaveRoom(sender);
      sender.setState(State.OUTSIDE);

      String message = "LEFT_ROOM " + username + " " + sender.getRoom() + "\n";
      serverSendMessage(socket_channel, message);
      
      message = "LEFT " + username + '\n';
      ChatUser[] userList = leave.getUsers();

      for(ChatUser user : userList) {
        SocketChannel socket_channel_output = user.getSocket();
        ByteBuffer bufferAux = encoder.encode(CharBuffer.wrap(message.toString()));
        socket_channel_output.write(bufferAux);
      }
    }
    else { 
      String message = "ERROR_NO_ROOM " + sender.getUsername() + "\n";
      serverSendMessage(socket_channel, message); 
    }
  }

  static private void commandBye(ChatUser sender, SocketChannel socket_channel) throws IOException {
      String message = "BYE" + sender.getUsername() + "\n";
      serverSendMessage(socket_channel, message);
      socket_channel.close();
  }
}
