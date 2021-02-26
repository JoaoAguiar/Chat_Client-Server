import java.util.*;

public class ChatRoom {
	private String room;
	private ArrayList<ChatUser> userList;

	// Criar uma sala
	public ChatRoom(String room) {
		this.room = room;
		this.userList = new ArrayList<ChatUser>();
	}

	// Array com todos os users da sala
	public ChatUser[] getUsers() {
		ChatUser[] list = new ChatUser[this.userList.size()];
		int i = 0;

		for(ChatUser user : this.userList) {
			list[i] = user;
			i++;
		}

		return list;
	}
	
	// Obter a sala
	public String getRoom() { 
		return this.room; 
	}
	
	// Juntar a uma sala
	public void joinRoom(ChatUser user) { 
		this.userList.add(user); 
	}
	
	// Sair de uma sala
	public void leaveRoom(ChatUser user) { 
		this.userList.remove(user); 
	}
}
