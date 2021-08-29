// Copyright (C) 2021 Paul Holzinger
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package dev.holzinger.pibell;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX Application GUI for the pibell client
 *
 * @author Paul Holzinger
 */
public class GUI extends Application implements App {
    // tray icon image location
    // icon from https://iconscout.com/icon/notification-bell-3114519 (CC0 license)
    private URL imageLoc = ClassLoader.getSystemResource("dev/holzinger/pibell/bell.png");

    // store the application stage reference so that it can be shown and hidden
    // based on system tray icon operations
    private Stage stage;

    // statusLabel reference so we can change the text later
    private Label statusLabel;

    // logField reference so we can change the text later
    private TextArea logField;

    private Client client;

    // reference to this object since "this" is not accessible in the
    // Runnable
    private App app = this;

    // german date format
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

    // trayIcon reference so it can be accessed later
    private java.awt.TrayIcon trayIcon;

    @Override
    public void start(Stage stage) {
        // stores a reference to the stage.
        this.stage = stage;

        // sets up the tray icon (using awt code run on the swing thread).
        Platform.runLater(this::addAppToTray);

        // get command line args and create new client
        Parameters parms = getParameters();
        List<String> raw = parms.getRaw();
        String address = "";
        if (raw.size() == 1) {
            address = raw.get(0);
            this.newClient(address);
        }

        // create the layout for the javafx stage.
        StackPane layout = new StackPane(createContent(address));
        layout.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);");
        layout.setPrefSize(600, 300);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        // set the window title
        stage.setTitle("pibell");

        // set taskbar icon
        stage.getIcons().add(new Image(imageLoc.toString()));

        showStage();
    }

    /**
     * @param serverAddress
     * @return the main window application content.
     */
    private Node createContent(String serverAddress) {
        VBox mainBox = new VBox(20);

        HBox hbox = new HBox(25);
        hbox.setAlignment(Pos.CENTER);

        TextField address = new TextField(serverAddress);
        address.setPromptText("Server Adresse");
        hbox.getChildren().add(address);

        statusLabel = new Label("");

        Button button = new Button("Verbinden");
        button.setDefaultButton(true);

        // keep the reference to this object so it can be used in the event handler
        GUI gui = this;

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                gui.newClient(address.getText());
            }
        });
        hbox.getChildren().add(button);

        mainBox.getChildren().add(hbox);
        mainBox.getChildren().add(statusLabel);

        logField = new TextArea();
        logField.setEditable(false);
        logField.setWrapText(true);
        mainBox.getChildren().add(logField);

        HBox centerBox = new HBox(5, mainBox);
        centerBox.setPadding(new Insets(25, 10, 10, 10));
        centerBox.setAlignment(Pos.CENTER);

        return centerBox;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support
            if (!java.awt.SystemTray.isSupported()) {
                System.err.println("Kein system tray support");
                return;
            }

            // instructs the javafx system not to exit implicitly when the last application
            // window is shut because the window will be restored when the task icon is
            // clicked
            Platform.setImplicitExit(false);

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            // load image
            java.awt.Image image = ImageIO.read(imageLoc);
            // resize icon
            trayIcon = new java.awt.TrayIcon(image);
            // trayIcon.setImageAutoSize(true);
            int trayIconWidth = new java.awt.TrayIcon(image).getSize().width;
            trayIcon = new java.awt.TrayIcon(image.getScaledInstance(trayIconWidth, -1, java.awt.Image.SCALE_SMOOTH));

            // if the user clicks on the tray icon, show the main app stage
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Beenden");
            exitItem.addActionListener(event -> {
                Platform.exit();
                tray.remove(trayIcon);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.err.println("System tray icon konnte nicht initialisiert werden");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of
     * all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

    private void newClient(String address) {
        if (client != null) {
            client.Close();
        }
        try {
            client = new Client(address, app);
            Thread clientThread = new Thread(client);
            // set daemon to true to allow jvm to exit when the gui thread is closed
            clientThread.setDaemon(true);
            clientThread.start();
        } catch (URISyntaxException ex) {
            app.writeError(ex.getMessage());
            client = null;
        }
    }

    public void createAlarm(String msg) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                trayIcon.displayMessage("Türklingel", msg, java.awt.TrayIcon.MessageType.INFO);
                app.writeLog("Alarm: " + msg);
            }
        });

    }

    public void writeLog(String msg) {
        // run in GUI thread, it is not safe to change gui from another thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                String message = dateFormat.format(date) + ": " + msg + "\n";
                System.out.print(message);
                if (logField != null) {
                    // append the text to the start of the log field
                    logField.insertText(0, message);
                }
            }
        });
    }

    public void writeSuccess(String msg) {
        // run in GUI thread, it is not safe to change gui from another thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(msg);
                // change font color to green
                statusLabel.setStyle("-fx-text-fill: green;");
                app.writeLog(msg);
            }
        });
    }

    public void writeError(String msg) {
        // run in GUI thread, it is not safe to change gui from another thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(msg);
                // change font color to red
                statusLabel.setStyle("-fx-text-fill: red;");
                app.writeLog("Fehler: " + msg);
            }
        });
    }

}