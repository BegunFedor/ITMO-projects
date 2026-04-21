package client.gui;

import client.network.ClientConnection;
import common.models.*;
import common.network.CommandRequest;
import common.network.CommandResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final String username;
    private final String passwordHash;
    private final ClientConnection connection;
    private JTable table;
    private DefaultTableModel tableModel;
    private final CanvasPanel canvasPanel;
    private TableRowSorter<DefaultTableModel> sorter;
    private String currentLanguage = "ru";
    private Map<String, Map<String, String>> languageResources;
    private JPanel controlPanel;
    private Map<String, Color> userColors = new HashMap<>();
    private Image backgroundImage;
    private Timer autoRefreshTimer;
    private boolean autoRefreshEnabled = false;

    public MainFrame(String username, String passwordHash, ClientConnection connection) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.connection = connection;
        initializeLanguages();
        loadBackgroundImage();

        setTitle(getText("title"));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        autoRefreshTimer = new Timer(1000, e -> loadData());
        autoRefreshTimer.start();

        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        backgroundPanel.setLayout(new BorderLayout());

        initMenuBar();
        canvasPanel = new CanvasPanel(userColors);

        createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, canvasPanel);
        splitPane.setResizeWeight(0.5);

        controlPanel = createControlPanel();
        backgroundPanel.add(controlPanel, BorderLayout.NORTH);
        backgroundPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(backgroundPanel);
        loadData();
        setVisible(true);
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("back2.jpg"));
        } catch (IOException e) {
            System.err.println("Фон не загружен: " + e.getMessage());
        }
    }

    private void initializeLanguages() {
        languageResources = new HashMap<>();

        Map<String, String> ru = Map.ofEntries(
                Map.entry("title", "Управление StudyGroups"),
                Map.entry("menu.language", "Язык"),
                Map.entry("filter.label", "Фильтр:"),
                Map.entry("button.add", "Добавить"),
                Map.entry("button.remove", "Удалить"),
                Map.entry("button.update", "Обновить"),
                Map.entry("button.refresh", "Перезагрузить"),
                Map.entry("button.visualize", "Визуализация"),
                Map.entry("button.logout", "Выйти"),
                Map.entry("column.id", "ID"),
                Map.entry("column.name", "Название"),
                Map.entry("column.coordinates", "Координаты"),
                Map.entry("column.students", "Студенты"),
                Map.entry("column.form", "Форма обучения"),
                Map.entry("column.semester", "Семестр"),
                Map.entry("column.admin", "Админ"),
                Map.entry("column.owner", "Владелец")
        );

        Map<String, String> en = Map.ofEntries(
                Map.entry("title", "StudyGroups Manager"),
                Map.entry("menu.language", "Language"),
                Map.entry("filter.label", "Filter:"),
                Map.entry("button.add", "Add"),
                Map.entry("button.remove", "Remove"),
                Map.entry("button.update", "Update"),
                Map.entry("button.refresh", "Refresh"),
                Map.entry("button.visualize", "Visualize"),
                Map.entry("button.logout", "Logout"),
                Map.entry("column.id", "ID"),
                Map.entry("column.name", "Name"),
                Map.entry("column.coordinates", "Coordinates"),
                Map.entry("column.students", "Students"),
                Map.entry("column.form", "Form"),
                Map.entry("column.semester", "Semester"),
                Map.entry("column.admin", "Admin"),
                Map.entry("column.owner", "Owner")
        );

        Map<String, String> et = Map.ofEntries(
                Map.entry("title", "StudyGroups Haldur"),
                Map.entry("menu.language", "Keel"),
                Map.entry("filter.label", "Filter:"),
                Map.entry("button.add", "Lisa"),
                Map.entry("button.remove", "Eemalda"),
                Map.entry("button.update", "Uuenda"),
                Map.entry("button.refresh", "Värskenda"),
                Map.entry("button.logout", "Logi välja"),
                Map.entry("column.id", "ID"),
                Map.entry("column.name", "Nimi"),
                Map.entry("column.coordinates", "Koordinaadid"),
                Map.entry("column.students", "Õpilased"),
                Map.entry("column.form", "Vorm"),
                Map.entry("column.semester", "Semester"),
                Map.entry("column.admin", "Admin"),
                Map.entry("column.owner", "Omanik")
        );

        Map<String, String> sq = Map.ofEntries(
                Map.entry("title", "Menaxhues i StudyGroups"),
                Map.entry("menu.language", "Gjuha"),
                Map.entry("filter.label", "Filtër:"),
                Map.entry("button.add", "Shto"),
                Map.entry("button.remove", "Hiq"),
                Map.entry("button.update", "Përditëso"),
                Map.entry("button.refresh", "Rifresko"),
                Map.entry("button.logout", "Dil"),
                Map.entry("column.id", "ID"),
                Map.entry("column.name", "Emri"),
                Map.entry("column.coordinates", "Koordinatat"),
                Map.entry("column.students", "Studentët"),
                Map.entry("column.form", "Forma"),
                Map.entry("column.semester", "Semestri"),
                Map.entry("column.admin", "Admin"),
                Map.entry("column.owner", "Pronari")
        );

        languageResources.put("ru", ru);
        languageResources.put("en", en);
        languageResources.put("et", et);
        languageResources.put("sq", sq);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu langMenu = new JMenu(getText("menu.language"));

        JMenuItem ru = new JMenuItem("Русский");
        ru.addActionListener(e -> switchLanguage("ru"));
        JMenuItem en = new JMenuItem("English");
        en.addActionListener(e -> switchLanguage("en"));
        JMenuItem et = new JMenuItem("Eesti");
        et.addActionListener(e -> switchLanguage("et"));
        JMenuItem sq = new JMenuItem("Shqip");
        sq.addActionListener(e -> switchLanguage("sq"));

        langMenu.add(ru);
        langMenu.add(en);
        langMenu.add(et);
        langMenu.add(sq);
        menuBar.add(langMenu);

        setJMenuBar(menuBar);
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();
        super.dispose();
    }

    private void switchLanguage(String lang) {
        currentLanguage = lang;
        updateTexts();
    }
    private void updateTableHeaders() {
        String[] headers = {
                getText("column.id"),
                getText("column.name"),
                getText("column.coordinates"),
                getText("column.students"),
                getText("column.form"),
                getText("column.semester"),
                getText("column.admin"),
                getText("column.owner")
        };

        for (int i = 0; i < headers.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderValue(headers[i]);
        }

        table.getTableHeader().repaint();
    }

    private void updateTexts() {
        setTitle(getText("title"));
        initMenuBar();
        remove(controlPanel);
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        updateTableHeaders();
        revalidate();
        repaint();
    }

    private void createTable() {
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 7;
            }
        };

        String[] columns = {
                getText("column.id"),
                getText("column.name"),
                getText("column.coordinates"),
                getText("column.students"),
                getText("column.form"),
                getText("column.semester"),
                getText("column.admin"),
                getText("column.owner")
        };
        for (String col : columns) tableModel.addColumn(col);

        table = new JTable(tableModel) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String owner = (String) getValueAt(row, 7);
                Color color = userColors.getOrDefault(owner, getBackground());
                if (!isRowSelected(row)) {
                    c.setBackground(color);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(getSelectionBackground());
                    c.setForeground(getSelectionForeground());
                }
                return c;
            }
        };

        table.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            if (row < 0 || e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;

            try {
                int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                String name = tableModel.getValueAt(row, 1).toString();
                String[] coords = tableModel.getValueAt(row, 2).toString().split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                int students = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
                FormOfEducation form = FormOfEducation.valueOf(tableModel.getValueAt(row, 4).toString());
                Semester semester = Semester.valueOf(tableModel.getValueAt(row, 5).toString());
                String adminName = tableModel.getValueAt(row, 6).toString();
                String owner = tableModel.getValueAt(row, 7).toString();

                double weight = 60;
                common.models.Color eyeColor = common.models.Color.GREEN;
                Country nationality = Country.RUSSIA;


                Person admin = new Person(adminName, weight, eyeColor, nationality);
                StudyGroup updated = new StudyGroup(name, new Coordinates(x, y), students, form, semester, admin);
                updated.setOwner(owner);

                CommandRequest req = new CommandRequest("update", new String[]{String.valueOf(id)}, updated, username, passwordHash);
                connection.sendRequest(req);
                CommandResponse resp = connection.receiveResponse();

                if (!resp.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении: " + resp.getMessage());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении: " + ex.getMessage());
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
    }

    public Locale getCurrentLocale() {
        return switch (currentLanguage) {
            case "en" -> Locale.ENGLISH;
            case "et" -> new Locale("et", "EE");
            case "sq" -> new Locale("sq", "AL");
            case "ru" -> new Locale("ru", "RU");
            default -> Locale.getDefault();
        };
    }




    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel filterLabel = new JLabel(getText("filter.label"));
        JTextField filterField = new JTextField(15);
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = filterField.getText();
                sorter.setRowFilter(text.isBlank() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        JButton addBtn = new JButton(getText("button.add"));
        JButton removeBtn = new JButton(getText("button.remove"));
        JButton updateBtn = new JButton(getText("button.update"));
        JButton autoRefreshToggle = new JButton("Автообновление: Выкл");

        autoRefreshToggle.addActionListener(e -> {
            autoRefreshEnabled = !autoRefreshEnabled;

            if (autoRefreshEnabled) {
                autoRefreshToggle.setText("Автообновление: Вкл");

                if (autoRefreshTimer == null) {
                    autoRefreshTimer = new Timer(5000, ev -> loadData());
                }
                autoRefreshTimer.setRepeats(true);
                autoRefreshTimer.start();

            } else {
                autoRefreshToggle.setText("Автообновление: Выкл");
                if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
                    autoRefreshTimer.stop();
                }
            }
        });

        JButton logoutBtn = new JButton(getText("button.logout"));

        addBtn.addActionListener(e -> new AddDialog(this, connection, username, passwordHash, getCurrentLocale()).setVisible(true));
        removeBtn.addActionListener(e -> removeGroup());
        updateBtn.addActionListener(e -> openUpdateDialog());
        autoRefreshToggle.addActionListener(e -> loadData());
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(connection));
        });

        left.add(filterLabel);
        left.add(filterField);
        left.add(addBtn);
        left.add(removeBtn);
        left.add(updateBtn);

        right.add(autoRefreshToggle);
        right.add(logoutBtn);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    public void loadData() {
        try {
            CommandRequest request = new CommandRequest("show", new String[]{}, null, username, passwordHash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();

            tableModel.setRowCount(0);

            List<StudyGroup> groups = new ArrayList<>();
            userColors.clear();

            if (response.getPayload() instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj instanceof StudyGroup group) {
                        tableModel.addRow(new Object[]{
                                group.getId(), group.getName(),
                                group.getCoordinates().getX() + ", " + group.getCoordinates().getY(),
                                group.getStudentsCount(), group.getFormOfEducation(),
                                group.getSemesterEnum(),
                                group.getGroupAdmin() != null ? group.getGroupAdmin().getName() : "N/A",
                                group.getOwner()
                        });
                        groups.add(group);
                        userColors.putIfAbsent(group.getOwner(),
                                new Color((group.getOwner().hashCode() * 31) | 0xFF000000));
                    }
                }
            }

            canvasPanel.updateGroups(groups);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void openUpdateDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите группу для обновления");
            return;
        }

        try {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

            CommandRequest request = new CommandRequest("show", new String[]{}, null, username, passwordHash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();

            if (response.getPayload() instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj instanceof StudyGroup sg && sg.getId() == id) {

                        UpdateDialog dialog = new UpdateDialog(this, connection, username, passwordHash, id, sg, getCurrentLocale());
                        dialog.setVisible(true);
                        loadData();
                        return;
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Группа с ID " + id + " не найдена.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении: " + e.getMessage());
        }
    }

    private void removeGroup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите группу для удаления");
            return;
        }

        try {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            CommandRequest request = new CommandRequest("remove_by_id", new String[]{String.valueOf(id)}, null, username, passwordHash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();
            if (response.isSuccess()) loadData();
            else JOptionPane.showMessageDialog(this, response.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка удаления: " + e.getMessage());
        }
    }

    private String getText(String key) {
        Map<String, String> langMap = languageResources.get(currentLanguage);
        if (langMap == null) return key;
        return langMap.getOrDefault(key, key);
    }

    private static class BackgroundPanel extends JPanel {
        private final Image image;

        public BackgroundPanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
