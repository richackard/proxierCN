package com.richackard.proxier.data;


import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;

import java.sql.*;

/**
 * This class serves as the DB Manager Module for this application.
 */
public class DatabaseManager {

    private Connection dbConnection;

    static final String PROPERTIES_DATA = "proxierList.xml";
    static final String XML_SCHEMA = "xmlSchema.xsd";
    static final String DATA_PATH = "./data/";

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
                "ServerList(serverIp TEXT, port INTEGER, disabled NUMERIC, lastRecordedSpeed INTEGER)";
        statement.execute(createTableCommand);
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





}
