package com.richackard.proxier.data;


import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public ResultSet queryDB(String sql){
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
    public void updateDB(String sql){
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
    public void addServer(String ip, int port) throws Exception{
        if(connected){
            String sqlQuery = String.format("SELECT %s, %s FROM %s WHERE %s=\"%s\" AND %s=\"%d\";",
                    SERVER_IP_COL,
                    PORT_COL,
                    TABLE_NAME,
                    SERVER_IP_COL,
                    ip,
                    PORT_COL,
                    port);
            System.out.println(sqlQuery);
            ResultSet result = queryDB(sqlQuery);
            // This server is not in the database.
            if(!result.next()){
                // First check speed, if speed is a value that is greater than 0, it is a valid server.
                int time = checkSpeed(ip);
                // If time is valid, then add the record.
                if(time >= 0){
                    String insertSql = String
                            .format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (\"%s\", \"%d\", \"%s\", \"%d\", \"%s\", \"%d\" );",
                                    TABLE_NAME, SERVER_IP_COL, PORT_COL, DISABLED_COL, LAST_RECORDED_SPEED_COL, CREATEDATE_COL, DISABLE_COUNT_COL,
                                    ip, port, "false", time, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) ,0);
                    updateDB(insertSql);
                    System.out.printf("Server: %s with port %d added to the server\n", ip, port);
                }
                else{
                    System.out.printf("Host %s is not reachable\n", ip);
                }
            }
            else{
                System.out.println("Host existed in the DB.");
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


    public void printAllServers(){

    }
}
