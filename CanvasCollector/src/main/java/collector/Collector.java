package collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Collector
{
    private static Collector collector_instance;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final HttpClientBuilder clientBuilder;
    private final CloseableHttpClient httpClient;
    private final ExecutorService executor;
    private LinkedHashMap<String, String> cfgMap;

    private Collector()
    {
        this.connectionManager = new PoolingHttpClientConnectionManager();
        this.clientBuilder = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(
                RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());
        this.httpClient = clientBuilder.build();
        this.executor = Executors.newCachedThreadPool();
    }

    public static Collector get_collector()
    {
        if (Collector.collector_instance == null) Collector.collector_instance = new Collector();
        return Collector.collector_instance;
    }

    private static LinkedHashMap<String, String> load_config(String cfg_filepath)
    {
        LinkedHashMap<String, String> cfg_map = null;
        try
        {
            LoadSettings settings = LoadSettings.builder().setLabel("Custom user configuration").build();
            Load loader = new Load(settings);
            FileReader cfg_file = new FileReader(cfg_filepath);
            cfg_map = (LinkedHashMap<String, String>) loader.loadFromReader(cfg_file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return cfg_map;
    }

    public void set_config(String cfg_filepath)
    {
        this.cfgMap = load_config(cfg_filepath);
    }

    public ExecutorService get_executor()
    {
        return executor;
    }

    public String get_dest_dir()
    {
        if (this.cfgMap == null) return null;
        return cfgMap.get("DESTINATION");
    }

    public ArrayList<Map<String, Object>> get_data(String resource, Boolean searchAll)
    {
        if (cfgMap == null) return null;

        try
        {
            URIBuilder uriBuilder = new URIBuilder(cfgMap.get("API_ENDPOINT") + "/" + resource);
            uriBuilder.addParameter("per_page", "32767");
            if (!searchAll && resource.equals("courses")) uriBuilder.addParameter("enrollment_state", "active");
            URI uri = uriBuilder.build();
            HttpGet getMethod = new HttpGet(uri);
            getMethod.addHeader("Authorization", "Bearer  " + cfgMap.get("TOKEN"));
            getMethod.addHeader("Content-Type", "application/json");

            CloseableHttpResponse response = this.httpClient.execute(getMethod);
            HttpEntity entity = response.getEntity();
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Map<String, Object>> result_list;
            InputStream contentStream = entity.getContent();
            result_list = mapper.readValue(contentStream, ArrayList.class);
            return result_list;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Map<String, Object>> get_course_folders(String courseName, String courseID)
    {
        try
        {
            String resourcePath = "courses/" + courseID + "/" + "folders";
            return get_data(resourcePath, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Map<String, Object>> get_folder_files(String folderName, String folderID)
    {
        try
        {
            String resourcePath = "folders/" + folderID + "/" + "files";
            return get_data(resourcePath, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void download_file(String fileDestinationPath, String fileUrl)
    {
        try
        {
            URI uri = new URI(fileUrl);
            HttpGet getMethod = new HttpGet(uri);
            getMethod.addHeader("Authorization", "Bearer  " + cfgMap.get("TOKEN"));

            CloseableHttpResponse response = this.clientBuilder.build().execute(getMethod);
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            Path destinationPath = Paths.get(fileDestinationPath);
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (RequestAbortedException e)
        {
            System.out.printf("%s cancelled.\n", fileDestinationPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void download_selected_course_files(String selectedDirectory, List<Map<String, Object>> selectedItemsList,
                                               Consumer updateLabel)
    {
        this.executor.execute(
                () -> download_selected_course_files_worker(selectedDirectory, selectedItemsList, updateLabel));
    }

    public void download_selected_course_files_worker(String selectedDirectory,
                                                      List<Map<String, Object>> selectedItemsList, Consumer updateLabel)
    {
        final Consumer<String> prepare_directory = (String directoryPathStr) -> {
            Path directoryPath = Paths.get(directoryPathStr);
            File directoryFile = directoryPath.toFile();
            if (!(directoryFile.exists() && directoryFile.isDirectory()))
            {
                directoryFile.mkdirs();
            }
        };

        prepare_directory.accept(selectedDirectory);
        ArrayList<Future> taskList = new ArrayList<>();
        for (var eachCourse : selectedItemsList)
        {
            Path coursePath = Paths.get(selectedDirectory, eachCourse.get("name").toString());
            prepare_directory.accept(coursePath.toString());

            ArrayList<Map<String, Object>> courseFolders = get_course_folders(eachCourse.get("name").toString(),
                    eachCourse.get("id").toString());

            if (courseFolders != null)
            {
                for (var eachFolder : courseFolders)
                {
                    Path courseFolderPath = Paths.get(coursePath.toString(), eachFolder.get("name").toString());
                    prepare_directory.accept(courseFolderPath.toString());

                    ArrayList<Map<String, Object>> folderFiles = get_folder_files(eachFolder.get("name").toString(),
                            eachFolder.get("id").toString());
                    if (folderFiles != null)
                    {
                        for (var eachFile : folderFiles)
                        {
                            Path courseFolderFilePath = Paths.get(courseFolderPath.toString(),
                                    eachFile.get("filename").toString());
                            Runnable task = () -> download_file(courseFolderFilePath.toString(),
                                    eachFile.get("url").toString());
                            taskList.add(executor.submit(task));
                        }
                    }
                }
            }
        }
        boolean jobDone = true;
        try
        {
            for (var job : taskList)
            {
                job.get();
                jobDone = jobDone && job.isDone();
            }
        }
        catch (ExecutionException | InterruptedException e)
        {
            e.printStackTrace();
        }
        boolean finalJobDone = jobDone;
        Platform.runLater(() -> {updateLabel.accept(finalJobDone);});
    }

    public void terminate()
    {
        System.out.println("Started terminate()");
        try
        {
            this.executor.shutdown();   //stop receiving new jobs
            //block 5s for existing jobs to complete
            if (!this.executor.awaitTermination(5, TimeUnit.SECONDS))
            {
                System.out.println("Awaited 5s");
                this.executor.shutdownNow();
                System.out.println("Shutting down executor...");
                if (!this.executor.awaitTermination(30, TimeUnit.SECONDS))
                {
                    System.out.printf("Executor shutdown: %b\n", this.executor.isShutdown());
                }
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("Completed terminate()");
    }
}
