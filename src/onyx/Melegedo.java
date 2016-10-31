/*
 * Melegedo.java
 *
 * Created on 2009. március 23., 10:29
 */
package onyx;

import com.mysql.jdbc.PreparedStatement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DateFormatter;

/**
 *
 * @author  PIRI
 */
public class Melegedo extends javax.swing.JPanel implements DocumentListener, ActionListener,
        TableModelListener, ListSelectionListener, FocusListener {

    private Connection con;
    private Integer[] id;
    private Object[][] tabla;
    private SQLMuvelet lek;
    private ResultSet res;
    private String ev = "00";
    private String ho = "00";
    private String nap = "00";
    private String[] param;
    private Object adat;
    private int selectedrowindex;
    private boolean lezarva = false;
    public static final String version = "1.0";
    private java.sql.PreparedStatement stmt;
    private int temp;
    private Date datum;
    private int aktualissor;
    private final JPopupMenu popupmenu;
    private Boolean[] mark;
    private String[] megjegyzes;
    private String[] szulido;
    private Integer t;
    private String sqlstr;

    /** Creates new form Melegedo */
    public Melegedo() {
        tabla = new Object[0][15];
        DecimalFormat dcf = new DecimalFormat("00");
        initComponents();
        tfEv.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        ev = tfEv.getText();
        ho = dcf.format(cbHonap.getSelectedIndex());

        popupmenu = new JPopupMenu();
        JMenuItem pmenu1 = new JMenuItem("Adatlap");
        pmenu1.setActionCommand("adatlap");
        pmenu1.addActionListener(this);
        popupmenu.add(pmenu1);
        popupmenu.validate();
        tblMelegedo.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblMelegedo.rowAtPoint(p);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblMelegedo.rowAtPoint(p);
                }
            }
        });
    }

    public Object[] getDatum() {
        Object[] ret = new Object[3];
        DecimalFormat dcf = new DecimalFormat("00");
        ret[0] = tfEv.getText();
        ret[1] = dcf.format(cbHonap.getSelectedIndex());
        //ret[2] = tfNap.getText();

        ret[2] = dcf.format(Integer.valueOf(tfNap.getText()));
        return ret;
    }

    public void honapLezaras() {
        Uzenet uzenet = new Uzenet("<html></b>Figyelem!</b></br>A lezárás után az adatok<br>nem módosíthatók!<br><br>Kívánja folytatni?", "Lezárás", Uzenet.QUESTION);
        if (uzenet.getValasz() == Uzenet.YES) {
            datum = Date.valueOf(ev + "-" + ho + "-01");
            DecimalFormat dcf = new DecimalFormat("00");
            String kovho = dcf.format(Integer.valueOf(ho) + 1);
            Date datum_vege = Date.valueOf(ev + "-" + kovho + "-31");
            lek = new SQLMuvelet();
            con = lek.getConnection();
            try {
                stmt = con.prepareStatement("UPDATE melegedo SET lezarva=1 WHERE datum<=? AND datum>?");
                stmt.setDate(1, datum);
                stmt.setDate(2, datum_vege);
                if (stmt.executeUpdate() > 0) {
                    new Uzenet("A " + ev + "." + ho + ". hónap le lett zárva!", "Lezárás", Uzenet.INFORMATION);
                }
                con.close();
                Main.foablak.disableLezaras(Globals.MELEGEDO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private int dataToCode(int sor) {
        //a sor adatokból egy egész számot állít elő,
        //ami tárolásra kerül az adatbázisban
        int code = 0;
        for (int i = 0; i < 11; i++) {
            if (Boolean.parseBoolean(tabla[sor][i + 2].toString())) {
                code |= (int) Math.pow(2, (double) i);
            }
        }
        return code;
    }

    private boolean[] codeToData(int code) {
        //az adatbázisban tárolt egész számból egy tömböt állít elő
        //minden elem 1 vagy 0 lehet.
        boolean[] data = new boolean[13];
        for (int i = 0; i < 11; i++) {
            if ((code & (int) Math.pow(2,
                    (double) i)) == (int) Math.pow(2, (double) i)) {
                data[i] = true;
            } else {
                data[i] = false;
            }
        }
        return data;
    }

    private void osszegTabla() {
//összegzi az oszlopokat
        for (int i = 2; i < 13; i++) {
            int osszeg = 0;
            for (int j = 0; j < tabla.length; j++) {
                if (Boolean.parseBoolean(tabla[j][i].toString())) {
                    osszeg++;
                }
            }
            tblOsszesen.getModel().setValueAt(osszeg, 0, i - 2);
        }
//összegzi az ellátottakat. Számol mindenkit, aki legalább 1 szolgáltatást igénybevett
        int ellatottak = 0;
        for (int i = 0; i < tabla.length; i++) {
            for (int j = 2; j < 13; j++) {
                if (Boolean.parseBoolean(tabla[i][j].toString())) {
                    ellatottak++;
                    break;
                }
            }
        }
        lblEllatottakSzama.setText(String.valueOf(ellatottak));
    }

    private void setTabla(Object[][] t) {
        TableModel model = new DefaultTableModel(t, Globals.FEJLEC_MELEGEDO) {
//        tblMelegedo.setModel(new javax.swing.table.DefaultTableModel(
//                    t, Globals.FEJLEC_MELEGEDO) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, true, true, true, true,
                true, true, true, true, true, true, true, true, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        tblMelegedo.setModel(model);
        tblMelegedo.setRowSorter(sorter);
        for (int i = 0; i < 13; i++) {
            tblMelegedo.getColumnModel().getColumn(i).
                    setMinWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
            tblMelegedo.getColumnModel().getColumn(i).
                    setMaxWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
            tblMelegedo.getColumnModel().getColumn(i).
                    setPreferredWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
        }
        tblMelegedo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblMelegedo.getModel().addTableModelListener(this);
        tblMelegedo.getSelectionModel().addListSelectionListener(this);
    }

    private void okAction() {
        //datum =  Date.valueOf("2010-01-01");
        datum = Date.valueOf(ev + "-" + ho + "-" + nap);
        lek = new SQLMuvelet();
        con = lek.getConnection();
        try {
            if (ho.equals(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)))) {
                sqlstr = "SELECT COUNT(melegedo.ID) FROM melegedo LEFT JOIN ugyfel ON melegedo.ID=ugyfel.ID WHERE datum=? AND hidden_mel=0";
            } else {
                sqlstr = "SELECT COUNT(melegedo.ID) FROM melegedo LEFT JOIN ugyfel ON melegedo.ID=ugyfel.ID WHERE datum=?";
            }
            stmt = con.prepareStatement(sqlstr);
            stmt.setDate(1, datum);
            res = stmt.executeQuery();
            res.next();
            int sorokszama = res.getInt(1);
            if (sorokszama > 0) {
                tabla = new Object[sorokszama][15];
                id = new Integer[sorokszama];
                mark = new Boolean[sorokszama];
                megjegyzes = new String[sorokszama];
                szulido = new String[sorokszama];
                if (ho.equals(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)))) {
                    sqlstr = "SELECT melegedo.ID, ugyfel.iktsz, ugyfel.nev, ugyfel.szul_ido, lezarva, szolg01, szolg02, szolg03, szolg04, szolg05, szolg06, szolg07, szolg08, szolg09, szolg10, szolg11, szolg12, szolg13, mark, megjegyzes FROM melegedo LEFT JOIN ugyfel ON melegedo.ID=ugyfel.ID WHERE datum=? AND hidden_mel=0";
                } else {
                    sqlstr = "SELECT melegedo.ID, ugyfel.iktsz, ugyfel.nev, ugyfel.szul_ido, lezarva, szolg01, szolg02, szolg03, szolg04, szolg05, szolg06, szolg07, szolg08, szolg09, szolg10, szolg11, szolg12, szolg13, mark, megjegyzes FROM melegedo LEFT JOIN ugyfel ON melegedo.ID=ugyfel.ID WHERE datum=?";
                }
                stmt = con.prepareStatement(sqlstr);
                stmt.setDate(1, datum);
                res = stmt.executeQuery();
                for (int i = 0; i < sorokszama; i++) {
                    res.next();
                    id[i] = res.getInt("ID");
                    mark[i] = res.getBoolean("mark");
                    megjegyzes[i] = res.getString("megjegyzes");
                    szulido[i] = res.getString("szul_ido");
                    lezarva = res.getBoolean("lezarva");
                    tabla[i][0] = res.getString("iktsz");
                    tabla[i][1] = res.getString("nev");
                    for (int j = 2; j < 15; j++) {
                        tabla[i][j] = res.getBoolean(j + 4);
                    }
                }
                setTabla(tabla);
                osszegTabla();
                if (lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK))) {
                    new Uzenet("Lezárt Hónap!\nAz adatok nem módosíthatók!", "Melegedő", Uzenet.INFORMATION);
                }
            } else {
                lek = new SQLMuvelet();
                con = lek.getConnection();
                try {
                    stmt = con.prepareStatement("SELECT COUNT(ID) FROM ugyfel WHERE melegedo=1 AND (deleted=0 OR del_date>?) AND hidden_mel=0");
                    stmt.setDate(1, datum);
                    res = stmt.executeQuery();
                    res.next();
                    sorokszama = res.getInt(1);
                    if (sorokszama > 0) {
                        tabla = new Object[sorokszama][15];
                        id = new Integer[sorokszama];
                        mark = new Boolean[sorokszama];
                        megjegyzes = new String[sorokszama];
                        szulido = new String[sorokszama];
                        stmt = con.prepareStatement("SELECT ID, iktsz, nev, mark, megjegyzes, szul_ido  FROM ugyfel WHERE melegedo=1 AND statusz='u' AND (deleted=0 OR del_date>?) AND hidden_mel=0");
                        stmt.setDate(1, datum);
                        res = stmt.executeQuery();
                        for (int i = 0; i < sorokszama; i++) {
                            res.next();
                            id[i] = res.getInt("ID");
                            mark[i] = res.getBoolean("mark");
                            megjegyzes[i] = res.getString("megjegyzes");
                            szulido[i] = res.getString("szul_ido");
                            tabla[i][0] = res.getString("iktsz");
                            tabla[i][1] = res.getString("nev");
                            for (int j = 2; j < 15; j++) {
                                tabla[i][j] = false;
                            }
                        }
                        setTabla(tabla);
                        osszegTabla();
                    } else {
                        new Uzenet("A lista ures!", "Melegedő", Uzenet.WARNING);
                        tabla = new Object[0][15];
                        setTabla(tabla);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            con.close();
        } catch (SQLException se) {
            if (se.getMessage().equals("After end of result set")) {
                System.out.println("hiba van");
            } else {
                se.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

        lblCimsor = new javax.swing.JLabel();
        tfEv = new javax.swing.JTextField();
        cbHonap = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMelegedo = new javax.swing.JTable(){
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                boolean szinez=false;

                if(isCellSelected(rowIndex, vColIndex)){
                    c.setBackground(Color.BLUE);
                    c.setForeground(Color.WHITE);
                }else{
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                if(mark[tblMelegedo.convertRowIndexToModel(rowIndex)]){
                    c.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
                    if(c instanceof JComponent){
                        JComponent jc=(JComponent)c;
                        jc.setToolTipText("<html>"+megjegyzes[tblMelegedo.convertRowIndexToModel(rowIndex)]+"<br>"+szulido[tblMelegedo.convertRowIndexToModel(rowIndex)]);
                    }
                }else{
                    if(c instanceof JComponent){
                        JComponent jc=(JComponent)c;
                        jc.setToolTipText(szulido[tblMelegedo.convertRowIndexToModel(rowIndex)]);
                    }
                }
                return c;

            }
        };
        btnMentes = new javax.swing.JButton();
        lblNev = new javax.swing.JLabel();
        tfNap = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblOsszesen = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblEllatottakSzama = new javax.swing.JLabel();
        btnImport = new javax.swing.JButton();

        lblCimsor.setFont(new java.awt.Font("Tahoma", 1, 18));
        lblCimsor.setText("Melegedő");

        tfEv.addFocusListener(this);

        cbHonap.addFocusListener(this);
        cbHonap.setModel(new javax.swing.DefaultComboBoxModel(Globals.HONAPOK));

        tblMelegedo.setModel(new javax.swing.table.DefaultTableModel(
            tabla, Globals.FEJLEC_MELEGEDO ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, true, true,
                true, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        for(int i=0;i<13;i++){
            tblMelegedo.getColumnModel().getColumn(i).
            setMinWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
            tblMelegedo.getColumnModel().getColumn(i).
            setMaxWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
            tblMelegedo.getColumnModel().getColumn(i).
            setPreferredWidth(Globals.OSZLOPMERET_MELEGEDO[i]);
        }
        tblMelegedo.setColumnSelectionAllowed(false);
        tblMelegedo.getTableHeader().setReorderingAllowed(false);
        JTableHeader header=tblMelegedo.getTableHeader();
        header.setFont(new Font("Arial Narrow", Font.PLAIN, 10));
        tblMelegedo.getModel().addTableModelListener(this);
        jScrollPane1.setViewportView(tblMelegedo);
        tblMelegedo.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        btnMentes.addActionListener(this);
        btnMentes.setActionCommand("mentes");
        btnMentes.setText("Mentés");
        btnMentes.setEnabled(false);

        lblNev.setFont(new java.awt.Font("Tahoma", 1, 14));

        tfNap.addFocusListener(this);
        tfNap.getDocument().addDocumentListener(this);
        tfNap.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "ok");
        tfNap.getActionMap().put("ok", new OKaction());

        tblOsszesen.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "null", "null", "null", "null", "null", "null", "null"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblOsszesen.getTableHeader().setResizingAllowed(false);
        tblOsszesen.getTableHeader().setReorderingAllowed(false);
        tblOsszesen.setTableHeader(null);
        for(int i=0;i<11;i++){
            tblOsszesen.getColumnModel().getColumn(i).setMinWidth(65);
            tblOsszesen.getColumnModel().getColumn(i).setMaxWidth(65);
            tblOsszesen.getColumnModel().getColumn(i).setPreferredWidth(65);
        }
        jScrollPane2.setViewportView(tblOsszesen);
        tblOsszesen.getColumnModel().getColumn(0).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(1).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(2).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(3).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(4).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(5).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(6).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(7).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(8).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(9).setResizable(false);
        tblOsszesen.getColumnModel().getColumn(10).setResizable(false);

        jLabel1.setText("Összesen:");

        btnOk.addActionListener(this);
        btnOk.setActionCommand("ok");
        btnOk.setText("OK");
        btnOk.setEnabled(false);

        jLabel2.setText("Név:");

        jLabel3.setText("Ellátottak száma:");

        lblEllatottakSzama.setFont(new java.awt.Font("Tahoma", 1, 18));
        lblEllatottakSzama.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        btnImport.setText("Import");
        btnImport.setEnabled(false);
        btnImport.setActionCommand("import");
        btnImport.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 998, Short.MAX_VALUE)
                                .addComponent(lblCimsor)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(tfEv, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(tfNap, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(btnOk)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(lblNev, javax.swing.GroupLayout.PREFERRED_SIZE, 618, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnImport)))
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblEllatottakSzama, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 717, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(38, 38, 38)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnMentes)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCimsor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfEv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfNap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNev, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOk)
                            .addComponent(jLabel2)))
                    .addComponent(btnImport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel3)
                        .addComponent(lblEllatottakSzama, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnMentes)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnMentes;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cbHonap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCimsor;
    private javax.swing.JLabel lblEllatottakSzama;
    private javax.swing.JLabel lblNev;
    private javax.swing.JTable tblMelegedo;
    private javax.swing.JTable tblOsszesen;
    private javax.swing.JTextField tfEv;
    private javax.swing.JTextField tfNap;
    // End of variables declaration//GEN-END:variables

//DocumentListener implementáció
    public void insertUpdate(DocumentEvent e) {
        if (!tfEv.getText().isEmpty() && !tfNap.getText().isEmpty() && cbHonap.getSelectedIndex() > 0) {
            ev = tfEv.getText();
            DecimalFormat dcf = new DecimalFormat("00");
            t = cbHonap.getSelectedIndex();
            ho = dcf.format(t);
            t = Integer.valueOf(tfNap.getText());
            nap = dcf.format(t);
            btnOk.setEnabled(true);
        } else {
            btnOk.setEnabled(false);
        }

    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    class OKaction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            okAction();
        }
    }
