import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public final class CameraCapture {
	private static VideoCapture camera = new VideoCapture(0);

	public static void captureFrame(String name) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			Mat frame = new Mat();
			if (camera.read(frame)) {
				System.out.println("Frame Obtained");
				System.out.println("Captured Frame Width " + frame.width()
						+ " Height " + frame.height());
				Highgui.imwrite(FaceDetector.IMAGE_WRITE_PATH + name, frame);
				System.out.println("OK");
			}
		}
	}
	
	public static void release() {
		camera.release();
	}
}