import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.metal.*;

public class MDI2 {
    static JDesktopPane desktopPane = new JDesktopPane();
    static JPanel rectanglePanel = new JPanel(); // Keep the rectangle panel
    static JFrame mainFrame = new JFrame("Texitor MKi");
    static JMenuBar mainMenuBar = new JMenuBar(); // Main menu bar (always visible)
    static JButton newEditorButton = new JButton("New Editor"); // Always-visible button
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1024, 768);
            mainFrame.setContentPane(desktopPane);
            
            mainFrame.add(newEditorButton);
            newEditorButton.setBounds(5, 20, 190, 20);
            
            mainFrame.setJMenuBar(mainMenuBar);
            
            newEditorButton.addActionListener(e -> createNewEditor());
            
            mainFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    rectanglePanel.setBounds(0, 0, 200, desktopPane.getHeight());
                }
            });
            
            mainFrame.setVisible(true);
            rectanglePanel.setBackground(Color.WHITE);
            mainFrame.add(rectanglePanel);
        });
    }
    
    private static void createNewEditor() {
        editor newEditor = new editor();
        newEditor.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                if (e.getInternalFrame() instanceof editor) {
                    editor activeEditor = (editor) e.getInternalFrame();
                    updateMainMenuBar(activeEditor.getEditorMenuBar());
                }
            }
            
            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                resetMainMenuBar();
            }
        });
        
        newEditor.setSize(400, 300);
        newEditor.setVisible(true);
        newEditor.setLocation(50 + (int) (Math.random() * 300), 50 + (int) (Math.random() * 300));
        newEditor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                int minX = 200, minY = 0;
                int maxX = desktopPane.getWidth() - newEditor.getWidth();
                int maxY = desktopPane.getHeight() - newEditor.getHeight();

                // Get current position
                Point p = newEditor.getLocation();
                int newX = Math.max(minX, Math.min(p.x, maxX));
                int newY = Math.max(minY, Math.min(p.y, maxY));

                // Set the corrected position
                newEditor.setLocation(newX, newY);
            }
        });
        desktopPane.add(newEditor);
        desktopPane.moveToFront(newEditor);
    }
    
    private static void updateMainMenuBar(JMenuBar editorMenuBar) {
        mainMenuBar.removeAll();
        
        for (Component component : editorMenuBar.getComponents()) {
            mainMenuBar.add(component);
        }
        
        mainFrame.setJMenuBar(mainMenuBar);
        mainFrame.revalidate();
    }
    
    private static void resetMainMenuBar() {
        mainMenuBar.removeAll();
        mainFrame.setJMenuBar(mainMenuBar);
        mainFrame.revalidate();
    }
}

class editor extends JInternalFrame implements ActionListener {
    JTextArea t;
    JMenuBar mb;
    File currentFile;
    
    editor() {
        super("Idle", true, true, true, true);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
        t = new JTextArea();
        mb = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem openFile = new JMenuItem("Open");
        
        saveFile.addActionListener(this);
        openFile.addActionListener(this);
        
        fileMenu.add(saveFile);
        fileMenu.add(openFile);
        
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cut = new JMenuItem("Cut");
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        
        cut.addActionListener(this);
        copy.addActionListener(this);
        paste.addActionListener(this);
        
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        
        mb.add(fileMenu);
        mb.add(editMenu);
        
        t.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
            // Ctrl + S to Save
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    saveFile();
                    e.consume(); // Prevent default behavior
                }
    
                // Ctrl + O to Open
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_O) {
                    openFile();
                    e.consume(); // Prevent default behavior
                }

                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    t.replaceSelection("    ");
                    e.consume();
                }

                // Auto-closing brackets and quotes
                switch (e.getKeyChar()) {
                    case '[':
                        t.replaceSelection("]");
                        t.setCaretPosition(t.getCaretPosition() - 1);
                        e.consume();
                        break;
                    case '(':
                        t.replaceSelection(")");
                        t.setCaretPosition(t.getCaretPosition() - 1);
                        e.consume();
                        break;
                    case '{':
                        t.replaceSelection("}");
                        t.setCaretPosition(t.getCaretPosition() - 1);
                        e.consume();
                        break;
                    case '\'':
                        t.replaceSelection("'");  // Single quote needs to be properly escaped
                        t.setCaretPosition(t.getCaretPosition() - 1);
                        e.consume();
                        break;
                    case '"':
                        t.replaceSelection("\""); // Double quote escape
                        t.setCaretPosition(t.getCaretPosition() - 1);
                        e.consume();
                        break;
                }
            }
        });

        
        add(new JScrollPane(t));
        setSize(500, 500);
        setVisible(true);
    }
    
    public JMenuBar getEditorMenuBar() {
        return mb;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Save")) {
            saveFile();
        } else if (s.equals("Open")) {
            openFile();
        } else if (s.equals("Cut")) {
            t.cut();
        } else if (s.equals("Copy")) {
            t.copy();
        } else if (s.equals("Paste")) {
            t.paste();
        }
    }

    
    
    private void saveFile() {
        try {
            String currentLaf = UIManager.getLookAndFeel().getClass().getName();
    
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
    
            JFileChooser j = new JFileChooser();
            int r;
            if (currentFile == null) {
                r = j.showSaveDialog(this);
            } else {
                j.setSelectedFile(currentFile); // Default to the current file location
                r = j.showSaveDialog(this);
            }
            
            if (r == JFileChooser.APPROVE_OPTION) {
                File fileToSave = j.getSelectedFile();
                try (BufferedWriter w = new BufferedWriter(new FileWriter(fileToSave))) {
                    w.write(t.getText());
                    currentFile = fileToSave; // Update the current file
                    setTitle(currentFile.getAbsolutePath());
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(this, evt.getMessage());
                }
            }
    
            UIManager.setLookAndFeel(currentLaf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void openFile() {
        try {
            String currentLaf = UIManager.getLookAndFeel().getClass().getName();
    
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
    
            JFileChooser j = new JFileChooser();
            int r = j.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File selectedFile = j.getSelectedFile();
                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                    t.read(br, null);
                    currentFile = selectedFile; // Save the current file
                    setTitle(currentFile.getAbsolutePath());
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(this, evt.getMessage());
                }
            }
    
            UIManager.setLookAndFeel(currentLaf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
