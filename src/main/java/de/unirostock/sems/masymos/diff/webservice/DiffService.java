package de.unirostock.sems.masymos.diff.webservice;

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

import de.unirostock.sems.masymos.diff.DiffExecutor;

@Path( "/diff/service" )
public class DiffService {
	
	protected final GraphDatabaseService graphDb;
	protected final DiffExecutor diffExecutor;
	
	public DiffService( @Context GraphDatabaseService graphDb) {
		// init everything
		this.graphDb = graphDb;
		this.diffExecutor = DiffExecutor.instance();
	}
	
	/**
	 * Returns the status of the current DiffExecutor queue 
	 */
	@GET
	@Path( "/status" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getQueueStatus() {
		return Response.status( Status.NOT_IMPLEMENTED ).build();
	}
	
	/**
	 * Triggers the diff generation in the DiffExecutor queue
	 */
	@POST
	@Path( "/trigger" )
	@Consumes( MediaType.APPLICATION_JSON )
	@Produces( MediaType.APPLICATION_JSON )
	public Response triggerDiffGeneration() {
		return Response.status( Status.NOT_IMPLEMENTED ).build();
	}
	
	
	/**
	 * Returns some statistics, regarding diffs in the database
	 */
	@GET
	@Path( "/stats" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getStats() {
		return Response.status( Status.NOT_IMPLEMENTED ).build();
	}
}
