package tourGuide.tracker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(); 
	private final TourGuideService tourGuideService;
	private boolean stop = false;
	
	//@Autowired
	private IUserDAO userDAO;
	
	public Tracker(TourGuideService tourGuideService, IUserDAO userDAO) {
		logger.info("Tracker constructor");
		this.tourGuideService = tourGuideService;
		this.userDAO = (UserDAOForTesting) userDAO;
		executorService.submit(this);
	}
	 
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		logger.info("stopTracking");
		stop = true;
		executorService.shutdownNow();
	}
	
	@Override
	public void run(){
		logger.info("Tracker Run()");
		
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);

			List<User> users = userDAO.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> executor.execute(() -> tourGuideService.trackUserLocation(u)));
			executor.shutdown();
			
			try {
				executor.awaitTermination(20, TimeUnit.MINUTES);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}
}
