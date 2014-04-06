import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public final class CameraCapture {
	private static VideoCapture camera = new VideoCapture(0);

	public synchronized static void captureFrame(String name) {
		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			Mat frame = new Mat();
			if (camera.read(frame)) {
//				System.out.println("Frame Obtained");
//				System.out.println("Captured Frame Width " + frame.width()
//						+ " Height " + frame.height());
				Highgui.imwrite(FaceDetector.IMAGE_WRITE_PATH + name, frame);
//				System.out.println("OK");
			}
		}
	}
	
	public synchronized static Mat captureFrame() {
		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			Mat frame = new Mat();
			if (camera.read(frame)) {
				return frame;
			}
		}
		
		return null;
	}
	
	public synchronized static void release() {
		if(camera != null) {
			camera.release();
		}
	}
}