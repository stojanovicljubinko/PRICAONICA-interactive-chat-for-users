package sistem.eb.messager.interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ClientInterface extends Thread implements sistem.eb.messager.interfaces.URCommand {

    //elements 
    final JTextPane info_TP = new JTextPane();
    final JTextPane listUsers_TP = new JTextPane();
    final JTextField importChat_TF = new JTextField();

    private String oldMessage = "";
    private Thread read;
    private String serverName;
    private int PORT;
    private String name;
    BufferedReader input;
    PrintWriter output;
    Socket server;
    int usernameIndex = 0;

    Stack<String> undo = new Stack<>();
    Stack<String> redo = new Stack<>();

    public ClientInterface() {
        this.serverName = "localhost";
        this.PORT = 1100;
        this.name = "nickname";
        String fontfamily = "Arial, sans-serif";
        Font font = new Font(fontfamily, Font.PLAIN, 15);
        final JFrame JFrame1 = new JFrame("PRIČAONICA sistema elektronske biblioteke - SEB");
        JFrame1.getContentPane().setLayout(null);
        JFrame1.setSize(700, 500);
        JFrame1.setResizable(false);
        JFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Module du fil de discussion
        this.info_TP.setBounds(25, 25, 490, 320);
        this.info_TP.setFont(font);
        this.info_TP.setMargin(new Insets(6, 6, 6, 6));
        this.info_TP.setEditable(false);
        JScrollPane jtextFilDiscuSP = new JScrollPane(info_TP);
        jtextFilDiscuSP.setBounds(25, 25, 490, 320);
        this.info_TP.setContentType("text/html");
        this.info_TP.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        // Module de la liste des utilisateurs
        this.listUsers_TP.setBounds(520, 25, 156, 320);
        this.listUsers_TP.setEditable(true);
        this.listUsers_TP.setFont(font);
        this.listUsers_TP.setMargin(new Insets(6, 6, 6, 6));
        this.listUsers_TP.setEditable(false);
        JScrollPane jsplistuser = new JScrollPane(listUsers_TP);
        jsplistuser.setBounds(520, 25, 156, 320);
        this.listUsers_TP.setContentType("text/html");
        this.listUsers_TP.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        // Field message user input
        this.importChat_TF.setBounds(0, 350, 400, 50);
        this.importChat_TF.setFont(font);
        this.importChat_TF.setMargin(new Insets(6, 6, 6, 6));
        final JScrollPane jtextInputChatSP = new JScrollPane(importChat_TF);
        jtextInputChatSP.setBounds(25, 350, 650, 50);
        // button send
        final JButton sent_B = new JButton("Send");
        sent_B.setFont(font);
        sent_B.setBounds(575, 410, 100, 35);
        // button Disconnect
        final JButton disconnect_B = new JButton("Disconnect");
        disconnect_B.setFont(font);
        disconnect_B.setBounds(25, 410, 130, 35);
        importChat_TF.addKeyListener(new KeyAdapter() {

            // send message on Enter
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }

                // Get last message typed
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    String currentMessage = importChat_TF.getText().trim();
                    importChat_TF.setText(oldMessage);
                    oldMessage = currentMessage;
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    String currentMessage = importChat_TF.getText().trim();
                    importChat_TF.setText(oldMessage);
                    oldMessage = currentMessage;
                }
            }
        });
        // Click on send button
        sent_B.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendMessage();
            }
        });
        // Connection view
        final JTextField name_TF = new JTextField(this.name);
        name_TF.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (usernameIndex++ == 0) {
                    name_TF.setText("");
                }
            }
        });
        name_TF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (usernameIndex++ == 0) {
                    name_TF.setText("");
                }
            }

        });
        importChat_TF.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                undo.push(importChat_TF.getText());
            }
        });
        final JTextField port_TF = new JTextField(Integer.toString(this.PORT));
        final JTextField serverName_TF = new JTextField(this.serverName);
        final JButton connect_B = new JButton("Connect");
        // check if those field are not empty
        name_TF.getDocument().addDocumentListener(new TextListener(name_TF, port_TF, serverName_TF, connect_B));
        port_TF.getDocument().addDocumentListener(new TextListener(name_TF, port_TF, serverName_TF, connect_B));
        serverName_TF.getDocument().addDocumentListener(new TextListener(name_TF, port_TF, serverName_TF, connect_B));
        //position of modules
        connect_B.setFont(font);
        serverName_TF.setBounds(25, 380, 135, 40);
        name_TF.setBounds(375, 380, 135, 40);
        port_TF.setBounds(200, 380, 135, 40);
        connect_B.setBounds(575, 380, 100, 40);
        // pane colours
        this.info_TP.setBackground(Color.WHITE);
        this.listUsers_TP.setBackground(Color.WHITE);
        // adding elements
        JFrame1.add(connect_B);
        JFrame1.add(jtextFilDiscuSP);
        JFrame1.add(jsplistuser);
        JFrame1.add(name_TF);
        JFrame1.add(port_TF);
        JFrame1.add(serverName_TF);
        JFrame1.setVisible(true);
        
        // info chat
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader("info.html"));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
        }
        String content = contentBuilder.toString();

        appendToPane(this.info_TP, content);
        // On connect
        connect_B.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    name = name_TF.getText();
                    String port = port_TF.getText();
                    serverName = serverName_TF.getText();
                    PORT = Integer.parseInt(port);

                    appendToPane(info_TP, "<br><span  style=\"color: #666666;\">Connecting to " + serverName + " on port " + PORT + ".</span>");
                    server = new Socket(serverName, PORT);

                    appendToPane(info_TP, "<span  style=\"color: #666666;\">Connected to "
                            + server.getRemoteSocketAddress() + "</span>");

                    input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    output = new PrintWriter(server.getOutputStream(), true);

                    // send nickname to server
                    output.println(name);

                    // create new Read Thread
                    read = new Read();
                    read.start();
                    JFrame1.remove(name_TF);
                    JFrame1.remove(port_TF);
                    JFrame1.remove(serverName_TF);
                    JFrame1.remove(connect_B);
                    JFrame1.add(sent_B);
                    JFrame1.add(jtextInputChatSP);
                    JFrame1.add(disconnect_B);
                    JFrame1.revalidate();
                    JFrame1.repaint();
                    info_TP.setBackground(Color.WHITE);
                    listUsers_TP.setBackground(Color.WHITE);
                } catch (Exception ex) {
                    appendToPane(info_TP, "<span>Could not connect to Server</span>");
                    JOptionPane.showMessageDialog(JFrame1, "Nemoguće uspostaviti vezu sa serverom!");
                }
            }

        });
        // adding items
        disconnect_B.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFrame1.add(name_TF);
                JFrame1.add(port_TF);
                JFrame1.add(serverName_TF);
                JFrame1.add(connect_B);
                JFrame1.remove(sent_B);
                JFrame1.remove(jtextInputChatSP);
                JFrame1.remove(disconnect_B);
                JFrame1.revalidate();
                JFrame1.repaint();
                read.interrupt();
                listUsers_TP.setText(null);
                appendToPane(info_TP, "<span>Connection closed.</span>");
                output.close();
            }
        });
        port_TF.setEditable(false);
        serverName_TF.setEditable(false);
        port_TF.setBackground(Color.WHITE);
        serverName_TF.setBackground(Color.WHITE);
        //undo - redo implements
        importChat_TF.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    undo();
                } catch (Exception ex) {
                    //nedefinisano
                }

            }
        });
        importChat_TF.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    redo();
                } catch (Exception ex) {
                    //nedefinisano
                }

            }
        });
        importChat_TF.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        importChat_TF.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        JFrame1.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sistem/eb/messager/icons/logo.png")));

    }

