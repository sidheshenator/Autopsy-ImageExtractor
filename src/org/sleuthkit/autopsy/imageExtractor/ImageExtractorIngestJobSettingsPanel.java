/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.imageExtractor;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;


public class ImageExtractorIngestJobSettingsPanel extends IngestModuleIngestJobSettingsPanel {

    /**
     * Creates new form ImageExtractorIngestJobSettingsPanel
     */
    
    private final Map<String, Boolean> supportedFormatState = new LinkedHashMap<String, Boolean>();
    private final List<String> supportedFormats = new ArrayList<String>();
    private SupportedFormatsListModel supportedFormatsModel;
    private SupportedFormatsListRenderer supportedFormatsListRenderer;
    
    private final ImageExtractorModuleSettings settings;
    
    ImageExtractorIngestJobSettingsPanel(ImageExtractorModuleSettings settings) {
        this.settings = settings;
        initComponents();
        initSupportedFormatList();
        customizeSupportFormatListComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        supportedFormatList = new javax.swing.JList<String>();
        selectAll = new javax.swing.JButton();
        deselectAll = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ImageExtractorIngestJobSettingsPanel.class, "ImageExtractorIngestJobSettingsPanel.jLabel1.text")); // NOI18N

        supportedFormatList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(supportedFormatList);

        org.openide.awt.Mnemonics.setLocalizedText(selectAll, org.openide.util.NbBundle.getMessage(ImageExtractorIngestJobSettingsPanel.class, "ImageExtractorIngestJobSettingsPanel.selectAll.text")); // NOI18N
        selectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deselectAll, org.openide.util.NbBundle.getMessage(ImageExtractorIngestJobSettingsPanel.class, "ImageExtractorIngestJobSettingsPanel.deselectAll.text")); // NOI18N
        deselectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectAll, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deselectAll))
                        .addGap(107, 107, 107))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectAll)
                        .addGap(18, 18, 18)
                        .addComponent(deselectAll)))
                .addContainerGap(127, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllActionPerformed
        for(String supportedFormat : supportedFormats) {
            supportedFormatState.put(supportedFormat, Boolean.TRUE);
        }
        supportedFormatList.repaint();
    }//GEN-LAST:event_selectAllActionPerformed

    private void deselectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllActionPerformed
        for(String supportedFormat : supportedFormats) {
            supportedFormatState.put(supportedFormat, Boolean.FALSE);
        }
        supportedFormatList.repaint();
    }//GEN-LAST:event_deselectAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deselectAll;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton selectAll;
    private javax.swing.JList<String> supportedFormatList;
    // End of variables declaration//GEN-END:variables

    @Override
    public IngestModuleIngestJobSettings getSettings() {
        return settings;
    }

    private void initSupportedFormatList() {
        for (ImageExtractorIngestModule.SupportedFormats supportedFormat : ImageExtractorIngestModule.SupportedFormats.values()) {
            supportedFormatState.put(supportedFormat.toString(), Boolean.FALSE);
        }
        
        supportedFormats.addAll(supportedFormatState.keySet());
        
        supportedFormatsModel = new SupportedFormatsListModel();
        supportedFormatsListRenderer = new SupportedFormatsListRenderer();
        supportedFormatList.setModel(supportedFormatsModel);
        supportedFormatList.setCellRenderer(supportedFormatsListRenderer);
        supportedFormatList.setVisibleRowCount(10);
        
        supportedFormatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
              
                int index = supportedFormatList.locationToIndex(evt.getPoint());
                String value = supportedFormatsModel.getElementAt(index);
                boolean state = !supportedFormatState.get(value);
                supportedFormatState.put(value, state);
                supportedFormatList.repaint();
                settings.setSupportedFormatState(value, state);
            }
        });
    }

    private void customizeSupportFormatListComponents() {
        for(String supportedFormat : supportedFormats) {
            supportedFormatState.put(supportedFormat, settings.getSupportedFormatState(supportedFormat));
        }
    }
    
    private class SupportedFormatsListModel implements ListModel<String> {

        @Override
        public int getSize() {
            return supportedFormats.size();
        }

        @Override
        public String getElementAt(int index) {
            return supportedFormats.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            return;
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            return;
        }
    }
    
    private class SupportedFormatsListRenderer extends JCheckBox implements ListCellRenderer<String> {

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                setEnabled(list.isEnabled());
                setSelected(supportedFormatState.get(value.toString()));
                setFont(list.getFont());
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setText(value.toString());
                return this;
            }
            return new JLabel();
        }
    }
}
