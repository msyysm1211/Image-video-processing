
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;


public class test {

    JFrame frame;
    JLabel lbIm1;
    JLabel lbIm2;
    BufferedImage img;
    BufferedImage compressedImg;
    int width = 1920;
    int height = 1080;
    int numOfDiagonals;
    int[][] rVals = new int[height][width];
    int[][] gVals = new int[height][width];
    int[][] bVals = new int[height][width];
    double[][] yVals = new double[height][width];
    double[][] prVals = new double[height][width];
    double[][] pbVals = new double[height][width];
    double[][] block = new double[8][8];
    double[][] dctBlock = new double[8][8];

    public void enforceDiagonals(double[][] dctBlock){

        for(int i = 0;i<8;i++){
            for(int j = 0;j<8;j++){
                if((i+j)>=numOfDiagonals)
                    dctBlock[i][j] = 0.0;
            }
        }

    }

    public void dctTransformAndInverse(double[][] block){
        double cu, cv;

        //DCT Transform
        for(int u=0;u<8;u++){
            for(int v=0;v<8;v++){
                dctBlock[u][v] = 0;
                cu = 1;
                cv = 1;
                if(u==0)
                    cu = 1/Math.sqrt(2);
                if(v==0)
                    cv = 1/Math.sqrt(2);
                for(int x = 0;x<8;x++){
                    for(int y = 0;y<8;y++){
                        dctBlock[u][v] += (block[x][y]*Math.cos(((2*x+1)*u*Math.PI)/16)*Math.cos(((2*y+1)*v*Math.PI)/16));
                    }
                }
                dctBlock[u][v] = (0.25*cu*cv*dctBlock[u][v]);
            }
        }

        enforceDiagonals(dctBlock);

        //Inverse DCT Transform
        for(int x=0;x<8;x++){
            for(int y=0;y<8;y++){
                block[x][y] = 0;
                for(int u=0;u<8;u++){
                    for(int v=0;v<8;v++){
                        cu = 1;
                        cv = 1;
                        if(u==0)
                            cu = 1/Math.sqrt(2);
                        if(v==0)
                            cv = 1/Math.sqrt(2);
                        block[x][y] += (cu*cv*dctBlock[u][v]*Math.cos(((2*x+1)*u*Math.PI)/16)*Math.cos(((2*y+1)*v*Math.PI)/16));
                    }
                }
                block[x][y] = (0.25*block[x][y]);
            }
        }
    }

    public void blockBasedDCT(double[][] channel){
        int row, col;

        //Code to segment channel matrix into blocks
        for(int i = 0;i<(height/8);i++){
            for(int j = 0;j<(width/8);j++){
                row = -1;
                for(int y = i*8; y<(i*8+8); y++){
                    row++;
                    col = 0;
                    for(int x = j*8;x<(j*8+8);x++){
                        block[row][col] = channel[y][x];
                        col++;
                    }
                }
                dctTransformAndInverse(block);
                row = -1;
                for(int y = i*8; y<(i*8+8); y++){
                    row++;
                    col = 0;
                    for(int x = j*8;x<(j*8+8);x++){
                        channel[y][x] = block[row][col];
                        col++;
                    }
                }
            }
        }
    }

    public void showIms(String[] args){
        numOfDiagonals = Integer.parseInt(args[1]);

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        compressedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            File file = new File(args[0]);
            InputStream is = new FileInputStream(file);

            long len = file.length();
            byte[] bytes = new byte[(int)len];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }


            int ind = 0;
            for(int y = 0; y < height; y++){

                for(int x = 0; x < width; x++){

                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x,y,pix);

                    int rgb = img.getRGB(x,y);

                    rVals[y][x] =(rgb>>16) & 0xff;
                    gVals[y][x] =(rgb>>8) & 0xff;
                    bVals[y][x] =(rgb) & 0xff;

                    ind++;
                }
            }

            //Convert to YPrPb space
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    yVals[y][x] = (0.299*(double)rVals[y][x] + 0.587*(double)gVals[y][x] + 0.114*(double)bVals[y][x]);
                    pbVals[y][x] = (-0.169*(double)rVals[y][x] - 0.331*(double)gVals[y][x] + 0.5*(double)bVals[y][x]);
                    prVals[y][x] = (0.5*(double)rVals[y][x] - 0.419*(double)gVals[y][x] - 0.081*(double)bVals[y][x]);
                }
            }

            //Breaking channels into 8x8 blocks and applying DCT
            blockBasedDCT(yVals);
            blockBasedDCT(pbVals);
            blockBasedDCT(prVals);

            //Creating compressed image
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    rVals[y][x] = (int) Math.round(yVals[y][x] + 1.402*prVals[y][x]);
                    gVals[y][x] = (int) Math.round(yVals[y][x] - 0.344*pbVals[y][x] - 0.714*prVals[y][x]);
                    bVals[y][x] = (int) Math.round(yVals[y][x] + 1.772*pbVals[y][x]);

                    if(rVals[y][x]<0)
                        rVals[y][x] = 0;
                    else if(rVals[y][x]>255)
                        rVals[y][x] = 255;

                    if(gVals[y][x]<0)
                        gVals[y][x] = 0;
                    else if(gVals[y][x]>255)
                        gVals[y][x] = 255;

                    if(bVals[y][x]<0)
                        bVals[y][x] = 0;
                    else if(bVals[y][x]>255)
                        bVals[y][x] = 255;
                    int rgb = (rVals[y][x]<<16) + (gVals[y][x]<<8) + bVals[y][x];
                    compressedImg.setRGB(x,y,rgb);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Use labels to display the images
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        JLabel lbText1 = new JLabel("Original image (Left)");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbText2 = new JLabel("Image after modification (Right)");
        lbText2.setHorizontalAlignment(SwingConstants.CENTER);
        lbIm1 = new JLabel(new ImageIcon(img));
        lbIm2 = new JLabel(new ImageIcon(compressedImg));

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

    public static void main(String[] args) {
        test ren = new test();
        ren.showIms(args);
    }

}