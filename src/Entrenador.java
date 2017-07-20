import weka.core.FastVector;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

import weka.core.*;
import weka.core.converters.ArffLoader;

public class Entrenador {
    private Instances datostraining = null;
    private Instances datostesting = null;
    private static String training =  "training/train.arff";
    private static String testing = "testing/test.arff";
    public Entrenador(){
        //entrenamiento();
        pruebas();
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    private void entrenamiento(){
        File dir = new File("entrenamiento");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int i=0, indice=0;
            for (File child : directoryListing) {
                if(i>0) {
                    System.out.println("Imagen del indice "+ indice + "\n");
                    if(indice ==0){
                        generarArchivoWeka(getBinaryFromImage(child), training);
                        datostraining = cargarArchivoWeka(training, directoryListing.length);
                    }
                    try{
                        if(ImageIO.read(child)!=null){
                            indice++;
                            System.out.println("class "+getClass(child));
                            datostraining.add(addInstance(getBinaryFromImage(child), getClass(child), datostraining));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                i++;
            }
            saveArchivoWeka(datostraining, training);
        }
    }
    private void pruebas(){
        File dir = new File("pruebas");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int i=0, indice=0;
            for (File child : directoryListing) {
                if(i>0) {
                    System.out.println("Imagen del indice "+ indice + "\n");
                    if(indice ==0){
                        generarArchivoWeka(getBinaryFromImage(child), testing);
                        datostesting = cargarArchivoWeka(testing, directoryListing.length);
                    }
                    try{
                        if(ImageIO.read(child)!=null){
                            indice++;
                            System.out.println("class "+getClass(child));
                            datostesting.add(addInstance(getBinaryFromImage(child), getClass(child), datostesting));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                i++;
            }
            saveArchivoWeka(datostesting, testing);
        }
    }
    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    private Instance addInstance(int[ ] arr, String clase, Instances data){
        Instance instance = new DenseInstance(arr.length+1);
        String[] alphabetMinuscula = { "ami", "bmi", "cmi", "dmi", "emi", "fmi", "gmi", "hmi", "imi", "jmi", "kmi", "lmi", "mmi", "nmi","nnmi", "omi", "pmi", "qmi", "rmi", "smi", "tmi", "umi", "vmi", "wmi", "xmi", "ymi", "zmi"};
        String[] alphabetMayuscula = {"Ama", "Bma", "Cma", "Dma", "Ema", "Fma", "Gma", "Hma", "Ima", "Jma", "Kma", "Lma", "Mma", "Nma","NNma" , "Oma", "Pma", "Qma", "Rma", "Sma", "Tma", "Uma", "Vma", "Wma", "Xma", "Yma", "Zma"};
        String[] numeros = {"0","1","2","3","4","5","6","7","8","9"};
        FastVector Clase = new FastVector(alphabetMayuscula.length+alphabetMinuscula.length+numeros.length);

        Clase.appendElements(Arrays.asList(alphabetMayuscula));
        Clase.appendElements(Arrays.asList(alphabetMinuscula));
        Clase.appendElements(Arrays.asList(numeros));

        instance.setDataset(data);

        for(int i=0; i< arr.length; i++){
            instance.setValue(i, arr[i]);
        }

        instance.setValue(arr.length, clase);
        return instance;
    }

    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    private static FastVector generarArchivoWeka(int[] arreglo, String path){
        FastVector attributes = new FastVector(arreglo.length);
        FastVector attr= new FastVector();
        Attribute pixeles;
        attributes.add("0");
        attributes.add("1");

        for(int i =0; i< arreglo.length; i++){
            pixeles = new Attribute("P"+i, attributes);
            attr.add(pixeles);
        }

        String[] alphabetMinuscula = { "ami", "bmi", "cmi", "dmi", "emi", "fmi", "gmi", "hmi", "imi", "jmi", "kmi", "lmi", "mmi", "nmi","nnmi", "omi", "pmi", "qmi", "rmi", "smi", "tmi", "umi", "vmi", "wmi", "xmi", "ymi", "zmi"};
        String[] alphabetMayuscula = {"Ama", "Bma", "Cma", "Dma", "Ema", "Fma", "Gma", "Hma", "Ima", "Jma", "Kma", "Lma", "Mma", "Nma","NNma" , "Oma", "Pma", "Qma", "Rma", "Sma", "Tma", "Uma", "Vma", "Wma", "Xma", "Yma", "Zma"};
        String[] numeros = {"0","1","2","3","4","5","6","7","8","9"};
        FastVector clase = new FastVector(alphabetMayuscula.length+alphabetMinuscula.length+numeros.length);
        clase.appendElements(Arrays.asList(alphabetMayuscula));
        clase.appendElements(Arrays.asList(alphabetMinuscula));
        clase.appendElements(Arrays.asList(numeros));
        Attribute ClassAtribute = new Attribute("class", clase);
        attr.add(ClassAtribute);

        Instances datos = new Instances("test", attr, 0);

        System.out.println(datos.attribute(0));
        try{
            PrintWriter writer = new PrintWriter(path);
            writer.println(datos);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return attr;
    }

    private int[] getBinaryFromImage(File imageFile)
    {
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        img = resize(img,60,60);
        img = toGray(img);
        img = binarize(img);

        byte[][] pixels = new byte[img.getWidth()][];
        int[] second = new int[img.getWidth()*img.getHeight()];
        System.out.println(imageFile.toString());
        for (int x = 0; x < img.getWidth(); x++) {
            pixels[x] = new byte[img.getHeight()];
            for (int y = 0; y < img.getHeight(); y++) {
                pixels[x][y] = (byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                second[x*y]=(byte) (img.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                System.out.print(pixels[x][y]);
            }
            System.out.println();
        }
        second = byteArrayToIntArray(pixels, img.getWidth(), img.getHeight());
        return second;
    }
    private static BufferedImage binarize(BufferedImage original) {
        int red;
        int newPixel;

        int threshold = otsuTreshold(original);

        BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {

                // Get pixels
                red = new Color(original.getRGB(i, j)).getRed();
                int alpha = new Color(original.getRGB(i, j)).getAlpha();
                if(red > threshold) {
                    newPixel = 255;
                }
                else {
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

        for(int i=0; i<histogram.length; i++) histogram[i] = 0;

        for(int i=0; i<input.getWidth(); i++) {
            for(int j=0; j<input.getHeight(); j++) {
                int red = new Color(input.getRGB (i, j)).getRed();
                histogram[red]++;
            }
        }

        return histogram;

    }
    private static int otsuTreshold(BufferedImage original) {

        int[] histogram = imageHistogram(original);
        int total = original.getHeight() * original.getWidth();

        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0,wF;

        float varMax = 0;
        int threshold = 0;

        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;

            if(wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if(varBetween > varMax) {
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

        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {

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

    private void saveArchivoWeka(Instances datos, String path){
        try{
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(datos);
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private int[] byteArrayToIntArray(byte[][] matrizBytes, int ancho, int altura){
        int[] arrEnteros = new int[altura*ancho];
        for(int i=0;i<ancho; i++){
            for(int j=0; j<altura; j++){
                arrEnteros[i*j] = matrizBytes[i][j];
            }
        }
        return arrEnteros;
    }
    private Instances cargarArchivoWeka(String path, int n){
        Instances data = null;
        try{
                BufferedReader bf = new BufferedReader(new FileReader(path));
                try{
                    ArffLoader.ArffReader arffrdr = new ArffLoader.ArffReader(bf,n);
                    data = arffrdr.getData();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }catch (FileNotFoundException ex){
                ex.printStackTrace();
            }

        return data;
    }

    private String getClass(File f){
        //TODO: Modificar para que coja numeros, un algoritmo diferente de captar la clase es necesario también.

        String clase;
        System.out.println(f.getName());
        clase = f.getName().substring(0,1);
        if(f.getName().substring(0, 2).equalsIgnoreCase("nn")){
            char maomi = f.getName().charAt(3);
            if(maomi=='a'){
                clase = "NNma";
            }else {
                clase = "nnmi";
            }
        }
        else if((clase.charAt(0)>='a' && clase.charAt(0)<='z') || (clase.charAt(0)>='A' && clase.charAt(0)<='Z')){
            char maomi = f.getName().charAt(2);
            if(maomi=='a'){
                clase = clase.toUpperCase();
                clase = clase + "ma";
            }else{
                clase = clase + "mi";
            }
        }

        return clase;
    }

}