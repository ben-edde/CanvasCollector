import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectorTest
{
    @Test
    void get_data()
    {
    }

    @Test
    void get_course_folder()
    {
        ArrayList<Map<String, Object>> results;
        results = Collector.get_course_folders("Demo", "42871");
        assertEquals(4, results.size());
        for (var each : results)
        {
            assertEquals(LinkedHashMap.class, each.getClass());
        }
    }

    @Test
    void get_folder_files()
    {
        ArrayList<Map<String, Object>> results;
        results = Collector.get_folder_files("Demo", "1317704");
        assertEquals(8, results.size());
        for (var each : results)
        {
            assertEquals(LinkedHashMap.class, each.getClass());
        }
    }

    @Test
    void download_file()
    {
        ArrayList<Map<String, Object>> results;
        String fileDestinationPath = "cat.png";
        String fileUrl = "https://upload.wikimedia.org/wikipedia/commons/b/b1/VAN_CAT.png";
        File downloadedFile = new File(fileDestinationPath);
        try
        {
            Collector.download_file(fileDestinationPath, fileUrl);

            assertTrue(downloadedFile.exists());
            assertTrue(downloadedFile.isFile());
            assertEquals(fileDestinationPath, downloadedFile.getName());
        }
        finally
        {
            if (downloadedFile.isFile())
            {downloadedFile.delete();}
        }
    }

    @Test
    void download_selected_course_files()
    {
        String testDirectory = "tmp";
        Map<String, Object> courseMap = Map.of("name", "testCourse", "id", "26868");
        List<Map<String, Object>> testItemList = new ArrayList<>(Arrays.asList(courseMap));
        File testDirectoryFile = new File(testDirectory);
        testDirectoryFile.deleteOnExit();

        Collector.download_selected_course_files(testDirectory, testItemList);
    }

}