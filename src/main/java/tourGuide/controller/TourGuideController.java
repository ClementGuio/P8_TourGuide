package tourGuide.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	Logger logger = LoggerFactory.getLogger(TourGuideController.class);
	
	@Autowired
	TourGuideService tourGuideService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RewardsService rewardsService;
	
    @RequestMapping("/")
    public String index() {
    	List<User> users = userService.getAllUsers();
    	System.out.println(users.get(0));
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) throws Exception{
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
    //  OK - Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) throws Exception{
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	RewardCentral rewardCentral = new RewardCentral(); 
    	User user = userService.getUser(userName);

    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
    	logger.debug("userLocation : "+visitedLocation.location.latitude+" , "+visitedLocation.location.longitude);
    	List<Attraction> nearAttractions = tourGuideService.getNearByAttractions(visitedLocation);
    	Map<String,Double> userCoord = new LinkedHashMap<String,Double>();
    	userCoord.put("latitude", visitedLocation.location.latitude);
    	userCoord.put("longitude", visitedLocation.location.longitude);
		for (Attraction attraction : nearAttractions) {
    		Map<String,Object> fields = new LinkedHashMap<String,Object>();
    		// Name of Tourist attraction,
    		fields.put("Attraction name", attraction.attractionName);
    		// Tourist attractions lat/long
    		Map<String,Double> attCoord = new LinkedHashMap<String,Double>();
    		attCoord.put("latitude", attraction.latitude);
    		attCoord.put("longitude", attraction.longitude);
    		fields.put("Attraction coordinates",attCoord);
    		// The user's location lat/long
    		fields.put("User's coordinates", userCoord);
    		// The distance in miles between the user's location and each of the attractions
    		fields.put("Distance",rewardsService.getDistance(visitedLocation.location,new Location(attraction.latitude,attraction.longitude)));
    		// The reward points for visiting each Attraction
    		fields.put("Reward points", rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId()));
    		
    		result.add(fields);
    	}
    	
    	
    	return JsonStream.serialize(result);
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	User user = userService.getUser(userName);
    	return JsonStream.serialize(user.getUserRewards());
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
    	// OK - Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	List<User> allUsers = userService.getAllUsers();
    	Map<String,Object> currentLocations = new HashMap<String,Object>();
    	for (User user : allUsers) {
    		Map<String,Double> userCoord = new LinkedHashMap<String,Double>();
    		userCoord.put("latitude", user.getLastVisitedLocation().location.latitude);
    		userCoord.put("longitude", user.getLastVisitedLocation().location.longitude);
    		currentLocations.put(user.getUserId().toString(), userCoord);
    	}
    	return JsonStream.serialize(currentLocations);
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(userService.getUser(userName));
    	return JsonStream.serialize(providers);
    }
}