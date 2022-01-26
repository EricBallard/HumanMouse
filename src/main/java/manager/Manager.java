package manager;

import javafx.application.Application;
import manager.gui.GUI;

public class Manager {

    public static void main(String[] args) throws Exception {
        System.out.println("HELLO WORLD!");

        GUI gui = new GUI();
        Application.launch(gui.getClass());
    }
}
