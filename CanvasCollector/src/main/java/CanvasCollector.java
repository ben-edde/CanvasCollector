import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

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
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return cfg_map;
    }

    public static void main(String[] args)
    {
        LinkedHashMap<String, String> cfg_map = load_config("cfg/config.yaml");
        if (cfg_map != null)
            try
            {
                URL url = new URL(cfg_map.get("API_ENDPOINT") + "/courses");
//                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.addRequestProperty("Authorization", "Bearer " + cfg_map.get("TOKEN"));
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
//                System.out.println(con.getResponseCode());
//                System.out.println(con.getResponseMessage());
            } catch (Exception e)
            {
                e.printStackTrace();
            }

    }
}
