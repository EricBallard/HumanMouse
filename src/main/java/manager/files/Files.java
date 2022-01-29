package manager.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.gui.Controller;
import manager.mouse.MousePath;
import org.apache.commons.io.FileUtils;

import java.io.*;

public class Files {

    public static final File directory = FileUtils.getUserDirectory();

    public static boolean load(File file, Controller controller) {
        MousePath.Paths paths;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        paths = gson.fromJson(reader, MousePath.Paths.class);

        if (paths != null) {
            System.out.println("Loaded " + paths.totalPaths + " paths!");
            controller.setPaths(paths);
            controller.toggleToolbarButtons(false, file.getName());
            return true;
        }

        return false;
    }

    public static boolean save(File file, Controller controller) {
        try (Writer writer = new FileWriter(file.getPath())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(controller.paths, writer);

            System.out.println("Saved paths!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save mouse paths to file due to, " + e.getMessage());
        }
        return false;
    }
}
