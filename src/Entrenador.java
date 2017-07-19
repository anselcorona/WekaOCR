import weka.core.FastVector;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

import weka.core.*;
import weka.core.converters.ArffLoader;

public class Entrenador {
    private Instances datos = null;
    private static String training =  "training/test.arff";
    public Entrenador(){
        entrenamiento();
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public void entrenamiento(){
        File dir = new File("/Users/anselcorona/desktop/wekatrain");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int i=0, indice=0;
            for (File child : directoryListing) {
                if(i>0) {
                    System.out.println("Imagen del indice "+ indice + "\n");
                    if(indice ==0){
                        generarArchivoWeka(getBinaryFromImage(child));
                        datos = cargarArchivoWeka(training, directoryListing.length);
                    }
                    try{
                        if(ImageIO.read(child)!=null){
                            indice++;
                            System.out.println("class "+getClass(child));
                            datos.add(addInstance(getBinaryFromImage(child), getClass(child), datos));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                i++;
            }
            saveArchivoWeka(datos);
        }
    }
    @SuppressWarnings({"rawtypes", "deprecation", "unchecked"})
    public Instance addInstance(int[ ] arr, String clase, Instances data){
        Instance instance = new DenseInstance(arr.length+1);
        String[] alphabetMinuscula = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n","ene", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        String[] alphabetMayuscula = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N","ENE" , "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
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
    public static FastVector generarArchivoWeka(int[] arreglo){
        FastVector attributes = new FastVector(arreglo.length);
        FastVector attr= new FastVector();
        Attribute pixeles;
        attributes.add("0");
        attributes.add("1");

        for(int i =0; i< arreglo.length; i++){
            pixeles = new Attribute("P"+i, attributes);
            attr.add(pixeles);
        }

        String[] alphabetMinuscula = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n","nn", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        String[] alphabetMayuscula = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N","NN" , "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        FastVector clase = new FastVector(alphabetMayuscula.length+alphabetMinuscula.length);
        clase.appendElements(Arrays.asList(alphabetMayuscula));
        clase.appendElements(Arrays.asList(alphabetMinuscula));
        Attribute ClassAtribute = new Attribute("class", clase);

        attr.add(ClassAtribute);

        Instances datos = new Instances("Objeto de Instancias", attr, 0);

        System.out.println(datos.attribute(0));

        try{
            PrintWriter writer = new PrintWriter(training);
            writer.println(datos);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return attr;
    }

    public int[] getBinaryFromImage(File imageFile)
    {
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {

        }
        img = resize(img,60,60);

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

    public void saveArchivoWeka(Instances datos){
        try{
            PrintWriter writer = new PrintWriter(training, "UTF-8");
            writer.println(datos);
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public int[] byteArrayToIntArray(byte[][] matrizBytes, int ancho, int altura){
        int[] arrEnteros = new int[altura*ancho];
        for(int i=0;i<ancho; i++){
            for(int j=0; j<altura; j++){
                arrEnteros[i*j] = matrizBytes[i][j];
            }
        }
        return arrEnteros;
    }
    public Instances cargarArchivoWeka(String path, int n){
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

    public String getClass(File f){
        //TODO: Modificar para que coja numeros, un algoritmo diferente de captar la clase es necesario también.
        String clase;
        System.out.println(f.getName());

        if(f.getName().substring(0, 3 ).equals("NN") )
        {
            clase = "NN";
            System.out.println(clase);
        }
        else if(f.getName().substring(0, 3).equals("nn") )
        {
            clase = "nn";
            System.out.println(clase);
        }
        else
        {
            clase = f.getName().substring(0,1);
            System.out.println(clase);
        }
        return clase;
    }
}