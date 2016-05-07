package com.richackard.proxier.task;


import com.richackard.proxier.data.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a task which is to check whether there are meaningless servers in the database.
 */
public class CheckingDataTask implements Runnable {

    static final String lock = "";

    private DatabaseManager databaseManager;

    public CheckingDataTask(DatabaseManager dm){
        databaseManager = dm;
    }

    @Override
    public void run(){
        synchronized (lock){
            System.out.printf("\n%s => Scheduled Checking Task Starts Running...\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            databaseManager.checkServers();;
            System.out.printf("\n%s => Checking Task Finished Running...\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            System.out.printf("\n%s => Generating JSON Data...\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            generateData();
            System.out.printf("\n%s => Finished Generating JSON Data...\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
    }


    public synchronized void generateData(){
        databaseManager.generateData();
    }
}
