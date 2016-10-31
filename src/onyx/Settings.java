/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/*
 * Settings.java
 *
 * Created on 2009.05.10., 23:03:51
 * v1.0
 *
 * v1.1   2009.07.12.
 * Hozzáadva:       ünnepnapok kezelése
 *                  szinezés kezelése
 *                  programbeállítások kezelése
 */
package onyx;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author PIRI
 */
public class Settings extends javax.swing.JPanel implements ActionListener, DocumentListener, FocusListener, ItemListener, ListSelectionListener {

    private String[] param;
    private SQLMuvelet lek;
    private String[] eredmeny;
    private ResultSet res;
    private Connection con;
    private PreparedStatement stmt;
    private String[] unnepnap;
    private boolean[] tipus;//1=ünnep, 0=munkanap
    private String[][] tabla;
    private int sor;

    /** Creates new form Settings */
    public Settings() {
        getUnnepNapok();
        initComponents();
        tpSettings.setEnabledAt(3, Globals.checkJogosultsag(Globals.JOG_MANAGEMENT));
        if (Main.DEBUG) {
            pfPwOld.setEnabled(false);
            pfPwNew.setEnabled(false);
            pfPwConfirm.setEnabled(false);
        }
    }

    private void getUnnepNapok() {
        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT datum, tipus FROM unnep");
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (ünnepnapok betöltése)", "Hiba", Uzenet.ERROR);
            ex.printStackTrace();
        }
        try {
            res.last();
            int b = res.getRow();//eredmény hossza
            res.beforeFirst();
            tabla = new String[b][3];
            tipus = new boolean[b];
            unnepnap = new String[b];
            b = 0;

            while (res.next()) {
                unnepnap[b] = res.getString("datum");
                tipus[b] = res.getBoolean("tipus");
                tabla[b][0] = unnepnap[b].substring(0, 4).equals("1900") ? "" : unnepnap[b].substring(0, 4);
                tabla[b][1] = Globals.HONAPOK[Integer.valueOf(unnepnap[b].substring(5, 7))];
                tabla[b][2] = unnepnap[b].substring(8);
                b++;
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean isUnnepNap(String datum) {
        for (int i = 0; i < unnepnap.length; i++) {
            if (unnepnap[i].substring(0, 4).equals("1900")) {
                if (datum.substring(5).equals(unnepnap[i].substring(5)) && tipus[i]) {
                    return true;
                }
            } else {
                if (datum.equals(unnepnap[i]) && tipus[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMunkaNap(String datum) {
        for (int i = 0; i < unnepnap.length; i++) {
            if (unnepnap[i].substring(0, 4).equals("1900")) {
                if (datum.substring(5).equals(unnepnap[i].substring(5)) && !tipus[i]) {
                    return true;
                }
            } else {
                if (datum.equals(unnepnap[i]) && !tipus[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isHetvege(String datum) {
        Calendar cal = new GregorianCalendar();
        cal.set(Integer.valueOf(datum.substring(0, 4)), Integer.valueOf(datum.substring(5, 7)) - 1, Integer.valueOf(datum.substring(8)));
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        return dayofweek == Calendar.SATURDAY || dayofweek == Calendar.SUNDAY ? true : false;
    }

    private String[] irszToVaros(String irsz) {
        param = new String[1];
        param[0] = irsz;
        lek = new SQLMuvelet(4, param);
        eredmeny = new String[2];
        res = lek.getLekerdezes();
        try {
            while (res.next()) {
                eredmeny[0] = res.getString(1);
                eredmeny[1] = res.getString(2);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return eredmeny;
    }

    private String[] varosToIrsz(String varos) {

        param = new String[1];
        param[0] = varos;
        lek = new SQLMuvelet(5, param);
        res = lek.getLekerdezes();
        try {
            res.last();
            int b = res.getRow();//eredmény hossza
            res.beforeFirst();
            eredmeny = new String[b];
            b = 0;

            while (res.next()) {
                eredmeny[b] = res.getString(1);//tömb feltöltése az eredménnyel
                b++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eredmeny;
    }

    private String varosToIrsz(String varos, String vresz) {
        param = new String[2];
        param[0] = varos;
        param[1] = vresz;
        lek = new SQLMuvelet(6, param);
        res = lek.getLekerdezes();
        String eredm = new String();
        try {
            res.next();
            eredm = res.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eredm;
    }
    private void browse(JTextField tf, FileNameExtensionFilter extfilter){
        JFileChooser chooser=new JFileChooser();
        chooser.setFileFilter(extfilter);
            chooser.setFileSelectionMode(extfilter==null?JFileChooser.DIRECTORIES_ONLY:JFileChooser.FILES_AND_DIRECTORIES);
            if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                File file=chooser.getSelectedFile();
                tf.setText(file.toString());
            }





    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        btnMentes = new javax.swing.JButton();
        tpSettings = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfMaxTeritesiDij = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfMinimalNyugdij = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        tfKategoriaC = new javax.swing.JTextField();
        tfKategoriaB1 = new javax.swing.JTextField();
        tfKategoriaA = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        tfKategoriaB2 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        tfDijNapokSzama = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        tfDijJovMaxTerheles = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        btnWeekEndColor1 = new javax.swing.JButton();
        btnMarkColor = new javax.swing.JButton();
        btnNevsorJeloles = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        tfUnnepEv = new javax.swing.JTextField();
        cbUnnepHo = new javax.swing.JComboBox();
        tfUnnepNap = new javax.swing.JTextField();
        rbMunkaszunet = new javax.swing.JRadioButton();
        rbMunkanap = new javax.swing.JRadioButton();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUnnep = new javax.swing.JTable(){
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if(tipus[rowIndex]){
                    c.setForeground(Color.RED);
                }else{
                    c.setForeground(Color.GREEN);
                }

                return c;
            }
        }
        ;
        chbMindenEv = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tfAllIrsz = new javax.swing.JTextField();
        tfAllVaros = new javax.swing.JTextField();
        cbAllVarosResz = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        tfAllUtca = new javax.swing.JTextField();
        cbAllKozterulet = new javax.swing.JComboBox();
        tfAllHazSzam = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        tfCimNelkuli = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        tfCimNelkuliIrsz = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        tfCimNelkuliVaros = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        pfPwOld = new javax.swing.JPasswordField();
        pfPwNew = new javax.swing.JPasswordField();
        pfPwConfirm = new javax.swing.JPasswordField();
        jPanel5 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        tfConfigPath = new javax.swing.JTextField();
        btnBrowseConfig1 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        tfSavePath = new javax.swing.JTextField();
        btnBrowseSavePath = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        tfDBServerPath = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        tfDBBackupPath = new javax.swing.JTextField();
        btnBrowseBackupPath = new javax.swing.JButton();
        btnBrowseServer = new javax.swing.JButton();
        pfDBPassword = new javax.swing.JPasswordField();
        tfDBUser = new javax.swing.JTextField();
        tfDBName = new javax.swing.JTextField();
        tfDBPort = new javax.swing.JTextField();
        tfDBPath = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        tfEtkeztetesNaploNev = new javax.swing.JTextField();
        tfMelegedoNaploNev = new javax.swing.JTextField();
        tfEjjeliNaploNev = new javax.swing.JTextField();
        tfAtmenetiNaploNev = new javax.swing.JTextField();

        btnMentes.addActionListener(this);
        btnMentes.setActionCommand("mentes");
        btnMentes.setText("Mentés");
        btnMentes.setEnabled(false);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Díjak", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Max. térítési díj:");

        tfMaxTeritesiDij.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfMaxTeritesiDij.setText(Config.settings.getProperty("max_ter_dij").toString());
        tfMaxTeritesiDij.getDocument().addDocumentListener(this);

        jLabel4.setText("Ft");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Minimál nyugdíj:");

        tfMinimalNyugdij.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfMinimalNyugdij.setText(Config.settings.getProperty("min_nyugdij").toString());
        tfMinimalNyugdij.getDocument().addDocumentListener(this);

        jLabel5.setText("Ft");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel14.setText("Kategóriák:");

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel15.setText("A:");

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel16.setText("B:");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel17.setText("C:");

        tfKategoriaC.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfKategoriaC.setEnabled(false);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, tfKategoriaB2, org.jdesktop.beansbinding.ELProperty.create("${text}"), tfKategoriaC, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        tfKategoriaB1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfKategoriaB1.setText(Config.settings.getProperty("kat1").toString());
        tfKategoriaB1.getDocument().addDocumentListener(this);

        tfKategoriaA.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfKategoriaA.setEnabled(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, tfKategoriaB1, org.jdesktop.beansbinding.ELProperty.create("${text}"), tfKategoriaA, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel18.setText("% alatt");

        jLabel19.setText("% és");

        tfKategoriaB2.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfKategoriaB2.setText(Config.settings.getProperty("kat2").toString());
        tfKategoriaB2.getDocument().addDocumentListener(this);

        jLabel20.setText("% között");

        jLabel21.setText("% felett");

        jLabel39.setText("Napok száma:");

        tfDijNapokSzama.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfDijNapokSzama.setText(Config.settings.getProperty("dij_napok_szama", "30").toString());
        tfDijNapokSzama.getDocument().addDocumentListener(this);

        jLabel40.setText("Jövedelem terhelés:");

        tfDijJovMaxTerheles.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfDijJovMaxTerheles.setText(Config.settings.getProperty("dij_jov_max_terheles", "30").toString());
        tfDijJovMaxTerheles.getDocument().addDocumentListener(this);

        jLabel41.setText("%");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel40)
                            .addComponent(jLabel39))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfMaxTeritesiDij, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfMinimalNyugdij, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                            .addComponent(tfDijNapokSzama, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                            .addComponent(tfDijJovMaxTerheles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel41, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel14))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfKategoriaB1, 0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfKategoriaA, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfKategoriaC, 0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfKategoriaB2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel20))
                            .addComponent(jLabel18))))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tfDijJovMaxTerheles, tfDijNapokSzama, tfMaxTeritesiDij, tfMinimalNyugdij});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfMaxTeritesiDij, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfMinimalNyugdij, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(tfDijNapokSzama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(tfDijJovMaxTerheles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 178, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(tfKategoriaA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(tfKategoriaB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(tfKategoriaB2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(tfKategoriaC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(93, 93, 93))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Színek", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(102, 102, 102))); // NOI18N
        jPanel7.setOpaque(false);

        btnWeekEndColor1.setActionCommand("weekendcolor");
        btnWeekEndColor1.addActionListener(this);
        btnWeekEndColor1.setText("Hétvége színe");
        btnWeekEndColor1.setToolTipText("Változtatáshoz kattintson ide!");
        btnWeekEndColor1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnWeekEndColor1.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("weekendcolor").toString())));
        btnWeekEndColor1.setContentAreaFilled(false);
        btnWeekEndColor1.setOpaque(true);
        btnWeekEndColor1.setFocusPainted(false);
        btnWeekEndColor1.setVerifyInputWhenFocusTarget(false);
        btnWeekEndColor1.repaint();
        btnWeekEndColor1.validate();

        btnMarkColor.setActionCommand("markcolor");
        btnMarkColor.addActionListener(this);
        btnMarkColor.setText("Megjelölés színe");
        btnMarkColor.setToolTipText("Változtatáshoz kattintson ide!");
        btnMarkColor.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
        btnMarkColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnMarkColor.setContentAreaFilled(false);
        btnMarkColor.setOpaque(true);
        btnMarkColor.setFocusPainted(false);
        btnMarkColor.setVerifyInputWhenFocusTarget(false);

        btnNevsorJeloles.setActionCommand("nevsorjeloles");
        btnNevsorJeloles.addActionListener(this);
        btnNevsorJeloles.setText("Névsor jelölés");
        btnNevsorJeloles.setToolTipText("Változtatáshoz kattintson ide!");
        btnNevsorJeloles.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnNevsorJeloles.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("nevsor_jeloles_szin").toString())));
        btnNevsorJeloles.setContentAreaFilled(false);
        btnNevsorJeloles.setOpaque(true);
        btnNevsorJeloles.setFocusPainted(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNevsorJeloles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                    .addComponent(btnMarkColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                    .addComponent(btnWeekEndColor1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnWeekEndColor1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnMarkColor, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNevsorJeloles, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(79, 79, 79))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ünnep- és munkanapok", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(102, 102, 102))); // NOI18N

        tfUnnepEv.setText("2009");

        cbUnnepHo.setModel(new javax.swing.DefaultComboBoxModel(Globals.HONAPOK));

        rbMunkaszunet.setText("Munkaszüneti nap");
        buttonGroup1.add(rbMunkaszunet);
        rbMunkaszunet.addItemListener(this);

        rbMunkanap.setText("Rendkívüli munkanap");
        buttonGroup1.add(rbMunkanap);
        rbMunkanap.addItemListener(this);

        btnAdd.setText("Hozzáadás");
        btnAdd.setEnabled(false);
        btnAdd.setActionCommand("add");
        btnAdd.addActionListener(this);

        btnRemove.setText("Törlés");
        btnRemove.setEnabled(false);
        btnRemove.setActionCommand("remove");
        btnRemove.addActionListener(this);

        tblUnnep.setModel(new javax.swing.table.DefaultTableModel(
            tabla,Globals.FEJLEC_UNNEPNAP
        ));
        tblUnnep.setToolTipText("<html>Piros - Munkaszüneti nap<br>Zöld - Rendkívüli munkanap");
        for(int i=0;i<3;i++){
            tblUnnep.getColumnModel().getColumn(i).setMinWidth(Globals.OSZLOPMERET_UNNEPNAP[i]);
            tblUnnep.getColumnModel().getColumn(i).setMaxWidth(Globals.OSZLOPMERET_UNNEPNAP[i]);
            tblUnnep.getColumnModel().getColumn(i).setPreferredWidth(Globals.OSZLOPMERET_UNNEPNAP[i]);
        }
        tblUnnep.getSelectionModel().addListSelectionListener(this);
        jScrollPane1.setViewportView(tblUnnep);

        chbMindenEv.setText("Minden évben");
        chbMindenEv.addItemListener(this);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbMunkanap)
                    .addComponent(rbMunkaszunet)
                    .addComponent(chbMindenEv)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(tfUnnepEv, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbUnnepHo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfUnnepNap, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnRemove, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel10Layout.createSequentialGroup()
                        .addComponent(rbMunkaszunet)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbMunkanap)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chbMindenEv)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfUnnepEv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbUnnepHo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfUnnepNap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemove)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
                        .addGap(221, 221, 221))))
        );

        tpSettings.addTab("Alapértékek", jPanel1);

        jLabel8.setText("Irsz.:");

        jLabel9.setText("Város:");

        jLabel10.setText("Városrész:");

        tfAllIrsz.setText(Config.settings.getProperty("irsz").toString());
        tfAllIrsz.getDocument().addDocumentListener(this);
        tfAllIrsz.addFocusListener(this);

        tfAllVaros.setText(Config.settings.getProperty("varos").toString());
        tfAllVaros.getDocument().addDocumentListener(this);
        tfAllVaros.addFocusListener(this);

        String[] m=new String[1];
        m[0]=Config.settings.getProperty("vresz").toString();
        cbAllVarosResz.setModel(new javax.swing.DefaultComboBoxModel(m));
        cbAllVarosResz.setSelectedItem(m);
        cbAllVarosResz.addItemListener(this);

        jLabel11.setText("Utca:");

        jLabel12.setText("Közterület:");

        jLabel13.setText("Házszám:");

        tfAllUtca.setText(Config.settings.getProperty("utca").toString());
        tfAllUtca.getDocument().addDocumentListener(this);

        cbAllKozterulet.setModel(new javax.swing.DefaultComboBoxModel(Globals.KOZTERULET));
        cbAllKozterulet.setSelectedItem(Config.settings.getProperty("kozterulet").toString());
        cbAllKozterulet.addItemListener(this);

        tfAllHazSzam.setText(Config.settings.getProperty("hazszam").toString());
        tfAllHazSzam.getDocument().addDocumentListener(this);

        jLabel22.setText("Lakcím nélküli :");

        tfCimNelkuli.setText(Config.settings.getProperty("cim_nelkuli").toString());
        tfCimNelkuli.getDocument().addDocumentListener(this);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel23.setText("Irsz.:");

        tfCimNelkuliIrsz.setText(Config.settings.getProperty("cim_nelkuli_irsz"));
        tfCimNelkuliIrsz.getDocument().addDocumentListener(this);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel24.setText("Város:");

        tfCimNelkuliVaros.setText(Config.settings.getProperty("cim_nelkuli_varos"));
        tfCimNelkuliVaros.getDocument().addDocumentListener(this);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel25.setText("Szöveg:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addContainerGap(719, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfAllUtca, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbAllKozterulet, 0, 97, Short.MAX_VALUE)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfAllHazSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                    .addComponent(jLabel13)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(20, 20, 20)
                                        .addComponent(jLabel9))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(tfAllIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfAllVaros, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(cbAllVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(489, 489, 489))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel24)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfCimNelkuliIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tfCimNelkuli, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tfCimNelkuliVaros, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(620, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAllIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfAllVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbAllVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAllUtca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbAllKozterulet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfAllHazSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCimNelkuliIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCimNelkuliVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCimNelkuli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addContainerGap(244, Short.MAX_VALUE))
        );

