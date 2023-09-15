
import org.w3c.dom.ranges.Range;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.Timer;

class Image {
    private int width;
    private int height;
    private byte rgb[] = null;
    private float hsv[][][] = null;
    private BufferedImage bufferedImage = null;

    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        rgb = new byte[width * height * 3];
    }

    public void readImageRGB(String imagePath) {
        try {
            int len = width * height * 3;
            if (rgb == null) rgb = new byte[len];

            File file = new File(imagePath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);
            raf.read(rgb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getRGB(int x, int y) {
        int idx = y * width + x;
        int offset = width * height;
        int[] ret = new int[3];
        ret[0] = Byte.toUnsignedInt(rgb[idx]);
        ret[1] = Byte.toUnsignedInt(rgb[idx + offset]);
        ret[2] = Byte.toUnsignedInt(rgb[idx + offset * 2]);
        return ret;
    }

    public void setRGB(int x, int y, int r, int g, int b) {
        int idx = y * width + x;
        int offset = width * height;
        rgb[idx] = (byte) r;
        rgb[idx + offset] = (byte) g;
        rgb[idx + offset * 2] = (byte) b;
    }

    public void setRGB(int x, int y, int[] value) {
        int idx = y * width + x;
        int offset = width * height;
        rgb[idx] = (byte) value[0];
        rgb[idx + offset] = (byte) value[1];
        rgb[idx + offset * 2] = (byte) value[2];
    }

    public float[][][] getHSV() {
        if (hsv != null) return hsv;
        if (rgb == null) return null;

        int ind = 0;
        hsv = new float[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte r = rgb[ind];
                byte g = rgb[ind + height * width];
                byte b = rgb[ind + height * width * 2];
                int R = Byte.toUnsignedInt(r);
                int G = Byte.toUnsignedInt(g);
                int B = Byte.toUnsignedInt(b);
                float[] ret = RGBtoHSV(R, G, B);
                hsv[y][x][0] = ret[0];
                hsv[y][x][1] = ret[1];
                hsv[y][x][2] = ret[2];
                ind++;
            }
        }
        return hsv;
    }

    public float[] RGBtoHSV(int R, int G, int B) {
        float[] hsbValues = new float[3];
        Color.RGBtoHSB(R, G, B, hsbValues);
        hsbValues[0] *= 360;
        hsbValues[1] *= 100;
        hsbValues[2] *= 100;
        return hsbValues;
    }

    public BufferedImage getBufferedImage() {
        if (bufferedImage != null) return bufferedImage;
        if (rgb == null) return null;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int ind = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte r = rgb[ind];
                byte g = rgb[ind + height * width];
                byte b = rgb[ind + height * width * 2];
                int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                bufferedImage.setRGB(x, y, pix);
                ind++;
            }
        }
        return bufferedImage;
    }
}

public class VideoDisplay {
    private static final int frameNumber = 480;
    private static final int width = 640; // default image width and height
    private static final int height = 480;
    private static final int[] move = {-1, 0, 1};

    JFrame frame;
    JLabel lbImg;
    BufferedImage[] images = new BufferedImage[frameNumber];

    String[] getFilePath(String dictionary) throws IOException {
        Path path = Paths.get(dictionary);
        List<Path> imagePath = new ArrayList<>(Files.list(path).toList());
        Collections.sort(imagePath);
        String[] FilePath = new String[imagePath.size()];
        for (int i = 0; i < imagePath.size(); i++) {
            FilePath[i] = String.valueOf(imagePath.get(i));
        }
        return FilePath;
    }

