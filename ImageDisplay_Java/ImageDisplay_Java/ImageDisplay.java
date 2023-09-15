
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

    JFrame frame;
    JLabel lbIm1;
    JLabel lbIm2;
    BufferedImage originalimage;
    BufferedImage newImage;
    int width = 1920; // default image width and height
    int height = 1080;
    int intput_value_Y;
    int intput_value_U;
    int intput_value_V;
    double Sw;
    double Sh;
    int scale_height;
    int scale_width;
    int isantialiasing;
    YUV[][] YUVmatrix;
    int[] dx = {-1, 0, 1};
    int[] dy = {-1, 0, 1};

    class YUV {
        double Y;
        double U;
        double V;

        YUV(double y, double u, double v) {
            Y = y;
            U = u;
            V = v;
        }
    }

    /**
     * Read Image RGB
     * Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);
            YUVmatrix = new YUV[height][width];
            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int R = Byte.toUnsignedInt(r);
                    int G = Byte.toUnsignedInt(g);
                    int B = Byte.toUnsignedInt(b);

                    double[] YUV = RGBtoYUV(R, G, B);
                    //store YUV value in YUVmatrix
                    YUVmatrix[y][x] = new YUV(YUV[0], YUV[1], YUV[2]);
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
            subsample();
            ConvertYUVtoRGB();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showIms(String[] args) {

        // Read a parameter from command line
        intput_value_Y = Integer.valueOf(args[1]);
        intput_value_U = Integer.valueOf(args[2]);
        intput_value_V = Integer.valueOf(args[3]);
        Sw = Double.valueOf(args[4]);
        Sh = Double.valueOf(args[5]);
        isantialiasing = Integer.valueOf(args[6]);

        scale_height = (int) (height * Sh);
        scale_width = (int) (width * Sw);

        // Read in the specified image
        originalimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[0], originalimage);
         BufferedImage finalimage= scalingImage(newImage);

        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbText1 = new JLabel("Original Image");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbText2 = new JLabel("Image After Modification");
        lbText2.setHorizontalAlignment(SwingConstants.CENTER);
        lbIm1 = new JLabel(new ImageIcon(originalimage));
        lbIm2 = new JLabel(new ImageIcon(finalimage));

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
        frame.getContentPane().add(lbIm1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(lbIm2, c);

        frame.pack();
        frame.setVisible(true);
    }

    void subsample() {
        int height = YUVmatrix.length;
        int weight = YUVmatrix[0].length;
        for (int i = 0; i < height; i++) {
            if (intput_value_Y > 1) {
                int gap_y=intput_value_Y;
                for (int j = 0; j < weight - intput_value_Y; j += intput_value_Y) {
                    for (int k = j + 1; k < j + intput_value_Y; k++) {
                        YUVmatrix[i][k].Y = (YUVmatrix[i][j].Y *(intput_value_Y-(k-j)) + YUVmatrix[i][j + intput_value_Y].Y*(k-j)) / gap_y;
                    }
                }
                for (int j = weight - intput_value_Y; j < weight; j++) {
                    YUVmatrix[i][j].Y = YUVmatrix[i][weight - intput_value_Y].Y;
                }
            }
            if (intput_value_U > 1) {
                int gap_u=intput_value_U;
                for (int j = 0; j < weight - intput_value_U; j += intput_value_U) {
                    for (int k = j + 1; k < j + intput_value_U; k++) {
                        YUVmatrix[i][k].U = (YUVmatrix[i][j].U *(intput_value_U-(k-j)) + YUVmatrix[i][j + intput_value_U].U*(k-j)) / gap_u;
                    }
                }
                for (int j = weight - intput_value_U; j < weight; j++) {
                    YUVmatrix[i][j].U = YUVmatrix[i][weight - intput_value_U].U;
                }
            }
            if (intput_value_V > 1) {
                int gap_v=intput_value_V;
                for (int j = 0; j < weight - intput_value_V; j += intput_value_V) {
                    for (int k = j + 1; k < j + intput_value_V; k++) {
                        YUVmatrix[i][k].V = (YUVmatrix[i][j].V *(intput_value_V-(k-j)) + YUVmatrix[i][j + intput_value_V].V*(k-j)) / gap_v;
                    }
                }
                for (int j = weight - intput_value_V; j < weight; j++) {
                    YUVmatrix[i][j].V = YUVmatrix[i][weight - intput_value_V].V;
                }
            }
        }
    }

    public void ConvertYUVtoRGB() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double[] RGB = YUVtoRGB(YUVmatrix[i][j].Y, YUVmatrix[i][j].U, YUVmatrix[i][j].V);
                int r = (int) Math.max(0, Math.min(255, RGB[0]));
                int g = (int) Math.max(0, Math.min(255, RGB[1]));
                int b = (int) Math.max(0, Math.min(255, RGB[2]));
                int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                newImage.setRGB(j, i, pix);
            }
        }
    }

    public BufferedImage scalingImage(BufferedImage image) {
        if (isantialiasing == 0 && Sw == 1 && Sh == 1) {
            return image;
        }
        double dy= width/ scale_width;
        double dx= height/scale_height;
        BufferedImage newImage = new BufferedImage(scale_width, scale_height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < scale_height; i++) {
            for (int j = 0; j < scale_width; j++) {
                int ori_x=(int)(i*dx+dx/2);
                int ori_y=(int)(j*dy+dy/2);
                int color = image.getRGB(ori_y, ori_x);
                newImage.setRGB(j, i, color);
            }
        }
        if (isantialiasing == 1) {
            newImage = antiAliasImage(newImage);
        }
        return newImage;
    }

    public BufferedImage antiAliasImage(BufferedImage originalimage) {
        BufferedImage afterantiAlias = new BufferedImage(originalimage.getWidth(), originalimage.getHeight(), BufferedImage.TYPE_INT_RGB);
        int h = originalimage.getHeight() - 1;
        int w = originalimage.getWidth() - 1;
        for (int i = 1; i < h; i++) {
            for (int j = 1; j < w; j++) {
                int r = 0, g = 0, b = 0;
                for (int dx : dx) {
                    for (int dy : dy) {
                        int color = originalimage.getRGB(j + dy, i + dx);
                        r += (color >> 16) & 0xff;
                        g += (color >> 8) & 0xff;
                        b += (color) & 0xff;
                    }
                    int pix = 0xff000000 | ((r / 9 & 0xff) << 16) | ((g / 9 & 0xff) << 8) | (b / 9 & 0xff);

                    afterantiAlias.setRGB(j, i, pix);
                }
            }
        }
        return afterantiAlias;
    }

    public double[] YUVtoRGB(double Y, double U, double V) {
        double[] RGB = new double[3];
        RGB[0] = 1.000 * Y + 0.956 * U + 0.621 * V;
        RGB[1] = 1.000 * Y - 0.272 * U - 0.647 * V;
        RGB[2] = 1.000 * Y - 1.106 * U + 1.703 * V;
        return RGB;
    }

    public double[] RGBtoYUV(double R, double G, double B) {
        double[] YUV = new double[3];
        YUV[0] = 0.299 * R + 0.587 * G + 0.114 * B;
        YUV[1] = 0.596 * R - 0.274 * G - 0.322 * B;
        YUV[2] = 0.211 * R - 0.523 * G + 0.312 * B;
        return YUV;
    }

    public static void main(String[] args) {
        ImageDisplay ren = new ImageDisplay();
        ren.showIms(args);
    }

}
