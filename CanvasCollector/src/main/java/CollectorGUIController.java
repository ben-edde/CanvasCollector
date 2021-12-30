import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class CollectorGUIController implements Initializable
{
    @FXML
    public ListView availableListView;
    @FXML
    public ListView selectedListView;
    @FXML
    public TextField selectedDirectoryText;
    File selectedDirectory;
    ObservableList<Map<String, Object>> availableItemsList, selectedItemsList;

    public CollectorGUIController()
    {
        this.availableItemsList = FXCollections.observableArrayList();
        this.selectedItemsList = FXCollections.observableArrayList();
    }

    @FXML
    void fetch_items()
    {
        this.availableItemsList = FXCollections.observableArrayList((Collector.get_data("courses")));
        this.selectedItemsList = FXCollections.observableArrayList();
        update_list_view();
    }

    void update_list_view()
    {
        this.availableListView.setItems(this.availableItemsList);
        this.selectedListView.setItems(this.selectedItemsList);
    }

    @FXML
    void add_selected_item()
    {
        ObservableList<Map<String, Object>> selectedItems =
                this.availableListView.getSelectionModel().getSelectedItems();
        this.selectedItemsList.addAll(selectedItems);
        this.availableItemsList.removeAll(selectedItems);
        update_list_view();
    }

    @FXML
    void remove_selected_item()
    {
        ObservableList<Map<String, Object>> selectedItems =
                this.selectedListView.getSelectionModel().getSelectedItems();
        this.availableItemsList.addAll(selectedItems);
        this.selectedItemsList.removeAll(selectedItems);
        update_list_view();
    }

    @FXML
    void choose_directory()
    {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null)
        {
            selectedDirectoryText.setText(selectedDirectory.toString());
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.availableListView.setCellFactory(param -> new ElementCell());
        this.selectedListView.setCellFactory(param -> new ElementCell());
        fetch_items();
    }

    static class ElementCell extends ListCell<Map<String, Object>>
    {
        Map<String, Object> element;
        HBox hbox = new HBox();
        Label label = new Label("");
        Pane pane = new Pane();

        public ElementCell()
        {
            super();
            hbox.getChildren().addAll(label, pane);
            HBox.setHgrow(pane, Priority.ALWAYS);
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
