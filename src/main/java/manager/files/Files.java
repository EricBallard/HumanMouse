package manager.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import manager.gui.Controller;
import manager.mouse.Paths;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
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

    public static void load(File file, Controller controller) {
        // Lock UI
        controller.disabled(true);

        new Thread(() -> {
            // Read
            Paths paths = read(file);

            // Un-lock UI
            controller.disabled(false);

            if (paths != null) {
                Platform.runLater(() -> {
                    // Set paths
                    controller.setPaths(paths);
                    controller.toggleToolbarButtons(false, file.getName());

                    // Unlock UI
                    controller.disabled(false);
                });

                System.out.println("Loaded " + paths.totalPaths + " paths!");
            }
        }).start();
    }

    @Nullable
    static Paths read(File file) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(reader, Paths.class);
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
        // Lock UI
        controller.disabled(true);

        new Thread(() -> {
            Paths mergedPaths = new Paths();

            // Read
            for (File file : selected) {
                // Load paths
                Paths paths = read(file);

                if (paths == null) {
                    System.out.println("FAILED TO MERGE, FAILED TO LOAD " + file.getName());
                    controller.disabled(false);
                    return;
                }

                // Cache loaded paths
                if (mergedPaths.list.addAll(paths.list))
                    mergedPaths.totalPaths += paths.totalPaths;
                else {
                    System.out.println("FAILED TO ADD PATHS: " + file.getName());
                    return;
                }
            }

            Platform.runLater(() -> {
                // Save merged
                controller.paths = mergedPaths;
                controller.buttons.savePaths(null);

                // Un-lock UI
                controller.disabled(false);
            });

            // Log
            System.out.println("Merged " + selected.size() +
                    " files with a total of " + mergedPaths.totalPaths + " paths!");
        }).start();
    }
}
