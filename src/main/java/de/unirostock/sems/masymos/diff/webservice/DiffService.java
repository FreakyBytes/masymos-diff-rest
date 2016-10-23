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
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unirostock.sems.masymos.diff.DiffExecutor;
import de.unirostock.sems.masymos.diff.ManagerUtil;
import de.unirostock.sems.masymos.diff.thread.PriorityExecutor;
import scala.collection.convert.Wrappers.SeqWrapper;

@Path( "/diff/service" )
public class DiffService {
	
	protected final ObjectMapper objectMapper = new ObjectMapper();
	
	
	public DiffService( @Context GraphDatabaseService graphDb) {
	}
	
	/**
	 * Returns the status of the current DiffExecutor queue 
	 * @throws JsonProcessingException 
	 */
	@GET
	@Path( "/status" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getQueueStatus(@Context GraphDatabaseService graphDb) throws JsonProcessingException {
		ManagerUtil.initManager(graphDb);
		
		Map<String, Object> status = new HashMap<>();
		PriorityExecutor executor = DiffExecutor.instance().getExecutor();
		
		status.put("minPoolSize", executor.getCorePoolSize());
		status.put("maxPoolSize", executor.getMaximumPoolSize());
		status.put("currentPoolSize", executor.getPoolSize());
		status.put("activeTasks", executor.getActiveCount());
		status.put("queuedTasks", executor.getQueue().size());
		status.put("completedTasks", executor.getCompletedTaskCount());
		
		return Response.status( Status.OK ).entity( objectMapper.writeValueAsString(status) ).build();
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
			return Response.status( Status.OK ).entity( objectMapper.writeValueAsString(status) ).build();
		} catch (InterruptedException | ExecutionException e) {
			status.put("status", "error");
			status.put("message", e.getMessage());
			status.put("stacktrace", e.getStackTrace().toString());
			return Response.status( Status.INTERNAL_SERVER_ERROR ).entity( objectMapper.writeValueAsString(status) ).build();
		}
		
	}
	
	
	/**
	 * Returns some statistics, regarding diffs in the database
	 * @throws JsonProcessingException 
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path( "/stats" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getStats(@Context GraphDatabaseService graphDb) throws JsonProcessingException {
		ManagerUtil.initManager(graphDb);
		Map<String, Object> status = new HashMap<>();
		
		
		try ( Transaction tx = graphDb.beginTx() ) {
			
			// statistics about overall count of different DIFF_NODE types
			Result diffTypeStatResult = graphDb.execute("Match (d:DIFF_NODE) Return DISTINCT labels(d) as label, count(d) as count;");
			while( diffTypeStatResult.hasNext() ) {
				Map<String, Object> row = diffTypeStatResult.next();
				for( String label : (SeqWrapper<String>) row.get("label") ) {
					status.put("count_" + label, (status.containsKey("count_" + label) ? (long) status.get("count_" + label) : 0) + (long) row.get("count") ); 
				}
			}
			diffTypeStatResult.close();
			
			// average versions per model
			Result versionStatResult = graphDb.execute("Match (d:DOCUMENT) Return count(distinct d.FILEID) as count_DISTINCT_DOCUMENT, count(d) as count_DOCUMENT, count(d)/count(distinct d.FILEID) as average_VERSION_PER_MODEL;");
			if( versionStatResult.hasNext() ) {
				Map<String, Object> row = versionStatResult.next();
				status.put("count_DISTINCT_DOCUMENT", row.get("count_DISTINCT_DOCUMENT"));
				status.put("count_DOCUMENT", row.get("count_DOCUMENT"));
				status.put("average_VERSION_PER_MODEL", row.get("average_VERSION_PER_MODEL"));
			}
			versionStatResult.close();
			
		}
		
		return Response.status( Status.OK ).entity( objectMapper.writeValueAsString(status) ).build();
	}
}
