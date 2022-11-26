package tourGuide.dao;

import java.util.List;

import tourGuide.user.User;

public interface IUserDAO {

	User getUser(String userName);

	List<User> getAllUsers();

	void addUser(User user);
	
	void deleteUser(String userName);

}