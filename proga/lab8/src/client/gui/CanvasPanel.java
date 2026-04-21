package client.gui;

import common.models.StudyGroup;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class CanvasPanel extends JPanel {
    private static class AnimatedGroup {
        StudyGroup group;
        Color color;
        int currentX, currentY;
        int targetX, targetY;
        int size;

        AnimatedGroup(StudyGroup group, Color color, int x, int y, int size) {
            this.group = group;
            this.color = color;
            this.currentX = x;
            this.currentY = y;
            this.targetX = x;
            this.targetY = y;
            this.size = size;
        }

        void setTarget(int x, int y) {
            this.targetX = x;
            this.targetY = y;
        }

        void updatePosition() {
            currentX += (targetX - currentX) * 0.1;
            currentY += (targetY - currentY) * 0.1;
        }

        boolean contains(int x, int y) {
            int dx = x - (currentX + size / 2);
            int dy = y - (currentY + size / 2);
            return dx * dx + dy * dy <= (size / 2) * (size / 2);
        }
    }

    private final List<AnimatedGroup> animatedGroups = new ArrayList<>();
    private final Map<String, Color> userColors;
    private final Timer animationTimer;
    private int padding = 50;
    private int maxX = 100;
    private int maxY = 100;
    private int step = 50;



    public CanvasPanel(Map<String, Color> sharedUserColors) {
        this.userColors = sharedUserColors;
        setBackground(Color.WHITE);

        animationTimer = new Timer(16, e -> {
            for (AnimatedGroup ag : animatedGroups) {
                ag.updatePosition();
            }
            repaint();
        });
        animationTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (AnimatedGroup ag : animatedGroups) {
                    if (ag.contains(e.getX(), e.getY())) {
                        showGroupInfo(ag.group);
                        break;
                    }
                }
            }
        });
    }

    public void updateGroups(List<StudyGroup> groups) {
        maxX = groups.stream().mapToInt(g -> g.getCoordinates().getX()).max().orElse(100);
        maxY = groups.stream().mapToInt(g -> g.getCoordinates().getY()).max().orElse(100);

        Map<Integer, AnimatedGroup> oldMap = new HashMap<>();
        for (AnimatedGroup ag : animatedGroups) {
            oldMap.put(ag.group.getId(), ag);
        }

        animatedGroups.clear();

        for (StudyGroup group : groups) {
            int tx = scaleX(group.getCoordinates().getX());
            int ty = scaleY(group.getCoordinates().getY());
            int size = 20 + group.getStudentsCount() / 10;
            Color color = userColors.getOrDefault(group.getOwner(), Color.GRAY);

            AnimatedGroup ag = oldMap.get(group.getId());
            if (ag != null) {
                ag.group = group;
                ag.color = color;
                ag.size = size;
                ag.setTarget(tx, ty);
            } else {
                ag = new AnimatedGroup(group, color, tx, ty, size);
            }

            animatedGroups.add(ag);
        }
    }

    private int scaleX(int x) {
        return padding + (x * (getWidth() - 2 * padding)) / Math.max(1, maxX);
    }

    private int scaleY(int y) {
        return getHeight() - padding - (y * (getHeight() - 2 * padding)) / Math.max(1, maxY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (AnimatedGroup ag : animatedGroups) {
            g2d.setColor(ag.color);
            g2d.fillOval(ag.currentX, ag.currentY, ag.size, ag.size);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(ag.currentX, ag.currentY, ag.size, ag.size);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String name = ag.group.getName();
            int textWidth = fm.stringWidth(name);
            int textX = ag.currentX + ag.size / 2 - textWidth / 2;
            int textY = ag.currentY - 5;
            g2d.drawString(name, textX, textY);
        }
    }

    private void drawGrid(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.LIGHT_GRAY);

        int w = getWidth();
        int h = getHeight();

        for (int i = padding; i <= w - padding; i += step) {
            g2.drawLine(i, padding, i, h - padding);
        }
        for (int i = padding; i <= h - padding; i += step) {
            g2.drawLine(padding, i, w - padding, i);
        }

        g2.setColor(Color.BLACK);
        g2.drawLine(padding, h - padding, w - padding, h - padding);
        g2.drawLine(padding, padding, padding, h - padding);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= maxX; i += Math.max(1, maxX / 10)) {
            int x = scaleX(i);
            g2.drawString(String.valueOf(i), x, h - padding + 15);
        }
        for (int i = 0; i <= maxY; i += Math.max(1, maxY / 10)) {
            int y = scaleY(i);
            g2.drawString(String.valueOf(i), padding - 30, y + 5);
        }
    }

    private void showGroupInfo(StudyGroup group) {
        String msg = String.format("""
                ID: %d\nНазвание: %s\nКоординаты: (%d, %d)\nСтуденты: %d\nСеместр: %s\nФорма: %s\nАдмин: %s\nВладелец: %s""",
                group.getId(), group.getName(), group.getCoordinates().getX(), group.getCoordinates().getY(),
                group.getStudentsCount(), group.getSemesterEnum(), group.getFormOfEducation(),
                group.getGroupAdmin() != null ? group.getGroupAdmin().getName() : "N/A", group.getOwner()
        );
        JOptionPane.showMessageDialog(this, msg, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        animationTimer.stop();
    }
}