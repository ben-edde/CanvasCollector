import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;

public class CollectorGUI extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
//        CanvasCollector client=new CanvasCollector();
        ObservableList<Map<String, Object>> elementList = FXCollections.observableArrayList();
        ArrayList<Map<String, Object>> results = CanvasCollector.get_data("courses");
        elementList.addAll(results);
        StackPane pane = new StackPane();
        Scene scene = new Scene(pane, 300, 150);
        primaryStage.setScene(scene);

        ListView<Map<String, Object>> lv = new ListView<>(elementList);
        lv.setCellFactory(param -> new ElementCell());
        pane.getChildren().add(lv);

        primaryStage.show();
    }

    static class ElementCell extends ListCell<Map<String, Object>>
    {
        Map<String, Object> element;
        HBox hbox = new HBox();
        Label label = new Label("");
        Pane pane = new Pane();
        Button button = new Button("Select");

        public ElementCell()
        {
            super();

            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);

            button.setOnAction(event -> button_handler());
        }

        //TODO: download selected items here
        void button_handler()
        {
            label.setText(Integer.toString((Integer) element.get("id")));
        }

        @Override
        protected void updateItem(Map<String, Object> item, boolean empty)
        {
            super.updateItem(item, empty);
            element = item;
            setText(null);
            setGraphic(null);

            if (item != null && !empty)
            {
                label.setText((String) item.get("name"));
                setGraphic(hbox);
            }
        }
    }
}
