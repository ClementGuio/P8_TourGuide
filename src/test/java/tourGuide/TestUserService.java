package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;


public class TestUserService {

	UserService userService = new UserService();
	
	@Test
	public void addAndGetUser() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		userService.addUser(user);
		
		User retrivedUser = userService.getUser(user.getUserName());
		
		assertEquals(user, retrivedUser);
	}
	
	@Test
	public void getAllUsers() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userService.addUser(user);
		userService.addUser(user2);
		
		List<User> allUsers = userService.getAllUsers();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void deleteUser() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		
		userService.addUser(user);
		
		userService.deleteUser(user.getUserName());
		
		List<User> allUsers = userService.getAllUsers();
		
		assertFalse(allUsers.contains(user));
	}
	
}
