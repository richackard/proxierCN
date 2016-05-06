package com.richackard.proxier.startup;

import com.richackard.proxier.data.DatabaseManager;
import com.richackard.proxier.service.CommandInterface;

public class Startup {

    public static void main(String[] args){
        try {
            // Initialize DB Module.
            DatabaseManager db = new DatabaseManager();

            // Initialize Command Line Interface.
            CommandInterface cli = new CommandInterface(db);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("**Initialization Error**");
        }
    }
}
