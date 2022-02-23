package manager.files;

import manager.gui.Controller;
import manager.mouse.MousePath;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

public class Database {

    Connection con;

    final static String TABLE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS data (" +
                    "xSpan INTEGER NOT NULL, " +
                    "ySpan INTEGER NOT NULL, " +
                    "totalTime INTEGER NOT NULL, " +
                    "totalPoints INTEGER NOT NULL, " +
                    "points TEXT NOT NULL" +
                    ");";

    public boolean connect(File file, Controller controller) {
        if (con != null)
            return true;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.buttons.showInfo("HumanMouse-Manager | PACK", "Database Error", e.getMessage());
            return false;
        }

        System.out.println("Connected to DB!");
        return true;
    }

    public void pack(File file, Controller controller) {
        if (!connect(file, controller) || !initSchema(controller))
            return;

        for (MousePath path : controller.paths.list) {
            if (!addPath(path, controller))
                break;
        }

        System.out.println("Packed mouse-paths to database!");
    }

    @Nullable
    public MousePath getPath(int xSpan, int ySpan) {
        String sql = "SELECT * FROM data ORDER BY ABS(? - xSpan) + ABS(? - ySpan) LIMIT 5";
        PreparedStatement statement;

        try {
            statement = con.prepareStatement(sql);
            statement.setInt(1, xSpan);
            statement.setInt(2, ySpan);

            ResultSet rs = statement.executeQuery();
            ArrayList<Object[]> results = new ArrayList<>();

            while (rs.next()) {
                Object[] data = new Object[5];
                data[0] = rs.getInt("xSpan");
                data[1] = rs.getInt("ySpan");
                data[2] = rs.getInt("totalTime");
                data[3] = rs.getInt("totalPoints");
                data[4] = rs.getString("points");

                results.add(data);
            }

            if (!results.isEmpty()) {
                Object[] data = results.get(new Random().nextInt(results.size()));
                int xs = (int) data[0], ys = (int) data[1];
                int tt = (int) data[2], tp = (int) data[3];
                String ps = (String) data[4];

                return new MousePath(xs, ys, tt, tp, ps);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addPath(MousePath path, Controller controller) {
        String sql = "INSERT INTO data(xSpan, ySpan, totalTime, totalPoints, points) VALUES(?,?,?,?,?)";
        PreparedStatement statement;

        try {
            statement = con.prepareStatement(sql);

            statement.setInt(1, path.xSpan);
            statement.setInt(2, path.ySpan);
            statement.setInt(3, (int) path.totalTime);
            statement.setInt(4, path.totalPoints);
            statement.setString(5, path.getPointsAsString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            controller.buttons.showInfo("HumanMouse-Manager | PACK", "Database Error", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean initSchema(Controller controller) {
        try {
            con.prepareStatement(TABLE_SCHEMA).execute();
        } catch (SQLException e) {
            e.printStackTrace();
            controller.buttons.showInfo("HumanMouse-Manager | PACK", "Database Error", e.getMessage());
            return false;
        }
        return true;
    }
}
