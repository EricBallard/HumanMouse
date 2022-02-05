package manager.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.stage.FileChooser;
import manager.gui.Controller;
import manager.mouse.MousePath;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;

public class Files {

    public static final File directory = FileUtils.getUserDirectory();

    public static FileChooser getChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        chooser.setInitialDirectory(Files.directory);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json Files", "*.json"));
        return chooser;
    }

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
            controller.setPaths(paths);
            controller.toggleToolbarButtons(false, file.getName());
            System.out.println("Loaded " + paths.totalPaths + " paths!");
            return true;
        }

        return false;
    }

    public static void save(File file, Controller controller) {
        try (Writer writer = new FileWriter(file.getPath())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(controller.paths, writer);

            controller.toggleToolbarButtons(false, file.getName());
            System.out.println("Saved paths!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save mouse paths to file due to, " + e.getMessage());
        }
    }

    public static void merge(List<File> selected, Controller controller) {
        MousePath.Paths mergedPaths = new MousePath.Paths();

        for (File file : selected) {
            // Load paths
            if (!load(file, controller)) {
                System.out.println("FAILED TO LOAD FILE: " + file.getName());
                return;
            }

            // Cache loaded paths
            if (mergedPaths.list.addAll(controller.paths.list))
                mergedPaths.totalPaths += controller.paths.totalPaths;
            else {
                System.out.println("FAILED TO ADD PATHS: " + file.getName());
                return;
            }
        }


        // Save merged
        controller.paths = mergedPaths;
        controller.buttons.savePaths(null);

        // Log
        System.out.println("Merged " + selected.size() +
                " files with a total of " + mergedPaths.totalPaths + " paths!");

    }
}
