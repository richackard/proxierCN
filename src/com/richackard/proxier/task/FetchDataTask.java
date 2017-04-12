package com.richackard.proxier.task;


import com.richackard.proxier.data.DatabaseManager;
import com.richackard.proxier.data.ServerEntity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a task which is to fetch new server from websites.
 */
public class FetchDataTask implements Runnable {
    static final String lock = "";


    static final String CNPROXY = "http://cn-proxy.com/";

    private DatabaseManager databasemgr;

    public FetchDataTask(DatabaseManager dm){
        this.databasemgr = dm;
    }


    @Override
    public void run(){
        synchronized (lock) {
            System.out.printf("\n%s => Scheduled Fetching Task Starts Running...\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            fetchFromProxyCN();
            System.out.printf("\n%s => Fetching Task Finished Running...",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
    }


    public void fetchFromProxyCN(){
        try {
            URL cnProxy = new URL(CNPROXY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(cnProxy.openStream()));
            String inputLine = null;
            StringBuffer sb = new StringBuffer();
            while((inputLine = reader.readLine()) != null) {
                sb.append(inputLine.trim());
            }
            List<ServerEntity> resultList = analyzeServers(sb);
            for(ServerEntity entity: resultList){
                databasemgr.addServer(entity.getIp(), entity.getPort());
            }

        }
        catch(MalformedURLException mue){
            System.out.println("CNProxy Domain Not Working");
        }
        catch(IOException ioe){
            System.out.print(ioe);
            System.out.println("IOException Occurred While Connecting to the cn proxy.");
        }
    }

    public synchronized List<ServerEntity> analyzeServers(StringBuffer htmlText){
        List<ServerEntity> list = new ArrayList<>();
        Pattern ipAndPort = Pattern.compile("<td>(\\d+\\.\\d+\\.\\d+\\.\\d+)<\\/td>\\s*?<td>(\\d+)<\\/td>.*?width:\\s*(\\d+)%");
        Matcher matcher = ipAndPort.matcher(htmlText.toString().trim());
        while(matcher.find()) {
            String ipAddress = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            int speed = Integer.parseInt(matcher.group(3));
            if(speed > 75)
                list.add(new ServerEntity(ipAddress, port));
            else
                System.out.printf("Host : %s Rejected due to low speed.\n", ipAddress);
        }
        return list;
    }
}
