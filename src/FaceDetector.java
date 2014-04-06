import java.util.HashMap;
import java.util.Iterator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {
	public static final String IMAGE_READ_PATH = "resources/";
	//Change to bin/ in testing and src/ in production
	public static final String IMAGE_WRITE_PATH = "src/resources/"; 
	public static final int THRESHOLD = 55;

	public enum CameraTypes {
		ENTER, EXIT
	}

	public static void main(String[] args) {
		int captureNumber = 0;
		String username = "krdj";
		String password = "m0ng0b0ng0s";
		int port = 10064;
		String name = "facebase";

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("\nRunning FaceDetector");

		HashMap<UniqueFace, Rect> tracker = new HashMap<UniqueFace, Rect>();
		Imshow im = new Imshow("Running...");
		Database db = new Database(name, port, username, password);

		CascadeClassifier faceDetector = new CascadeClassifier();
		faceDetector.load(FaceDetector.class
				.getResource("resources/haarcascade_frontalface_alt.xml")
				.getPath().substring(1)); //WINDOWS

		// faceDetector.load(FaceDetector.class
		// .getResource("resources/haarcascade_frontalface_alt.xml")
		// .getPath()); //OSX

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Closing...");
				CameraCapture.release();
				Database.close();
			}
		});

		Mat currentImage = null;
		while (true) {
			String filename = "capture" + Integer.toString(captureNumber)
					+ ".jpg";
			CameraCapture.captureFrame(filename);

			currentImage = Highgui.imread(FaceDetector.class
					.getResource(IMAGE_READ_PATH + filename).getPath()
					.substring(1)); // WINDOWS

			// currentImage = Highgui.imread(FaceDetector.class
			// .getResource(IMAGE_READ_PATH + filename).getPath()); //OSX

			MatOfRect faceDetections = new MatOfRect();
			faceDetector.detectMultiScale(currentImage, faceDetections);

			// System.out.println(String.format("Detected %s faces",
			// faceDetections.toArray().length));

			for (UniqueFace face : tracker.keySet()) {
				Rect lastPosition = tracker.get(face);
				tracker.put(face, null);

				for (Rect rect : faceDetections.toArray()) {
					int lastLeft = lastPosition.x, left = rect.x;
					int lastRight = lastPosition.x + lastPosition.width, right = rect.x
							+ rect.width;
					int lastUp = lastPosition.y, up = rect.y;
					int lastDown = lastPosition.y + lastPosition.height, down = rect.y
							+ rect.width;

					if (lastLeft < right && lastRight > left && lastUp < down
							&& lastDown > up) { // intersects
						face.addImage(clip(currentImage, rect));
						tracker.put(face, rect);
					}
				}
			}

			Iterator<UniqueFace> i = tracker.keySet().iterator();
			while (i.hasNext()) {
				UniqueFace face = i.next();
				if (tracker.get(face) == null) {
					// Face no longer visible send accumulated info to database
					for (int j = 0; j < db.maxID(); j++) {
						if (face.compareFace(db.readFace(j), THRESHOLD) == true) {
							face.setID(j);
						}
					}
					face.sendData(db, CameraTypes.ENTER);
					i.remove();
				}
			}

			for (Rect rect : faceDetections.toArray()) {
				Core.rectangle(currentImage, new Point(rect.x, rect.y),
						new Point(rect.x + rect.width, rect.y + rect.height),
						new Scalar(0, 255, 0));
				if (!tracker.values().contains(rect)) {
					UniqueFace newFace = new UniqueFace();
					newFace.addImage(clip(currentImage, rect));
					tracker.put(newFace, rect);
				}
			}

			im.showImage(currentImage);
			filename = "output" + Integer.toString(captureNumber) + ".jpg";
			// System.out.println(String.format("Writing %s", filename));
			Highgui.imwrite(IMAGE_WRITE_PATH + filename, currentImage);
			++captureNumber;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static Mat clip(Mat image, Rect rect) {
		return image.clone().submat(rect.y, rect.y + rect.height, rect.x,
				rect.x + rect.width);
	}
}