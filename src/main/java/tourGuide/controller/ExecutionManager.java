package tourGuide.controller;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;

@Component
public class ExecutionManager implements CommandLineRunner{
	Logger logger = LoggerFactory.getLogger(ExecutionManager.class);
	
	private boolean testMode = true;
	public Tracker tracker;
	
	@Autowired
	UserDAOForTesting userDAO;
	
	@Autowired
	TourGuideService tourGuideService;
	
	@Autowired
	UserService userService;
	
	@Override
	public void run(String... args) throws Exception {
		Locale.setDefault(Locale.ENGLISH); 
		if(testMode) {
			logger.info("TestMode enabled");
			
			logger.debug("Initializing users");
			userDAO.initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		runTracker(this.tourGuideService,userDAO);
		
	}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	public void runTracker(TourGuideService tourGuideService,IUserDAO userDAO) {
		logger.info("runTracker");
		System.out.println("runTracker");
		tracker = new Tracker(tourGuideService, userDAO);
		addShutDownHook();
	}

}
