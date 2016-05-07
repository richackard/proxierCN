package com.richackard.proxier.service;


import com.richackard.proxier.data.DatabaseManager;
import com.richackard.proxier.task.FetchDataTask;

import java.util.Scanner;

/**
 * This class serves as the command line interface which interacts with
 * the server administrator.
 */
public class CommandInterface {

    private Scanner input;

    private ProxierController controller;

    static final String COMMAND_CHECK = "check";
    static final String COMMAND_REFRESH = "refresh";
    static final String COMMAND_BACKUP_NOW = "backup";
    static final String COMMAND_BACKUP_SETTING = "setbackup";
    static final String COMMAND_CONNECT_DB = "connectdb";
    static final String COMMAND_DISCONNECT_DB = "disconnectdb";
    static final String COMMAND_EXIT_SERVER = "shutdown";
    static final String COMMAND_ADD_SERVER = "add";
    static final String COMMAND_GENERATE_DATA = "generate";
    static final String COMMAND_DELETE_SERVER = "delete";
    static final String COMMAND_INJECT_SQL = "inject";


    public CommandInterface(ProxierController dataControl){
        input = new Scanner(System.in);
        System.out.println("Launching Interface...");
        this.controller = dataControl;
        launchInterface();
    }


    /**
     * This method is used to launch the command line interface.
     */
    private void launchInterface(){
        System.out.println("Interface Launched Successfully...");
        consoleWrite(DatabaseManager.getWelcomeMessage());
        while(true){
            System.out.print(">> ");
            processCommand(input.nextLine());
        }
    }

    /**
     * This method is used to interact with controller module with specific commands.
     * @param command the command to be executed.
     */
    private void processCommand(String command){
        command = command.trim();
        switch(command){
            case COMMAND_REFRESH:{
                controller.refreshImmediately();
                break;
            }
            case COMMAND_CHECK:{
                controller.checkImmediately();
                break;
            }
            case COMMAND_EXIT_SERVER:{
                controller.shutDown();
                break;
            }
            case COMMAND_GENERATE_DATA:{
                controller.generateData();
            }
        }
    }


    private void consoleWrite(String str){
        System.out.println(str);
    }



}
