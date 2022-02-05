# Structure
* Collector.java: worker
* CollectorGUIController.java: GUI component event handling
* CollectorApp: base application of GUI
* Launcher.java: launcher class for build
* CollectorGUI.fxml: layout config of GUI
* CollectorTest: UT
* Build.gradle: config for build

## Collector
* Singleton: provides concurrent access for diff GUI events
* Http client need custom cookie conf
* Build one http client and reuse it
* Multithread with cached thread pool executor service
* Load config with snakeyaml
* Store config in LinkedHashMap
* Use URIBuilder to build URI from URI string then adding parameters
* Compose request object using built URI then add headers
* Parse result content stream as ArrayList
* Receive Consumer as callback for updating status label in GUI
* Non-blocking for download call, but one manager task blocks until all sub-tasks done then send result to Consumer
* Graceful terminate: await 5s then terminate all uncompleted jobs after GUI closed

## CollectorGUIController
* Implement `Initializable` to be able to initialize
* Inject component attributes annotated with `@FXML`
    * Need to use the same name for each component
* Show items in list view which hold an `ObservableList`
    * Each item is stored in a list cell
    * Cell factory of list view instances use cell class to instantiate cells for items
* Items are selected then moved between 2 lists
* `DirectoryChooser` chooses destination directory
* `FileChooser` chooses config file

## CollectorApp
* Loads layout from FXML
* Issues terminate signal to collector
	
## Build
* Javafx is excluded in jdk -> cannot build directly
* Build as jar
* `META-INF/MANIFEST.MF` is needed to indicate main class, don’t put under resources
* Add an extra launcher class instead of using Application class
* Copy fxml into jar, better to share same structure on source code and resources so in the end they are placed together
* When building In IDEA, use extract lib to pack everything as single jar, otherwise, class path is needed for each lib used
