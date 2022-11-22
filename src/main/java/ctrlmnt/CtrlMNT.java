package ctrlmnt;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class CtrlMNT implements Runnable {

	private ControllableService svc = null;
	private MongoClient mongo = null;
	private MongoDatabase msDB = null;

	public CtrlMNT(ControllableService serviceRest) {
		this.svc = serviceRest;
		this.mongo = MongoClients.create("mongodb://localhost:27017");
		this.msDB = this.mongo.getDatabase("sys");
	}

	public void run() { 
		MongoCollection<Document> msCol = this.msDB.getCollection("ms");
		System.out.println(msCol.countDocuments());
		FindIterable<Document> ms = msCol.find(Filters.eq("name", this.svc.getName()));
		Document msDoc = null;
		if ((msDoc = ms.first()) != null) {
			this.svc.setHw(msDoc.getDouble("hw").floatValue());
		} else {
			System.out.println("mircoservice"+this.svc.getName()+"not found"); 
		}
	}
}
