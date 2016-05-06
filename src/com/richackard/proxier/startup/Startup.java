package com.richackard.proxier.startup;


import com.richackard.proxier.data.DatabaseManager;

public class Startup {

    public static void main(String[] args){
        try {
            DatabaseManager db = new DatabaseManager();
        }
        catch(Exception e){
            System.out.println("**Initialization Error**");
        }
    }
}
