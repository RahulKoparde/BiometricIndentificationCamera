import java.awt.Point;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class UniqueFace {
	private ArrayList<Mat> depictions;
	private int faceID = 0;
	private static int faceCounter = 0;
	private Imshow disp = new Imshow("disp");
	
	public UniqueFace() {
		depictions = new ArrayList<Mat>();
		faceID = faceCounter++;
	}
	
	public int getID() {
		return faceID;
	}
	
	public ArrayList<Mat> getImages() {
		return depictions;
	}
	
	public void setID(int id) {
		faceID = id;
	}

	public void addImage(Mat m) {
		depictions.add(m);
	}
	
	public void sendData(Database db, FaceDetector.CameraTypes cameraType) {	
		db.writeFace(Integer.toString(faceID), depictions, cameraType);
	}
	
	public void print(int index) {
			Imshow im = new Imshow("Unique Face " + Integer.toString(faceID)
					+ "." + Integer.toString(index));
			im.showImage(depictions.get(index));
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
	
	public boolean compareFace(UniqueFace face, int threshold) {
		for(int i = 0; (i < face.getImages().size() && i < 7); ++i) {
			Mat m1 = face.getImages().get(i);
			for(int j = 0; (j < depictions.size() && j < 7); ++j) {
				Mat m2 = depictions.get(j);
				int matches = 0;
				
				Size size = new Size(Math.max(m1.rows(), m2.rows()), Math.max(m1.cols(), m2.cols()));
				Mat result = new Mat(size, m1.type());
				Imgproc.resize(m1, m1, size);
				Imgproc.resize(m2, m2, size);
				Core.absdiff(m2, m1, result);
				Imgproc.threshold(result, result, threshold, 255, Imgproc.THRESH_BINARY_INV);

				disp.setTitle("diff databaseimg=" + i + " selfimg=" + j);
				disp.setSize((int)result.width(), (int)result.height());
				disp.showImage(result);
				
				for(int r = 0; r < result.rows(); ++r) {
					for(int c = 0; c < result.cols(); ++c) {
						double[] value = result.get(r, c);
						if(value[0] + value[1] + value[2] >= 255) {
							++matches;
						}
					}
				}
				
//				System.out.println("Pixels matched: " + matches + " out of " + result.total());
				if(matches >= (result.total() / 11) * 10) {
					return true;
				}
			}
		}
		
		return false;
	}
}
