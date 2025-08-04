package com.mongodb.ms0.example.javasample.service;

import com.mongodb.connection.ServerDescription;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoDBHealthCheckService {


    private List<ServerDescription> servers;
    private ServerDescription primaryServer;
    private boolean healthy;


    public List<ServerDescription> getServers() {
        return servers;
    }

    public void setServers(List<ServerDescription> servers) {
        this.servers = servers;
        for (ServerDescription server : servers) {
            if (server.isPrimary()){
                this.primaryServer = server;
            }
        }



    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }


}