// check if if all field are not empty
    public class TextListener implements DocumentListener {

        JTextField JTextField1;
        JTextField JTextField2;
        JTextField JTextField3;
        JButton JButton;

        public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn) {
            this.JTextField1 = jtf1;
            this.JTextField2 = jtf2;
            this.JTextField3 = jtf3;
            this.JButton = jcbtn;
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            if (JTextField1.getText().trim().equals("")
                    || JTextField2.getText().trim().equals("")
                    || JTextField3.getText().trim().equals("")) {
                JButton.setEnabled(false);
            } else {
                JButton.setEnabled(true);
            }
        }

        public void insertUpdate(DocumentEvent e) {
            if (JTextField1.getText().trim().equals("")
                    || JTextField2.getText().trim().equals("")
                    || JTextField3.getText().trim().equals("")) {
                JButton.setEnabled(false);
            } else {
                JButton.setEnabled(true);
            }
        }

    }

    // sent message
    public void sendMessage() {
        try {
            String message = importChat_TF.getText().trim();
            if (message.equals("")) {
                return;
            }
            this.oldMessage = message;
            output.println(message);
            importChat_TF.requestFocus();
            importChat_TF.setText(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        ClientInterface client = new ClientInterface();
    }

    // read new incoming messages
    class Read extends Thread {

        public void run() {
            String message;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    message = input.readLine();
                    if (message != null) {
                        if (message.charAt(0) == '[') {
                            message = message.substring(1, message.length() - 1);
                            ArrayList<String> ListUser = new ArrayList<String>(
                                    Arrays.asList(message.split(", "))
                            );
                            listUsers_TP.setText(null);
                            for (String user : ListUser) {
                                appendToPane(listUsers_TP, "sistem/" + user);
                            }
                        } else {
                            appendToPane(info_TP, message);
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to parse incoming message");
                }
            }
        }
    }

    // send html to pane
    private void appendToPane(JTextPane JTextPane, String Message) {
        HTMLDocument doc = (HTMLDocument) JTextPane.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) JTextPane.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), Message, 0, 0, null);
            JTextPane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void redo() {
        if (!this.redo.empty()) {
            this.undo.push(this.redo.pop());
            if (!this.redo.empty()) {
                this.importChat_TF.setText(this.redo.peek());
            }
        }
    }

    @Override
    public void undo() {
        if (!this.undo.empty()) {
            this.redo.push(this.undo.pop());
            if (!this.undo.empty()) {
                this.importChat_TF.setText(this.undo.peek());
            }
        }
    }

}
