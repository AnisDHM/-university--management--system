package view.components;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**

 */
public class AdvancedSearchSystem {
    
    // ============================================================================
    // COMPOSANT DE RECHERCHE AVEC AUTOCOMPLÉTION
    // ============================================================================
    
    public static class SearchBar extends JPanel {
        private JTextField searchField;
        private JPopupMenu suggestionsPopup;
        private DefaultListModel<SearchResult> suggestionsModel;
        private JList<SearchResult> suggestionsList;
        private List<SearchResult> allResults;
        private SearchCallback callback;
        private Timer searchTimer;
        private List<String> searchHistory;
        
        public SearchBar(String placeholder, List<SearchResult> results, SearchCallback callback) {
            this.allResults = results;
            this.callback = callback;
            this.searchHistory = new ArrayList<>();
            
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            
            // Icône de recherche
            JLabel searchIcon = new JLabel("🔍");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            
            // Champ de recherche
            searchField = new JTextField();
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            searchField.putClientProperty("JTextField.placeholderText", placeholder);
            
            // Panel principal
            JPanel searchPanel = new JPanel(new BorderLayout());
            searchPanel.setBackground(Color.WHITE);
            searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new ModernUIComponents.RoundedBorder(new Color(200, 200, 200), 1, 25),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            searchPanel.add(searchIcon, BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);
            
            // Bouton effacer
            JButton clearBtn = new JButton("✕");
            clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            clearBtn.setForeground(new Color(150, 150, 150));
            clearBtn.setBorderPainted(false);
            clearBtn.setContentAreaFilled(false);
            clearBtn.setFocusPainted(false);
            clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            clearBtn.setVisible(false);
            clearBtn.addActionListener(e -> {
                searchField.setText("");
                clearBtn.setVisible(false);
                hideSuggestions();
            });
            
            searchPanel.add(clearBtn, BorderLayout.EAST);
            
            add(searchPanel, BorderLayout.CENTER);
            
            // Popup de suggestions
            setupSuggestionsPopup();
            
            // Listeners
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    scheduleSearch();
                    clearBtn.setVisible(!searchField.getText().isEmpty());
                }
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    scheduleSearch();
                    clearBtn.setVisible(!searchField.getText().isEmpty());
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    scheduleSearch();
                }
            });
            
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    handleKeyPress(e);
                }
            });
            
            searchField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!searchField.getText().isEmpty()) {
                        showSuggestions();
                    } else if (!searchHistory.isEmpty()) {
                        showHistory();
                    }
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    // Délai pour permettre le clic sur suggestion
                    Timer timer = new Timer(200, evt -> hideSuggestions());
                    timer.setRepeats(false);
                    timer.start();
                }
            });
        }
        
        private void setupSuggestionsPopup() {
            suggestionsPopup = new JPopupMenu();
            suggestionsPopup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            
            suggestionsModel = new DefaultListModel<>();
            suggestionsList = new JList<>(suggestionsModel);
            suggestionsList.setCellRenderer(new SearchResultRenderer());
            suggestionsList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            suggestionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            suggestionsList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        selectSuggestion();
                    }
                }
            });
            
            suggestionsList.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int index = suggestionsList.locationToIndex(e.getPoint());
                    suggestionsList.setSelectedIndex(index);
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(suggestionsList);
            scrollPane.setBorder(null);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            
            suggestionsPopup.add(scrollPane);
        }
        
        private void scheduleSearch() {
            if (searchTimer != null && searchTimer.isRunning()) {
                searchTimer.stop();
            }
            
            searchTimer = new Timer(300, e -> performSearch());
            searchTimer.setRepeats(false);
            searchTimer.start();
        }
        
        private void performSearch() {
            String query = searchField.getText().trim();
            
            if (query.isEmpty()) {
                hideSuggestions();
                return;
            }
            
            // Recherche fuzzy
            List<SearchResult> results = fuzzySearch(query, allResults);
            
            if (!results.isEmpty()) {
                updateSuggestions(results);
                showSuggestions();
            } else {
                hideSuggestions();
            }
        }
        
        private List<SearchResult> fuzzySearch(String query, List<SearchResult> data) {
            String lowerQuery = query.toLowerCase();
            List<SearchResult> results = new ArrayList<>();
            
            for (SearchResult item : data) {
                String lowerTitle = item.title.toLowerCase();
                String lowerSubtitle = item.subtitle != null ? item.subtitle.toLowerCase() : "";
                
                // Score de pertinence
                int score = 0;
                
                // Correspondance exacte au début
                if (lowerTitle.startsWith(lowerQuery)) {
                    score += 100;
                }
                
                // Contient la query
                if (lowerTitle.contains(lowerQuery)) {
                    score += 50;
                }
                
                // Correspondance dans le subtitle
                if (lowerSubtitle.contains(lowerQuery)) {
                    score += 25;
                }
                
                // Correspondance de mots
                String[] queryWords = lowerQuery.split("\\s+");
                for (String word : queryWords) {
                    if (lowerTitle.contains(word)) {
                        score += 10;
                    }
                }
                
                if (score > 0) {
                    SearchResult result = new SearchResult(item);
                    result.relevanceScore = score;
                    results.add(result);
                }
            }
            
            // Trier par pertinence
            results.sort((a, b) -> Integer.compare(b.relevanceScore, a.relevanceScore));
            
            return results.subList(0, Math.min(10, results.size()));
        }
        
        private void updateSuggestions(List<SearchResult> results) {
            suggestionsModel.clear();
            for (SearchResult result : results) {
                suggestionsModel.addElement(result);
            }
        }
        
        private void showSuggestions() {
            if (suggestionsModel.isEmpty()) return;
            
            suggestionsPopup.show(searchField, 0, searchField.getHeight());
            suggestionsPopup.setPreferredSize(new Dimension(searchField.getWidth(), 
                Math.min(200, suggestionsModel.size() * 50)));
            suggestionsPopup.revalidate();
        }
        
        private void showHistory() {
            suggestionsModel.clear();
            for (String query : searchHistory) {
                SearchResult historyItem = new SearchResult(
                    "🕐 " + query,
                    "Recherche récente",
                    SearchResult.Type.HISTORY,
                    query
                );
                suggestionsModel.addElement(historyItem);
            }
            showSuggestions();
        }
        
        private void hideSuggestions() {
            suggestionsPopup.setVisible(false);
        }
        
        private void handleKeyPress(KeyEvent e) {
            if (!suggestionsPopup.isVisible()) return;
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    int nextIndex = suggestionsList.getSelectedIndex() + 1;
                    if (nextIndex < suggestionsModel.size()) {
                        suggestionsList.setSelectedIndex(nextIndex);
                        suggestionsList.ensureIndexIsVisible(nextIndex);
                    }
                    e.consume();
                    break;
                    
                case KeyEvent.VK_UP:
                    int prevIndex = suggestionsList.getSelectedIndex() - 1;
                    if (prevIndex >= 0) {
                        suggestionsList.setSelectedIndex(prevIndex);
                        suggestionsList.ensureIndexIsVisible(prevIndex);
                    }
                    e.consume();
                    break;
                    
                case KeyEvent.VK_ENTER:
                    selectSuggestion();
                    e.consume();
                    break;
                    
                case KeyEvent.VK_ESCAPE:
                    hideSuggestions();
                    e.consume();
                    break;
            }
        }
        
        private void selectSuggestion() {
            SearchResult selected = suggestionsList.getSelectedValue();
            if (selected != null) {
                String query = searchField.getText();
                if (!query.isEmpty() && !searchHistory.contains(query)) {
                    searchHistory.add(0, query);
                    if (searchHistory.size() > 10) {
                        searchHistory.remove(10);
                    }
                }
                
                hideSuggestions();
                
                if (callback != null) {
                    callback.onResultSelected(selected);
                }
            }
        }
        
        public void setResults(List<SearchResult> results) {
            this.allResults = results;
        }
        
        public String getSearchText() {
            return searchField.getText();
        }
        
        public void clearSearch() {
            searchField.setText("");
            hideSuggestions();
        }
    }
    
    // ============================================================================
    // MODÈLE DE RÉSULTAT DE RECHERCHE
    // ============================================================================
    
    public static class SearchResult {
        public String title;
        public String subtitle;
        public Type type;
        public Object data;
        public int relevanceScore;
        
        public enum Type {
            STUDENT, PROFESSOR, MODULE, GRADE, HISTORY, OTHER
        }
        
        public SearchResult(String title, String subtitle, Type type, Object data) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
            this.data = data;
            this.relevanceScore = 0;
        }
        
        public SearchResult(SearchResult other) {
            this.title = other.title;
            this.subtitle = other.subtitle;
            this.type = other.type;
            this.data = other.data;
            this.relevanceScore = other.relevanceScore;
        }
        
        public String getIcon() {
            switch (type) {
                case STUDENT: return "🎓";
                case PROFESSOR: return "👨‍🏫";
                case MODULE: return "📚";
                case GRADE: return "📝";
                case HISTORY: return "🕐";
                default: return "📄";
            }
        }
    }
    
    // ============================================================================
    // RENDERER PERSONNALISÉ POUR LES RÉSULTATS
    // ============================================================================
    
    private static class SearchResultRenderer extends JPanel implements ListCellRenderer<SearchResult> {
        private JLabel iconLabel;
        private JLabel titleLabel;
        private JLabel subtitleLabel;
        
        public SearchResultRenderer() {
            setLayout(new BorderLayout(10, 5));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            
            iconLabel = new JLabel();
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            
            titleLabel = new JLabel();
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            subtitleLabel = new JLabel();
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            subtitleLabel.setForeground(new Color(120, 120, 120));
            
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);
            
            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends SearchResult> list,
                SearchResult value, int index, boolean isSelected, boolean cellHasFocus) {
            
            iconLabel.setText(value.getIcon());
            titleLabel.setText(value.title);
            subtitleLabel.setText(value.subtitle != null ? value.subtitle : "");
            
            if (isSelected) {
                setBackground(new Color(230, 240, 255));
            } else {
                setBackground(Color.WHITE);
            }
            
            return this;
        }
    }
    
    // ============================================================================
    // CALLBACK INTERFACE
    // ============================================================================
    
    public interface SearchCallback {
        void onResultSelected(SearchResult result);
    }
    
    // ============================================================================
    // PANNEAU DE FILTRES AVANCÉS
    // ============================================================================
    
    public static class AdvancedFiltersPanel extends JPanel {
        private Map<String, JCheckBox> filterCheckboxes;
        private Map<String, JComboBox<String>> filterComboBoxes;
        private FilterCallback callback;
        
        public AdvancedFiltersPanel() {
            this.filterCheckboxes = new HashMap<>();
            this.filterComboBoxes = new HashMap<>();
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                new ModernUIComponents.RoundedBorder(new Color(220, 220, 220), 1, 12),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        }
        
        public void addCheckboxFilter(String name, String label, boolean defaultValue) {
            JCheckBox checkbox = new JCheckBox(label, defaultValue);
            checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            checkbox.setOpaque(false);
            checkbox.addActionListener(e -> notifyFilterChange());
            
            filterCheckboxes.put(name, checkbox);
            add(checkbox);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        public void addComboBoxFilter(String name, String label, String[] options) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            panel.setOpaque(false);
            
            JLabel lbl = new JLabel(label + ":");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            JComboBox<String> combo = new JComboBox<>(options);
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            combo.addActionListener(e -> notifyFilterChange());
            
            panel.add(lbl);
            panel.add(combo);
            
            filterComboBoxes.put(name, combo);
            add(panel);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        public void addSeparator() {
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(sep);
            add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        public boolean isFilterEnabled(String name) {
            JCheckBox checkbox = filterCheckboxes.get(name);
            return checkbox != null && checkbox.isSelected();
        }
        
        public String getComboBoxValue(String name) {
            JComboBox<String> combo = filterComboBoxes.get(name);
            return combo != null ? (String) combo.getSelectedItem() : null;
        }
        
        public void setCallback(FilterCallback callback) {
            this.callback = callback;
        }
        
        private void notifyFilterChange() {
            if (callback != null) {
                callback.onFiltersChanged();
            }
        }
        
        public Map<String, Object> getActiveFilters() {
            Map<String, Object> filters = new HashMap<>();
            
            for (Map.Entry<String, JCheckBox> entry : filterCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    filters.put(entry.getKey(), true);
                }
            }
            
            for (Map.Entry<String, JComboBox<String>> entry : filterComboBoxes.entrySet()) {
                filters.put(entry.getKey(), entry.getValue().getSelectedItem());
            }
            
            return filters;
        }
    }
    
    public interface FilterCallback {
        void onFiltersChanged();
    }
    
    // ============================================================================
    // PANNEAU DE RECHERCHE COMPLET (BARRE + FILTRES)
    // ============================================================================
    
    public static class CompleteSearchPanel extends JPanel {
        private SearchBar searchBar;
        private AdvancedFiltersPanel filtersPanel;
        private JButton toggleFiltersBtn;
        private boolean filtersVisible = false;
        
        public CompleteSearchPanel(String placeholder, List<SearchResult> results, 
                                   SearchCallback searchCallback) {
            setLayout(new BorderLayout(10, 10));
            setOpaque(false);
            
            // Panel supérieur: barre de recherche + bouton filtres
            JPanel topPanel = new JPanel(new BorderLayout(10, 0));
            topPanel.setOpaque(false);
            
            searchBar = new SearchBar(placeholder, results, searchCallback);
            
            toggleFiltersBtn = new JButton("⚙️ Filtres");
            toggleFiltersBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            toggleFiltersBtn.setFocusPainted(false);
            toggleFiltersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            toggleFiltersBtn.addActionListener(e -> toggleFilters());
            
            topPanel.add(searchBar, BorderLayout.CENTER);
            topPanel.add(toggleFiltersBtn, BorderLayout.EAST);
            
            // Panel de filtres (initialement caché)
            filtersPanel = new AdvancedFiltersPanel();
            filtersPanel.setVisible(false);
            
            add(topPanel, BorderLayout.NORTH);
            add(filtersPanel, BorderLayout.CENTER);
        }
        
        private void toggleFilters() {
            filtersVisible = !filtersVisible;
            filtersPanel.setVisible(filtersVisible);
            toggleFiltersBtn.setText(filtersVisible ? "⚙️ Masquer" : "⚙️ Filtres");
            revalidate();
            repaint();
        }
        
        public SearchBar getSearchBar() {
            return searchBar;
        }
        
        public AdvancedFiltersPanel getFiltersPanel() {
            return filtersPanel;
        }
    }
}