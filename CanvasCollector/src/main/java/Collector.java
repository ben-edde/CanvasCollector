import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import java.util.function.Consumer;

public class Collector
{
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

    public static ArrayList<Map<String, Object>> get_data(String resource)
    {
        LinkedHashMap<String, String> cfg_map = load_config("cfg/config.yaml");
        if (cfg_map != null)
        {
            try
            {
                URIBuilder uriBuilder = new URIBuilder(cfg_map.get("API_ENDPOINT") + "/" + resource);
                uriBuilder.addParameter("per_page", "32767");
                URI uri = uriBuilder.build();
                HttpGet getMethod = new HttpGet(uri);
                getMethod.addHeader("Authorization", "Bearer  " + cfg_map.get("TOKEN"));
                getMethod.addHeader("Content-Type", "application/json");

                CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(
                        RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();

                CloseableHttpResponse response = httpclient.execute(getMethod);
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<Map<String, Object>> result_list;
                result_list = mapper.readValue(entity.getContent(), ArrayList.class);
                httpclient.close();
                response.close();
                return result_list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<Map<String, Object>> get_course_folders(String courseName, String courseID)
    {
        try
        {
            String resourcePath = "courses/" + courseID + "/" + "folders";
            return get_data(resourcePath);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Map<String, Object>> get_folder_files(String folderName, String folderID)
    {
        try
        {
            String resourcePath = "folders/" + folderID + "/" + "files";
            return get_data(resourcePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void download_file(String fileDestinationPath, String fileUrl)
    {
        LinkedHashMap<String, String> cfg_map = load_config("cfg/config.yaml");
        try
        {
            URI uri = new URI(fileUrl);
            HttpGet getMethod = new HttpGet(uri);
            getMethod.addHeader("Authorization", "Bearer  " + cfg_map.get("TOKEN"));
            CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(
                    RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
            CloseableHttpResponse response = httpclient.execute(getMethod);
            HttpEntity entity = response.getEntity();

            InputStream inputStream = entity.getContent();
            Path destinationPath = Paths.get(fileDestinationPath);
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            httpclient.close();
            response.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void download_selected_course_files(String selectedDirectory,
                                                      List<Map<String, Object>> selectedItemsList)
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
        for (var eachCourse : selectedItemsList)
        {
            Path coursePath = Paths.get(selectedDirectory, eachCourse.get("name").toString());
            prepare_directory.accept(coursePath.toString());
            ArrayList<Map<String, Object>> courseFolders = Collector.get_course_folders(
                    eachCourse.get("name").toString(), eachCourse.get("id").toString());
            if (courseFolders != null)
            {
                for (var eachFolder : courseFolders)
                {
                    Path courseFolderPath = Paths.get(coursePath.toString(), eachFolder.get("name").toString());
                    prepare_directory.accept(courseFolderPath.toString());
                    ArrayList<Map<String, Object>> folderFiles = Collector.get_folder_files(
                            eachFolder.get("name").toString(), eachFolder.get("id").toString());
                    if (folderFiles != null)
                    {
                        for (var eachFile : folderFiles)
                        {
                            Path courseFolderFilePath = Paths.get(courseFolderPath.toString(),
                                    eachFile.get("filename").toString());
                            Collector.download_file(courseFolderFilePath.toString(), eachFile.get("url").toString());
                        }
                    }
                }
            }

        }
    }
}
