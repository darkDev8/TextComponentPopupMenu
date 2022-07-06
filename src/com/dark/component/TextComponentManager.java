package com.dark.component;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class TextComponentManager {

    private JTextComponent component;
    private TextComponentMenuType menuType;

    private JPopupMenu menu;

    private JMenuItem cutItem;
    private JMenuItem copyItem;
    private JMenuItem pasteItem;
    private JMenuItem deleteItem;
    private JMenuItem dateTimeItem;
    private JMenuItem selectAllItem;
    private JCheckBoxMenuItem rightToLeftItem;
    private JMenuItem clearItem;
    private JMenuItem findItem;

    private Font font;
    private Color textColor;

    private int[] padding;
    private boolean enableFindError;

    private UndoManager undo;
    private Document doc;
    private String selectedText;

    private TextComponentManager(MenuBuilder builder) {
        this.component = builder.component;
        this.menuType = builder.menuType;

        this.menu = builder.menu;

        cutItem = builder.cutItem;
        copyItem = builder.copyItem;
        pasteItem = builder.pasteItem;
        deleteItem = builder.deleteItem;
        dateTimeItem = builder.dateTimeItem;
        selectAllItem = builder.selectAllItem;
        rightToLeftItem = builder.rightToLeftItem;
        clearItem = builder.clearItem;
        findItem = builder.findItem;

        this.font = builder.font;
        this.textColor = builder.textColor;
        this.padding = builder.padding;
        this.enableFindError = builder.enableFindError;

        this.undo = new UndoManager();
        this.doc = component.getDocument();
        selectedText = null;

        enableTextComponentUndoListener(undo, doc);
        implementMenuItemListeners();

        undo();
        redo();
    }

    public void show() {

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent releasedEvent) {
                if (SwingUtilities.isRightMouseButton(releasedEvent)) {
                    selectedText = component.getSelectedText();

                    selectAllItem.setEnabled(!component.getText().equals(""));
                    clearItem.setEnabled(!component.getText().equals(""));
                    findItem.setEnabled(!component.getText().equals(""));

                    copyItem.setEnabled(selectedText != null);
                    cutItem.setEnabled(selectedText != null);
                    deleteItem.setEnabled(selectedText != null);

                    menu.show(component, releasedEvent.getX(), releasedEvent.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent releasedEvent) {
            }
        });
    }

    public void selectAll() {
        component.selectAll();
    }

    public void clear() {
        component.setText("");
    }

    public void undo() {
        component.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canUndo()) {
                        undo.undo();
                    }
                } catch (CannotUndoException e) {
                    e.printStackTrace();
                }
            }
        });

        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
    }

    public void redo() {
        component.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canRedo()) {
                        undo.redo();
                    }
                } catch (CannotRedoException e) {
                    e.printStackTrace();
                }
            }
        });

        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }

    public void rightToLeft() {
        if (rightToLeftItem.isSelected()) {
            component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            component.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }
    }

    public void copy() {
        try {
            StringSelection stringSelection = new StringSelection(selectedText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cut() {
        copy();
        component.setText(component.getText().replace(selectedText, ""));
    }

    public void paste() {
        if (selectedText == null) {
            int cursorPosition = component.getCaretPosition();
            String newText = component.getText().substring(0, cursorPosition)
                    .concat(pasteClipboard())
                    .concat(component.getText().substring(cursorPosition));

            component.setText(newText);
        } else {
            component.setText(component.getText().replace(selectedText, pasteClipboard()));
        }
    }

    public void delete() {
        component.setText(component.getText().replace(selectedText, ""));
    }

    public void dateTime() {
        if (selectedText == null) {
            int cursorPosition = component.getCaretPosition();
            String newText = component.getText().substring(0, cursorPosition)
                    .concat(getSystemDateTime())
                    .concat(component.getText().substring(cursorPosition));

            component.setText(newText);
        } else {
            component.setText(component.getText().replace(selectedText, getSystemDateTime()));
        }
    }

    private String pasteClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            DataFlavor flavor = DataFlavor.stringFlavor;
            return clipboard.isDataFlavorAvailable(flavor) ? clipboard.getData(flavor).toString() : new String();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String getSystemTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH : mm : ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private String getSystemDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private String getSystemDateTime() {
        return String.format("%s %s", getSystemDate(), getSystemTime());
    }

    private void enableTextComponentUndoListener(UndoManager undo, Document doc) {
        doc.addUndoableEditListener((UndoableEditEvent evt) -> {
            undo.addEdit(evt.getEdit());
        });
    }

    private void implementMenuItemListeners() {
        selectAllItem.addActionListener((ActionEvent ae) -> {
            selectAll();
        });

        clearItem.addActionListener((ActionEvent ae) -> {
            clear();
        });

        rightToLeftItem.addActionListener((ActionEvent ae) -> {
            rightToLeft();
        });

        copyItem.addActionListener((ActionEvent ae) -> {
            copy();
        });

        cutItem.addActionListener((ActionEvent ae) -> {
            cut();
        });

        pasteItem.addActionListener((ActionEvent ae) -> {
            paste();
        });

        deleteItem.addActionListener((ActionEvent ae) -> {
            delete();
        });

        dateTimeItem.addActionListener((ActionEvent ae) -> {
            dateTime();
        });

        findItem.addActionListener((ActionEvent ae) -> {
            String text = JOptionPane.showInputDialog(null, "Type text you want to search", "Search...", JOptionPane.PLAIN_MESSAGE);

            if (text != null) {
                String componentText = component.getText();
                if (componentText.contains(text)) {
                    int startIndex = componentText.indexOf(text), endIndex = (startIndex + text.length());

                    component.setSelectionStart(startIndex);
                    component.setSelectionEnd(endIndex);
                } else {
                   if (enableFindError) {
                       JOptionPane.showMessageDialog(null, "The text doesn't found.", "No text found", JOptionPane.ERROR_MESSAGE);
                   }
                }
            }
        });
    }

    public JTextComponent getComponent() {
        return component;
    }

    public Font getFont() {
        return font;
    }

    public Color getTextColor() {
        return textColor;
    }

    public static class MenuBuilder {

        private JTextComponent component;
        private TextComponentMenuType menuType;

        private JPopupMenu menu;

        private JMenuItem cutItem;
        private JMenuItem copyItem;
        private JMenuItem pasteItem;
        private JMenuItem deleteItem;
        private JMenuItem dateTimeItem;
        private JMenuItem selectAllItem;
        private JCheckBoxMenuItem rightToLeftItem;
        private JMenuItem clearItem;
        private JMenuItem findItem;

        private Font font;
        private Color textColor;

        private int[] padding = {2, 2, 2, 2};
        private boolean enableFindError;

        public MenuBuilder(JTextComponent component) {
            this.component = component;

            this.menu = new JPopupMenu();
            this.cutItem = new JMenuItem("Cut");
            this.copyItem = new JMenuItem("Copy");
            this.pasteItem = new JMenuItem("Paste");
            this.deleteItem = new JMenuItem("Delete");
            this.dateTimeItem = new JMenuItem("Date/Time");
            this.selectAllItem = new JMenuItem("Select all");
            this.rightToLeftItem = new JCheckBoxMenuItem("Right To Left");
            this.clearItem = new JMenuItem("Clear");
            this.findItem = new JMenuItem("Find");

            font = new Font("Inter medium", Font.PLAIN, 12);
            textColor = Color.black;
            enableFindError = false;

            menuType = TextComponentMenuType.MAXIMUM;
        }

        public MenuBuilder setComponent(JTextComponent component) {
            this.component = component;
            return this;
        }

        public MenuBuilder setFont(Font menuFont) {
            this.font = menuFont;
            return this;
        }

        public MenuBuilder setTextColor(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public MenuBuilder setPadding(int[] padding) {
            this.padding = padding;
            return this;
        }

        public MenuBuilder setEnableFindError(boolean enableFindError) {
            this.enableFindError = enableFindError;
            return this;
        }

        public MenuBuilder setMenuType(TextComponentMenuType menuType) {
            this.menuType = menuType;
            return this;
        }

        public TextComponentManager build() {
            List<JMenuItem> menuItems = Arrays.asList(
                    cutItem, copyItem, pasteItem, deleteItem,
                    dateTimeItem, selectAllItem, rightToLeftItem,
                    clearItem, findItem
            );

            for (JMenuItem item : menuItems) {
                item.setFont(font);
                item.setBorder(new EmptyBorder(padding[0], padding[1], padding[2], padding[3]));
                item.setForeground(textColor);
            }

            menu.removeAll();
            switch (menuType) {
                case MAXIMUM -> {
                    menu.add(selectAllItem);
                    menu.add(new JSeparator());
                    menu.add(cutItem);
                    menu.add(copyItem);
                    menu.add(pasteItem);
                    menu.add(deleteItem);
                    menu.add(new JSeparator());
                    menu.add(dateTimeItem);
                    menu.add(new JSeparator());
                    menu.add(rightToLeftItem);
                    menu.add(new JSeparator());
                    menu.add(clearItem);
                    menu.add(findItem);
                }

                case NORMAL -> {
                    menu.add(selectAllItem);
                    menu.add(new JSeparator());
                    menu.add(cutItem);
                    menu.add(copyItem);
                    menu.add(pasteItem);
                    menu.add(deleteItem);
                    menu.add(new JSeparator());
                    menu.add(dateTimeItem);
                    menu.add(new JSeparator());
                    menu.add(clearItem);
                }

                case MINIMUM -> {
                    menu.add(selectAllItem);
                    menu.add(new JSeparator());
                    menu.add(cutItem);
                    menu.add(copyItem);
                    menu.add(pasteItem);
                    menu.add(deleteItem);
                }
            }

            return new TextComponentManager(this);
        }
    }
}
