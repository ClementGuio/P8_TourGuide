package tourGuide.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.dao.IUserDAO;
import tourGuide.dao.UserDAOForTesting;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Configuration
public class TourGuideModule {
	
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	/*
	@Bean
	public UserDAOForTesting getUserDAO() {
		return new UserDAOForTesting();
	}
	
	@Bean
	public TourGuideService getTourGuideService() {
		return new TourGuideService(getGpsUtil(),getRewardsService());
	}
	
	@Bean
	public UserService getUserService() {
		return new UserService(getUserDAO());
	}
	*/
}
