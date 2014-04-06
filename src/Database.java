import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.opencv.core.Mat;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Database {
	private static MongoClient client = null;
	private DB db = null;
	private DBCollection faceCollection = null;
	private static final String faceCollectionName = "images";

	public Database(String name, int port, String username, String password) {
		synchronized (this) {
			String uri = "mongodb://" + username + ":" + password
					+ "@oceanic.mongohq.com:" + port + "/facebase";
			MongoClientURI mongoClientURI = new MongoClientURI(uri);
			try {
				if (client == null) {
					client = new MongoClient(mongoClientURI);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("Database not available");
			}

			db = client.getDB(name);
			db.authenticate(username, password.toCharArray());
			faceCollection = db.getCollection(faceCollectionName);

			// MongoCredential credentials = MongoCredential
			// .createMongoCRCredential(username, name,
			// password.toCharArray());
		}
	}

	public synchronized void writeFace(String id, ArrayList<Mat> images,
			FaceDetector.CameraTypes cameraType) {
		byte[][] imagedata = new byte[images.size()][];
		int[] widths = new int[images.size()];
		int[] heights = new int[images.size()];
		int[] types = new int[images.size()];

		for (int i = 0; i < images.size(); ++i) {
			int count = (int) (images.get(i).total() * images.get(i).channels());
			byte[] buff = new byte[count];
			images.get(i).get(0, 0, buff);
			imagedata[i] = buff;
		}

		for (int i = 0; i < images.size(); ++i) {
			widths[i] = images.get(i).width();
			heights[i] = images.get(i).height();
			types[i] = images.get(i).type();
		}

		BasicDBObject document = new BasicDBObject();
		document.put("id", id);
		document.put("widths", widths);
		document.put("heights", heights);
		document.put("types", types);
		document.put("image", imagedata);
		document.put("enter", cameraType.ordinal());

		// Timestamp
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		document.put("timestamp", date);

		System.out.println("Wrote face with " + images.size() + " pictures at "
				+ date + " using camera " + cameraType.ordinal());
		faceCollection.insert(document);
	}

	private Integer[] objectToInteger(Object[] arr) {
		Integer[] cast = new Integer[arr.length];
		for (int i = 0; i < arr.length; ++i) {
			cast[i] = (Integer) arr[i];
		}

		return cast;
	}

	public synchronized UniqueFace readFace(int id) {
		BasicDBObject query = new BasicDBObject("id", Integer.toString(id));
		UniqueFace face = new UniqueFace();

		if (faceCollection != null) {
			DBCursor cursor = faceCollection.find(query);
			while (cursor.hasNext()) {
				DBObject result = cursor.next();

				byte[][] bytes = objectToByte2D(((BasicDBList) result
						.get("image")).toArray());
				Integer[] widths = objectToInteger(((BasicDBList) result
						.get("widths")).toArray());
				Integer[] heights = objectToInteger(((BasicDBList) result
						.get("heights")).toArray());
				Integer[] types = objectToInteger(((BasicDBList) result
						.get("types")).toArray());
				String time = (result.get("timestamp")).toString();

				face.setID(Integer.parseInt((String) result.get("id")));
				for (int i = 0; i < widths.length; ++i) {
					Mat mat = new Mat(widths[i], heights[i], types[i]);
					mat.put(0, 0, bytes[i]);
					face.addImage(mat);
				}

				System.out.println("Read face with "
						+ Integer.toString(face.getImages().size())
						+ " pictures at " + time);
			}
		} else {
			System.out.println("null collection");
		}

		return face;
	}

	private byte[][] objectToByte2D(Object[] arr) {
		byte[][] cast2D = new byte[arr.length][];
		for (int i = 0; i < arr.length; ++i) {
			byte[] cast = (byte[]) (arr[i]);
			cast2D[i] = cast;
		}

		// for (int i = 0; i < cast2D.length; ++i) {
		// for (int j = 0; j < cast2D[i].length; ++j) {
		// System.out.println(cast2D[i][j]);
		// }
		// }

		return cast2D;
	}

	public synchronized void print() {
		if (faceCollection != null) {
			DBCursor cursor = faceCollection.find();
			printCursor(cursor);
		} else {
			System.out.println("null collection");
		}
	}

	private synchronized void printCursor(DBCursor cursor) {
		try {
			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}
		} finally {
			cursor.close();
		}
	}

	public static void close() {
		if (client != null) {
			client.close();
		}
	}

	public synchronized int maxID() {
		int max = 0;

		if (faceCollection != null) {
			DBCursor cursor = faceCollection.find();
			while (cursor.hasNext()) {
				DBObject result = cursor.next();
				int id = Integer.parseInt((String) result.get("id"));
				if (id > max) {
					max = id;
				}
			}
		}

		return max;
	}
}