        tpSettings.addTab("Cím", jPanel2);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Régi jelszó:");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Új jelszó:");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Új jelszó mégegyszer:");

        pfPwOld.getDocument().addDocumentListener(this);

        pfPwNew.getDocument().addDocumentListener(this);

        pfPwConfirm.getDocument().addDocumentListener(this);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pfPwNew)
                    .addComponent(pfPwOld)
                    .addComponent(pfPwConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(550, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(pfPwOld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(pfPwNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(pfPwConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(363, Short.MAX_VALUE))
        );

        tpSettings.addTab("Jelszó csere", jPanel3);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Program", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel37.setText("Konfigurációs file:");

        tfConfigPath.setText(Config.ini.getProperty("configpath"));
        tfConfigPath.getDocument().addDocumentListener(this);

        btnBrowseConfig1.setText("...");
        btnBrowseConfig1.addActionListener(this);
        btnBrowseConfig1.setActionCommand("browseconfig");
        btnBrowseConfig1.setToolTipText("Tallózás");

        jLabel26.setText("Mentések helye:");

        tfSavePath.setText(Config.settings.getProperty("savepath"));
        tfSavePath.getDocument().addDocumentListener(this);

        btnBrowseSavePath.setText("...");
        btnBrowseSavePath.setActionCommand("browsesavepath");
        btnBrowseSavePath.addActionListener(this);
        btnBrowseSavePath.setToolTipText("Tallózás");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel37)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(tfSavePath, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                .addComponent(tfConfigPath, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnBrowseSavePath, 0, 0, Short.MAX_VALUE)
                                .addComponent(btnBrowseConfig1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel26))
                .addGap(672, 672, 672))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfConfigPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseConfig1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfSavePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseSavePath))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Adatbázis", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel27.setText("Elérési útvonal:");

        jLabel28.setText("Port:");

        jLabel29.setText("Adatbázis név:");

        jLabel30.setText("Felhasználónév:");

        jLabel31.setText("Jelszó:");

        jLabel32.setText("Adatbázis szerver telepítési útvonal:");

        tfDBServerPath.setText(Config.settings.getProperty("db_server_path"));
        tfDBServerPath.getDocument().addDocumentListener(this);

        jLabel34.setText("Adatbázis biztonsági mentés:");

        tfDBBackupPath.setText(Config.settings.getProperty("db_backup_path"));
        tfDBBackupPath.getDocument().addDocumentListener(this);

        btnBrowseBackupPath.setText("...");
        btnBrowseBackupPath.setToolTipText("Tallózás");
        btnBrowseBackupPath.setActionCommand("browsebackuppath");
        btnBrowseBackupPath.addActionListener(this);

        btnBrowseServer.setText("...");
        btnBrowseServer.setToolTipText("Tallózás");
        btnBrowseServer.setActionCommand("browseserverpath");
        btnBrowseServer.addActionListener(this);

        pfDBPassword.setText(Config.settings.getProperty("dbpass"));
        pfDBPassword.getDocument().addDocumentListener(this);

        tfDBUser.setText(Config.settings.getProperty("dbuser"));
        tfDBUser.getDocument().addDocumentListener(this);

        tfDBName.setText(Config.settings.getProperty("dbname"));
        tfDBName.getDocument().addDocumentListener(this);

        tfDBPort.setText(Config.settings.getProperty("dbport"));
        tfDBPort.getDocument().addDocumentListener(this);

        tfDBPath.setText(Config.settings.getProperty("dbpath"));
        tfDBPath.getDocument().addDocumentListener(this);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel29)
                            .addComponent(jLabel27)
                            .addComponent(jLabel28)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pfDBPassword)
                            .addComponent(tfDBUser)
                            .addComponent(tfDBPath, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addComponent(tfDBPort, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfDBName)))
                    .addComponent(jLabel32)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tfDBBackupPath, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfDBServerPath, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBrowseBackupPath, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBrowseServer, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel34))
                .addContainerGap(500, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(tfDBPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(tfDBPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(tfDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(tfDBUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(pfDBPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDBServerPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseServer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDBBackupPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseBackupPath))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tpSettings.addTab("Program", jPanel5);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Megnevezés"));

        jLabel33.setText("Étkeztetés:");

        jLabel35.setText("Nappali melegedő:");

        jLabel36.setText("Éjjeli menedék:");

        jLabel38.setText("Átmeneti szálló:");

        tfEtkeztetesNaploNev.setText(Config.settings.getProperty("etk_naplo_nev"));
        tfEtkeztetesNaploNev.getDocument().addDocumentListener(this);

        tfMelegedoNaploNev.setText(Config.settings.getProperty("melegedo_naplo_nev"));
        tfMelegedoNaploNev.getDocument().addDocumentListener(this);

        tfEjjeliNaploNev.setText(Config.settings.getProperty("ejjeli_naplo_nev"));
        tfEjjeliNaploNev.getDocument().addDocumentListener(this);

        tfAtmenetiNaploNev.setText(Config.settings.getProperty("atmeneti_naplo_nev"));
        tfAtmenetiNaploNev.getDocument().addDocumentListener(this);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(43, 43, 43)
                        .addComponent(tfEtkeztetesNaploNev, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel35)
                            .addComponent(jLabel36)
                            .addComponent(jLabel38))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfAtmenetiNaploNev, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                            .addComponent(tfEjjeliNaploNev, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                            .addComponent(tfMelegedoNaploNev))))
                .addContainerGap(479, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(tfEtkeztetesNaploNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(tfMelegedoNaploNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(tfEjjeliNaploNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(tfAtmenetiNaploNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(236, Short.MAX_VALUE))
        );

        tpSettings.addTab("Naplók", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tpSettings, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE)
                    .addComponent(btnMentes))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tpSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnMentes)
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBrowseBackupPath;
    private javax.swing.JButton btnBrowseConfig1;
    private javax.swing.JButton btnBrowseSavePath;
    private javax.swing.JButton btnBrowseServer;
    private javax.swing.JButton btnMarkColor;
    private javax.swing.JButton btnMentes;
    private javax.swing.JButton btnNevsorJeloles;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnWeekEndColor1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JComboBox cbAllKozterulet;
    private javax.swing.JComboBox cbAllVarosResz;
    private javax.swing.JComboBox cbUnnepHo;
    private javax.swing.JCheckBox chbMindenEv;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPasswordField pfDBPassword;
    private javax.swing.JPasswordField pfPwConfirm;
    private javax.swing.JPasswordField pfPwNew;
    private javax.swing.JPasswordField pfPwOld;
    private javax.swing.JRadioButton rbMunkanap;
    private javax.swing.JRadioButton rbMunkaszunet;
    private javax.swing.JTable tblUnnep;
    private javax.swing.JTextField tfAllHazSzam;
    private javax.swing.JTextField tfAllIrsz;
    private javax.swing.JTextField tfAllUtca;
    private javax.swing.JTextField tfAllVaros;
    private javax.swing.JTextField tfAtmenetiNaploNev;
    private javax.swing.JTextField tfCimNelkuli;
    private javax.swing.JTextField tfCimNelkuliIrsz;
    private javax.swing.JTextField tfCimNelkuliVaros;
    private javax.swing.JTextField tfConfigPath;
    private javax.swing.JTextField tfDBBackupPath;
    private javax.swing.JTextField tfDBName;
    private javax.swing.JTextField tfDBPath;
    private javax.swing.JTextField tfDBPort;
    private javax.swing.JTextField tfDBServerPath;
    private javax.swing.JTextField tfDBUser;
    private javax.swing.JTextField tfDijJovMaxTerheles;
    private javax.swing.JTextField tfDijNapokSzama;
    private javax.swing.JTextField tfEjjeliNaploNev;
    private javax.swing.JTextField tfEtkeztetesNaploNev;
    private javax.swing.JTextField tfKategoriaA;
    private javax.swing.JTextField tfKategoriaB1;
    private javax.swing.JTextField tfKategoriaB2;
    private javax.swing.JTextField tfKategoriaC;
    private javax.swing.JTextField tfMaxTeritesiDij;
    private javax.swing.JTextField tfMelegedoNaploNev;
    private javax.swing.JTextField tfMinimalNyugdij;
    private javax.swing.JTextField tfSavePath;
    private javax.swing.JTextField tfUnnepEv;
    private javax.swing.JTextField tfUnnepNap;
    private javax.swing.JTabbedPane tpSettings;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    public static final String version = "1.1";
    Log log = new Log();

    public void insertUpdate(DocumentEvent e) {
        btnMentes.setEnabled(true);
        Globals.MENTVE = false;
    }

    public void removeUpdate(DocumentEvent e) {
        btnMentes.setEnabled(true);
        Globals.MENTVE = false;
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (e.getSource() == tfAllIrsz) {
            if (!tfAllIrsz.getText().isEmpty()) {
                String[] varos = new String[2];
                varos = irszToVaros(tfAllIrsz.getText());
                tfAllVaros.setText(varos[0]);
                cbAllVarosResz.addItem(varos[1]);
                cbAllVarosResz.setSelectedItem(varos[1]);
                cbAllVarosResz.setEnabled(true);
            }
        }
        if (e.getSource() == tfAllVaros) {
            if (tfAllIrsz.getText().isEmpty()) {
                cbAllVarosResz.removeItemListener(this);
                cbAllVarosResz.removeAllItems();
                cbAllVarosResz.setEnabled(false);
                String[] irsz;
                String[] v = new String[1];
                v[0] = tfAllVaros.getText();
                irsz = varosToIrsz(tfAllVaros.getText());
                if (irsz.length > 1) {     //ha több bejegyzés is van, akkor van városrész
                    lek = new SQLMuvelet(7, v);
                    res = lek.getLekerdezes();
                    try {
                        while (res.next()) {      //combobox feltöltése a városrészek neveivel
                            cbAllVarosResz.addItem(res.getString(1));
                        }
                    } catch (SQLException ev) {
                        ev.printStackTrace();
                    }

                    cbAllVarosResz.setEnabled(true);
                    cbAllVarosResz.requestFocusInWindow();  //a fókusz a combobox-on marad
                    cbAllVarosResz.addItemListener(this);
                } else {
                    tfAllIrsz.setText(irsz[0]);
                    tfAllUtca.requestFocusInWindow();
                }
            }
        }

    }

    public void itemStateChanged(ItemEvent e) {

        if (e.getSource() == rbMunkaszunet || e.getSource() == rbMunkanap) {
            btnAdd.setEnabled(true);
        } else {
            if (e.getSource() == chbMindenEv) {
                tfUnnepEv.setEnabled(!chbMindenEv.isSelected());
                if (!tfUnnepEv.isEnabled()) {
                    tfUnnepEv.setText("");
                } else {
                    tfUnnepEv.setText(Globals.getDatumMa().substring(0, 4));
                }
            } else {
                if (e.getSource() == cbAllVarosResz) {
                    String irsz = varosToIrsz(tfAllVaros.getText(), cbAllVarosResz.getSelectedItem().toString());
                    tfAllIrsz.setText(irsz);
                }
                btnMentes.setEnabled(true);
                Globals.MENTVE = false;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("mentes")) {
            Config.settings.setProperty("max_ter_dij", tfMaxTeritesiDij.getText());
            Config.settings.setProperty("min_nyugdij", tfMinimalNyugdij.getText());
            Config.settings.setProperty("kat1", tfKategoriaB1.getText());
            Config.settings.setProperty("kat2", tfKategoriaB2.getText());
            Config.settings.setProperty("irsz", tfAllIrsz.getText());
            Config.settings.setProperty("varos", tfAllVaros.getText());
            Config.settings.setProperty("vresz", cbAllVarosResz.getSelectedItem().toString());
            Config.settings.setProperty("utca", tfAllUtca.getText());
            Config.settings.setProperty("kozterulet", cbAllKozterulet.getSelectedItem().toString());
            Config.settings.setProperty("hazszam", tfAllHazSzam.getText());
            Config.settings.setProperty("cim_nelkuli", tfCimNelkuli.getText());
            Config.settings.setProperty("cim_nelkuli_irsz", tfCimNelkuliIrsz.getText());
            Config.settings.setProperty("cim_nelkuli_varos", tfCimNelkuliVaros.getText());
            Config.settings.setProperty("weekendcolor", String.valueOf(btnWeekEndColor1.getBackground().getRGB() & 0xFFFFFF));
            Config.settings.setProperty("markcolor", String.valueOf(btnMarkColor.getBackground().getRGB() & 0xFFFFFF));
            Config.settings.setProperty("dbpath", tfDBPath.getText());
            Config.settings.setProperty("dbport", tfDBPort.getText());
            Config.settings.setProperty("dbname", tfDBName.getText());
            Config.settings.setProperty("dbuser", tfDBUser.getText());
            Config.settings.setProperty("dbpass", new String(pfDBPassword.getPassword()));
            Config.settings.setProperty("savepath", tfSavePath.getText());
            Config.settings.setProperty("etk_naplo_nev", tfEtkeztetesNaploNev.getText());
            Config.settings.setProperty("melegedo_naplo_nev", tfMelegedoNaploNev.getText());
            Config.settings.setProperty("ejjeli_naplo_nev", tfEjjeliNaploNev.getText());
            Config.settings.setProperty("atmeneti_naplo_nev", tfAtmenetiNaploNev.getText());
            Config.settings.setProperty("nevsor_jeloles_szin", String.valueOf(btnNevsorJeloles.getBackground().getRGB()&0xFFFFFF));
            Config.settings.setProperty("db_backup_path", tfDBBackupPath.getText());
            Config.settings.setProperty("db_server_path", tfDBServerPath.getText());
            Config.settings.setProperty("dij_napok_szama", tfDijNapokSzama.getText());
            Config.settings.setProperty("dij_jov_max_terheles", tfDijJovMaxTerheles.getText());
//            ini=new Properties();
            Config.ini.setProperty("configpath", tfConfigPath.getText());
            try{
                FileOutputStream inisave=new FileOutputStream("onyx.ini");
                Config.ini.store(inisave, "Config file elérési útja");
                inisave.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
            try {
                FileOutputStream out = new FileOutputStream(Config.ini.getProperty("configpath"));
                Config.settings.store(out, "");
                out.close();
                log.writeLog("Beállítások mentve");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (!(new String(pfPwOld.getPassword()).equals(""))) {
                param = new String[2];
                param[0] = String.valueOf(Globals.getUserID());
                param[1] = new String(pfPwOld.getPassword());
                try {
                    lek = new SQLMuvelet(21, param);
                    res = lek.getLekerdezes();
                    if (res.next()) {
                        String pwuj = new String(pfPwNew.getPassword());
                        String pwconfirm = new String(pfPwConfirm.getPassword());
                        if (pwuj.equals(pwconfirm)) {
                            param[1] = pwuj;
                            lek = new SQLMuvelet(22, param);
                            if (lek.setUpdate()) {
                                new Uzenet("A jelszó megváltozott", "Jelszócsere", Uzenet.INFORMATION);
                                log.writeLog("Jelszó megváltoztatva");
                            } else {
                                new Uzenet("Hiba! A jelszó nem változott meg!!!", "Hiba", Uzenet.ERROR);
                                log.writeLog("Sikertelen jelszócsere");
                            }
                        } else {
                            pfPwNew.setText("");
                            pfPwConfirm.setText("");
                            new Uzenet("A jelszavak nem egyeznek!", "Hiba", Uzenet.ERROR);
                        }
                    } else {
                        pfPwOld.setText("");
                        pfPwNew.setText("");
                        pfPwConfirm.setText("");
                        new Uzenet("Hibás régi jelszó!", "Hiba", Uzenet.ERROR);
                        pfPwOld.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            btnMentes.setEnabled(false);
            Globals.MENTVE = true;
        }
        if (e.getActionCommand().equals("weekendcolor")) {
            Color newcolor = JColorChooser.showDialog(this, "Válasszon színt!", btnWeekEndColor1.getBackground());
            if (newcolor != null) {
                btnWeekEndColor1.setBackground(newcolor);
                btnMentes.setEnabled(true);
                Globals.MENTVE=false;
            }
        }
        if(e.getActionCommand().equals("markcolor")){
            Color newcolor=JColorChooser.showDialog(this, "Válasszon színt!", btnMarkColor.getBackground());
            if(newcolor!=null){
                btnMarkColor.setBackground(newcolor);
                btnMentes.setEnabled(true);
                Globals.MENTVE=false;
            }
        }
        if(e.getActionCommand().equals("nevsorjeloles")){
            Color newcolor = JColorChooser.showDialog(this, "Válasszon színt!", btnNevsorJeloles.getBackground());
            if (newcolor != null) {
                btnNevsorJeloles.setBackground(newcolor);
                btnMentes.setEnabled(true);
                Globals.MENTVE=false;
            }
        }
        if(e.getActionCommand().equals("browseconfig")){
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Konfigurációs file", "cfg");
            browse(tfConfigPath, filter);
        }
        if(e.getActionCommand().equals("browsesavepath"))browse(tfSavePath,null);
        if(e.getActionCommand().equals("browsebackuppath"))browse(tfDBBackupPath,null);
        if(e.getActionCommand().equals("browseserverpath"))browse(tfDBServerPath,null);
        if (e.getActionCommand().equals("add")) {
            if (tfUnnepNap.getText().isEmpty() || cbUnnepHo.getSelectedIndex() == -1 || (tfUnnepEv.isEnabled() && tfUnnepEv.getText().isEmpty())) {
                new Uzenet("Adjon meg egy érvényes dátumot!", "Hiba", Uzenet.WARNING);
                btnAdd.setEnabled(false);
            } else {
                if (Integer.valueOf(tfUnnepNap.getText()) > Globals.HONAP_HOSSZ[cbUnnepHo.getSelectedIndex()]) {
                    new Uzenet("Adjon meg egy érvényes dátumot!", "Hiba", Uzenet.WARNING);
                    btnAdd.setEnabled(false);
                } else {
                    for (int i = 0; i < tabla.length; i++) {
                        if (tfUnnepNap.getText().length() == 1) {
                            tfUnnepNap.setText("0" + tfUnnepNap.getText());
                        }
                        if (tabla[i][0].equals(tfUnnepEv.getText()) && tabla[i][1].equals(cbUnnepHo.getSelectedItem()) && tabla[i][2].equals(tfUnnepNap.getText())) {
                            new Uzenet("A dátum már a listán van!", "Figyelmeztetés", Uzenet.WARNING);
                            chbMindenEv.setSelected(false);
                            cbUnnepHo.setSelectedIndex(0);
                            tfUnnepNap.setText("");
                            rbMunkanap.setSelected(false);
                            rbMunkaszunet.setSelected(false);
                            btnAdd.setEnabled(false);
                        }
                    }
                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        stmt = (PreparedStatement) con.prepareStatement("INSERT INTO unnep SET datum=?, tipus=?");
                        DecimalFormat dcf = new DecimalFormat("00");
                        String datestring = (tfUnnepEv.getText().isEmpty() ? "1900" : tfUnnepEv.getText()) + "-" + dcf.format(cbUnnepHo.getSelectedIndex()) + "-" + tfUnnepNap.getText();
                        stmt.setDate(1, java.sql.Date.valueOf(datestring));
                        stmt.setBoolean(2, rbMunkaszunet.isSelected());
                        stmt.executeUpdate();
                        getUnnepNapok();
                        tblUnnep.setModel(new javax.swing.table.DefaultTableModel(tabla, Globals.FEJLEC_UNNEPNAP));
                        con.close();
                        log.writeLog("Ünnepnap ("+datestring+") mentve");
                        btnAdd.setEnabled(false);
                    } catch (SQLException ex) {
                        new Uzenet("Adatbázis hiba (unnepnap mentés)", "Hiba", Uzenet.ERROR);
                        ex.printStackTrace();
                    }

                }
            }
        }
        if (e.getActionCommand().equals("remove")) {
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
                stmt = (PreparedStatement) con.prepareStatement("DELETE FROM unnep WHERE datum=? AND tipus=?");
                String unnep=unnepnap[sor];
                stmt.setDate(1, java.sql.Date.valueOf(unnep));
                stmt.setBoolean(2, tipus[sor]);
                stmt.executeUpdate();
                getUnnepNapok();
                tblUnnep.setModel(new javax.swing.table.DefaultTableModel(tabla, Globals.FEJLEC_UNNEPNAP));
                con.close();
                log.writeLog("Ünnepnap ("+unnep+") törölve");
                btnRemove.setEnabled(false);
            } catch (SQLException ex) {
                new Uzenet("Adatbázis hiba (ünnep törlés)", "Hiba", Uzenet.ERROR);
                ex.printStackTrace();
            }

        }
    }

    public void valueChanged(ListSelectionEvent e) {
        btnRemove.setEnabled(true);
        sor = tblUnnep.getSelectedRow();
    }
}
