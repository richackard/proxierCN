package com.richackard.proxier.data;


import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class serves as the DB Manager Module for this application.
 */
public class DatabaseManager {

    private Connection dbConnection;

    static final String PROPERTIES_DATA = "proxierList.xml";
    static final String XML_SCHEMA = "xmlSchema.xsd";
    static final String DATA_PATH = "./data/";
    static final String TABLE_NAME = "ServerList";
    static final String SERVER_IP_COL = "ServerIp";
    static final String PORT_COL = "port";
    static final String DISABLED_COL = "Disabled";
    static final String LAST_RECORDED_SPEED_COL = "LastRecordedSpeed";
    static final String CREATEDATE_COL = "CreateDate";
    static final String DISABLE_COUNT_COL = "DisableCount";

    static final String DB_FILE = "DB_FILE";
    static final String SERVER_NAME = "SERVER_NAME";
    static final String VERSION_NUMBER = "VERSION";

    static final String MUSICEASE_SERVER = "http://music.163.com/";
    static boolean connected = false;

    public DatabaseManager() throws ClassNotFoundException{
        // Load the database driver.
        Class.forName("org.sqlite.JDBC");

        // Load the properties for the program.
        try{
            // Loading the properties.
            loadProperties();
            System.out.println("Program Properties Loaded Successfully...");
            connect();
        }
        catch(Exception e){
            System.out.println("DB Module Initialization Error...");
            System.exit(127);
        }
    }

    /**
     * Method used to load in the program properties.
     * @throws InvalidXMLFileFormatException when XML file does not meet the schema.
     */
    private void loadProperties() throws InvalidXMLFileFormatException{
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        props.addProperty(PropertiesManager.DATA_PATH_PROPERTY, DATA_PATH);
        props.loadProperties(PROPERTIES_DATA, XML_SCHEMA);

    }

    private void connectToDB() throws SQLException{
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        dbConnection = DriverManager.getConnection(String.format("jdbc:sqlite:%s",props.getProperty(DB_FILE)));
        Statement statement = dbConnection.createStatement();
        // If table has not been created, create a new table.
        String createTableCommand = "CREATE TABLE IF NOT EXISTS " +
                "ServerList(ServerIp TEXT, Port INTEGER, Disabled NUMERIC, LastRecordedSpeed INTEGER, CreateDate TEXT, DisableCount INTEGER)";
        statement.execute(createTableCommand);
    }

    /**
     * This method is used to query a SQL statment and return the query result as a set.
     * @param sql query sql statement to be executes.
     * @return the result set if query successfully executed, null otherise.
     */
    public synchronized ResultSet queryDB(String sql){
        if(connected){
            try{
                Statement stmt = dbConnection.createStatement();
                ResultSet result = stmt.executeQuery(sql);
                return result;
            }
            catch(SQLException sqle){
                System.out.println("Error Occurred During DB Query.");
            }
        }
        return null;
    }

    /**
     * This method is used to update DB using a SQL statement.
     * @param sql the sql statement to be executed.
     */
    public synchronized void updateDB(String sql){
        if(connected){
            try {
                Statement stmt = dbConnection.createStatement();
                stmt.executeUpdate(sql);
            }
            catch(SQLException sqle){
                System.out.println("Error Occurred During DB Update.");
            }
        }
    }




    /**
     * This method is acted as a connecting command which will trigger an event to connect to db.
     */
    public void connect(){
        if(!connected){
            try {
                // Connecting to the database;
                connectToDB();
                System.out.println("Data Module Connection Established Successfully...");
                connected = true;
            }
            catch(SQLException sqle){
                System.out.println("Connection Failed!");
            }
        }

    }

    /**
     * This method is acted as a disconnecting command which will trigger an event to disconnect from db.
     */
    public void disconnect(){
        if(connected){
            try{
                //Trying to close the DB Connection.
                dbConnection.close();
                System.out.println("Data Module Connection Ended Successfully...");
            }
            catch(SQLException sqle){
                System.out.println("Error Occurred During DB Disconnecting Process!");
            }
        }
    }


    /**
     * This method is going to handle adding new ip address to the database.
     * If the ip and its port have already existed, then ignore it.
     * @param ip the new server's ip
     * @param port the new server's port
     */
    public void addServer(String ip, int port){
        if(connected){
            try {
                String sqlQuery = String.format("SELECT %s, %s FROM %s WHERE %s=\"%s\" AND %s=\"%d\";",
                        SERVER_IP_COL,
                        PORT_COL,
                        TABLE_NAME,
                        SERVER_IP_COL,
                        ip,
                        PORT_COL,
                        port);
                //System.out.println(sqlQuery);
                ResultSet result = queryDB(sqlQuery);
                // This server is not in the database.
                if (!result.next()) {
                    // First check speed, if speed is a value that is greater than 0, it is a valid server.
                    int time = checkSpeed(ip);
                    // If time is valid, then add the record.
                    if (time >= 0) {
                        if(checkProxy(ip, port)) {
                            String insertSql = String
                                    .format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (\"%s\", \"%d\", \"%s\", \"%d\", \"%s\", \"%d\" );",
                                            TABLE_NAME, SERVER_IP_COL, PORT_COL, DISABLED_COL, LAST_RECORDED_SPEED_COL, CREATEDATE_COL, DISABLE_COUNT_COL,
                                            ip, port, "false", time, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 0);
                            updateDB(insertSql);
                            System.out.printf("Server: %s with port %d added to the server\n", ip, port);
                        }
                        else{
                            System.out.printf("Host %s is not usable.\n", ip);
                        }
                    } else {
                        System.out.printf("Host %s is not reachable.\n", ip);
                    }
                } else {
                    System.out.println("Host existed in the DB.");
                }
            }
            catch(SQLException sqle){
                System.out.println("Error Occurred During DB Query.");
            }
        }
    }


