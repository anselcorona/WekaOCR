import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.*;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * Created by anselcorona on 7/20/17.
 */
public class ProgramaPrinicipal {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JButton buscarButton;
    private JTextPane textPane1;
    private JTextField textField1;
    private JButton clasificarButton;
    private JFileChooser fc;
    private File toClassify;
    Classifier cls = null;
    private Instances datostesting = null;
    private static String testing = "testing/test.arff";


    public ProgramaPrinicipal() {

        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                fc.showOpenDialog(panel1);
                setToClassify(fc.getSelectedFile());
                textField1.setText(toClassify.getPath());
                pruebas(toClassify);
            }
        });
        clasificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cls == null) {
                    try {
                        cls = (Classifier) SerializationHelper.read("model/modelo2.model");
                        try {
                            ConverterUtils.DataSource testing = new ConverterUtils.DataSource("testing/test.arff");
                            Instances testDataset = testing.getDataSet();
                            testDataset.setClassIndex(testDataset.numAttributes() - 1);
                            Instance newisnt = testDataset.instance(0);
                            double predval = cls.classifyInstance(newisnt);
                            String clase = testDataset.classAttribute().value((int) predval);
                            textPane1.setText(clase);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                } else {
                    try {
                        ConverterUtils.DataSource testing = new ConverterUtils.DataSource("testing/test.arff");
                        Instances testDataset = testing.getDataSet();
                        testDataset.setClassIndex(testDataset.numAttributes() - 1);
                        Instance newisnt = testDataset.instance(0);
                        double predval = cls.classifyInstance(newisnt);
                        String clase = testDataset.classAttribute().value((int) predval);
                        textPane1.setText(clase);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        });
    }

    public static void main() {
        JFrame frame = new JFrame("ProgramaPrinicipal");
        frame.setContentPane(new ProgramaPrinicipal().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Classifier buildClassifier() throws Exception {
        ConverterUtils.DataSource training = new ConverterUtils.DataSource("training/train.arff");
        Instances trainingset = training.getDataSet();
        trainingset.setClassIndex(trainingset.numAttributes() - 1);
        Classifier cls = new SMO();
        cls.buildClassifier(trainingset);
        return cls;
    }

    public void setToClassify(File f) {
        this.toClassify = f;
    }

    private int[] byteArrayToIntArray(byte[][] matrizBytes, int ancho, int altura) {
        int[] arrEnteros = new int[altura * ancho];
        for (int i = 0; i < ancho; i++) {
            for (int j = 0; j < altura; j++) {
                arrEnteros[i * j] = matrizBytes[i][j];
            }
        }
        return arrEnteros;
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    private int[] getBinaryFromImage(File imageFile) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        img = resize(img, 60, 60);
        img = toGray(img);
        img = binarize(img);

        byte[][] pixels = new byte[img.getWidth()][];
        int[] second = new int[img.getWidth() * img.getHeight()];
        System.out.println(imageFile.toString());
        for (int x = 0; x < img.getWidth(); x++) {
            pixels[x] = new byte[img.getHeight()];
            for (int y = 0; y < img.getHeight(); y++) {
                pixels[x][y] = (byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                second[x * y] = (byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
            }
        }
        second = byteArrayToIntArray(pixels, img.getWidth(), img.getHeight());
        return second;
    }

    private static BufferedImage binarize(BufferedImage original) {
        int red;
        int newPixel;

        int threshold = otsuTreshold(original);

        BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels
                red = new Color(original.getRGB(i, j)).getRed();
                int alpha = new Color(original.getRGB(i, j)).getAlpha();
                if (red > threshold) {
                    newPixel = 255;
                } else {
                    newPixel = 0;
                }
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel);

            }
        }

        return binarized;
    }

    private static int[] imageHistogram(BufferedImage input) {

        int[] histogram = new int[256];

        for (int i = 0; i < histogram.length; i++) histogram[i] = 0;

        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                int red = new Color(input.getRGB(i, j)).getRed();
                histogram[red]++;
            }
        }

        return histogram;

    }

    private static int otsuTreshold(BufferedImage original) {

        int[] histogram = imageHistogram(original);
        int total = original.getHeight() * original.getWidth();

        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0, wF;

        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;

            if (wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;

    }

    private static int colorToRGB(int alpha, int red, int green, int blue) {
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }

    private static BufferedImage toGray(BufferedImage original) {

        int alpha, red, green, blue;
        int newPixel;

        BufferedImage lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels by R, G, B
                alpha = new Color(original.getRGB(i, j)).getAlpha();
                red = new Color(original.getRGB(i, j)).getRed();
                green = new Color(original.getRGB(i, j)).getGreen();
                blue = new Color(original.getRGB(i, j)).getBlue();

                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                // Return back to original format
                newPixel = colorToRGB(alpha, red, red, red);

                // Write pixels into image
                lum.setRGB(i, j, newPixel);

            }
        }

        return lum;

    }

    private void pruebas(File f) {
        generarArchivoWeka(getBinaryFromImage(f), testing);
        datostesting = cargarArchivoWeka(testing, 1);
        try {
            if (ImageIO.read(f) != null) {
                datostesting.add(addInstance(getBinaryFromImage(f), "zmi", datostesting));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveArchivoWeka(datostesting, testing);
    }


    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    private Instance addInstance(int[] arr, String clase, Instances data) {
        Instance instance = new DenseInstance(arr.length + 1);
        String[] alphabetMinuscula = {"ami", "bmi", "cmi", "dmi", "emi", "fmi", "gmi", "hmi", "imi", "jmi", "kmi", "lmi", "mmi", "nmi", "nnmi", "omi", "pmi", "qmi", "rmi", "smi", "tmi", "umi", "vmi", "wmi", "xmi", "ymi", "zmi"};
        String[] alphabetMayuscula = {"Ama", "Bma", "Cma", "Dma", "Ema", "Fma", "Gma", "Hma", "Ima", "Jma", "Kma", "Lma", "Mma", "Nma", "NNma", "Oma", "Pma", "Qma", "Rma", "Sma", "Tma", "Uma", "Vma", "Wma", "Xma", "Yma", "Zma"};
        String[] numeros = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        FastVector Clase = new FastVector(alphabetMayuscula.length + alphabetMinuscula.length + numeros.length);

        Clase.appendElements(Arrays.asList(alphabetMayuscula));
        Clase.appendElements(Arrays.asList(alphabetMinuscula));
        Clase.appendElements(Arrays.asList(numeros));

        instance.setDataset(data);

        for (int i = 0; i < arr.length; i++) {
            instance.setValue(i, arr[i]);
        }

        instance.setValue(arr.length, clase);
        return instance;
    }

    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    private static FastVector generarArchivoWeka(int[] arreglo, String path) {
        FastVector attributes = new FastVector(arreglo.length);
        FastVector attr = new FastVector();
        Attribute pixeles;
        attributes.add("0");
        attributes.add("1");

        for (int i = 0; i < arreglo.length; i++) {
            pixeles = new Attribute("P" + i, attributes);
            attr.add(pixeles);
        }

        String[] alphabetMinuscula = {"ami", "bmi", "cmi", "dmi", "emi", "fmi", "gmi", "hmi", "imi", "jmi", "kmi", "lmi", "mmi", "nmi", "nnmi", "omi", "pmi", "qmi", "rmi", "smi", "tmi", "umi", "vmi", "wmi", "xmi", "ymi", "zmi"};
        String[] alphabetMayuscula = {"Ama", "Bma", "Cma", "Dma", "Ema", "Fma", "Gma", "Hma", "Ima", "Jma", "Kma", "Lma", "Mma", "Nma", "NNma", "Oma", "Pma", "Qma", "Rma", "Sma", "Tma", "Uma", "Vma", "Wma", "Xma", "Yma", "Zma"};
        String[] numeros = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        FastVector clase = new FastVector(alphabetMayuscula.length + alphabetMinuscula.length + numeros.length);
        clase.appendElements(Arrays.asList(alphabetMayuscula));
        clase.appendElements(Arrays.asList(alphabetMinuscula));
        clase.appendElements(Arrays.asList(numeros));
        Attribute ClassAtribute = new Attribute("class", clase);
        attr.add(ClassAtribute);

        Instances datos = new Instances("test", attr, 0);

        System.out.println(datos.attribute(0));
        try {
            PrintWriter writer = new PrintWriter(path);
            writer.println(datos);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return attr;
    }

    private void saveArchivoWeka(Instances datos, String path) {
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(datos);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Instances cargarArchivoWeka(String path, int n) {
        Instances data = null;
        try {
            BufferedReader bf = new BufferedReader(new FileReader(path));
            try {
                ArffLoader.ArffReader arffrdr = new ArffLoader.ArffReader(bf, n);
                data = arffrdr.getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        return data;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Classify", panel2);
        final JLabel label1 = new JLabel();
        label1.setText("Imagen:");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel2.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        buscarButton = new JButton();
        buscarButton.setText("Buscar");
        panel2.add(buscarButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textPane1 = new JTextPane();
        textPane1.setText("");
        panel2.add(textPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        textField1 = new JTextField();
        panel2.add(textField1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        clasificarButton = new JButton();
        clasificarButton.setText("Clasificar");
        panel2.add(clasificarButton, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
