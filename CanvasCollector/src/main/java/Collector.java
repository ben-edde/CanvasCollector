import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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
//                uriBuilder.addParameter("enrollment_state", "active");
                URI uri = uriBuilder.build();
                HttpGet getMethod = new HttpGet(uri);
                getMethod.addHeader("Authorization", "Bearer  " + cfg_map.get("TOKEN"));
                getMethod.addHeader("Content-Type", "application/json");
                CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(getMethod);
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<Map<String, Object>> result_list;
                result_list = mapper.readValue(entity.getContent(), ArrayList.class);
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
        String resourcePath = "courses/" + courseID + "/" + "folders";
        return get_data(resourcePath);
    }

    public static ArrayList<Map<String, Object>> get_folder_files(String folderName, String folderID)
    {
        String resourcePath = "folders/" + folderID + "/" + "files";
        return get_data(resourcePath);
    }

    public static void download_file(String fileDestinationPath, String fileUrl)
    {
        LinkedHashMap<String, String> cfg_map = load_config("cfg/config.yaml");
        try
        {
//            URIBuilder uriBuilder = new URIBuilder(fileUrl);
            URI uri = new URI(fileUrl);
            HttpGet getMethod = new HttpGet(uri);
            getMethod.addHeader("Authorization", "Bearer  " + cfg_map.get("TOKEN"));
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(getMethod);
            HttpEntity entity = response.getEntity();

            InputStream inputStream=entity.getContent();
            Path destinationPath= Paths.get(fileDestinationPath);
            Files.copy(inputStream,destinationPath, StandardCopyOption.REPLACE_EXISTING);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
