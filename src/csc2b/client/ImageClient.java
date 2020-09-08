package csc2b.client;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class ImageClient {
    private static PrintWriter pw = null;
    private static BufferedReader br = null;
    private static DataOutputStream out = null;
    private static DataInputStream in = null;
    private static int port;

    private static boolean connected = false;

    //Constructor
    public ImageClient(int port) {
        this.port = port;
        try {
            Socket severConnection = new Socket("localhost", port);

            pw = new PrintWriter(severConnection.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(severConnection.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(severConnection.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(severConnection.getInputStream()));

            System.out.println("Connected to the file server.");
            connected = true;

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method to read response from the server
    private static String readServerResponse(BufferedReader br){
        String res = null;
        try {
            res = br.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    //Method to write to the server
    private static void writeMessage(PrintWriter out, String message){
        out.println(message);
        out.flush();
    }

    public String uploadImage(String imageName, String imageExtension){
        String clientResponse = null;
        int ID = 1;

        String imageList = readImageList();
        StringTokenizer imageListTokens = new StringTokenizer(imageList, "@");
        while (imageListTokens.hasMoreTokens()){
            ID += 1;
            String entry = imageListTokens.nextToken();
        }

        pw.println("IMGSEND <" + ID + "> <" + imageName + "> <Image>");
        pw.flush();

        File imageFile = new File("./data/client/" + imageName + "." + imageExtension);
        int fileSize = (int) imageFile.length();
        System.out.println(fileSize);
        pw.println(fileSize);
        pw.flush();

        if (imageFile.exists()){

            try{
                //Send the image
                FileInputStream fs = new FileInputStream(imageFile);
                byte[] buffer = new byte[2048];
                int n = 0;

                while((n = fs.read(buffer)) > 0){
                    out.write(buffer, 0, n);
                    out.flush();
                }

                fs.close();
                System.out.println("File sent to server.");
                clientResponse = br.readLine();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return clientResponse;
    }

    public String readImageList(){
        pw.println("DATA");
        pw.flush();

        String serverResponse = null;
        try {
            serverResponse = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverResponse;
    }

    public String downloadImage(int fileID){
        pw.println("IMGRET <" + fileID + ">");
        pw.flush();

        String severResponse = null;

        FileOutputStream fos = null;
        try{
            String fileName = br.readLine();
            int fileSize = Integer.parseInt(br.readLine());
            System.out.println(fileSize);

            File downloadFile = new File("data/client/" + fileName);

            fos = new FileOutputStream(downloadFile);
            byte[] buffer = new byte[2048];
            int n = 0;
            int totalBytes = 0;

            while(totalBytes != fileSize)
            {
                n = in.read(buffer,0, buffer.length);
                fos.write(buffer,0,n);
                fos.flush();
                totalBytes += n;
            }

            fos.close();

            severResponse = fileName + " downloaded successfully.\n";
            System.out.println("File downloaded to the client directory.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return severResponse;
    }

    public boolean isClientConnected(){
        return connected;
    }
}
