/*
 * Author: ATUL
 * This code can be used as an alternative to imshow of OpenCV for JAVA-OpenCv
 * Make sure OpenCV Java is in your Build Path
 * Usage :
 * -------
 * Imshow ims = new Imshow("Title");
 * ims.showImage(Mat image);
 * Check Example for usage with Webcam Live Video Feed
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Imshow {

	public JFrame window;
	private ImageIcon image;
	private JLabel label;
	private MatOfByte matOfByte;
	private Boolean sizeCustom;
	private int height, width;
	
	public Imshow() {
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		image = new ImageIcon();
		label = new JLabel();
		matOfByte = new MatOfByte();
		label.setIcon(image);
		window.getContentPane().add(label);
		window.setResizable(false);
	}

	public Imshow(String title) {
		this();
		sizeCustom = false;
		window.setTitle(title);
	}

	public Imshow(String title, int height, int width) {
		this();
		sizeCustom = true;
		this.height = height;
		this.width = width;
		window.setTitle(title);
	}

	public void showImage(Mat img) {
		if (sizeCustom) {
			Imgproc.resize(img, img, new Size(height, width));
		}
		Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			image.setImage(bufImage);
			window.pack();
			label.updateUI();
			window.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}