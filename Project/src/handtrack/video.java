package handtrack;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class video extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame = new JFrame("Hand track");
	private JLabel lab = new JLabel();
	private static JLabel jl;
	private static JLabel model;

	private static boolean close = false;
	private static boolean start = false, hand = false;
	private static Point handbox1 = new Point();
	private static Point handbox2 = new Point();
	public static Point[] rect = new Point[4];
	public static int handx = 0, handy = 0;
	public JPanel jp, ap;
	public static String str = "?? ã????...", str2 = "";
	public int boxPoint[] = { 50, 650, 50, 400 };
	public static int centerboxPoint[] = { 250,450, 180, 300 };
	private static int count = 0, mode = 0;

// mode =0 -> ???콺, 1 -> ????
	/**
	 * Create the panel.
	 */
	public video() {

	}

	public void setframe(final VideoCapture webcam) {
		frame.setSize(1200, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(lab, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closed");
				close = true;
				webcam.release();
				e.getWindow().dispose();
			}
		});
//		add(jp = new JPanel());
//		jp.setBackground(Color.black);
		frame.add(jp = new JPanel(new GridLayout(0, 1, 30, 30)), BorderLayout.EAST);
		jp.setPreferredSize(new Dimension(200, 300));
		jp.setBackground(Color.black);

		jp.add(jl = new JLabel(str));
		jp.add(model = new JLabel("..."));
		jl.setFont(new Font("hy?߰???", Font.BOLD, 35));
		jl.setForeground(Color.yellow);
		model.setFont(new Font("hy?߰???", Font.BOLD, 35));
		model.setForeground(Color.yellow);

		jl.setBorder(new EmptyBorder(10, 0, 0, 0));
		jp.setBorder(new EmptyBorder(0, 0, 300, 0));
		frame.setVisible(true);
	}

	public void frametolabel(Mat matframe) {
		MatOfByte cc = new MatOfByte();
		Highgui.imencode(".JPG", matframe, cc);
		byte[] chupa = cc.toArray();
		InputStream ss = new ByteArrayInputStream(chupa);
		try {
			BufferedImage aa = ImageIO.read(ss);

			// ????
			BufferedImage reval = new BufferedImage(aa.getWidth(), aa.getHeight(), BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < aa.getHeight(); i++) {
				for (int j = 0; j < aa.getWidth(); j++) {
					int c = aa.getRGB(j, i);
					reval.setRGB(aa.getWidth() - j - 1, i, c);
				}
			}

			lab.setIcon(new ImageIcon(reval));
//			lab.setIcon(new ImageIcon(aa));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public Mat filtrocolorergb(int b, int g, int r, int b1, int g1, int r1, Mat immagine) {
		Mat modifica = new Mat();
		if (immagine != null) {
			Core.inRange(immagine, new Scalar(b, g, r), new Scalar(b1, g1, r1), modifica);
		} else {
			System.out.println("Errore immagine");
		}
		return modifica;
	}

	public Mat filtrocolorehsv(int h, int s, int v, int h1, int s1, int v1, Mat immagine) {
		Mat modifica = new Mat();
		if (immagine != null) {
			Core.inRange(immagine, new Scalar(h, s, v), new Scalar(h1, s1, v1), modifica);
		} else {
			System.out.println("Errore immagine");
		}
		return modifica;
	}

	public Mat skindetction(Mat orig) {
		Mat maschera = new Mat();
		Mat risultato = new Mat();
		Core.inRange(orig, new Scalar(0, 0, 0), new Scalar(30, 30, 30), risultato);
		Imgproc.cvtColor(orig, maschera, Imgproc.COLOR_BGR2HSV);
		for (int i = 0; i < maschera.size().height; i++) {
			for (int j = 0; j < maschera.size().width; j++) {
				if (maschera.get(i, j)[0] < 19 || maschera.get(i, j)[0] > boxPoint[0] && maschera.get(i, j)[1] > 25
						&& maschera.get(i, j)[1] < 220) {

					risultato.put(i, j, 255, 255, 255);

				} else {
					risultato.put(i, j, 0, 0, 0);
				}
			}

		}

		return risultato;

	}

	public Mat filtromorfologico(int kd, int ke, Mat immagine) {
		Mat modifica = new Mat();
		Imgproc.erode(immagine, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(ke, ke)));
//		Imgproc.erode(modifica, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(ke,ke)));
		Imgproc.dilate(modifica, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(kd, kd)));
		return modifica;

	}

	public List<MatOfPoint> cercacontorno(Mat originale, Mat immagine, boolean disegna, boolean disegnatutto,
			int filtropixel, int maxheight) {
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		List<MatOfPoint> contoursbig = new LinkedList<MatOfPoint>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(immagine, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));
		// ?ʷϻ? ?? ũ?? ?Ÿ???, ?ʷϻ? ???? ?Ÿ???

		for (int i = 0; i < contours.size(); i++) {
			if (contours.get(i).size().height > filtropixel && contours.get(i).size().height < maxheight) {
				contoursbig.add(contours.get(i));

				if (disegna && !disegnatutto)
					Imgproc.drawContours(originale, contours, i, new Scalar(0, 255, 0), 2, 8, hierarchy, 0,
							new Point());

			}

			if (disegnatutto && !disegna)
				Imgproc.drawContours(originale, contours, i, new Scalar(0, 255, 255), 2, 8, hierarchy, 0, new Point());

		}
		return contoursbig;
	}

	public List<Point> listacontorno(Mat immagine, int filtropixel) {
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		List<MatOfPoint> contoursbig = new LinkedList<MatOfPoint>();
		List<Point> listapunti = new LinkedList<Point>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(immagine, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));

		for (int i = 0; i < contours.size(); i++) {
			// System.out.println("Dimensione contorni"+contours.get(i).size().height);
			if (contours.get(i).size().height > filtropixel) {
				contoursbig.add(contours.get(i));
			}

		}
		if (contoursbig.size() > 0) {

			listapunti = contoursbig.get(0).toList();

		}
		return listapunti;
	}

	public List<Point> outviluppodifetti(Mat immagine, List<MatOfPoint> contours, boolean disegna, int sogliaprofondita,
			Point center) {
		List<Point> dita = new LinkedList<Point>();

		for (int i = 0; i < contours.size(); i++) {
			MatOfInt hull_ = new MatOfInt();
			MatOfInt4 convexityDefects = new MatOfInt4();

			@SuppressWarnings("unused")
			List<Point> punticontorno = new LinkedList<Point>();
			punticontorno = contours.get(i).toList();

			Imgproc.convexHull(contours.get(i), hull_);
			Imgproc.convexityDefects(contours.get(i), hull_, convexityDefects);
//			System.out.println(convexityDefects.size().height);
			int cnt = 0;

//			if (hull_.size().height >= 4 && convexityDefects.size().height < 30) {
			if (hull_.size().height >= 4) {

				List<Point> pts = new ArrayList<Point>();
				MatOfPoint2f pr = new MatOfPoint2f();
				Converters.Mat_to_vector_Point(contours.get(i), pts);
				// rettangolo
				pr.create((int) (pts.size()), 1, CvType.CV_32S);
				pr.fromList(pts);
				// fine rettangolo

				int[] buff = new int[4];
				int[] buff2 = new int[4];
				Point fp = new Point();

				for (int i1 = 0; i1 < convexityDefects.size().height; i1++) {
					convexityDefects.get(i1, 0, buff);

					if (buff[3] / 256 > sogliaprofondita) {
						Point p = new Point();
						Point p2 = new Point();

						for (int j = i1 + 1; j < convexityDefects.size().height; j++) {
							convexityDefects.get(j, 0, buff2);
							if (buff2[3] / 256 > sogliaprofondita) {
								if (pts.get(buff2[2]).x > boxPoint[0] && pts.get(buff2[2]).x < boxPoint[1]
										&& pts.get(buff2[2]).y > boxPoint[2] && pts.get(buff2[2]).y < boxPoint[3]) {

									p2.x = pts.get(buff2[2]).x;
									p2.y = pts.get(buff2[2]).y;
									break;
								}
							}
						}

						if (pts.get(buff[2]).x > boxPoint[0] && pts.get(buff[2]).x < boxPoint[1]
								&& pts.get(buff[2]).y > boxPoint[2] && pts.get(buff[2]).y < boxPoint[3]) {

//							System.out.println(buff[0] + " / " + buff[1] + " / "+ buff[2] + " / " + buff[3]+" == " + convexityDefects);
							cnt++;
							if (cnt == count - 1) {
								continue;
							}
							if (cnt == count) {
								p2.x = fp.x;
								p2.y = fp.y;
							}
							p.x = (pts.get(buff[2]).x + p2.x) / 2;
							p.y = (pts.get(buff[2]).y + p2.y) / 2;

							if (cnt == 1) {
								fp = pts.get(buff[2]);
//								System.err.println("aaaaaaaaaaaaaaaaaaaaaaa");
							}
//							System.out.println(pts.get(buff[2])+"/"+p2+"/"+p);

							dita.add(p);
							Core.circle(immagine, p, 6, new Scalar(0, 0, 255));
							Core.putText(immagine, cnt + "", p, Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 255, 0));
							if (disegna)
								Core.circle(immagine, p, 6, new Scalar(0, 0, 255));
						}
					}
				}
			}
		}

		return dita;
	}

	public List<Point> inviluppodifetti(Mat immagine, List<MatOfPoint> contours, boolean disegna,
			int sogliaprofondita) {
		List<Point> defects = new LinkedList<Point>();

		for (int i = 0; i < contours.size(); i++) {
			MatOfInt hull_ = new MatOfInt();
			MatOfInt4 convexityDefects = new MatOfInt4();

			@SuppressWarnings("unused")
			List<Point> punticontorno = new LinkedList<Point>();
			punticontorno = contours.get(i).toList();

			Imgproc.convexHull(contours.get(i), hull_);
			Imgproc.convexityDefects(contours.get(i), hull_, convexityDefects);
//			System.out.println(convexityDefects.size().height);

//			if (hull_.size().height >= 4 && convexityDefects.size().height < 30) {
			if (hull_.size().height >= 4) {

				List<Point> pts = new ArrayList<Point>();
				MatOfPoint2f pr = new MatOfPoint2f();
				Converters.Mat_to_vector_Point(contours.get(i), pts);
				// rettangolo
				pr.create((int) (pts.size()), 1, CvType.CV_32S);
				pr.fromList(pts);
				if (pr.height() > 0) {
					RotatedRect r = Imgproc.minAreaRect(pr);
					Point[] rect = new Point[4];
					r.points(rect);

					if (rect[2].x > 140 && rect[0].x < 740 && rect[2].y > 40 && rect[1].y < 390) {
						this.rect = rect;
//						System.out.println(rect[0]+"/"+rect[1]+"/"+rect[2]+"/"+rect[3]);
					}

					Core.line(immagine, rect[0], rect[1], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[0], rect[3], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[1], rect[2], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[2], rect[3], new Scalar(0, 100, 0), 2);
					Core.rectangle(immagine, r.boundingRect().tl(), r.boundingRect().br(),
							new Scalar(boxPoint[2], boxPoint[2], boxPoint[2]));
				}
				// fine rettangolo

				int[] buff = new int[4];
				int[] zx = new int[1];
				int[] zxx = new int[1];
				for (int i1 = 0; i1 < hull_.size().height; i1++) {
					if (i1 < hull_.size().height - 1) {
						hull_.get(i1, 0, zx);
						hull_.get(i1 + 1, 0, zxx);
					} else {
						hull_.get(i1, 0, zx);
						hull_.get(0, 0, zxx);
					}
					if (disegna)
						Core.line(immagine, pts.get(zx[0]), pts.get(zxx[0]), new Scalar(140, 140, 140), 2);
					if (pts.get(zx[0]).x > boxPoint[0] && pts.get(zxx[0]).x < boxPoint[1]
							&& pts.get(zx[0]).y > boxPoint[2] && pts.get(zxx[0]).y < boxPoint[3]) {
						handbox1 = pts.get(zx[0]);
						handbox2 = pts.get(zxx[0]);
					}

				}

				for (int i1 = 0; i1 < convexityDefects.size().height; i1++) {
					convexityDefects.get(i1, 0, buff);
					if (buff[3] / 256 > sogliaprofondita) {
//						if (pts.get(buff[2]).x > 0 && pts.get(buff[2]).x < 1024 && pts.get(buff[2]).y > 0 && pts.get(buff[2]).y < 768) {
						if (pts.get(buff[2]).x > boxPoint[0] && pts.get(buff[2]).x < boxPoint[1]
								&& pts.get(buff[2]).y > boxPoint[2] && pts.get(buff[2]).y < boxPoint[3]) {
							count++;
							defects.add(pts.get(buff[2]));
							Core.circle(immagine, pts.get(buff[2]), 6, new Scalar(0, 255, 0));
							Core.putText(immagine, count + "", pts.get(buff[2]), Core.FONT_HERSHEY_COMPLEX, 1,
									new Scalar(0, 0, 255));
							if (disegna)
								Core.circle(immagine, pts.get(buff[2]), 6, new Scalar(0, 255, 0));
						}
					}
				}
//				System.out.println(count);
//				if (defects.size() < 3) {
//					int dim = pts.size();
//					Core.circle(immagine, pts.get(0), 3, new Scalar(0, 255, 0), 2);
//					Core.circle(immagine, pts.get(0 + dim / 4), 3, new Scalar(0, 255, 0), 2);
//					defects.add(pts.get(0));
//					defects.add(pts.get(0 + dim / 4));
//
//				}
			}
		}
		return defects;
	}

	public void disegnrettangolo(Mat immagine) {
		Core.line(immagine, new Point(boxPoint[0], boxPoint[2]), new Point(boxPoint[1], boxPoint[2]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[0], boxPoint[3]), new Point(boxPoint[1], boxPoint[3]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[0], boxPoint[2]), new Point(boxPoint[0], boxPoint[3]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[1], boxPoint[2]), new Point(boxPoint[1], boxPoint[3]),
				new Scalar(255, 0, 0), 2);
	}

	public Point centropalmo(Mat immagine, List<Point> difetti) {
		MatOfPoint2f pr = new MatOfPoint2f();
		Point center = new Point();
		float[] radius = new float[1];
		pr.create((int) (difetti.size()), 1, CvType.CV_32S);
		pr.fromList(difetti);

		if (pr.size().height > 0) {
			start = true;
			Imgproc.minEnclosingCircle(pr, center, radius);

			// Core.circle(immagine, center,(int) radius[0], new Scalar(255,0,0));
			// Core.circle(immagine, center, 3, new Scalar(0,0,255),4);
		} else {
			start = false;
		}
		return center;

	}

	public List<Point> handDraw(Point center, List<Point> dita) {
		List<Point> defects = new LinkedList<Point>();

		for (int i = 0; i < dita.size(); i++) {
			defects.add(new Point(2 * dita.get(i).x - center.x, 2 * dita.get(i).y - center.y));
		}

		return defects;
	}

	public void disegnaditacentropalmo(Mat immagine, Point center, Point dito, List<Point> dita, List<Point> subpoint) {

		Core.line(immagine, new Point(boxPoint[0], boxPoint[2]), new Point(boxPoint[1], boxPoint[2]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[0], boxPoint[3]), new Point(boxPoint[1], boxPoint[3]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[0], boxPoint[2]), new Point(boxPoint[0], boxPoint[3]),
				new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(boxPoint[1], boxPoint[2]), new Point(boxPoint[1], boxPoint[3]),
				new Scalar(255, 0, 0), 2);

		// ?簢??
		if (dita.size() == 1) {
			Core.line(immagine, center, dito, new Scalar(0, 255, 255), 4);
			Core.circle(immagine, dito, 3, new Scalar(255, 0, 255), 3);
			// Core.putText(immagine, dito.toString(), dito, Core.FONT_HERSHEY_COMPLEX, 1,
			// new Scalar(0,200,255));
			hand = false;
			str = "?? ã????...";
			model.setText("...");
			jl.setText(str);
			revalidate();
			repaint();
//			System.err.println("aaa");
		} else {
			hand = true;
			int cnt = 0;
			double minX = 999999999, maxX = 0, minY = 999999999, maxY = 0;
			try {
				for (int i = 0; i < rect.length; i++) {
					if (rect[i].x < minX) {
						minX = rect[i].x;
					}
					if (rect[i].x > maxX) {
						maxX = rect[i].x;
					}

					if (rect[i].y < minY) {
						minY = rect[i].y;
					}
					if (rect[i].y > maxY) {
						maxY = rect[i].y;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			try {
				for (int i = 0; i < dita.size(); i++) {
					Core.line(immagine, center, dita.get(i), new Scalar(0, 255, 255), 4);
					Core.circle(immagine, dita.get(i), 3, new Scalar(255, 0, 255), 3);
					// sub

					if (subpoint.get(i).x > minX && subpoint.get(i).x < maxX && subpoint.get(i).y > minY
							&& subpoint.get(i).y < maxY) {
						cnt++;
						Core.line(immagine, dita.get(i), subpoint.get(i), new Scalar(0, 255, 255), 4);
						Core.circle(immagine, subpoint.get(i), 3, new Scalar(255, 0, 255), 3);
					}

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (cnt >= 4) {
				str = "?? ??";
			} else if (cnt <= 2) {
				str = "?? ????";
			}

			jl.setText(str);
			revalidate();
			repaint();
//			System.out.println(cnt);
		}

		Core.circle(immagine, center, 3, new Scalar(0, 0, 255), 3);

		// ????
		Core.putText(immagine, center.toString(), new Point(0, 0), Core.FONT_HERSHEY_COMPLEX, 1,
				new Scalar(0, 200, 255));

	}

	public Point filtromediamobile(List<Point> buffer, Point attuale) {
		Point media = new Point();
		media.x = 0;
		media.y = 0;
		for (int i = buffer.size() - 1; i > 0; i--) {
			buffer.set(i, buffer.get(i - 1));
			media.x = media.x + buffer.get(i).x;
			media.y = media.y + buffer.get(i).y;
		}
		buffer.set(0, attuale);
		media.x = (media.x + buffer.get(0).x) / buffer.size() - 10;
		media.y = (media.y + buffer.get(0).y) / buffer.size() + 40;
		return media;
	}

	public void connect(Point center) throws InterruptedException {
		try {
			Robot r = new Robot();
//			r.mouseMove((int)center.x, (int)center.y);
			if (mode == 0) {// ???콺
				r.mouseMove(1800 - (int) (center.x * 3 - 300), (int) center.y * 4 - 600);
				
				if (str.equals("?? ????")) {
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				}else if (str.equals("?? ??")) {
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				}
			} else {// ????
				
				if (center.x > boxPoint[0] && center.x < centerboxPoint[0]) {
					r.keyPress(39);
					r.keyRelease(39);
				}
				if (center.x < boxPoint[1] && center.x > centerboxPoint[1]) {
					r.keyPress(37);
					r.keyRelease(37);
				}
				if (center.y > boxPoint[2] && center.y < centerboxPoint[2]) {
					r.keyPress(38);
					r.keyRelease(38);
				}
				if (center.y < boxPoint[3] && center.y > centerboxPoint[3]) {
					r.keyPress(40);
					r.keyRelease(40);
				}
			}

		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static int mm = 0, time = 0, gg = 0, mm2 = 0;
	public static Point center = new Point(), p = new Point();
	public static Timer tm = new Timer(10, new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (center.x + 5 > p.x && center.x - 5 < p.x && center.y + 5 > p.y && center.y - 5 < p.y) {

				if (jl.getText().equals("?? ??")) {
					mm = 0;
				} else if (jl.getText().equals("?? ????")) {
					mm = 1;

				}
				
				if (model.getText().equals("???콺 ????")) {
					if (mm != mm2) {
						time = 0;
						gg++;

					}
				}else {
					if (center.x > centerboxPoint[0] && center.x < centerboxPoint[1] && center.y > centerboxPoint[2] && center.y < centerboxPoint[3]) {
						if (mm != mm2) {
							time = 0;
							gg++;

						}
					}
				}
//				System.err.println(gg);
				mm2 = mm;
				if (gg >= 5) {
					if (mode == 0) {
						mode = 1;
					} else {
						mode = 0;
					}
					gg = 0;
					time = 0;
					mm = 0;
					mm2 = 0;
				}
				if (hand) {
					if (mode == 0) {
						model.setText("???콺 ????");
					} else {
						model.setText("???? ????");
					}
				}
				time++;
				if (time == 100) {
					mm = 0;
					mm2 = 0;
					gg = 0;
					time = 0;
//					System.out.println("------------------------------");
				}
			}
			if (jl.getText().equals("?? ????")) {
				p.x = center.x;
				p.y = center.y;
			}
		}
	});

	public void GuideLine(Mat immagine) { //???̵? ?? 
		Core.line(immagine, new Point(boxPoint[0], (boxPoint[2] + boxPoint[3]) / 2),
				new Point(boxPoint[1], (boxPoint[2] + boxPoint[3]) / 2), new Scalar(255, 255, 0), 2);
		Core.line(immagine, new Point((boxPoint[0] + boxPoint[1]) / 2, boxPoint[2]),
				new Point((boxPoint[0] + boxPoint[1]) / 2, boxPoint[3]), new Scalar(255, 255, 0), 2);

		Core.line(immagine, new Point(centerboxPoint[0], centerboxPoint[2]),
				new Point(centerboxPoint[1], centerboxPoint[2]), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(centerboxPoint[0], centerboxPoint[3]),
				new Point(centerboxPoint[1], centerboxPoint[3]), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(centerboxPoint[0], centerboxPoint[2]),
				new Point(centerboxPoint[0], centerboxPoint[3]), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(centerboxPoint[1], centerboxPoint[2]),
				new Point(centerboxPoint[1], centerboxPoint[3]), new Scalar(255, 0, 0), 2);
	}

	public static void main(String[] args) throws InterruptedException, AWTException {
		
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try { //?ڵ?Ʈ??ŷ ?? ĸ?? ?̿??ϱ????? opencv ???? ?θ? ?ҷ?????
			File f=  new File("libs/opencv_java2413.dll");
			System.load(f.getAbsolutePath());
			System.out.println(f.getAbsolutePath());	
		} catch (Exception e) {
			// TODO: handle exception
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}
		
		video v = new video();
		VideoCapture webcam = new VideoCapture(0); //ķ Ű??
		webcam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 700);
		webcam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 1000);
		v.setframe(webcam);// ũ?? ???? ?? ????

		//???? ????
		Mat mimm = new Mat();
		Mat modifica = new Mat();
		Point centro = new Point();
		Point dito = new Point();
		List<Point> buffer = new LinkedList<Point>();
		List<Point> dita = new LinkedList<Point>();
		List<Point> subpoint = new LinkedList<Point>();

		long temp = 0;
		tm.start();

		while (!close) {
			if (!webcam.isOpened() && !close) { // ???? ó?? 
				System.out.println("Camera Error");
			} else {
				temp = System.currentTimeMillis();
				List<Point> difetti = new LinkedList<Point>();
				webcam.retrieve(mimm);
				v.disegnrettangolo(mimm);
				count = 0;

				modifica = v.filtromorfologico(2, 7, v.filtrocolorehsv(0, 0, 0, 180, 255, 40, mimm)); //?? ?ν?

				difetti = v.inviluppodifetti(mimm, v.cercacontorno(mimm, modifica, false, false, 300, 5000), false, 5); //???ν??? ???? ȭ???? ??, ??ġ ?ν?
//				for (int i = 0; i < difetti.size(); i++) {
//					System.out.println(difetti.get(i));
//				}
				if (count != 0) {
					if (buffer.size() < 7) {
						buffer.add(v.centropalmo(mimm, difetti)); 
					} else {
						centro = v.filtromediamobile(buffer, v.centropalmo(mimm, difetti)); //?߽??? ????
//						 System.out.println((int)centro.x+" "+(int)centro.y+" "
//						 		+ ""+(int)v.centropalmo(mimm,difetti).x+" "+(int)v.centropalmo(mimm,difetti).y);
					}

					dita = v.outviluppodifetti(mimm, v.cercacontorno(mimm, modifica, false, false, 300, 5000), false, 5,
							centro); //???? ??ǥ?? ?޾ƿ???

//					if (dita.size() == 1 && bufferdita.size() < 5) {
//						bufferdita.add(dita.get(0));
//						dito = dita.get(0);
//					} else {
//						if (dita.size() == 1) {
//							dito = v.filtromediamobile(bufferdita, dita.get(0));
//							// System.out.println((int)dito.x +" "+(int)dito.y+" "+(int)dita.get(0).x+"
//							// "+(int)dita.get(0).y);
//						}
//					}

//					v.disegnaditacentropalmo(mimm, centro, dito, difetti);
					subpoint = v.handDraw(centro, dita); // ???????? ?? ǥ??

					v.disegnaditacentropalmo(mimm, centro, dito, dita, subpoint); //??, ?ڽ? ǥ??
					center = centro; //??????ġ Ȯ??
					
					v.connect(centro); // ???콺 ?? Ű???? ????
//					v.mousetrack(dita, dito, centro, r, true, mimm, temp);

				}
				v.GuideLine(mimm); // ???̵? ?? ǥ??
				v.frametolabel(mimm); // ȭ?鿡 â ???̰? 
			}
		}
	}
}