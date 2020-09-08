package csc2b.gui;

import csc2b.client.ImageClient;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.StringTokenizer;

public class ImagePane extends StackPane {
    private String fileName = null;
    private String fileSize = null;
    private String fileID = null;

    public ImagePane(){

        //Create layout nodes
        TextField idField1 = new TextField("");
        idField1.setPrefWidth(100);
        TextField fileName = new TextField("");
        fileName.setEditable(false);
        fileName.setPrefWidth(180);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./data/client"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        Button selectFile = new Button("Select Image");
        TextArea messageForm = new TextArea("");
        messageForm.setEditable(false);
        ImageView imageView = new ImageView();
        imageView.setFitHeight(320);
        imageView.setFitWidth(320);
        imageView.setPreserveRatio(true);
        Label id1 = new Label("Image ID: ");
        id1.setPadding(new Insets(0, 10, 0, 0));
        Label id3 = new Label("  ");
        id3.setPadding(new Insets(0, 10, 0, 0));
        Label id4 = new Label("  ");
        id4.setPadding(new Insets(0, 10, 0, 0));
        Button showFiles = new Button("Show List");
        Button sendFile = new Button("Upload Image");
        Button getFile = new Button("Download Image");

        //Connect client to server
        ImageClient clientConnection = new ImageClient(7455);

        if (clientConnection.isClientConnected())
            messageForm.appendText("Connected to the image server.\r\n");
        else{
            messageForm.appendText("Failed to connect to the image server.\r\n");
        }

        //Show images list
        showFiles.setOnAction((ActionEvent e) -> {
            String message = clientConnection.readImageList();
            StringTokenizer messageTokens = new StringTokenizer(message, "@");
            messageForm.clear();
            while (messageTokens.hasMoreTokens()){
                messageForm.appendText(messageTokens.nextToken() + "\r\n");
            }
        });


        //Upload Image
        sendFile.setOnAction((ActionEvent e) -> {
            StringTokenizer fileTokens = new StringTokenizer(fileName.getText(), ".");
            String imageName = fileTokens.nextToken();
            String imageExtension = fileTokens.nextToken();
            String message = clientConnection.uploadImage(imageName, imageExtension);

            System.out.println(message);
            messageForm.appendText(message);
        });

        //Choose image
        selectFile.setOnAction((ActionEvent e) -> {
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                fileName.setText(selectedFile.getName());
            }
        });

        //Download image
        getFile.setOnAction((ActionEvent e) -> {
            System.out.println(idField1.getText());
            String message = clientConnection.downloadImage(Integer.parseInt(idField1.getText()));
            System.out.println(message);
            messageForm.appendText(message);

            StringTokenizer messageTokens = new StringTokenizer(message);
            String imageName = messageTokens.nextToken();
            System.out.println(imageName);

            Image image = new Image("file:data/client/" + imageName);
            imageView.setImage(image);

        });

        //Add nodes to pane
        HBox upBox = new HBox();
        upBox.setPadding(new Insets(50, 10, 10, 30));
        upBox.getChildren().addAll(selectFile, fileName, id3, sendFile);

        HBox downBox = new HBox();
        downBox.setPadding(new Insets(10, 10, 10, 30));
        downBox.getChildren().addAll(id1, idField1, id4, getFile);

        HBox showBox = new HBox();
        showBox.setPadding(new Insets(10, 10, 5, 30));
        showBox.getChildren().add(showFiles);

        BorderPane composer = new BorderPane();
        composer.setPadding(new Insets(15, 10, 0, 10));
        composer.setCenter(messageForm);

        HBox imageBox = new HBox();
        imageBox.setPadding(new Insets(0, 10, 5, 10));
        imageBox.getChildren().add(imageView);


        VBox vBox = new VBox();
        vBox.getChildren().addAll(upBox, downBox, showBox, composer, imageBox);

        BorderPane body = new BorderPane();
        body.setCenter(vBox);

        //Set the root node of the Scene
        getChildren().clear();
        getChildren().addAll(body);
    }
}
