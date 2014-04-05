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
	public static final String IMAGE_WRITE_PATH = "bin/resources/";

	public static void main(String[] args) {
		int captureNumber = 0;
		HashMap<UniqueFace, Rect> tracker = new HashMap<UniqueFace, Rect>();

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("\nRunning FaceDetector");

		Imshow im = new Imshow("Running...");

		CascadeClassifier faceDetector = new CascadeClassifier();
		faceDetector.load(FaceDetector.class
				.getResource("resources/haarcascade_frontalface_alt.xml")
				.getPath().substring(1));

		while (true) {
			String filename = "capture" + Integer.toString(captureNumber)
					+ ".jpg";
			CameraCapture.captureFrame(filename);
			Mat image = Highgui.imread(FaceDetector.class
					.getResource(IMAGE_READ_PATH + filename).getPath()
					.substring(1));

			MatOfRect faceDetections = new MatOfRect();
			faceDetector.detectMultiScale(image, faceDetections);

			System.out.println(String.format("Detected %s faces",
					faceDetections.toArray().length));

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
						face.addImage(clip(image, rect));
						tracker.put(face, rect);
					}
				}
			}

			Iterator<UniqueFace> i = tracker.keySet().iterator();
			while (i.hasNext()) {
				UniqueFace face = i.next();
				if (tracker.get(face) == null) {
					// Face no longer visible send accumulated info to database
					face.sendData();
					i.remove();
				}
			}

			for (Rect rect : faceDetections.toArray()) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(
						rect.x + rect.width, rect.y + rect.height), new Scalar(
						0, 255, 0));
				if (!tracker.values().contains(rect)) {
					UniqueFace newFace = new UniqueFace();
					newFace.addImage(clip(image, rect));
					tracker.put(newFace, rect);
				}
			}

			im.showImage(image);
			filename = "output" + Integer.toString(captureNumber) + ".jpg";
			System.out.println(String.format("Writing %s", filename));
			Highgui.imwrite(IMAGE_WRITE_PATH + filename, image);
			++captureNumber;

			try {
				Thread.sleep(10);
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