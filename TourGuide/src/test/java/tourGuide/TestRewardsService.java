package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.controller.ExecutionManager;
import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {
	
	@Autowired
	ExecutionManager executionManager;
	
	UserDAOForTesting userDAO = new UserDAOForTesting();
	
	@BeforeClass
	public static void setup() {
		Locale.setDefault(Locale.ENGLISH);
	}
	
	//@Ignore
	@Test
	public void userGetRewards() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user); //*
		List<UserReward> userRewards = user.getUserRewards();
		assertTrue(userRewards.size() == 1);
	}
	//@Ignore
	@Test
	public void isWithinAttractionProximity() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	//@Ignore // Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		
		InternalTestHelper.setInternalUserNumber(1);
		userDAO.initializeInternalUsers();
		StopWatch watch = new StopWatch();
		watch.start();
		rewardsService.calculateRewards(userDAO.getAllUsers().get(0));
		List<UserReward> userRewards = userDAO.getAllUsers().get(0).getUserRewards();
		watch.stop();
		System.out.println("TEST nearAllAttractions : Time elapsed : "+TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " seconds.");

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
