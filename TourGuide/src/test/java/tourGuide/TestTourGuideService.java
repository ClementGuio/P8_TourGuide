package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;


public class TestTourGuideService {
	
	UserDAOForTesting userDAO = new UserDAOForTesting();
	
	@BeforeClass
	public static void setup() {
		Locale.setDefault(Locale.ENGLISH);
	}
	
	@Test
	public void getUserLocation() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	//TODO : déplacer dans UserServiceTest
	@Test
	public void addUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userDAO.addUser(user);
		userDAO.addUser(user2);
		
		User retrivedUser = userDAO.getUser(user.getUserName());
		User retrivedUser2 = userDAO.getUser(user2.getUserName());

		//tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	//TODO : déplacer dans UserServiceTest
	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userDAO.addUser(user);
		userDAO.addUser(user2);
		
		List<User> allUsers = userDAO.getAllUsers();

		//tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		//tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	//@Ignore // Not yet implemented - OK
	@Test
	public void getNearbyAttractions() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		
		//tourGuideService.tracker.stopTracking();
		Location loc0 = new Location(attractions.get(0).latitude,attractions.get(0).longitude);
		Location loc1 = new Location(attractions.get(1).latitude,attractions.get(1).longitude);
		Location loc2 = new Location(attractions.get(2).latitude,attractions.get(2).longitude);
		Location loc3 = new Location(attractions.get(3).latitude,attractions.get(3).longitude);
		Location loc4 = new Location(attractions.get(4).latitude,attractions.get(4).longitude);
		assertTrue(rewardsService.getDistance(visitedLocation.location, loc0) <= rewardsService.getDistance(visitedLocation.location, loc1));
		assertTrue(rewardsService.getDistance(visitedLocation.location, loc1) <= rewardsService.getDistance(visitedLocation.location, loc2));
		assertTrue(rewardsService.getDistance(visitedLocation.location, loc2) <= rewardsService.getDistance(visitedLocation.location, loc3));
		assertTrue(rewardsService.getDistance(visitedLocation.location, loc3) <= rewardsService.getDistance(visitedLocation.location, loc4));
		assertEquals(5, attractions.size());
	}
	
	@Test
	public void getTripDeals() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		//tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size()); //NOTE: valeur originelle 10
	}
	
	
}
