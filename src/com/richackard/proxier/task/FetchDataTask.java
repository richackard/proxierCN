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
                sb.append(inputLine);
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
            System.out.println("IOException Occurred While Connecting to the cn proxy.");
        }
    }

    public synchronized List<ServerEntity> analyzeServers(StringBuffer htmlText){
        List<ServerEntity> list = new ArrayList<>();
        String startTag = "<tbody>";
        String endingTag = "</tbody>";
        String ipStartTag = "<tr>";
        String portStartTag = "<td>";
        // Index of the first <tbody> tag.
        int startIndex = htmlText.indexOf(startTag);
        while(true) {

            // Index of the first </tbody> tag
            int endIndex = htmlText.indexOf(endingTag, startIndex);
            // Cut a block which is contained by a pair of <tbody> tag.
            String validListBlock = htmlText.substring(startIndex, endIndex + 8);
            int trIndex = validListBlock.indexOf(ipStartTag);

            // This loop is going to parse a specific <tbody></tbody> block.
            while (true) {
                int ipStartingIndex = validListBlock.indexOf("d>", trIndex) + 2;
                int ipEndingIndex = validListBlock.indexOf("<", ipStartingIndex);
                String ipAddress = validListBlock.substring(ipStartingIndex, ipEndingIndex);

                int portStartingIndex = validListBlock.indexOf(portStartTag, ipEndingIndex) + 4;
                int portEndingIndex = validListBlock.indexOf("<", portStartingIndex);
                int port = Integer.parseInt(validListBlock.substring(portStartingIndex, portEndingIndex));

                int speedStartingIndex = validListBlock.indexOf("width: ", portEndingIndex) + 7;
                int speedEndingIndex = validListBlock.indexOf("%", speedStartingIndex);

                int speed = Integer.parseInt(validListBlock.substring(speedStartingIndex, speedEndingIndex));
                if(speed > 75)
                    list.add(new ServerEntity(ipAddress, port));
                else
                    System.out.printf("Host : %s Rejected due to low speed.\n", ipAddress);

                if ((trIndex = validListBlock.indexOf(ipStartTag, ipStartingIndex)) == -1)
                    break;
            }

            if((startIndex = htmlText.indexOf(startTag, endIndex)) == -1)
                break;
        }
        return list;
    }
}
