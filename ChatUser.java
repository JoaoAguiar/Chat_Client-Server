import java.nio.channels.*;

enum State { INIT, OUTSIDE, INSIDE }

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
	public void setUsername(String username) { 
		this.username = username; 
	}
	public void setState(State state) { 
		this.state = state; 
	}
	public void setRoom(ChatRoom room) { 
		this.room = room; 
	}
	
	/***** GETTERS *****/
	public String getUsername() { 
		return this.username; 
	}
	public State getState() { 
		return this.state; 
	}
	public ChatRoom getRoom() { 
		return this.room; 
	}
	public SocketChannel getSocket() { 
		return this.socket; 
	}
}
