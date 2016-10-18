package de.unirostock.sems.masymos.diff;

import org.neo4j.graphdb.GraphDatabaseService;

import de.unirostock.sems.masymos.configuration.Config;
import de.unirostock.sems.masymos.database.Manager;

public class ManagerUtil {
	
	public static void initManager(GraphDatabaseService graphDbSevice){
		if (!Config.instance().isWebSeverInstance()){
    		Config.instance().setDb(graphDbSevice);
    		Manager.instance();
    	}   
	}
	
}