    /**
     * This method is used to go through all servers and check for availability.
     */
    public void checkServers(){
        if(connected){
            String sqlQuery = String.format("SELECT * FROM %s", TABLE_NAME);
            ResultSet set = queryDB(sqlQuery);
            try {
                while(set.next()){
                    // Get the ip address
                    String ip = set.getString(SERVER_IP_COL);
                    // Get the port number of the proxy server.
                    int port = set.getInt(PORT_COL);
                    // If the proxy server's connectivity is good.
                    if(checkProxy(ip, port)){
                        // Ping server and get the speed.
                        int speed = checkSpeed(ip);
                        String sqlUpdate = null;
                        if(speed > 0) {
                            sqlUpdate = String.format("UPDATE %s SET %s=\"false\", %s=\"%d\", %s=\"%d\" WHERE %s=\"%s\" AND %s=\"%d\";",
                                    TABLE_NAME,
                                    DISABLED_COL,
                                    LAST_RECORDED_SPEED_COL,
                                    speed,
                                    DISABLE_COUNT_COL,
                                    0,
                                    SERVER_IP_COL,
                                    ip,
                                    PORT_COL,
                                    port);
                        }
                        else{
                            sqlUpdate = String.format("UPDATE %s SET %s=\"false\", %s=\"%d\" WHERE %s=\"%s\" AND %s=\"%d\";",
                                    TABLE_NAME,
                                    DISABLED_COL,
                                    DISABLE_COUNT_COL,
                                    0,
                                    SERVER_IP_COL,
                                    ip,
                                    PORT_COL,
                                    port);
                        }
                        updateDB(sqlUpdate);
                    }
                    else{
                        // If the server has already failed the test for 5 times, then delete it from the db.
                        int disabledCount = set.getInt(DISABLE_COUNT_COL);
                        String sqlUpdate = null;
                        if(disabledCount == 5){
                            System.out.printf("Server %s with port %d is no longer available and is going to be deleted from DB...\n",
                                    ip,
                                    port);
                            sqlUpdate = String.format("DELETE FROM %s WHERE %s=\"%s\" AND %s=\"%d\";",
                                    TABLE_NAME,
                                    SERVER_IP_COL,
                                    ip,
                                    PORT_COL,
                                    port);
                        }
                        else{
                            System.out.printf("Server %s with port %d is not available. It has already failed the check %d times...\n",
                                    ip,
                                    port,
                                    disabledCount);
                            sqlUpdate = String.format("UPDATE %s SET %s=\"true\", %s=\"%d\" WHERE %s=\"%s\" AND %s=\"%d\";",
                                    TABLE_NAME,
                                    DISABLED_COL,
                                    DISABLE_COUNT_COL,
                                    ++disabledCount,
                                    SERVER_IP_COL,
                                    ip,
                                    PORT_COL,
                                    port);
                        }
                        updateDB(sqlUpdate);
                    }
                }
            }
            catch(SQLException sqle){
                System.out.println("Error During DB Query.");
            }
        }
    }


    /**
     * This method is used to check the time needed to reach to a specific host.
     * It is going to take 5 samples and calculate the average
     * @param host target host.
     * @return time needed to reach the target host.
     */
    public int checkSpeed(String host){
        try {
            int sum = 0;
            for(int i = 0; i < 5; i++) {
                Long start = System.currentTimeMillis();
                if (!InetAddress.getByName(host).isReachable(2000)) return -1;
                sum += (int) (System.currentTimeMillis() - start);
            }
            return sum / 5;
        }
        catch(UnknownHostException uhe){
            System.out.printf("Can't reach %s.\n", host);
            return -1;
        }
        catch(IOException ioe){
            System.out.println("Speed Checking Failed.");
            return -1;
        }
    }


    public static String getWelcomeMessage(){
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        StringBuffer sb = new StringBuffer();
        sb.append(new Date() + "\n");
        sb.append(props.getProperty(SERVER_NAME) + " Version: " + props.getProperty(VERSION_NUMBER));
        return sb.toString();
    }


    public static boolean checkProxy(String ip, int port){
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            HttpURLConnection conn = (HttpURLConnection) new URL(MUSICEASE_SERVER).openConnection(proxy);
            conn.setConnectTimeout(2000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.printf("IP: %s, Port: %d => Response Code: %d\n", ip, port, responseCode);
            if(responseCode == 200)
                return true;
            return false;
        }
        catch(MalformedURLException me){
            System.out.println("Invalid Domain Detected...");
        }
        catch(IOException ioe){
            System.out.println("Error Occurred During Proxy Checking...");
        }
        return false;
    }

}
