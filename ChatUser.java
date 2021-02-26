import java.nio.channels.*;

enum States { INIT, OUTSIDE, INSIDE }

public class ChatUser {
	private ChatRoom room;
	private SocketChannel socket;
	private State state;
	private String username;

	public ChatUser(SocketChannel socket, String username, State state) {
		this.username = username;
		this.socket = socket;
		this.state = state;
	}
	public ChatUser(SocketChannel socket, State state) {
		this.username = "";
		this.socket = socket;
		this.state = state;
	}
	public ChatUser(SocketChannel socket, String username) {
		this.username = username;
		this.socket = socket;
		this.state = State.INIT;
	}
  	public ChatUser(SocketChannel socket) {
  		this.username = "";
    	this.state = State.INIT;
    	this.socket = socket;
  	}

	/***** SETTERS *****/

	// Nome
	public void setUsername(String username) { 
		this.username = username; 
	}
	// Estado
	public void setState(State state) { 
		this.state = state; 
	}
	// Sala
	public void setRoom(ChatRoom room) { 
		this.room = room; 
	}
	
	/***** GETTERS *****/
	
	// Nome
	public String getUsername() { 
		return this.username; 
	}
	// Estado
	public State getState() { 
		return this.state; 
	}
	// Sala
	public ChatRoom getRoom() { 
		return this.room; 
	}
	// Socket
	public SocketChannel getSocket() { 
		return this.socket; 
	}
}
