package ctrlmnt;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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
		FindIterable<Document> mss = msCol.find();
		FindIterable<Document> ms = msCol.find(Filters.eq("name", this.svc.getName()));
		
		MongoCursor<Document> it = mss.iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		Document msDoc = null;
		if ((msDoc = ms.first()) != null) {
			this.svc.setHw(msDoc.getDouble("hw").floatValue());
		} else {
			System.out.println("mircoservice"+this.svc.getName()+"not found"); 
		}
	}
}
