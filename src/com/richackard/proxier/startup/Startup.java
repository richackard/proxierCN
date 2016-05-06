package com.richackard.proxier.startup;

import com.richackard.proxier.data.DatabaseManager;

public class Startup {

    public static void main(String[] args){
        try {
            DatabaseManager db = new DatabaseManager();
            db.addServer("218.26.120.170",8080);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("**Initialization Error**");
        }
    }
}
