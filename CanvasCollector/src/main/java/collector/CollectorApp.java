package collector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CollectorApp extends Application
{
    Collector collector;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void init()
    {
        this.collector = Collector.get_collector();
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectorGUI.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("collector.Collector");
            primaryStage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        System.out.println("Called stop()");
        this.collector.terminate();
        System.out.println("Completed stop()");
    }
}
