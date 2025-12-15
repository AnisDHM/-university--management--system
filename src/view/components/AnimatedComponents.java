package view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.border.*;

/**
 * Composants UI avec animations avancées et effets WOW
 * - Cartes animées 3D
 * - Graphiques interactifs
 * - Progress bars circulaires
 * - Notifications stylées
 */
public class AnimatedComponents {
    
    // ============================================================================
    // CARTE ANIMÉE 3D AVEC FLIP
    // ============================================================================
    
    public static class FlipCard extends JPanel {
        private JPanel frontPanel;
        private JPanel backPanel;
        private boolean isFlipped = false;
        private Timer flipTimer;
        private float flipProgress = 0f;
        
        public FlipCard(JPanel front, JPanel back) {
            this.frontPanel = front;
            this.backPanel = back;
            
            setLayout(new BorderLayout());
            setOpaque(false);
            add(frontPanel, BorderLayout.CENTER);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isFlipped) flip();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (isFlipped) flip();
                }
            });
        }
        
        private void flip() {
            if (flipTimer != null && flipTimer.isRunning()) return;
            
            flipTimer = new Timer(10, e -> {
                flipProgress += 0.1f;
                
                if (flipProgress >= 1.0f) {
                    flipProgress = 0f;
                    isFlipped = !isFlipped;
                    
                    removeAll();
                    add(isFlipped ? backPanel : frontPanel, BorderLayout.CENTER);
                    revalidate();
                    
                    ((Timer) e.getSource()).stop();
                }
                
                repaint();
            });
            flipTimer.start();
        }
    }
    
    // ============================================================================
    // PROGRESS BAR CIRCULAIRE ANIMÉ
    // ============================================================================
    
    public static class CircularProgressBar extends JPanel {
        private int progress = 0;
        private int targetProgress = 0;
        private String label = "";
        private Color progressColor = new Color(70, 130, 240);
        private Timer animationTimer;
        
        public CircularProgressBar(int size) {
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
        }
        
        public void setProgress(int progress, String label) {
            this.targetProgress = Math.min(100, Math.max(0, progress));
            this.label = label;
            animateProgress();
        }
        
        public void setProgressColor(Color color) {
            this.progressColor = color;
        }
        
        private void animateProgress() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            
            animationTimer = new Timer(20, e -> {
                if (progress < targetProgress) {
                    progress += Math.max(1, (targetProgress - progress) / 10);
                } else if (progress > targetProgress) {
                    progress -= Math.max(1, (progress - targetProgress) / 10);
                }
                
                if (Math.abs(progress - targetProgress) <= 1) {
                    progress = targetProgress;
                    ((Timer) e.getSource()).stop();
                }
                
                repaint();
            });
            animationTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int size = Math.min(getWidth(), getHeight()) - 20;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            g2.setStroke(new BasicStroke(10));
            g2.setColor(new Color(230, 230, 230));
            g2.drawOval(x, y, size, size);
            
            g2.setColor(progressColor);
            int angle = (int) (360 * (progress / 100.0));
            g2.drawArc(x, y, size, size, 90, -angle);
            
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
            String text = progress + "%";
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 10;
            g2.drawString(text, textX, textY);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fm = g2.getFontMetrics();
            textX = (getWidth() - fm.stringWidth(label)) / 2;
            textY = (getHeight() + fm.getAscent()) / 2 + 15;
            g2.setColor(new Color(120, 120, 120));
            g2.drawString(label, textX, textY);
            
            g2.dispose();
        }
    }
    
    // ============================================================================
    // GRAPHIQUE BARRES ANIMÉ
    // ============================================================================
    
    public static class AnimatedBarChart extends JPanel {
        private String[] labels;
        private double[] values;
        private double[] currentValues;
        private Color[] colors;
        private Timer animationTimer;
        private String title;
        
        public AnimatedBarChart(String title, String[] labels, double[] values, Color[] colors) {
            this.title = title;
            this.labels = labels;
            this.values = values;
            this.colors = colors;
            this.currentValues = new double[values.length];
            
            setPreferredSize(new Dimension(400, 300));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                new ModernUIComponents.RoundedBorder(new Color(220, 220, 220), 1, 12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            
            animateIn();
        }
        
        private void animateIn() {
            animationTimer = new Timer(20, e -> {
                boolean allComplete = true;
                
                for (int i = 0; i < values.length; i++) {
                    if (currentValues[i] < values[i]) {
                        currentValues[i] += (values[i] - currentValues[i]) * 0.1;
                        allComplete = false;
                    }
                }
                
                if (allComplete) {
                    ((Timer) e.getSource()).stop();
                }
                
                repaint();
            });
            animationTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.setColor(Color.BLACK);
            g2.drawString(title, 20, 30);
            
            int barWidth = (getWidth() - 60) / labels.length;
            int maxHeight = getHeight() - 100;
            double maxValue = getMaxValue();
            
            for (int i = 0; i < labels.length; i++) {
                int x = 30 + i * barWidth;
                int barHeight = (int) ((currentValues[i] / maxValue) * maxHeight);
                int y = getHeight() - 50 - barHeight;
                
                GradientPaint gradient = new GradientPaint(
                    x, y, colors[i].brighter(),
                    x, y + barHeight, colors[i]
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(x, y, barWidth - 20, barHeight, 8, 8);
                
                g2.setColor(colors[i].darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(x, y, barWidth - 20, barHeight, 8, 8);
                
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String valueStr = String.format("%.1f", currentValues[i]);
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (barWidth - 20 - fm.stringWidth(valueStr)) / 2;
                g2.drawString(valueStr, textX, y - 5);
                
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                fm = g2.getFontMetrics();
                textX = x + (barWidth - 20 - fm.stringWidth(labels[i])) / 2;
                g2.drawString(labels[i], textX, getHeight() - 30);
            }
            
            g2.dispose();
        }
        
        private double getMaxValue() {
            double max = 0;
            for (double value : values) {
                max = Math.max(max, value);
            }
            return max * 1.1;
        }
    }
    
    // ============================================================================
    // NOTIFICATION SLIDE ÉLÉGANTE (CORRIGÉE)
    // ============================================================================
    
    public static class SlideNotification extends JWindow {
        private static final int DISPLAY_DURATION = 3000; // 3 secondes
        private Timer slideTimer;
        private boolean isSlidingOut = false;
        
        public SlideNotification(Frame parent, String message, String icon, Color accentColor) {
            super(parent);
            setAlwaysOnTop(true);
            
            JPanel contentPanel = new JPanel(new BorderLayout(15, 15)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(accentColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                    g2.dispose();
                }
            };
            contentPanel.setOpaque(false);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
            
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            iconLabel.setForeground(accentColor);
            
            JLabel messageLabel = new JLabel(message);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            JButton closeBtn = new JButton("✕");
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            closeBtn.setBorderPainted(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setForeground(new Color(150, 150, 150));
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // IMPORTANT : fermer réellement la fenêtre
            closeBtn.addActionListener(e -> slideOut());
            
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(messageLabel, BorderLayout.CENTER);
            topPanel.add(closeBtn, BorderLayout.EAST);
            
            contentPanel.add(iconLabel, BorderLayout.WEST);
            contentPanel.add(topPanel, BorderLayout.CENTER);
            
            setContentPane(contentPanel);
            pack();
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = Math.min(400, screenSize.width - 100);
            setSize(width, getHeight());
            int x = screenSize.width - getWidth() - 20;
            int y = screenSize.height; // point de départ (hors écran)
            setLocation(x, y);
        }
        
        public void showNotification() {
            setVisible(true);
            slideIn();
        }
        
        private void slideIn() {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int targetY = screenSize.height - getHeight() - 50;
            
            if (slideTimer != null && slideTimer.isRunning()) {
                slideTimer.stop();
            }
            
            slideTimer = new Timer(15, e -> {
                int currentY = getY();
                int nextY = currentY - Math.max(2, (currentY - targetY) / 4);
                setLocation(getX(), nextY);
                
                if (nextY <= targetY + 1) {
                    setLocation(getX(), targetY);
                    ((Timer) e.getSource()).stop();
                    
                    // Auto-close après DISPLAY_DURATION
                    Timer t = new Timer(DISPLAY_DURATION, evt -> slideOut());
                    t.setRepeats(false);
                    t.start();
                }
            });
            slideTimer.start();
        }
        
        private void slideOut() {
            if (isSlidingOut) return; // évite plusieurs animations
            isSlidingOut = true;
            
            if (slideTimer != null && slideTimer.isRunning()) {
                slideTimer.stop();
            }
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int endY = screenSize.height;
            
            slideTimer = new Timer(15, e -> {
                int currentY = getY();
                int nextY = currentY + Math.max(2, (endY - currentY) / 4);
                setLocation(getX(), nextY);
                
                if (nextY >= endY) {
                    ((Timer) e.getSource()).stop();
                    dispose();   // ferme définitivement la fenêtre
                }
            });
            slideTimer.start();
        }
    }
    
    // ============================================================================
    // CARTE STATISTIQUE AVEC SPARKLINE
    // ============================================================================
    
    public static class StatCardWithSparkline extends JPanel {
        private String title;
        private String value;
        private String trend;
        private double[] sparklineData;
        private Color accentColor;
        
        public StatCardWithSparkline(String title, String value, String trend, 
                                      double[] sparklineData, Color accentColor) {
            this.title = title;
            this.value = value;
            this.trend = trend;
            this.sparklineData = sparklineData;
            this.accentColor = accentColor;
            
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                new ModernUIComponents.RoundedBorder(new Color(220, 220, 220), 1, 12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            
            setPreferredSize(new Dimension(250, 150));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(248, 249, 250));
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2.setColor(new Color(120, 120, 120));
            g2.drawString(title, 20, 30);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
            g2.setColor(Color.BLACK);
            g2.drawString(value, 20, 70);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            boolean isPositive = trend.startsWith("+");
            g2.setColor(isPositive ? new Color(40, 167, 69) : new Color(220, 53, 69));
            g2.drawString(trend, 20, 90);
            
            if (sparklineData != null && sparklineData.length > 1) {
                drawSparkline(g2);
            }
            
            g2.dispose();
        }
        
        private void drawSparkline(Graphics2D g2) {
            int sparklineY = getHeight() - 40;
            int sparklineHeight = 30;
            int sparklineWidth = getWidth() - 40;
            
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for (double d : sparklineData) {
                max = Math.max(max, d);
                min = Math.min(min, d);
            }
            
            g2.setColor(accentColor);
            g2.setStroke(new BasicStroke(2));
            
            GeneralPath path = new GeneralPath();
            for (int i = 0; i < sparklineData.length; i++) {
                double normalized = (sparklineData[i] - min) / (max - min);
                int x = 20 + (int) (i * sparklineWidth / (sparklineData.length - 1));
                int y = sparklineY + sparklineHeight - (int) (normalized * sparklineHeight);
                
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            
            g2.draw(path);
            
            for (int i = 0; i < sparklineData.length; i++) {
                double normalized = (sparklineData[i] - min) / (max - min);
                int x = 20 + (int) (i * sparklineWidth / (sparklineData.length - 1));
                int y = sparklineY + sparklineHeight - (int) (normalized * sparklineHeight);
                
                g2.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }
    
    // ============================================================================
    // BOUTON AVEC EFFET RIPPLE
    // ============================================================================
    
    public static class RippleButton extends JButton {
        private Point rippleCenter;
        private int rippleRadius = 0;
        private Timer rippleTimer;
        private Color rippleColor;
        
        public RippleButton(String text) {
            super(text);
            rippleColor = new Color(255, 255, 255, 100);
            
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setBackground(new Color(70, 130, 240));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    rippleCenter = e.getPoint();
                    rippleRadius = 0;
                    startRipple();
                }
            });
        }
        
        private void startRipple() {
            if (rippleTimer != null && rippleTimer.isRunning()) {
                rippleTimer.stop();
            }
            
            rippleTimer = new Timer(20, e -> {
                rippleRadius += 15;
                
                if (rippleRadius > getWidth() * 2) {
                    rippleRadius = 0;
                    ((Timer) e.getSource()).stop();
                }
                
                repaint();
            });
            rippleTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            if (rippleRadius > 0 && rippleCenter != null) {
                g2.setColor(rippleColor);
                g2.fillOval(
                    rippleCenter.x - rippleRadius,
                    rippleCenter.y - rippleRadius,
                    rippleRadius * 2,
                    rippleRadius * 2
                );
            }
            
            super.paintComponent(g);
        }
    }
    
    // ============================================================================
    // MÉTHODE UTILITAIRE POUR AFFICHER NOTIFICATION
    // ============================================================================
    
 // ============================================================================
    // MÉTHODE UTILITAIRE POUR AFFICHER NOTIFICATION (compatible Java 8+)
// ============================================================================
public static void showSlideNotification(Frame parent, String message,
                                         NotificationType type) {
    String icon;
    Color color;

    switch (type) {
        case SUCCESS:
            icon = "✓";
            color = new Color(40, 167, 69);
            break;
        case ERROR:
            icon = "✗";
            color = new Color(220, 53, 69);
            break;
        case WARNING:
            icon = "⚠";
            color = new Color(255, 193, 7);
            break;
        case INFO:
        default:
            icon = "ⓘ";
            color = new Color(23, 162, 184);
            break;
    }

    SlideNotification notification = new SlideNotification(parent, message, icon, color);
    notification.showNotification();
}

    
    public enum NotificationType {
        SUCCESS, ERROR, WARNING, INFO
    }
}