    Image replaceBackground_mode1(Image foregroudImage, Image backgroudImage) {
        Image processedImage = new Image(width, height);
        boolean[][] isGreen = new boolean[height][width];
        float[][][] foreHSV = foregroudImage.getHSV();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isGreenScreenbyHSV(foreHSV[y][x][0], foreHSV[y][x][1], foreHSV[y][x][2])) {
                    isGreen[y][x] = true;
                    processedImage.setRGB(x, y, backgroudImage.getRGB(x, y));
                } else {
                    processedImage.setRGB(x, y, foregroudImage.getRGB(x, y));
                }
            }
        }
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                for (int ymove : move) {
                    for (int xmove : move) {
                        int y_index = y + ymove;
                        int x_index = x + xmove;
                        if (isGreen[y_index][x_index]) {
                            processedImage.setRGB(x, y, backgroudImage.getRGB(x, y));
                        }
                    }
                }
            }
        }
        return processedImage;
    }

    Image replaceBackground_mode0(Image foregroudImage, Image foregroundImage2, Image backgroudImage) {
        Image processedImage = new Image(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float[][][] pixel1 = foregroudImage.getHSV();
                float[][][] pixel2 = foregroundImage2.getHSV();
                if (isBackground(pixel1[y][x][0], pixel1[y][x][1], pixel1[y][x][2], pixel2[y][x][0], pixel2[y][x][1], pixel2[y][x][2])) {
                    processedImage.setRGB(x, y, backgroudImage.getRGB(x, y));
                } else {
                    processedImage.setRGB(x, y, foregroudImage.getRGB(x, y));
                }
            }
        }
        return processedImage;
    }

    void processImage(String foregroundDic, String backgroundDic, int mode) throws IOException {
        String[] forePath = getFilePath(foregroundDic);
        String[] backPath = getFilePath(backgroundDic);
        Image lastforeImage = new Image(width, height);
        for (int i = 0; i < frameNumber; i++) {
            Image foreImage = new Image(width, height);
            Image backImage = new Image(width, height);
            foreImage.readImageRGB(forePath[i]);
            backImage.readImageRGB(backPath[i]);
            if (mode == 1) {
                Image image = replaceBackground_mode1(foreImage, backImage);
                images[i] = image.getBufferedImage();
            }
            if (mode == 0) {
                if (i >= 1) {
                    Image image = replaceBackground_mode0(foreImage, lastforeImage, backImage);
                    images[i - 1] = image.getBufferedImage();
                    if (i == frameNumber - 1) {
                        images[i] = image.getBufferedImage();
                    }
                }
                lastforeImage = foreImage;
            }
        }

    }

    // 96<=r<=108
    // 157<=g<=174
    // 53<=b<=66

    public boolean isGreenScreenbyHSV(double h, double s, double v) {
        if (70 <= h && h <= 170 && s >= 30 && 30 <= v) {
            return true;
        }
        return false;
    }

    public boolean isBackground(double img1_h, double img1_s, double img1_v, double img2_h, double img2_s, double img2_v) {
        if (range(0, 0, Math.abs(img1_h - img2_h)) && range(0, 20, Math.abs(img1_s - img2_s)) &&
                range(0, 40, Math.abs(img1_v - img2_v))) {
            return true;
        }
        return false;
    }

    boolean range(double low, double high, double target) {
        if (target >= low && target <= high) {
            return true;
        }
        return false;
    }

    public void showVideo(String[] args) throws IOException {

        // Read a parameter from command line
        String foregroundDic = args[0];
        String backgroundDic = args[1];
        int mode = Integer.valueOf(args[2]);
        //processImage("/Users/mashengyuan/Project/576/VideoDisplay/src/input/foreground_1", "/Users/mashengyuan/Project/576/VideoDisplay/src/input/background_static_1");
        processImage(foregroundDic, backgroundDic, mode);
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        lbImg = new JLabel(new ImageIcon(images[0]));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbImg, c);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if (i < frameNumber) {
                    lbImg.setIcon(new ImageIcon(images[i]));
                    i++;
                }
            }
        };
        timer.schedule(task, 0, 42);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        VideoDisplay ren = new VideoDisplay();
        ren.showVideo(args);
    }
}
