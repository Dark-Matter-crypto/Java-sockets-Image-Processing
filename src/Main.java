import csc2b.gui.ImagePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Present scene to stage
        primaryStage.setTitle("Astrophysics");
        ImagePane layout = new ImagePane();
        Scene scene = new Scene(layout, 550, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
