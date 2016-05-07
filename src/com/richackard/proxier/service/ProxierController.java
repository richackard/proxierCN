package com.richackard.proxier.service;

import com.richackard.proxier.data.DatabaseManager;

/**
 * This class is used to act like a controller between the interface and the service level.
 */
public class ProxierController {

    private ProxierService service;

    public ProxierController(ProxierService ps ){
        this.service = ps;
    }


    public void refreshImmediately(){
        service.refreshImmediately();
    }

    public void checkImmediately(){
        service.checkImmediately();;
    }

    public void shutDown(){
        service.shutDown();
    }

    public void generateData(){
        service.generateData();
    }

    public void helpMenu(){

    }

}
