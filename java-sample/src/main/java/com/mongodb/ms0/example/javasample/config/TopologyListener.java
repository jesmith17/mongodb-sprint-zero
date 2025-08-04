package com.mongodb.ms0.example.javasample.config;

import com.mongodb.Tag;
import com.mongodb.connection.ServerDescription;
import com.mongodb.event.ClusterDescriptionChangedEvent;
import com.mongodb.event.ClusterListener;

import java.util.Iterator;
import java.util.List;

public class TopologyListener implements ClusterListener {


    @Override
    public void clusterDescriptionChanged(ClusterDescriptionChangedEvent event) {
        List<ServerDescription> servers = event.getNewDescription().getServerDescriptions();
        for (ServerDescription server: servers ){
            if (server.isPrimary()) {
                server.getTagSet().forEach(tag -> {
                    if (tag.getName().equals("region")) {
                        System.out.println("Primary region is " + tag.getValue());
                    }
                });
            }
        }
    }
}
