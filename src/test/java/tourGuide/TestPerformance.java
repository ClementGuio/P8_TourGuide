package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestPerformance {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	
	UserDAOForTesting userDAO = new UserDAOForTesting();
	
	private final int NBUSERS = 100;	
	@BeforeClass
	public static void setup() {
		Locale.setDefault(Locale.ENGLISH);
	}

	@Ignore
	@Test
	public void highVolumeTrackLocation_WithoutOptimization() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NBUSERS);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		userDAO.initializeInternalUsers();
		List<User> allUsers = new ArrayList<>();
		allUsers = userDAO.getAllUsers();
		
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for(User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}
		stopWatch.stop();

		System.out.println("highVolumeTrackLocation_WithoutOptimization: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
	//@Ignore
	@Test
	public void highVolumeTrackLocation() throws Exception{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NBUSERS);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		userDAO.initializeInternalUsers();

		List<User> allUsers = new ArrayList<>();
		allUsers = userDAO.getAllUsers();
		System.out.println("nb Users : "+allUsers.size());
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);
			
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		allUsers.forEach(u -> executor.execute(() -> tourGuideService.trackUserLocation(u)));
		System.out.println("Queue size : "+executor.getQueue().size());
		executor.shutdown();
		executor.awaitTermination(15, TimeUnit.MINUTES);
		stopWatch.stop();
		
		System.out.println("Queue size : "+executor.getQueue().size());
		System.out.println("TEST highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	

	@Ignore
	@Test
	public void highVolumeGetRewards_withoutOptimization() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(NBUSERS); // 1213 secondes (~20min)
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		userDAO.initializeInternalUsers();
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = userDAO.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
		
	    allUsers.forEach(u -> rewardsService.calculateRewards(u));
	    
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();

		System.out.println("highVolumeGetRewards_withoutOptimization: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	

	
	//@Ignore
	@Test
	public void highVolumeGetRewards() throws Exception{  // 100000 users : ~ 800 secondes
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		
		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(NBUSERS);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		userDAO.initializeInternalUsers();
		
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = userDAO.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
	    		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);
	    allUsers.forEach(u -> { 
	    	try {
	    		executor.execute(() -> rewardsService.calculateRewards(u));
	    	}catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    });
	    
	    executor.shutdown();
	    executor.awaitTermination(25, TimeUnit.MINUTES);
		
	    	
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		
		System.out.println("TEST highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}
