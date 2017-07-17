import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {
    public MainWindow ()
    {
        this.setVisible(true);
        this.setSize(400,200);
        trainTheShit();
    }
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 30, 0, -30, 30, null);
        g2d.dispose();
        return dimg;
    }

    public static void trainTheShit(){
        File dir = new File("/Users/anselcorona/desktop/wekatrain");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(!child.toString().equalsIgnoreCase("/Users/anselcorona/desktop/wekatrain/.DS_Store")) {
                    System.out.println(child.toString());
                    getBinaryStringFromImage(child);
                    System.out.print("\n\n\n\n\n");
                }
            }
        }
    }
    public static void getBinaryStringFromImage(File imageFile)
    {
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        img = resize(img,30,30);
        byte[][] pixels = new byte[30][];
        byte[] second = new byte[900];
        for (int x = 0; x < 30; x++) {
            pixels[x] = new byte[30];
            System.out.println();
            for (int y = 0; y < 30; y++) {
                pixels[x][y] = (byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                second[x*y]=(byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                System.out.print(pixels[x][y]);
            }
        }/*
        for (Byte B:second) {
            System.out.print(B);
        }*/
        //return second.toString();
    }
}
