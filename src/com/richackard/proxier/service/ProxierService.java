package com.richackard.proxier.service;


import com.richackard.proxier.data.DatabaseManager;
import com.richackard.proxier.task.CheckingDataTask;
import com.richackard.proxier.task.FetchDataTask;
import properties_manager.PropertiesManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as the service module for the application,
 * which frequently fetches the newest servers and checks the unreliable servers
 * which are already in the database.
 */
public class ProxierService {

    static final String REFRESHING_TIME = "REFRESHING_TIME";
    static final String CHECKING_TIME = "CHECKING_TIME";

    static int initialDelay = 5;
    static int refreshingTime = -1;
    static int checkingTime = -1;

    // Proxier Controller.
    private DatabaseManager databasemgr;

    // Scheduler used to schdedule tasks.
    private ScheduledExecutorService scheduler;

    public ProxierService(DatabaseManager databasemgr){
        this.databasemgr = databasemgr;
        System.out.println("Service Launching...");
        loadData();
        setUpSchedules();
        System.out.println("Service Launched Successfully...");
    }

    private void loadData(){
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        this.refreshingTime = Integer.parseInt(props.getProperty(REFRESHING_TIME));
        this.checkingTime = Integer.parseInt(props.getProperty(CHECKING_TIME));
        System.out.println("-------------------------------------------------");
        System.out.printf("Current Refreshing Frequency: %d seconds\n", refreshingTime);
        System.out.printf("Current Checking Frequency: %d seconds\n", checkingTime);
        System.out.println("-------------------------------------------------");
    }


    private void setUpSchedules(){
        this.scheduler = Executors.newScheduledThreadPool(5);
        // Set up two tasks,
        scheduler.scheduleAtFixedRate(new FetchDataTask(databasemgr), initialDelay, refreshingTime , TimeUnit.SECONDS);
        //scheduler.scheduleAtFixedRate(new CheckingDataTask(), initialDelay, checkingTime, TimeUnit.SECONDS);
    }




}
