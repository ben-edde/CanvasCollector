import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CollectorGUIController implements Initializable
{
    File selectedDirectory;
    Collector collector;
    ObservableList<Map<String, Object>> availableItemsList, selectedItemsList;
    @FXML
    private ListView availableListView;
    @FXML
    private ListView selectedListView;
    @FXML
    private TextField selectedDirectoryText;
    @FXML
    private CheckBox optionSearchAll;
    @FXML
    private Label statusLabel;

    public CollectorGUIController()
    {
        this.availableItemsList = FXCollections.observableArrayList();
        this.selectedItemsList = FXCollections.observableArrayList();
        this.collector = Collector.get_collector();
    }

    @FXML
    void fetch_items()
    {
        this.availableItemsList = FXCollections.observableArrayList(
                (collector.get_data("courses", optionSearchAll.isSelected())));
        this.selectedItemsList = FXCollections.observableArrayList();
        update_list_view();
        this.statusLabel.setText("OK");
    }

    @FXML
    void download_selected_items()
    {
        Consumer<Boolean> update_status = (Boolean jobDone) ->
        {
            if (jobDone)
            {this.statusLabel.setText("Download completed.");}
            else
            {this.statusLabel.setText("Download cancelled.");}
        };
        if (this.selectedDirectory == null) return;
        this.statusLabel.setText("Downloading...");
        collector.download_selected_course_files(this.selectedDirectory.toString(), this.selectedItemsList,
                update_status);
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
        String cfgDestDir = this.collector.get_dest_dir();
        if (cfgDestDir != null)
        {
            this.selectedDirectoryText.setText(cfgDestDir);
            this.selectedDirectory = new File(cfgDestDir);
        }
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
