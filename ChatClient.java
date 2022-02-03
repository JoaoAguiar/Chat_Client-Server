import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class ChatClient {
    // Variaveis relaticas GUI
    JFrame frame = new JFrame("Chat");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();

    // Codificador de mensagens
    private final Charset charset = Charset.forName("UTF8");
    private final CharsetEncoder encoder = charset.newEncoder();

    private SocketChannel socketChannel;

    // Metodo usado para adicionar uma string à caixa de texto
    public void printMessage(String message) {
        //Mensagem a dizer que mudou o nome!!!
        message = message.replace("\n", "");
        String[] tokens = message.split(" ");
        
        if(tokens[0].equals("NICK")) {
            message = "User tem novo nick: " + tokens[1] + "\n";
        }
        else if(tokens[0].equals("NEWNICK")) {
            message = tokens[1] + " mudou o nick para " + tokens[2] + "\n";
        }
        else if(tokens[0].equals("BYE")) {
            message = tokens[1] + " saiu do chat\n";
        }
        else if(tokens[0].equals("ERROR_MESSAGE_OUTSIDE")) {
            message = tokens[1] + " nao consegue enviar mensagem porque esta fora de um grupo\n";
        }
        else if(tokens[0].equals("JOIN_ROOM")) {
            message = tokens[1] + " entras-te numa sala (" + tokens[2] + ")\n";
        }
        else if(tokens[0].equals("LEFT_JOIN_ROOM")) {
            message = tokens[1] + " saiu da sala " + tokens[2] + " e entrou na sala " + tokens[3] + "\n";
        }
        else if(tokens[0].equals("LEFT_ROOM")) {
            message = tokens[1] + " saiu da sala " + tokens[2] + "\n";
        }
        else if(tokens[0].equals("ERROR")) {
            message = "ERRO\n";
        }
        else if(tokens[0].equals("ERROR_NO_ROOM")) {
            message = tokens[1] + " nao esta em nenhum sala\n";
        }
        
        System.out.print("Printing in chat box: " + message);
        
        chatArea.append(message);
    }

    // Construtor do chat do cliente
    public ChatClient(String server, int port) throws IOException {
        // Inicialização do GUI
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } 
                catch(IOException ex) {
                } 
                finally {
                    chatBox.setText("");
                }
            }
        });

        // Criar um Socket Channel
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(server, port));
        }
        catch(IOException ex) {
        }
    }

    // Metodo envocado sempre que um cliente quer mandar uma mensagem
    public void newMessage(String message) throws IOException {
        socketChannel.write(encoder.encode(CharBuffer.wrap(message)));
    }

    public void run() throws IOException {
      BufferedReader input = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));

      while(true) {
        String message = input.readLine();

        if(message == null) {
            break;
        }

        message = message + '\n';

        // Escreve a mensagem no chat
        printMessage(message);
      }

      // Fecha a Socket Channel
      socketChannel.close();
      System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }
}