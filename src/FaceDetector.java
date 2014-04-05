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

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("\nRunning FaceDetector");

		Imshow im = new Imshow("Running...");
		
		CascadeClassifier faceDetector = new CascadeClassifier();
		faceDetector.load(FaceDetector.class
				.getResource("resources/haarcascade_frontalface_alt.xml")
				.getPath().substring(1));

		while (captureNumber < 1000) {
			String filename = "capture" + Integer.toString(captureNumber)
					+ ".jpg";
			CameraCapture.captureFrame(filename);
			Mat image = Highgui.imread(FaceDetector.class
					.getResource(IMAGE_READ_PATH + filename).getPath()
					.substring(1));
			
			im.showImage(image);

			MatOfRect faceDetections = new MatOfRect();
			faceDetector.detectMultiScale(image, faceDetections);

			System.out.println(String.format("Detected %s faces",
					faceDetections.toArray().length));

			for (Rect rect : faceDetections.toArray()) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(
						rect.x + rect.width, rect.y + rect.height), new Scalar(
						0, 255, 0));
			}
			
			im.showImage(image);

			filename = "output" + Integer.toString(captureNumber) + ".jpg";
			System.out.println(String.format("Writing %s", filename));
			Highgui.imwrite(IMAGE_WRITE_PATH + filename, image);
			++captureNumber;
						
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}

		CameraCapture.release();
	}
}