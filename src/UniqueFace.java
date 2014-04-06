import java.awt.Point;
import java.util.ArrayList;

import org.opencv.core.Mat;

public class UniqueFace {
	private ArrayList<Mat> depictions;
	private int faceID = 0;
	private static int faceCounter = 0;

	public UniqueFace() {
		depictions = new ArrayList<Mat>();
		faceID = faceCounter++;
	}
	
	public void setID(int id) {
		faceID = id;
	}

	public void addImage(Mat m) {
		depictions.add(m);
	}
	
	public void sendData(Database db) {	
		db.writeFace(Integer.toString(faceID), depictions);
	}
	
	public void print() {
		int i = 0;
		int x = 0;
		int y = 0;
		for (Mat m : depictions) {
			Imshow im = new Imshow("Unique Face " + Integer.toString(faceID)
					+ "." + Integer.toString(i++));
			im.showImage(m);
			im.window.setLocation(new Point(x, y));
			x += im.window.getWidth();
			if(x > 1366) {
				x = 0;
				y += im.window.getHeight();
			}
			if(y > 786) {
				y = 0;
			}
		}
	}
	
	public int getID() {
		return faceID;
	}
	
	public ArrayList<Mat> getImages() {
		return depictions;
	}
}
