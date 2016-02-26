package com.wise.GUI;

import com.wise.EventDispatcher;
import com.wise.pen.Pen;
import com.wise.pen.PenManager;
import com.wise.powerpoint.MSPowerPoint;
import org.hid4java.HidDevice;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by Chaun on 2/25/2016.
 * This class help the user find the right pen and file.
 * Then it starts the pen and its event listener.
 */
public class CustomizedDialog {
    public static JDialog penSelectionDialog(){
        // The dialog to return
        final JDialog dialog = new JDialog();

        dialog.setLayout(new BorderLayout());
        dialog.setModal(false);
        // Produce the header of the table;
        Vector<String> headers = new Vector<String>();
        headers.add("VendorID");
        headers.add("ProductID");
        headers.add("SerialNumber");
        //System.out.println(data.toString());
        final JTable penTable = new JTable(getTableData(),headers);
        JScrollPane scrollPane = new JScrollPane(penTable);
        // Table resize properly, Single row selection mode.
        penTable.setFillsViewportHeight(true);
        penTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        penTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Get those two buttons ready.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        final JButton confirmBtn = new JButton("Confirm");
        final JButton updateTable = new JButton("UpdateTable");
        confirmBtn.setEnabled(false);
        buttonPanel.add(updateTable);
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        // Table Selection Listner
        ListSelectionModel rowSM = penTable.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    confirmBtn.setEnabled(false);
                } else {
                    confirmBtn.setEnabled(true);
                }
            }
        });
        // Set button Listener
        // Upon selection and confirmation. The pen could automatically runs in a seperate thread.
        confirmBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = penTable.getSelectedRow();
                int vendorId = Integer.parseInt((String)penTable.getValueAt(row, 0));
                int productId = Integer.parseInt((String)penTable.getValueAt(row, 1));
                String serialNumber = (String)penTable.getValueAt(row, 2);
                Pen pen = PenManager.findPen(vendorId, productId, serialNumber);
                if(pen != null) {
                    //dialog.setVisible(false);
                    final JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(dialog);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        new EventDispatcher(pen, file.toString());
                        dialog.dispose();
                    } else {
                        dialog.dispose();
                    }
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to open a connection with the open.");
                    updateTable.doClick();
                }
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        updateTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Vector<String> headers = new Vector<String>();
                headers.add("VendorID");
                headers.add("ProductID");
                headers.add("SerialNumber");
                DefaultTableModel oldModel = (DefaultTableModel) penTable.getModel();
                DefaultTableModel newModel = new DefaultTableModel(getTableData(), headers);
                penTable.setModel(newModel);
            }
        });
        // Set Frame Centered, Set dialog content. Pack things properly
        dialog.add(scrollPane, BorderLayout.NORTH);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        return dialog;
    }


    // Helper Function to keep code clean.
    private static Vector<Vector<String>> getTableData(){
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        List<HidDevice> pens = PenManager.listPens();
        for(HidDevice pen:pens) {
            Vector<String> penInfo = new Vector<String>();
            penInfo.add(String.valueOf(pen.getVendorId()));
            penInfo.add(String.valueOf(pen.getProductId()));
            penInfo.add(pen.getSerialNumber());
            System.out.println(penInfo.elementAt(0));
            System.out.println(penInfo.elementAt(0));
            System.out.println(penInfo.elementAt(0));
            data.add(penInfo);
        }
        return data;
    }
}
