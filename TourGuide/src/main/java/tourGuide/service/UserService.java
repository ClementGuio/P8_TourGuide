package tourGuide.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class UserService {

	@Autowired
	UserDAOForTesting userDAO;
	
	public UserService() {
		this.userDAO = new UserDAOForTesting();
	}
	public User getUser(String userName) {
		return userDAO.getUser(userName);
	}
	
	public List<User> getAllUsers() {
		return userDAO.getAllUsers();
	}
	
	public void addUser(User user) {
		userDAO.addUser(user);
	}
	
	public void deleteUser(String userName) {
		userDAO.deleteUser(userName);;
	}
	
}