//ActionListener implementáció

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            btnImport.setEnabled(true);
            okAction();
        }

        if (e.getActionCommand().equals("mentes")) {
            try {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                lek = new SQLMuvelet();
                con = lek.getConnection();
                datum = Date.valueOf(ev + "-" + ho + "-" + nap);
                try {
                    stmt = con.prepareStatement("SELECT COUNT(ID) FROM melegedo WHERE datum=?");
                    stmt.setDate(1, datum);
                    res = stmt.executeQuery();
                    int szum = 0;
                    Boolean ertek = false;
                    res.next();
                    if (res.getInt(1) > 0) {        //már el volt mentve az adott hónap
                        for (int i = 0; i < tabla.length; i++) {

                            stmt = con.prepareStatement("UPDATE melegedo SET szolg01=?,szolg02=?,szolg03=?,szolg04=?,szolg05=?,szolg06=?,szolg07=?,szolg08=?,szolg09=?,szolg10=?,szolg11=?,szolg12=?,szolg13=?,szolg=?, rogz_id=?, rogzitve=NOW() WHERE ID=? AND datum=?");
                            szum = 0;
                            for (int j = 2; j < 15; j++) {
                                ertek = Boolean.parseBoolean(tabla[i][j].toString());
                                stmt.setBoolean(j - 1, ertek);
                                if (ertek) {
                                    szum++;
                                }
                            }
                            stmt.setInt(14, szum);
                            stmt.setInt(15, Globals.getUserID());
                            stmt.setInt(16, id[i]);
                            stmt.setDate(17, datum);
                            stmt.executeUpdate();
                        }
                    } else {      //
                        for (int i = 0; i < tabla.length; i++) {

                            stmt = con.prepareStatement("INSERT INTO melegedo (ID, datum, szolg, rogzitve, rogz_id, lezarva, szolg01, szolg02, szolg03, szolg04, szolg05, szolg06, szolg07, szolg08, szolg09, szolg10, szolg11, szolg12, szolg13) VALUES (?,?,?,NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                            stmt.setInt(1, id[i]);
                            stmt.setDate(2, datum);
                            stmt.setInt(4, Globals.getUserID());
                            stmt.setBoolean(5, false);
                            szum = 0;
                            for (int j = 2; j < 15; j++) {
                                ertek = Boolean.parseBoolean(tabla[i][j].toString());
                                stmt.setBoolean(j + 4, ertek);
                                if (ertek) {
                                    szum++;
                                }
                            }
                            stmt.setInt(3, szum);
                            stmt.executeUpdate();
                        }
                    }



                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                btnMentes.setEnabled(false);
                Globals.MENTVE = true;
                Main.foablak.enableLezaras(Globals.MELEGEDO);
                if (lezarva) {
                    honapLezaras();
                }
            } finally {
                this.setCursor(Cursor.getDefaultCursor());
            }


        }
        if (e.getActionCommand().equals("adatlap")) {
            final JFrame frame = new JFrame("Adatlap");
            Adatlap adatlap = new Adatlap(id[tblMelegedo.convertRowIndexToModel(aktualissor)], Globals.UGYFEL);
            adatlap.addComponentListener(new ComponentListener() {

                public void componentResized(ComponentEvent e) {
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                }

                public void componentHidden(ComponentEvent e) {
                    frame.dispose();
                }
            });
            frame.add(adatlap);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocation(280, 50);
            frame.pack();
            frame.setVisible(true);
        }
        if(e.getActionCommand().equals("import")){
            int idk;
//            final Map kivalaszt=new HashMap();
            datum = Date.valueOf(ev + "-" + ho + "-" + nap);
            final Map lista = new LinkedHashMap();
            JCheckBox chb;
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT ID, nev, iktsz, mark, hidden_mel "
                        + "FROM ugyfel WHERE melegedo=1 AND deleted=0 AND ID NOT IN (SELECT ID FROM melegedo WHERE datum=?) ORDER BY nev");
                stmt.setDate(1, datum);
                res = stmt.executeQuery();
                while (res.next()) {
                    idk = res.getInt("ID");
//                    kivalaszt.put(idk, false);
                    chb = new JCheckBox(res.getString("nev") + "(" + res.getString("iktsz") + ")", false);
                    if(res.getBoolean("mark")){
                        chb.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
                    }
                    if(res.getBoolean("hidden_mel")){
                        chb.setEnabled(false);
                    }
                    lista.put(idk, chb);
                }
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            final JFrame fr = new JFrame("Import (" + lista.size() + ")");
//            JPanel keret=new JPanel();
            JPanel gombok = new JPanel();
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Mégse");
            ok.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        int id;
                        int count = 0;
                        JCheckBox jcb;
                        Set keys = lista.keySet();
                        Integer[] k = (Integer[]) keys.toArray(new Integer[0]);
                        for (int i = 0; i < lista.size(); i++) {
                            jcb = (JCheckBox) lista.get(k[i]);
                            if (jcb.isSelected()) {
                                stmt = (PreparedStatement) con.prepareStatement("INSERT INTO melegedo SET"
                                        + " ID=?, datum=?, szolg='0', rogzitve=NOW(), rogz_id=?, lezarva='0'");
                                stmt.setInt(1, k[i]);
                                stmt.setDate(2, datum);
                                stmt.setInt(3, Globals.getUserID());
                                stmt.executeUpdate();
                                count++;
                            }
                        }
                        new Uzenet(String.valueOf(count) + " fő be lett importálva!\nA változások érvényesítéséhez\nújra be kell olvasni a hónapot!", "Import", Uzenet.INFORMATION);
                        con.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    fr.dispose();
                }
            });
            cancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    fr.dispose();
                }
            });
            gombok.add(ok);
            gombok.add(cancel);
            gombok.setPreferredSize(new Dimension(200, 40));
            JPanel panel = new JPanel(new GridLayout(0, 1));
            JScrollPane scp = new JScrollPane(panel);
            scp.setPreferredSize(new Dimension(400, 650));
            Collection c = lista.values();
            Iterator it = c.iterator();
            while (it.hasNext()) {
                panel.add((JCheckBox) it.next());
            }
