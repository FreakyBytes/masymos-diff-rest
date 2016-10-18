package de.unirostock.sems.masymos.diff.webservice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.graphdb.GraphDatabaseService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unirostock.sems.masymos.diff.DiffExecutor;
import de.unirostock.sems.masymos.diff.ManagerUtil;
import de.unirostock.sems.masymos.diff.thread.PriorityExecutor;

@Path( "/diff/service" )
public class DiffService {
	
	protected final ObjectMapper objectMapper = new ObjectMapper();
	
	
	public DiffService( @Context GraphDatabaseService graphDb) {
	}
	
	/**
	 * Returns the status of the current DiffExecutor queue 
	 */
	@GET
	@Path( "/status" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getQueueStatus(@Context GraphDatabaseService graphDb) {
		ManagerUtil.initManager(graphDb);
		
		Map<String, Object> status = new HashMap<>();
		PriorityExecutor executor = DiffExecutor.instance().getExecutor();
		
		status.put("minPoolSize", executor.getCorePoolSize());
		status.put("maxPoolSize", executor.getMaximumPoolSize());
		status.put("currentPoolSize", executor.getPoolSize());
		status.put("activeTasks", executor.getActiveCount());
		status.put("queuedTasks", executor.getQueue().size());
		status.put("completedTasks", executor.getCompletedTaskCount());
		
		return Response.status( Status.OK ).entity(status).build();
	}
	
	/**
	 * Triggers the diff generation in the DiffExecutor queue
	 * @throws JsonProcessingException 
	 */
	@POST
	@Path( "/trigger" )
	@Consumes( MediaType.APPLICATION_JSON )
	@Produces( MediaType.APPLICATION_JSON )
	public Response triggerDiffGeneration(@Context GraphDatabaseService graphDb) throws JsonProcessingException {
		ManagerUtil.initManager(graphDb);
		
		Map<String, Object> status = new HashMap<>();
		DiffExecutor executer = DiffExecutor.instance();
		
		try {
			executer.generateDiffs(0, false);
			status.put("status", "ok");
			return Response.status( Status.OK ).entity(status).build();
		} catch (InterruptedException | ExecutionException e) {
			status.put("status", "error");
			status.put("message", e.getMessage());
			status.put("stacktrace", e.getStackTrace().toString());
			return Response.status( Status.OK ).entity( objectMapper.writeValueAsString(status) ).build();
		}
		
	}
	
	
	/**
	 * Returns some statistics, regarding diffs in the database
	 */
	@GET
	@Path( "/stats" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getStats(@Context GraphDatabaseService graphDb) {
		ManagerUtil.initManager(graphDb);
		
		return Response.status( Status.NOT_IMPLEMENTED ).build();
	}
}
