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

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(1);//5
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;
	
	//TODO : Ajouter log4j
	
	public Tracker(TourGuideService tourGuideService) {
		System.out.println("Tracker constructor");
		this.tourGuideService = tourGuideService;
		
		executorService.submit(this);
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		System.out.println("stopTracking");
		stop = true;
		executorService.shutdownNow();
	}
	
	@Override
	public void run(){
		System.out.println("Locale : "+Locale.getDefault());
		Locale.setDefault(Locale.ENGLISH); //NOTE : Solution à l'erreur généré par GpsUtil.getUserLocation()
		System.out.println("Locale : "+Locale.getDefault());
		
		int nbProcs = Runtime.getRuntime().availableProcessors();
		System.out.println("nbProcs : "+nbProcs);
		
		//ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);

		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> {
				try {
					executor.submit(() -> tourGuideService.trackUserLocation(u));
					//tourGuideService.trackUserLocation(u);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			executor.shutdown();
			
			try {
				executor.awaitTermination(20, TimeUnit.MINUTES);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
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
