/*
 * Nevsor.java
 *
 * Created on 2009. március 15., 18:59
 * v1.1  2009.05.17
 * hozzáadva: sorrendezés funkció
 * v1.2  2009.08.03
 * hozzáadva: szűrés funkció
 * módosítva: közvetlen SQL lekérdezések
 * 
 */
package onyx;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author  PIRI
 */
public class Nevsor extends javax.swing.JPanel implements ActionListener, ListSelectionListener, DocumentListener, ItemListener, TableModelListener {

    private final JPopupMenu popupmenu;

    /** Creates new form Nevsor */
    public Nevsor(boolean tipus) {
        szolg_szin = new Color(Integer.valueOf(Config.settings.getProperty("nevsor_jeloles_szin", "0").toString()));
        if (tipus) {
            cimsor = "Dolgozók";
        } else {
            cimsor = "Regisztráció";
        }
        this.tipus = tipus;
        nevsorLetolt();
//        tabla = new Object[1][3];
//        fejlec = Globals.FEJLEC_UGYFEL;       //táblázat beállítása
//        oszlop = Globals.OSZLOPMERET_UGYFEL;  //ügyfélnévsorhoz
        initComponents();
        if (!tipus) {       //ha ügyfél névsor
            lblLathato.setEnabled(true);
            chbFilterAtmeneti.setEnabled(true);
            chbFilterEjjeli.setEnabled(true);
            chbFilterEtkeztetes.setEnabled(true);
            chbFilterMind.setEnabled(true);
            chbFilterNappali.setEnabled(true);
            chbFilterMind.setSelected(true);
            chbFilterAtmeneti.setSelected(true);
            chbFilterEjjeli.setSelected(true);
            chbFilterEtkeztetes.setSelected(true);
            chbFilterNappali.setSelected(true);
            chbFilterAtmeneti.addItemListener(this);
            chbFilterEjjeli.addItemListener(this);
            chbFilterEtkeztetes.addItemListener(this);
            chbFilterMind.addItemListener(this);
            chbFilterNappali.addItemListener(this);
        }
//        nevsorFrissit();
        this.addComponentListener(Main.foablak);
        lblCimsor.setText(cimsor);

        popupmenu = new JPopupMenu();
        JMenuItem pmenu1 = new JMenuItem("Adatlap");
        pmenu1.setActionCommand("adatlap");
        pmenu1.addActionListener(this);
        popupmenu.add(pmenu1);
        popupmenu.validate();
        tblNevsor.addMouseListener(new MouseAdapter() {

            private int aktualissor;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblNevsor.rowAtPoint(p);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblNevsor.rowAtPoint(p);
                }
            }
        });



    }

    private void nevsorLetolt() {
        int a = tipus ? 2 : 1;





        //tipus: 1= ügyfél, 2= dolgozó



        switch (a) {
            case 1:
                //ügyfélnévsor előállítása
                fejlec = Globals.FEJLEC_UGYFEL;       //táblázat beállítása
                oszlop = Globals.OSZLOPMERET_UGYFEL;  //ügyfélnévsorhoz
//                lek = new SQLMuvelet(1);



                lek = new SQLMuvelet();
                con = (Connection) lek.getConnection();
                try {
                    stmt = (PreparedStatement) con.prepareStatement("SELECT ID, nev, iktsz, felvetel, szul_ido, etkezes, melegedo, ejjeli, atmeneti, hidden_etk, hidden_mel, hidden_ejj, hidden_atm, mark, megjegyzes FROM ugyfel WHERE statusz='u' AND deleted=0 ");
//                    stmt.setBoolean(1, chbFilterEtkeztetes.isSelected());
//                    stmt.setBoolean(2, chbFilterNappali.isSelected());
//                    stmt.setBoolean(3, chbFilterEjjeli.isSelected());
//                    stmt.setBoolean(4, chbFilterAtmeneti.isSelected());
                    res = stmt.executeQuery();
                } catch (SQLException ex) {
                    new Uzenet("Adatbázis hiba (névsor betöltés)", "Hiba", Uzenet.ERROR);
                    ex.printStackTrace();
                }





//                res = lek.getLekerdezes();
                try {
                    if (res == null || !res.next()) { //a lekérdezés nem járt eredménnyel
                        JOptionPane.showMessageDialog(this, "A névsor üres!", "Névsor", JOptionPane.INFORMATION_MESSAGE);
                        break;  //információs üzenet és kilépés
                    }
                    res.last();
                    int b = res.getRow(); //hány soros a tábla?
                    tabla = new Object[b][7]; //tömbök méretének beállítása
                    id = new int[b];
                    szolgaltatas = new Boolean[b][4];
                    mark = new Boolean[b];
                    megjegyzes = new String[b];
                    res.beforeFirst();  //kurzor vissza az elejére
                    b = 0;                //a tömbmutatóval együtt
                    while (res.next()) {
                        id[b] = res.getInt(1);    //ID mező
                        tabla[b][0] = res.getString("iktsz");   //Iktsz
                        tabla[b][1] = res.getString("nev");  //Név
                        tabla[b][2] = res.getString("szul_ido"); //születési idő
                        tabla[b][3] = res.getBoolean("hidden_etk");
                        tabla[b][4] = res.getBoolean("hidden_mel");
                        tabla[b][5] = res.getBoolean("hidden_ejj");
                        tabla[b][6] = res.getBoolean("hidden_atm");
                        szolgaltatas[b][0] = res.getBoolean("etkezes");
                        szolgaltatas[b][1] = res.getBoolean("melegedo");
                        szolgaltatas[b][2] = res.getBoolean("ejjeli");
                        szolgaltatas[b][3] = res.getBoolean("atmeneti");
                        mark[b] = res.getBoolean("mark");
                        megjegyzes[b] = res.getString("megjegyzes");
                        b++;

                    }
                    lek.close();
                    break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            case 2:     //dolgozói névsor
                fejlec = Globals.FEJLEC_DOLGOZO;          //táblázat beállítása
                oszlop = Globals.OSZLOPMERET_DOLGOZO;     //dolgozói névsorhoz
//                lek = new SQLMuvelet(2);
//                res = lek.getLekerdezes();



                lek = new SQLMuvelet();
                con = (Connection) lek.getConnection();
                try {
                    stmt = (PreparedStatement) con.prepareStatement("SELECT u.ID, nev, szul_ido, beosztas FROM ugyfel AS u LEFT JOIN beosztasi_adat AS b ON u.ID=b.ID WHERE statusz='d' AND deleted=0");

                    res = stmt.executeQuery();
                } catch (SQLException ex) {
                    new Uzenet("Adatbázis hiba (névsor betöltés)", "Hiba", Uzenet.ERROR);
                    ex.printStackTrace();
                }




                try {
                    if (res == null) {     //a lekérdezés nem járt eredménnyel
                        JOptionPane.showMessageDialog(this, "A névsor üres!", "Névsor", JOptionPane.INFORMATION_MESSAGE);
                        break;          //inf. üzenet és kilépés
                    }
                    res.last();
                    int b = res.getRow();     //hány soros a tábla?
                    tabla = new Object[b][3]; //tömbök méretének beállítása
                    id = new int[b];
                    res.beforeFirst();      //kurzor vissza az elejére
                    b = 0;                    //a tömbmutatóval együtt
                    while (res.next()) {
                        id[b] = res.getInt(1);    //ID mező
                        tabla[b][0] = res.getString(2);  //Név
                        tabla[b][1] = getDatum(res.getString(3)); //születési idő
                        tabla[b][2] = res.getString(4);   //beosztás
                        b++;
                    }
                    lek.close();
                    break;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
        }
    }

    public void nevsorFrissit() {
//        this.setVisible(false);
        nevsorLetolt();

        TableModel model = new DefaultTableModel(tabla, fejlec) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,};
            boolean[] canEdit = new boolean[]{
                false, false, false, true, true, true,
                true
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
        tblNevsor.setModel(model);
        sorter = new TableRowSorter<TableModel>(model);
        tblNevsor.setRowSorter(sorter);
//        tblNevsor.setModel(new javax.swing.table.DefaultTableModel(tabla, fejlec));
        for (int i = 0; i < 3; i++) {
            tblNevsor.getColumnModel().getColumn(i).setMinWidth(oszlop[i]);
            tblNevsor.getColumnModel().getColumn(i).setMaxWidth(oszlop[i]);
            tblNevsor.getColumnModel().getColumn(i).setPreferredWidth(oszlop[i]);
        }
        btnAdatlap.setEnabled(false);
        setFilter();
        tblNevsor.getSelectionModel().addListSelectionListener(this);
        tblNevsor.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
        tblNevsor.getActionMap().put("enter", new AdatlapAction());
        tblNevsor.getModel().addTableModelListener(this);
    }

    public boolean getTipus() {
        return tipus;
    }

    private String getDatum(String s) {
        if (s == null) {
            return "";
        }
        if (s.equals(Globals.URES_DATUM)) {
            return "";
        } else {
            return s;
        }
    }

    public void setEnabledNevsor(boolean t) {
        tblNevsor.setEnabled(t);
        btnTorles.setEnabled(false);
        btnUj.setEnabled(t);
        tfFilter.setEnabled(t);
        jLabel1.setEnabled(t);
    }

    private void setFilter() {
//        filtertext = tfFilter.getText();
//        if (filtertext.length() == 0) {
//            sorter.setRowFilter(null);
//        } else {
//            filtertext = "^" + filtertext;
//            sorter.setRowFilter(RowFilter.regexFilter(filtertext));
//        }
        //filterimplementáció

        RowFilter filter = new RowFilter() {

            @Override
            public boolean include(Entry entry) {
                DefaultTableModel model = (DefaultTableModel) entry.getModel();
                filtertext = tfFilter.getText().toLowerCase();
                boolean inc = false;
                if (filtertext.length() > 0) {
                    String ertek1 = model.getValueAt((Integer) entry.getIdentifier(), 0).toString();
                    String ertek2 = model.getValueAt((Integer) entry.getIdentifier(), 1).toString();
                    inc = ertek1.toLowerCase().startsWith(filtertext) || ertek2.toLowerCase().startsWith(filtertext);
                } else {
                    inc = true;
                }
                Boolean etk = szolgaltatas[(Integer) entry.getIdentifier()][0];
                Boolean mel = szolgaltatas[(Integer) entry.getIdentifier()][1];
                Boolean ejj = szolgaltatas[(Integer) entry.getIdentifier()][2];
                Boolean atm = szolgaltatas[(Integer) entry.getIdentifier()][3];

                /*boolean etk = Boolean.valueOf(szolgaltatas[(Integer) entry.getIdentifier()][0].toString());
                boolean mel = Boolean.valueOf(szolgaltatas[(Integer) entry.getIdentifier()][1] .toString());
                boolean ejj = Boolean.valueOf(szolgaltatas[(Integer) entry.getIdentifier()][2] .toString());
                boolean atm = Boolean.valueOf(szolgaltatas[(Integer) entry.getIdentifier()][3] .toString());*/
                return inc && ((etk && chbFilterEtkeztetes.isSelected()) || (mel && chbFilterNappali.isSelected()) || (ejj && chbFilterEjjeli.isSelected()) || (atm && chbFilterAtmeneti.isSelected()));

            }
        };
        sorter.setRowFilter(filter);
    }

    private void filtersToStack() {
        stack = new Boolean[5];
        stack[0] = chbFilterAtmeneti.isSelected();
        stack[1] = chbFilterEjjeli.isSelected();
        stack[2] = chbFilterEtkeztetes.isSelected();
        stack[3] = chbFilterMind.isSelected();
        stack[4] = chbFilterNappali.isSelected();
    }

    private void stackToFilters() {
        chbFilterAtmeneti.setSelected(stack[0]);
        chbFilterEjjeli.setSelected(stack[1]);
        chbFilterEtkeztetes.setSelected(stack[2]);
        chbFilterMind.setSelected(stack[3]);
        chbFilterNappali.setSelected(stack[4]);
        setFilter();
    }

    private void adatlapAction() {
        btnAdatlap.setEnabled(false);
        Main.foablak.showAdatlap(id[selectedrowindex], tipus);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblNevsor = new javax.swing.JTable()
        {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                boolean szinez=false;

                if(isCellSelected(rowIndex, vColIndex)){    //ha kiválasztott, akkor kék/fehér
                    c.setBackground(Color.BLUE);
                    c.setForeground(Color.WHITE);
                }else{
                    if(vColIndex>2){
                        if(szolgaltatas[tblNevsor.convertRowIndexToModel(rowIndex)][vColIndex-3]){
                            c.setBackground(szolg_szin);
                        }else{
                            c.setBackground(Color.WHITE);
                        }
                    }else{
                        if(mark[tblNevsor.convertRowIndexToModel(rowIndex)]){
                            c.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
                            c.setForeground(Color.BLACK);
                            if(c instanceof JComponent){
                                JComponent jc=(JComponent)c;
                                jc.setToolTipText(megjegyzes[tblNevsor.convertRowIndexToModel(rowIndex)]);
                            }
                        }else{
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                            if(c instanceof JComponent){
                                JComponent jc=(JComponent)c;
                                jc.setToolTipText("");
                            }
                        }
                    }
                }
                return c;

            }
        };
        btnAdatlap = new javax.swing.JButton();
        btnUj = new javax.swing.JButton();
        btnTorles = new javax.swing.JButton();
        lblCimsor = new javax.swing.JLabel();
        tfFilter = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblLathato = new javax.swing.JLabel();
        chbFilterEtkeztetes = new javax.swing.JCheckBox();
        chbFilterNappali = new javax.swing.JCheckBox();
        chbFilterEjjeli = new javax.swing.JCheckBox();
        chbFilterAtmeneti = new javax.swing.JCheckBox();
        chbFilterMind = new javax.swing.JCheckBox();
        btnFrissit = new javax.swing.JButton();
        btnMentes = new javax.swing.JButton();

        TableModel model=new DefaultTableModel(tabla,fejlec){
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true, true,
                true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };
        tblNevsor.setModel(model);
        sorter=new TableRowSorter<TableModel>(model);
        tblNevsor.setRowSorter(sorter);
        tblNevsor.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblNevsor.getTableHeader().setResizingAllowed(false);
        tblNevsor.getTableHeader().setReorderingAllowed(false);
        tblNevsor.setToolTipText("<html>Szinezve = részt vesz a szolgáltatásban<br>Pipa = elrejtés az adatrögzítési listáról");
        for(int i=0;i<3;i++){
            tblNevsor.getColumnModel().getColumn(i).setMinWidth(oszlop[i]);
            tblNevsor.getColumnModel().getColumn(i).setMaxWidth(oszlop[i]);
            tblNevsor.getColumnModel().getColumn(i).setPreferredWidth(oszlop[i]);
        }
        tblNevsor.getSelectionModel().addListSelectionListener(this);
        tblNevsor.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
        tblNevsor.getActionMap().put("enter", new AdatlapAction());
        tblNevsor.getModel().addTableModelListener(this);
        jScrollPane1.setViewportView(tblNevsor);

        btnAdatlap.setText("Adatlap");
        btnAdatlap.setEnabled(false);
        btnAdatlap.addActionListener(this);
        btnAdatlap.setActionCommand("adatlap");

        btnUj.addActionListener(this);
        btnUj.setActionCommand("uj");
        btnUj.setText("Új felvétele");

        btnTorles.addActionListener(this);
        btnTorles.setActionCommand("torles");
        btnTorles.setText("Törlés");
        btnTorles.setEnabled(false);

        lblCimsor.setFont(new java.awt.Font("Tahoma", 1, 18));
        lblCimsor.setText("jLabel1");

        tfFilter.getDocument().addDocumentListener(this);
        tfFilter.setToolTipText("<html>A begépelt karakterekkel<br>kezdődő neveket mutatja a táblázatban");

        jLabel1.setText("Szűrés:");

        lblLathato.setText("Látható:");
        lblLathato.setEnabled(false);

        chbFilterEtkeztetes.setText("Étkeztetés");
        chbFilterEtkeztetes.setEnabled(false);

        chbFilterNappali.setText("Nappali melegedő");
        chbFilterNappali.setEnabled(false);

        chbFilterEjjeli.setText("Éjjeli menedék");
        chbFilterEjjeli.setEnabled(false);

        chbFilterAtmeneti.setText("Átmeneti szálló");
        chbFilterAtmeneti.setEnabled(false);

        chbFilterMind.setText("Mind");
        chbFilterMind.setEnabled(false);

        btnFrissit.setText("Frissítés");
        btnFrissit.setActionCommand("frissites");
        btnFrissit.addActionListener(this);
        btnFrissit.setToolTipText("Frissíti a névsort");

        btnMentes.setText("Mentés");
        btnMentes.setEnabled(false);
        btnMentes.setActionCommand("mentes");
        btnMentes.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblLathato)
                        .addGap(18, 18, 18)
                        .addComponent(chbFilterMind)
                        .addGap(18, 18, 18)
                        .addComponent(chbFilterEtkeztetes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chbFilterNappali)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chbFilterEjjeli)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chbFilterAtmeneti))
                    .addComponent(lblCimsor, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfFilter))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(btnAdatlap)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUj)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTorles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMentes)
                        .addGap(18, 18, 18)
                        .addComponent(btnFrissit))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCimsor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdatlap)
                    .addComponent(btnUj)
                    .addComponent(btnTorles)
                    .addComponent(btnFrissit)
                    .addComponent(btnMentes))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLathato)
                    .addComponent(chbFilterMind)
                    .addComponent(chbFilterNappali)
                    .addComponent(chbFilterEjjeli)
                    .addComponent(chbFilterAtmeneti)
                    .addComponent(chbFilterEtkeztetes))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdatlap;
    private javax.swing.JButton btnFrissit;
    private javax.swing.JButton btnMentes;
    private javax.swing.JButton btnTorles;
    private javax.swing.JButton btnUj;
    private javax.swing.JCheckBox chbFilterAtmeneti;
    private javax.swing.JCheckBox chbFilterEjjeli;
    private javax.swing.JCheckBox chbFilterEtkeztetes;
    private javax.swing.JCheckBox chbFilterMind;
    private javax.swing.JCheckBox chbFilterNappali;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCimsor;
    private javax.swing.JLabel lblLathato;
    private javax.swing.JTable tblNevsor;
    private javax.swing.JTextField tfFilter;
    // End of variables declaration//GEN-END:variables
    private Object[][] tabla;
    private Object[] fejlec;
    private int[] oszlop;
    private int selectedrowindex;
    private int[] id;
    private boolean tipus;
    private String cimsor;
    private SQLMuvelet lek;
    private ResultSet res;
    private String filtertext;
    private TableRowSorter<TableModel> sorter;
    public static final String version = "1.2";
    private Log log = new Log();
    private Connection con;
    private PreparedStatement stmt;
    Boolean[] stack;
    private Boolean[][] szolgaltatas;
    private Color szolg_szin;
    private Boolean[] mark;
    private String[] megjegyzes;
    private String hiba;

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("adatlap")) {
            //ID kinyerése b tömmből, majd adatlap indítása
            adatlapAction();
        }
        if (e.getActionCommand().equals("uj")) {
            Main.foablak.showAdatlap(Globals.UJ, tipus);
            if (tipus) {
                if (!log.writeLog("Új dolgozó felvétele")) {
                    new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                }
            } else {
                if (!log.writeLog("Új ügyfél felvétele")) {
                    new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                }
            }
        }
        if (e.getActionCommand().equals("torles")) {
            if (JOptionPane.showConfirmDialog(this, "Biztosan törölni akarja a kiválasztott nevet?", "Törlés", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                String[] param = new String[2];
                param[0] = Globals.getDatumIdoMost();
                param[1] = String.valueOf(id[selectedrowindex]);
                lek = new SQLMuvelet(19, param);
                lek.setUpdate();
                lek.close();
                btnTorles.setEnabled(false);
                tfFilter.setText("");
                tblNevsor.setRowSorter(null);
                if (tipus) {
                    if (!log.writeLog("Dolgozó törlése: " + tabla[selectedrowindex][0] + "(ID:" + id[selectedrowindex] + ")")) {
                        new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                    }
                } else {
                    if (!log.writeLog("Ügyfél törlése: " + tabla[selectedrowindex][1] + "(ID:" + id[selectedrowindex] + ")")) {
                        new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                    }
                }

                filtersToStack();
                nevsorFrissit();
                stackToFilters();
            }

        }
        if (e.getActionCommand().equals("frissites")) {
            filtersToStack();
            nevsorFrissit();
            stackToFilters();
        }
        if (e.getActionCommand().equals("mentes")) {
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            int sor;
            try {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                for (int i = 0; i < tabla.length; i++) {
                    sor = i;   //tblNevsor.convertRowIndexToModel(i);
                    hiba = String.valueOf(i) + "  " + String.valueOf(sor);
                    stmt = (PreparedStatement) con.prepareStatement("UPDATE ugyfel SET hidden_etk=?, hidden_mel=?, hidden_ejj=?, hidden_atm=? WHERE ID=?");
                    stmt.setBoolean(1, (Boolean) tabla[sor][3]);
                    stmt.setBoolean(2, (Boolean) tabla[sor][4]);
                    stmt.setBoolean(3, (Boolean) tabla[sor][5]);
                    stmt.setBoolean(4, (Boolean) tabla[sor][6]);
                    stmt.setInt(5, id[sor]);
                    stmt.executeUpdate();
                    btnMentes.setEnabled(false);
                    btnFrissit.setEnabled(true);
                    Globals.MENTVE = true;
                }

                con.close();
            } catch (Exception ex) {
                System.out.println(hiba);
                ex.printStackTrace();

            } finally {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }


    }

//ListSelectionListener implementáció
    public void valueChanged(ListSelectionEvent e) {
        if (tblNevsor.getSelectedRow() != -1) {
            selectedrowindex = tblNevsor.convertRowIndexToModel(tblNevsor.getSelectedRow());
            if (selectedrowindex != -1) {
                btnAdatlap.setEnabled(true);
                btnTorles.setEnabled(Globals.checkJogosultsag(Globals.JOG_FONOK));
            }
        }
    }

//DocumentListener implementáció
    public void insertUpdate(DocumentEvent e) {
        setFilter();
    }

    public void removeUpdate(DocumentEvent e) {
        setFilter();
    }

    public void changedUpdate(DocumentEvent e) {
    }

//ItemListener implementáció
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == chbFilterMind) {
            if (chbFilterMind.isSelected()) {
                chbFilterAtmeneti.setSelected(true);
                chbFilterEjjeli.setSelected(true);
                chbFilterEtkeztetes.setSelected(true);
                chbFilterNappali.setSelected(true);
            }
        }
        if (e.getSource() == chbFilterAtmeneti || e.getSource() == chbFilterEjjeli || e.getSource() == chbFilterEtkeztetes || e.getSource() == chbFilterNappali) {
            if (chbFilterAtmeneti.isSelected() && chbFilterEjjeli.isSelected() && chbFilterEtkeztetes.isSelected() && chbFilterNappali.isSelected()) {
                chbFilterMind.removeItemListener(this);
                chbFilterMind.setSelected(true);
                chbFilterMind.addItemListener(this);
            } else {
                chbFilterMind.removeItemListener(this);
                chbFilterMind.setSelected(false);
                chbFilterMind.addItemListener(this);
            }
            setFilter();
//            nevsorFrissit();
        }
    }

    public void tableChanged(TableModelEvent e) {
//        System.out.println(e.getFirstRow());
        if (e.getColumn() > 2) {
//            System.out.println(e.getColumn());
            int sor = e.getFirstRow();
//            lek = new SQLMuvelet();
//            con = (Connection) lek.getConnection();
//            try {
//                stmt = (PreparedStatement) con.prepareStatement("UPDATE ugyfel SET hidden_etk=?, hidden_mel=?, hidden_ejj=?, hidden_atm=? WHERE ID=?");
//                int sor = tblNevsor.convertRowIndexToModel(e.getFirstRow());
            tabla[sor][3] = (Boolean) tblNevsor.getModel().getValueAt(sor, 3);
            tabla[sor][4] = (Boolean) tblNevsor.getModel().getValueAt(sor, 4);
            tabla[sor][5] = (Boolean) tblNevsor.getModel().getValueAt(sor, 5);
            tabla[sor][6] = (Boolean) tblNevsor.getModel().getValueAt(sor, 6);
            btnMentes.setEnabled(true);
            btnFrissit.setEnabled(false);
            Globals.MENTVE = false;
//                stmt.setInt(5, id[sor]);
//                stmt.executeUpdate();
//                con.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
        }
//
//        }

    }

    class AdatlapAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            adatlapAction();
        }
    }
}
