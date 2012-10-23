package fr.javageek.jee.filter;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.junit.Test;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * <b>Description : Classe de test sur laquelle vous pouvez prendre exemple pour implementer la methode getStatus()</b>
 * 
 * @author Christian NGUYEN VAN THAN alias @JavaGeekFr<br>
 */
public class MyCustomActivationFilter extends ActivationFilter {
	
	private static final Logger LOG = Logger.getLogger(MyCustomActivationFilter.class);
	
	private static final String APPLICATION_NAME = "Demo";

	@Override
	protected Status getStatus() {
		if( retrieveValueFromMongoDb(APPLICATION_NAME) == 1) {
			return Status.ALIVE; 
		} else {
			return Status.DOWN;
		}
	}
	
	public int retrieveValueFromMongoDb(String applicationName) {
		Mongo m = null;
		int result = 0;
		try {
			m = new Mongo("localhost");
			DB db = m.getDB("activationDb");
			// authentification
			boolean auth = db.authenticate("javageek", "beer lover".toCharArray());
			// si authentification ok
			if (auth) {
				// je me place sur la bonne collection ^_^
				DBCollection coll = db.getCollection( "applications" );
				
				// Contenu de ma collection: obtenu avec un db.applications.find()
				/*
			 		{ "_id" : ObjectId("508713bc54b1191a36bfadbc"), "name" : "Demo", "active" : 1 }
			 		{ "_id" : ObjectId("508713ca54b1191a36bfadbd"), "name" : "Presentation", "active" : 0 }
			  
				 */
				
				// on construit la requete
				BasicDBObject searchQuery = new BasicDBObject();
				searchQuery.put("name", applicationName);
				// on lance la requete
				DBObject row = coll.findOne(searchQuery);
				Object res = row.get("active");
				if (res instanceof Number) {
					result = ((Number)res).intValue();
				}
			} else {
				fail( "Login failed !!" );
			}
		} catch (UnknownHostException e) {
			LOG.error("ah ah failed !!", e);
		} finally {
			if (m!=null) {
				m.close();
			}
		}
		return result;
	}
	
	@Test
	public void testRetriveValueFromMongo() {
		int result = retrieveValueFromMongoDb("Demo");
		assertTrue(result == 1);
		result = retrieveValueFromMongoDb("Presentation");
		assertTrue(result == 0);
	}

}
