package com.richackard.proxier.startup;

import com.richackard.proxier.data.DatabaseManager;
import com.richackard.proxier.service.ProxierController;
import com.richackard.proxier.service.CommandInterface;
import com.richackard.proxier.service.ProxierService;

public class Startup {

    public static void main(String[] args){
        try {
            // Initialize DB Module.
            DatabaseManager db = new DatabaseManager();
            // Initialize Service Module.
            ProxierService service = new ProxierService(db);
            // Initialize Controller Module.
            ProxierController controller = new ProxierController(service);
            // Initialize Command Line Interface.
            CommandInterface cli = new CommandInterface(controller);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("**Initialization Error**");
        }
    }
}
