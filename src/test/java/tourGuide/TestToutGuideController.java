package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jsoniter.JsonIterator;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.controller.ExecutionManager;
import tourGuide.controller.TourGuideController;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestToutGuideController {
	
	@Autowired
	MockMvc mvc;
	
	@Autowired
	ExecutionManager executionManager;
	
	@Autowired
	UserDAOForTesting userDAO;
	@Autowired
	TourGuideController controller;
	@Autowired
	TourGuideService tourGuideService;
	@Autowired 
	RewardsService rewardsService;
	@BeforeClass
	public static void setup() {
		InternalTestHelper.setInternalUserNumber(2);
	}
	
	@After
	public void close() {
		executionManager.tracker.stopTracking();
	}
	
	
	@Test
	public void testGetLocation() throws Exception {
		/*
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Location loc = new Location(Double.valueOf(1.456), Double.valueOf(-0.333));
		Date date = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), loc, date));		
		*/
		
		User user = userDAO.getAllUsers().get(0);
		Location userLocation = tourGuideService.getUserLocation(user).location;
		
		mvc.perform(MockMvcRequestBuilders
						.get("/getLocation?userName="+user.getUserName())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.status().isOk())
						.andExpect(MockMvcResultMatchers.content().json("{\"longitude\":"+ userLocation.longitude+",\"latitude\":"+userLocation.latitude+"}"));
	}
	
	@Test
	public void testGetNearbyAttractions() throws Exception {
		User user = userDAO.getAllUsers().get(0);
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		RewardCentral rewardCentral = new RewardCentral();
		mvc.perform(MockMvcRequestBuilders
						.get("/getNearbyAttractions?userName="+user.getUserName())
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(MockMvcResultMatchers.status().isOk())
						.andExpect(jsonPath("$[*]['Attraction name']",
								Matchers.containsInAnyOrder(attractions.get(0).attractionName,attractions.get(1).attractionName,
										attractions.get(2).attractionName,attractions.get(3).attractionName,attractions.get(4).attractionName)));
	}
	
	@Test
	public void testGetRewards() throws Exception {
		User user = userDAO.getAllUsers().get(0);
		
		mvc.perform(MockMvcRequestBuilders
					.get("/getRewards?userName="+user.getUserName())
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void testGetAllCurrentLocations() throws Exception {
		List<User> users = userDAO.getAllUsers();
		
		mvc.perform(MockMvcRequestBuilders
					.get("/getAllCurrentLocations")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(jsonPath("$[*]['latitude']",Matchers.containsInAnyOrder(users.get(0).getLastVisitedLocation().location.latitude,users.get(1).getLastVisitedLocation().location.latitude)));
	}
	
	@Test
	public void testGetTripDeals() throws Exception {
		User user = userDAO.getAllUsers().get(0);
		
		mvc.perform(MockMvcRequestBuilders
				.get("/getTripDeals?userName="+user.getUserName())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void testUpdatePreferences() throws Exception {
		User user = userDAO.getAllUsers().get(0);
		
		mvc.perform(MockMvcRequestBuilders
				.get("/updatePreferences?userName="+user.getUserName()+"&currency=USD&attractionProximity=3&lowerPricePoint=10&"
						+ "highPricePoint=1000&tripDuration=2&ticketQuantity=5&numberOfAdults=2&numberOfChildren=3"))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
		assertTrue(user.getUserPreferences().getTicketQuantity()==5);
	}
	
}
