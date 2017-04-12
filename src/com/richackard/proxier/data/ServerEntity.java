package com.richackard.proxier.data;

/**
 * An instance of this class represents a proxy server with its ip address and its port.
 */
public class ServerEntity {
    private String ip;
    private int port;

    public ServerEntity(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return String.format("%s:%d", ip, port);
    }
}
