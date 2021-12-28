import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CollectorGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        CanvasCollector client=new CanvasCollector();
        String result=CanvasCollector.ping();
        Label label=new Label(result);
        Scene scene=new Scene(label);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
