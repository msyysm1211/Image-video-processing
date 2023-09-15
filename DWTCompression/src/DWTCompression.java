import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.List;
import java.util.Timer;

class Image {
    private int width;
    private int height;
    private byte rgb[];
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

    public void setRGB(int x, int y, double r, double g, double b) {
        int idx = y * width + x;
        int offset = width * height;
        rgb[idx] = (byte) r;
        rgb[idx + offset] = (byte) g;
        rgb[idx + offset * 2] = (byte) b;
    }

    public void setRGB(int x, int y, double[] value) {
        int idx = y * width + x;
        int offset = width * height;
        rgb[idx] = (byte) value[0];
        rgb[idx + offset] = (byte) value[1];
        rgb[idx + offset * 2] = (byte) value[2];
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

public class DWTCompression {

    static private int height = 512;
    static private int width = 512;
    private Image image;
    private Image originalImage;
    private List<Image> res;

    private void processInput(String path, int level) {
        res = new ArrayList<Image>();
        image = new Image(width, height);
        originalImage = new Image(width, height);
        image.readImageRGB(path);
        originalImage.readImageRGB(path);
        double[][][] matrixRGB = new double[3][height][width];
        if (level == -1) {
            // Get original rgb matrix
            for (int j = 0; j <= 9; j++) {
                for (int i = 0; i < 3; i++) {
                    matrixRGB[i] = getRgbMatrix(originalImage, i);
                    matrixRGB[i] = dwtEncode(matrixRGB[i], j);
                    matrixRGB[i] = setZero(matrixRGB[i], j);
                    matrixRGB[i] = dwtDecode(matrixRGB[i], j);
                }
                Image processedImage = new Image(width, height);
                for (int x = 0; x < height; x++) {
                    for (int y = 0; y < width; y++) {
                        processedImage.setRGB(x, y, new double[]{matrixRGB[0][x][y], matrixRGB[1][x][y], matrixRGB[2][x][y]});
                    }
                }
                res.add(processedImage);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                matrixRGB[i] = getRgbMatrix(originalImage, i);
                matrixRGB[i] = dwtEncode(matrixRGB[i], level);
                matrixRGB[i] = setZero(matrixRGB[i], level);
                matrixRGB[i] = dwtDecode(matrixRGB[i], level);
            }
            Image processedImage = new Image(width, height);
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    processedImage.setRGB(x, y, new double[]{matrixRGB[0][x][y], matrixRGB[1][x][y], matrixRGB[2][x][y]});
                }
            }
            res.add(processedImage);
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                image.setRGB(i, j, new double[]{matrixRGB[0][i][j], matrixRGB[1][i][j], matrixRGB[2][i][j]});
            }
        }
    }

    private double[][] getRgbMatrix(Image inputImage, int type) {
        double[][] temp = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int[] rgb = inputImage.getRGB(i, j);
                temp[i][j] = rgb[type];
            }
        }
        return temp;
    }

    private double[][] dwtEncode(double[][] matrix, int level) {
        double[][] dwtMatrix = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                dwtMatrix[i][j] = matrix[i][j];
            }
        }
        //deal with row
        for (int i = 0; i < height; i++) {
            dwtMatrix[i] = processEncodeRow(dwtMatrix[i], level);
        }
        //deal with col
        rotateMatrix(dwtMatrix);
        for (int i = 0; i < width; i++) {
            dwtMatrix[i] = processEncodeRow(dwtMatrix[i], level);
        }
        rotateMatrix(dwtMatrix);
        return dwtMatrix;
    }

    double[] processEncodeRow(double[] input, int level) {
        double[] output = Arrays.copyOf(input, input.length);
        for (int length = input.length / 2; ; length = length / 2) {
            if (level == 9) return output;
            level++;
            for (int i = 0; i < length; ++i) {
                output[i] = (input[i * 2] + input[i * 2 + 1]) / 2;
                output[length + i] = (input[i * 2] - input[i * 2 + 1]) / 2;
            }
            if (length == 1) {
                return output;
            }
            System.arraycopy(output, 0, input, 0, length);
        }

    }

    private double[][] dwtDecode(double[][] matrix, int level) {
        double[][] dwtMatrix = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                dwtMatrix[i][j] = matrix[i][j];
            }
        }
       rotateMatrix(dwtMatrix);
        //deal with col
        for (int i = 0; i < width; i++) {
            processDecodeRow(dwtMatrix[i], level);
        }
        //deal with row
        rotateMatrix(dwtMatrix);
        for (int i = 0; i < height; i++) {
            processDecodeRow(dwtMatrix[i], level);
        }
        return dwtMatrix;
    }

    double[] processDecodeRow(double[] input, int level) {
        double[] output = Arrays.copyOf(input, input.length);
        int length = (int) Math.pow(2, level);
        for (; ; length = length * 2) {
            if (level == 9) return output;
            level++;
            for (int i = 0; i < length; ++i) {
                output[2 * i] = input[i] + input[length + i];
                output[2 * i + 1] = input[i] - input[length + i];
            }
            if (length == input.length) {
                return output;
            }
            System.arraycopy(output, 0, input, 0, length * 2);
        }
    }


    double[][] setZero(double[][] matrix, int level) {
        int length = (int) Math.pow(2, level);
        double[][] temp = new double[height][width];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (i < length && j < length) {
                    temp[i][j] = matrix[i][j];
                }
            }
        }
        return temp;
    }


    public void showVideo(String[] args) throws IOException {

        // Read a parameter from command line
        String path = args[0];
        int level = Integer.valueOf(args[1]);

        processInput(path, level);

        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbImg = new JLabel(new ImageIcon(originalImage.getBufferedImage()));
        JLabel lbText1 = new JLabel("Orignial Image");

        JLabel lbImg2 = new JLabel(new ImageIcon(image.getBufferedImage()));
        JLabel lbText2 = new JLabel("Processed Image");



        java.util.Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int size= res.size();
            int i=0;
            @Override
            public void run() {
                if (i < size) {
                    lbImg2.setIcon(new ImageIcon(res.get(i).getBufferedImage()));
                    i=(i+1)%size;
                }
            }
        };
        timer.schedule(task, 0, 500);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(lbText2, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbImg, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(lbImg2, c);

        frame.pack();
        frame.setVisible(true);

    }

    void rotateMatrix(double[][] matrix) {
        double[][] temp = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp[i][j] = matrix[j][i];
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            matrix[i]=Arrays.copyOf(temp[i], matrix.length);
        }
    }

    public static void main(String[] args) throws IOException {
        DWTCompression d = new DWTCompression();
        d.showVideo(args);
    }
}
