package manager.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.gui.Controller;
import manager.mouse.MousePath;

import javax.swing.*;
import java.io.*;

public class Files {

    public static final File directory = new JFileChooser().getFileSystemView().getDefaultDirectory();

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
}
