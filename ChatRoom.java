import java.util.*;

public class ChatRoom {
	private String room;
	private ArrayList<ChatUser> userList;

	public ChatRoom(String room) {
		this.room = room;
		this.userList = new ArrayList<ChatUser>();
	}

	public ChatUser[] getUsers() {
		ChatUser[] list = new ChatUser[this.userList.size()];
		int i = 0;

		for(ChatUser user : this.userList) {
			list[i] = user;
			i++;
		}

		return list;
	}
	
	public String getRoom() { 
		return this.room; 
	}
	
	public void joinRoom(ChatUser user) { 
		this.userList.add(user); 
	}
	
	public void leaveRoom(ChatUser user) { 
		this.userList.remove(user); 
	}
}
