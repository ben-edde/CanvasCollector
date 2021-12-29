import com.fasterxml.jackson.databind.ObjectMapper;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CanvasCollector
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
                URL url = new URL(cfg_map.get("API_ENDPOINT") + "/" + resource);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestProperty("Authorization", "Bearer  " + cfg_map.get("TOKEN"));

                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");

                ObjectMapper mapper = new ObjectMapper();
                ArrayList<Map<String, Object>> result_list;
                result_list = mapper.readValue(con.getInputStream(), ArrayList.class);
                return result_list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<Map<String, Object>> course_handler(String courseName, String courseID)
    {
        String resourcePath = "courses/" + courseID + "/" + "folders";
        return get_data(resourcePath);
    }
}
