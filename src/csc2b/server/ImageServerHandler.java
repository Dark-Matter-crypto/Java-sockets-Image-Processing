package csc2b.server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageServerHandler implements Runnable {
    private Socket clientConnection;

    private static PrintWriter pw = null;
    private static BufferedReader br = null;
    private static DataOutputStream out = null;
    private static DataInputStream in = null;

    private static final String IMAGES_LIST = "data/server/ImagesList.txt";

    //Constructor
    public ImageServerHandler(Socket clientConnection){
        this.clientConnection = clientConnection;

        try {
            pw = new PrintWriter(clientConnection.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(clientConnection.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(clientConnection.getInputStream()));

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Server ready to process images.");

        Pattern dataCommand = Pattern.compile("^DATA$");
        Pattern getCommand = Pattern.compile("^IMGRET\\s<(\\d+)>$");
        Pattern sendCommand = Pattern.compile("^IMGSEND\\s<(\\d+)>\\s<(\\w+)>\\s<Image>$");

        try{
            while(true){
                String command = br.readLine();
                System.out.println("Command: " + command);

                Matcher dataMatcher = dataCommand.matcher(command);
                Matcher getMatcher = getCommand.matcher(command);
                Matcher sendMatcher = sendCommand.matcher(command);

                if (dataMatcher.matches()){
                    File imagesList = new File(IMAGES_LIST);

                    if (imagesList.exists()){
                        Scanner txtin = null;

                        try{
                            txtin = new Scanner(imagesList);
                            String list = "";
                            while (txtin.hasNext()){
                                String entry = txtin.nextLine();
                                list += entry + "@";
                            }
                            pw.println(list);
                            pw.flush();

                        }catch(FileNotFoundException ex){
                            ex.printStackTrace();
                        }
                        finally {
                            if (txtin != null){
                                txtin.close();
                            }
                        }
                    }
                }
                else if (getMatcher.matches()){
                    File imagesList = new File(IMAGES_LIST);
                    String imageName = null;

                    if (imagesList.exists()){
                        Scanner txtin = null;

                        try{
                            System.out.println(1);
                            txtin = new Scanner(imagesList);
                            while (txtin.hasNextLine()){
                                System.out.println(2);
                                String line = txtin.nextLine();
                                System.out.println(line);
                                StringTokenizer fileTokens = new StringTokenizer(line);
                                if (fileTokens.nextToken().equals((String)getMatcher.group(1))){
                                    imageName = fileTokens.nextToken();
                                    System.out.println(imageName);
                                    break;
                                }
                            }
                        }catch(FileNotFoundException ex){
                            ex.printStackTrace();
                        }
                        finally {
                            if (txtin != null){
                                txtin.close();
                            }
                        }

                        File imageFile = new File("./data/server/" + imageName);
                        int fileSize = (int) imageFile.length();

                        if (imageFile.exists()){
                            //Send the file name
                            pw.println(imageName);
                            pw.flush();
                            pw.println(fileSize);
                            pw.flush();

                            //Send the binary file
                            FileInputStream fs = new FileInputStream(imageFile);
                            byte[] buffer = new byte[2048];
                            int n = 0;

                            while((n = fs.read(buffer)) > 0){
                                out.write(buffer, 0, n);
                                out.flush();
                            }

                            fs.close();
                            System.out.println("File sent to client");
                        }

                    }

                }
                else if (sendMatcher.matches()){

                    String imageName = sendMatcher.group(2);
                    File imageFile = new File("data/server/" + imageName + ".jpg");
                    int fileSize = Integer.parseInt(br.readLine());

                    FileOutputStream fos = new FileOutputStream(imageFile);

                    try{
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
                        System.out.println("File received from client.");

                        if (imageFile.exists()){
                            FileWriter filesList = new FileWriter(IMAGES_LIST, true);
                            BufferedWriter bw = new BufferedWriter(filesList);
                            PrintWriter txtout = new PrintWriter(bw);

                            try{
                                txtout = new PrintWriter(filesList);
                                txtout.println(sendMatcher.group(1) + " " + sendMatcher.group(2) + ".jpg\n");
                                txtout.flush();
                                pw.println("Image file received.");
                                pw.flush();
                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                            }
                            finally {
                                if(txtout != null)
                                    txtout.close();
                                try {
                                    if(bw != null)
                                        bw.flush();
                                    bw.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    if(filesList != null)
                                        filesList.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                    catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
                else {
                    System.out.println("Command not recognised.");
                    br.reset();
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
