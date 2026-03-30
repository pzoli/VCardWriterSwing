/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hu.infokristaly.vcardwriterswing;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author pzoli
 */
public class SerialPortManager extends javax.swing.JDialog implements jssc.SerialPortEventListener {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SerialPort.class.getName());
    private int selectedRow = -1;
    private static final int DATA_SEND_DELAY = 3000;
    private static final int BUFFER_SIZE = 20;
    
    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            SerialPortManager dialog = new SerialPortManager(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            
            String[] columns = {"Name", "E-mail", "Phone"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            dialog.tblPersons.setModel(model);

            // @Test Win
            //File file = new File("C:\\Users\\pzoli\\Documents\\NetBeansProjects\\VCardWriterSwing\\docs\\dataForVCard.csv");
            // @Test Mac
            //File file = new File("/Users/pzoli/NetBeansProjects/VCardWriterSwing/docs/dataForVCard.csv");
            // @Test Linux
            //File file = new File("/home/pzoli/NetBeansProjects/VCardWriterSwing/docs/dataForVCard.csv");
            
            //importCSV(file, model);
            
            dialog.tblPersons.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    dialog.selectedRow = dialog.tblPersons.getSelectedRow();
                    if (dialog.selectedRow != -1) {
                        String name = dialog.tblPersons.getValueAt(dialog.selectedRow, 0).toString();
                        String email = dialog.tblPersons.getValueAt(dialog.selectedRow, 1).toString();
                        String phone = dialog.tblPersons.getValueAt(dialog.selectedRow, 2).toString();

                        dialog.txtNameForClassic.setText(name);
                        dialog.txtEmailFroClassic.setText(email);
                        dialog.txtPhoneForClassic.setText(phone);
                    }
                }
            });

            dialog.tblPersons.getModel().addTableModelListener(e -> {
                if (e.getType() == TableModelEvent.UPDATE && dialog.tblPersons.isEditing()) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    int selectedRow = dialog.tblPersons.getSelectedRow();

                    if (row == selectedRow && row != -1) {
                        Object newValue = dialog.tblPersons.getValueAt(row, column);
                        String valueStr = (newValue != null) ? newValue.toString() : "";

                        switch (column) {
                            case 0 -> dialog.txtNameForClassic.setText(valueStr);
                            case 1 -> dialog.txtEmailFroClassic.setText(valueStr);
                            case 2 -> dialog.txtPhoneForClassic.setText(valueStr);
                        }
                    }
                }
            });

            dialog.txtNameForClassic.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { updateTable(); }
                public void removeUpdate(DocumentEvent e) { updateTable(); }
                public void insertUpdate(DocumentEvent e) { updateTable(); }

                private void updateTable() {
                    int selectedRow = dialog.tblPersons.getSelectedRow();
                    if (selectedRow != -1) {
                        if (dialog.tblPersons.isEditing()) {
                            dialog.tblPersons.getCellEditor().cancelCellEditing();
                        }
                        dialog.tblPersons.getModel().setValueAt(dialog.txtNameForClassic.getText(), selectedRow, 0);
                    }
                }
            });

            dialog.txtEmailFroClassic.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { updateTable(); }
                public void removeUpdate(DocumentEvent e) { updateTable(); }
                public void insertUpdate(DocumentEvent e) { updateTable(); }

                private void updateTable() {
                    int selectedRow = dialog.tblPersons.getSelectedRow();
                    if (selectedRow != -1) {
                        if (dialog.tblPersons.isEditing()) {
                            dialog.tblPersons.getCellEditor().cancelCellEditing();
                        }
                        dialog.tblPersons.getModel().setValueAt(dialog.txtEmailFroClassic.getText(), selectedRow, 1);
                    }
                }
            });

            dialog.txtPhoneForClassic.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { updateTable(); }
                public void removeUpdate(DocumentEvent e) { updateTable(); }
                public void insertUpdate(DocumentEvent e) { updateTable(); }

                private void updateTable() {
                    int selectedRow = dialog.tblPersons.getSelectedRow();
                    if (selectedRow != -1) {
                        if (dialog.tblPersons.isEditing()) {
                            dialog.tblPersons.getCellEditor().cancelCellEditing();
                        }
                        dialog.tblPersons.getModel().setValueAt(dialog.txtPhoneForClassic.getText(), selectedRow, 2);
                    }
                }
            });

            
            String[] portNames = SerialPortList.getPortNames();
            for (int i = 0; i < portNames.length; i++) {
                dialog.serialPortList.addItem(portNames[i]);
            }
            dialog.serialPortList.addItem("");
            dialog.setLocationRelativeTo(null);
            dialog.setTitle("Homework for serial port App");
            dialog.setVisible(true);
        });
    }

    private SerialPort serialPort;
    private ByteArrayOutputStream bout;   
    private Consumer<String> messageProcessor;
    
    public SerialPortManager(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public void setState() {
        boolean b = serialPort.isOpened();
        connectButton.setText(b ? "Disconnect" : "Connect");
        btnUpload.setEnabled(b);
        btnFormat.setEnabled(b);
        serialPortList.setEnabled(!b);
    }

    public void defaultMessageProcessor(String value) {
        log(value);
    }
    
    private void log(String msg) {
        log(msg,true);
    }
    
    private void log(String msg, boolean newLine) {
        if (msg != null) {
            java.awt.EventQueue.invokeLater(() -> {
                logArea.append(msg + (newLine ? "\n" : ""));
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }
    
    private static boolean importCSV(File file, DefaultTableModel model) {            
        model.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String header = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    model.addRow(new Object[]{data[0].trim(), data[1].trim(), data[2].trim()});
                }
            }
            return true; 
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            return false;
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        serialPortList = new javax.swing.JComboBox<>();
        connectButton = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();
        txtPhoneForClassic = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtEmailFroClassic = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtNameForClassic = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        btnAddPerson = new javax.swing.JButton();
        btnDeletePerson = new javax.swing.JButton();
        btnImportCSVFile = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPersons = new javax.swing.JTable();
        btnUpload = new javax.swing.JButton();
        btnFormat = new javax.swing.JButton();

        jLabel2.setText("Port:");

        connectButton.setText("Connect");
        connectButton.setPreferredSize(new java.awt.Dimension(72, 23));
        connectButton.addActionListener(this::connectButtonActionPerformed);

        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        logArea.setColumns(20);
        logArea.setRows(5);
        jScrollPane1.setViewportView(logArea);

        jLabel25.setText("Phone:");

        jLabel24.setText("E-mail:");

        jLabel23.setText("Name:");

        btnAddPerson.setText("Add person");
        btnAddPerson.addActionListener(this::btnAddPersonActionPerformed);

        btnDeletePerson.setText("Delete person");
        btnDeletePerson.addActionListener(this::btnDeletePersonActionPerformed);

        btnImportCSVFile.setText("Import CSV file");
        btnImportCSVFile.addActionListener(this::btnImportCSVFileActionPerformed);

        tblPersons.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "E-mail", "Phone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblPersons);

        btnUpload.setText("Upload");
        btnUpload.setEnabled(false);
        btnUpload.addActionListener(this::btnUploadActionPerformed);

        btnFormat.setText("Format mode");
        btnFormat.setEnabled(false);
        btnFormat.addActionListener(this::btnFormatActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNameForClassic, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmailFroClassic, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel25)
                        .addGap(162, 162, 162))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtPhoneForClassic, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnFormat)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUpload)))
                        .addGap(31, 31, 31))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAddPerson)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDeletePerson)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnImportCSVFile))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serialPortList, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(105, 105, 105)
                                .addComponent(btnClear))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE))
                        .addContainerGap(33, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClear)
                        .addComponent(serialPortList, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddPerson)
                    .addComponent(btnDeletePerson)
                    .addComponent(btnImportCSVFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtNameForClassic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(txtEmailFroClassic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(txtPhoneForClassic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpload)
                    .addComponent(btnFormat))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setBounds(0, 0, 683, 518);
    }// </editor-fold>//GEN-END:initComponents
    
    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        if (serialPortList.getSelectedItem() != null) {
            if (serialPort == null) {
                serialPort = new SerialPort(serialPortList.getSelectedItem().toString());
                messageProcessor = this::defaultMessageProcessor;
            }
            if (!serialPort.isOpened()) {
                try {
                    //Open port
                    serialPort.openPort();
                    //We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
                    serialPort.setParams(SerialPort.BAUDRATE_115200,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                    int mask = SerialPort.MASK_RXCHAR;
                    serialPort.setEventsMask(mask);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
                    serialPort.addEventListener(this);
                    setState();
                    bout = new ByteArrayOutputStream();
                    log("Connected.");
                } catch (SerialPortException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    log(ex.getMessage());
                    setState();
                }
            } else {
                try {
                    serialPort.closePort();
                    setState();
                    log("Disconnected.");
                } catch (SerialPortException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    log(ex.getMessage());
                    setState();
                }
            }
        }
    }//GEN-LAST:event_connectButtonActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        logArea.setText("");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnAddPersonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPersonActionPerformed
        ((DefaultTableModel)tblPersons.getModel()).addRow(new Object[]{"", "", ""});
        int lastRow = tblPersons.getRowCount() - 1;
        tblPersons.setRowSelectionInterval(lastRow, lastRow);
        tblPersons.scrollRectToVisible(tblPersons.getCellRect(lastRow, 0, true));
    }//GEN-LAST:event_btnAddPersonActionPerformed

    private void btnDeletePersonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePersonActionPerformed
        if (selectedRow != -1) {
            ((DefaultTableModel)tblPersons.getModel()).removeRow(selectedRow);
        }
    }//GEN-LAST:event_btnDeletePersonActionPerformed

    private void btnImportCSVFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportCSVFileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int response = fileChooser.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (importCSV(file,(DefaultTableModel)tblPersons.getModel())) {
                JOptionPane.showMessageDialog(null, "Import successful!");
            } else {
                JOptionPane.showMessageDialog(null, "Error on importing CSV!");
            }
        }
    }//GEN-LAST:event_btnImportCSVFileActionPerformed

    SwingWorker<Void, Void> worker;
    
    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
        if (serialPort == null) { 
            return;
        }        
        
        btnUpload.setEnabled(false);

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    String joinedString = '\r' + txtNameForClassic.getText()+";"+txtPhoneForClassic.getText()+";"+txtEmailFroClassic.getText()+ "\n";
                    byte[] data = joinedString.getBytes(Charset.forName("UTF-8"));
                    serialPort.writeBytes(data);
                    /*
                    for(int idx = 0;idx<data.length;idx+=BUFFER_SIZE) {
                        byte[] buff = Arrays.copyOfRange(data, idx, idx + BUFFER_SIZE);
                        serialPort.writeBytes(buff);
                        
                        while(serialPort.getOutputBufferBytesCount() > 0) {
                            Thread.sleep(10);
                        }
                        /*
                        if (data.length >= 64 && (idx + BUFFER_SIZE) < data.length) {
                            Thread.sleep(DATA_SEND_DELAY);
                        } 
                        
                    }
                    //*/
                    
                } catch ( SerialPortException ex) { // | InterruptedException
                    logger.log(java.util.logging.Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnUpload.setEnabled(true);
                try {
                    get(); // Megvárja a végét és ellenőrzi, volt-e hiba
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Itt nullázhatod a referenciát, ha osztályszinten tároltad
                    worker = null; 
                }
            }
        };
        worker.execute();
    }//GEN-LAST:event_btnUploadActionPerformed

    private void btnFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFormatActionPerformed
        try {
            serialPort.writeString("\t");
        } catch (SerialPortException ex) {
            System.getLogger(SerialPortManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnFormatActionPerformed

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {
            int count = event.getEventValue();
            if (count > 0) {
                //log("received bytes count: " + count);
                try {
                    byte[] buffer = serialPort.readBytes(count);
                    bout.write(buffer);
                    byte[] data = bout.toByteArray();
                    //log(new String(buffer), false);
                    int newLinePos =  IntStream.range(0, data.length)
                        .filter(i -> data[i] == '\n')
                        .findFirst()
                        .orElse(-1);
                    
                    if (newLinePos != -1) {
                        bout = new ByteArrayOutputStream();
                        if (newLinePos < data.length-1) {
                            byte[] after = Arrays.copyOfRange(data, newLinePos + 1, data.length);
                            bout.write(after);
                        }
                        byte[] before = Arrays.copyOfRange(data, 0, newLinePos);
                        String textBeforeNewLine = new String(before, StandardCharsets.UTF_8);
                        if (messageProcessor != null) {
                            messageProcessor.accept(textBeforeNewLine);
                        }
                    }
                } catch (SerialPortException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    log(ex.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    log(ex.getMessage());
                }
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddPerson;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeletePerson;
    private javax.swing.JButton btnFormat;
    private javax.swing.JButton btnImportCSVFile;
    private javax.swing.JButton btnUpload;
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea logArea;
    private javax.swing.JComboBox<String> serialPortList;
    private javax.swing.JTable tblPersons;
    private javax.swing.JTextField txtEmailFroClassic;
    private javax.swing.JTextField txtNameForClassic;
    private javax.swing.JTextField txtPhoneForClassic;
    // End of variables declaration//GEN-END:variables
}