//            keret.setPreferredSize(new Dimension(410, 750));
            Container cont=fr.getContentPane();
            cont.add(scp,BorderLayout.NORTH);
            cont.add(gombok,BorderLayout.SOUTH);
//            fr.setPreferredSize(new Dimension(400, 750));
//            fr.add(keret);
            fr.setResizable(false);
            fr.setLocation(300, 30);
            fr.validate();
            fr.pack();
            fr.setVisible(true);
        }
    }
//TableModelListener implementáció

    public void tableChanged(TableModelEvent e) {
        int sor = e.getFirstRow();
        int oszl = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        adat = model.getValueAt(sor, oszl); //kinyerjük a megváltozott adatot
        tabla[sor][oszl] = adat; //és eltároljuk a tömbben
        osszegTabla();
        btnMentes.setEnabled(!(lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK))));
        //változás esetén a mentés gomb aktív, kivéve, ha le van zárva
        Globals.MENTVE = lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK));
        Main.foablak.disableLezaras(Globals.MELEGEDO);
        //ha változás van, a lezárás menüpont inaktiválódik mentésig
    }
//ListSelectionListener implementáció

    public void valueChanged(ListSelectionEvent e) {
        selectedrowindex = tblMelegedo.getSelectedRow();
        if (selectedrowindex != -1) {
            lblNev.setText((String) tblMelegedo.getValueAt(selectedrowindex, 1));
        } else {
            lblNev.setText("");
        }
    }
//FocusListener implementáció

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (e.getSource() == tfEv || e.getSource() == tfNap || e.getSource() == cbHonap) {
            if (!tfEv.getText().isEmpty() && !tfNap.getText().isEmpty() && cbHonap.getSelectedIndex() > 0) {
                ev = tfEv.getText();
                DecimalFormat dcf = new DecimalFormat("00");
                t = cbHonap.getSelectedIndex();
                ho = dcf.format(t);
                t = Integer.valueOf(tfNap.getText());
                nap = dcf.format(t);
                btnOk.setEnabled(true);
            } else {
                btnOk.setEnabled(false);
            }

        }
    }
}
