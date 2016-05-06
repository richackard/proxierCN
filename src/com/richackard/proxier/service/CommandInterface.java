package com.richackard.proxier.service;

import com.richackard.proxier.data.DatabaseManager;

import java.util.Scanner;

/**
 * This class serves as the command line interface which interacts with
 * the server administrator.
 */
public class CommandInterface {

    private Scanner input;

    private DatabaseManager datamanager;

    static final String COMMAND_CHECK = "check";
    static final String COMMAND_REFRESH = "refresh";
    static final String COMMAND_BACKUP_NOW = "backup";
    static final String COMMAND_BACKUP_SETTING = "setbackup";
    static final String COMMAND_CONNECT_DB = "connectdb";
    static final String COMMAND_DISCONNECT_DB = "disconnectdb";
    static final String COMMAND_EXIT_SERVER = "shutdown";
    static final String COMMAND_ADD_SERVER = "add";
    static final String COMMAND_DELETE_SERVER = "delete";
    static final String COMMAND_INJECT_SQL = "inject";


    public CommandInterface(DatabaseManager datamgr){
        input = new Scanner(System.in);
        this.datamanager = datamgr;
        System.out.println("Launching Interface...");
        launchInterface();
    }


    /**
     * This method is used to launch the command line interface.
     */
    private void launchInterface(){
        System.out.println("Interface Launched Successfully...");
        consoleWrite(datamanager.getWelcomeMessage());
        String command = null;
        while(true){
            System.out.print(">> ");
            processCommand(input.nextLine());
        }
    }

    private void processCommand(String command){
        consoleWrite(command);
    }


    private void consoleWrite(String str){
        System.out.println(str);
    }



}
