package client.gui;

import client.network.ClientConnection;
import common.models.*;
import common.models.Color;
import common.network.CommandRequest;
import common.network.CommandResponse;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class AddDialog extends JDialog {
    private final ResourceBundle bundle;
    private final ClientConnection connection;
    private final String username;
    private final String passwordHash;
    private final MainFrame parent;

    private final JTextField nameField = new JTextField(15);
    private final JTextField xField = new JTextField(6);
    private final JTextField yField = new JTextField(6);
    private final JTextField studentsField = new JTextField(6);
    private final JComboBox<FormOfEducation> formBox = new JComboBox<>(FormOfEducation.values());
    private final JComboBox<Semester> semesterBox = new JComboBox<>(Semester.values());
    private final JTextField adminNameField = new JTextField(12);
    private final JTextField adminWeightField = new JTextField(6);
    private final JComboBox<Color> eyeBox = new JComboBox<>(Color.values());
    private final JComboBox<Country> countryBox = new JComboBox<>(Country.values());

    public AddDialog(MainFrame parent, ClientConnection connection, String username, String passwordHash, Locale locale) {
        super(parent, true);
        this.parent = parent;
        this.connection = connection;
        this.username = username;
        this.passwordHash = passwordHash;
        this.bundle = ResourceBundle.getBundle("common.locale.Messages", locale);

        setTitle(bundle.getString("dialog.add.title"));
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(12, 2, 5, 5));

        add(new JLabel(bundle.getString("dialog.name"))); add(nameField);
        add(new JLabel(bundle.getString("dialog.coordX"))); add(xField);
        add(new JLabel(bundle.getString("dialog.coordY"))); add(yField);
        add(new JLabel(bundle.getString("dialog.students"))); add(studentsField);
        add(new JLabel(bundle.getString("dialog.form"))); add(formBox);
        add(new JLabel(bundle.getString("dialog.semester"))); add(semesterBox);
        add(new JLabel(bundle.getString("dialog.adminName"))); add(adminNameField);
        add(new JLabel(bundle.getString("dialog.adminWeight"))); add(adminWeightField);
        add(new JLabel(bundle.getString("dialog.eyeColor"))); add(eyeBox);
        add(new JLabel(bundle.getString("dialog.nationality"))); add(countryBox);

        JButton submit = new JButton(bundle.getString("dialog.submit"));
        JButton cancel = new JButton(bundle.getString("dialog.cancel"));

        submit.addActionListener(e -> handleSubmit());
        cancel.addActionListener(e -> dispose());

        add(submit); add(cancel);
    }

    private void handleSubmit() {
        try {
            String name = nameField.getText().trim();
            int x = Integer.parseInt(xField.getText().trim());
            int y = Integer.parseInt(yField.getText().trim());
            int students = Integer.parseInt(studentsField.getText().trim());
            String adminName = adminNameField.getText().trim();
            double weight = Double.parseDouble(adminWeightField.getText().trim());

            if (name.isEmpty() || adminName.isEmpty())
                throw new IllegalArgumentException(bundle.getString("dialog.error.empty"));

            Coordinates coords = new Coordinates(x, y);
            Person admin = new Person(adminName, weight,
                    (Color) eyeBox.getSelectedItem(),
                    (Country) countryBox.getSelectedItem());

            StudyGroup group = new StudyGroup(name, coords, students,
                    (FormOfEducation) formBox.getSelectedItem(),
                    (Semester) semesterBox.getSelectedItem(),
                    admin);
            group.setOwner(username);

            CommandRequest request = new CommandRequest("add", new String[]{}, group, username, passwordHash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();

            JOptionPane.showMessageDialog(this, response.getMessage());

            if (response.isSuccess()) {
                parent.loadData();
                dispose();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, bundle.getString("dialog.error.invalid"));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
        }
    }
}