package com.attendance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.attendance.model.Admin;
import com.attendance.model.AttendanceManager;
import com.attendance.model.FileHandler;
import com.attendance.model.Student;
import com.attendance.ui.DashboardTab;
import com.attendance.ui.LoginScreen;
import com.attendance.ui.MarkAttendanceTab;
import com.attendance.ui.MyAttendanceTab;
import com.attendance.ui.RecordsTab;
import com.attendance.ui.RegisterTab;
import com.attendance.ui.ReportsTab;
import com.attendance.ui.Theme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {

    private AttendanceManager manager = new AttendanceManager();
    private Stage stage;
    private List<Button> navButtons = new ArrayList<>();
    private TabPane tabs = new TabPane();

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        try {
            FileHandler.load(manager);
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }

        stage.setScene(new Scene(buildLogin(), 1100, 680));
        stage.setTitle(Theme.SCHOOL_NAME + " - Smart Attendance and Shortage Alert System");
        stage.setMinWidth(950);
        stage.setMinHeight(640);
        stage.show();
    }

    // ---------- Login screen ----------
    private LoginScreen buildLogin() {
        return new LoginScreen(manager,
                admin -> showMain(admin, null),
                student -> showMain(null, student));
    }

    private void showLogin() {
        stage.getScene().setRoot(buildLogin());
    }

    // ---------- Main screen: admins get all pages, students get only their own ----------
    private void showMain(Admin adminUser, Student student) {
        navButtons = new ArrayList<>();
        tabs = new TabPane();

        String[] names;
        String userText;
        if (adminUser != null) {
            tabs.getTabs().addAll(
                    new DashboardTab(manager),
                    new RegisterTab(manager),
                    new MarkAttendanceTab(manager),
                    new RecordsTab(manager),
                    new ReportsTab(manager));
            names = new String[] { "Dashboard", "Register", "Mark Attendance", "Records", "Reports" };
            userText = adminUser.getFullName() + " (Admin)";
        } else {
            tabs.getTabs().add(new MyAttendanceTab(manager, student));
            names = new String[] { "My Attendance" };
            userText = student.getName() + " (Student)";
        }

        // The tab headers are hidden: the sidebar buttons switch between the pages
        tabs.setTabMinHeight(0);
        tabs.setTabMaxHeight(0);
        for (Tab t : tabs.getTabs()) {
            if (t.getContent() instanceof Region) {
                ((Region) t.getContent()).setBackground(Theme.background(Theme.PAGE, 0));
            }
        }

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar(names, userText));
        root.setCenter(tabs);
        root.setBackground(Theme.background(Theme.PAGE, 0));

        tabs.getSelectionModel().selectedIndexProperty()
                .addListener((obs, oldIndex, newIndex) -> highlight(newIndex.intValue()));
        highlight(0);

        stage.getScene().setRoot(root);
    }

    private VBox buildSidebar(String[] names, String userText) {
        VBox sidebar = new VBox(6);
        sidebar.setPadding(new Insets(26, 14, 20, 14));
        sidebar.setPrefWidth(230);
        sidebar.setMinWidth(230);
        sidebar.setBackground(Theme.background(Theme.NAVY, 0));

       Label appName = Theme.label(Theme.SCHOOL_NAME, FontWeight.BOLD, 19, Color.WHITE);
appName.setWrapText(true);
Label appSub = Theme.label("Smart Attendance System", FontWeight.NORMAL, 12, Color.web("#aab4e6"));
        VBox brand = new VBox(2, appName, appSub);
        brand.setPadding(new Insets(0, 0, 18, 8));
        sidebar.getChildren().add(brand);

        // Who is logged in
        Label signedIn = Theme.label("Signed in as", FontWeight.NORMAL, 11, Color.web("#aab4e6"));
        Label userName = Theme.label(userText, FontWeight.BOLD, 14, Color.WHITE);
        userName.setWrapText(true);
        VBox userBox = new VBox(2, signedIn, userName);
        userBox.setPadding(new Insets(10, 12, 10, 12));
        userBox.setBackground(Theme.background(Color.web("#2c3e7a"), 8));
        VBox.setMargin(userBox, new Insets(0, 0, 16, 0));
        sidebar.getChildren().add(userBox);

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            Button b = new Button(names[i]);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setFont(Theme.font(FontWeight.BOLD, 14));
            b.setPadding(new Insets(12, 16, 12, 16));
            b.setCursor(Cursor.HAND);

            b.setOnAction(e -> tabs.getSelectionModel().select(index));
            b.setOnMouseEntered(e -> {
                if (tabs.getSelectionModel().getSelectedIndex() != index) {
                    b.setBackground(Theme.background(Color.web("#2c3e7a"), 8));
                }
            });
            b.setOnMouseExited(e -> {
                if (tabs.getSelectionModel().getSelectedIndex() != index) {
                    b.setBackground(Background.EMPTY);
                }
            });

            navButtons.add(b);
            sidebar.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = Theme.label("Minimum attendance: 75%", FontWeight.NORMAL, 12, Color.web("#aab4e6"));
        footer.setPadding(new Insets(0, 0, 8, 8));

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER);
        logout.setFont(Theme.font(FontWeight.BOLD, 14));
        logout.setPadding(new Insets(10, 16, 10, 16));
        logout.setCursor(Cursor.HAND);
        logout.setTextFill(Color.WHITE);
        logout.setBackground(Theme.background(Theme.DANGER, 8));
        logout.setOnAction(e -> showLogin());

        sidebar.getChildren().addAll(spacer, footer, logout);
        return sidebar;
    }

    // Selected menu item = blue box with white text; the others are transparent
    private void highlight(int selected) {
        for (int i = 0; i < navButtons.size(); i++) {
            Button b = navButtons.get(i);
            if (i == selected) {
                b.setBackground(Theme.background(Theme.PRIMARY, 8));
                b.setTextFill(Color.WHITE);
            } else {
                b.setBackground(Background.EMPTY);
                b.setTextFill(Color.web("#c5cbf0"));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
