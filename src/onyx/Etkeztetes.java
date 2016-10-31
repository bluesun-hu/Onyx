/*
 * Etkeztetes.java
 *
 * Created on 2009. február 11., 11:49
 *
 * Version 1.0.1.   2009.06.15.
 * Módosítás:   Közvetlen SQL hívások
 *              Tárolás napi szintű (1 nap-1mező az adatbázisban)
 *              Ellenőrzőösszeg a kézi adatbázis módosítás felderítésére
 * Version 1.0.2.   2009.07.12.
 * Módosítás:   Context-menü
 *
 * Javítva:     Rendezés után duplázva mentette az adatokat
 *              Kiszolgálás lezárásig a jelenlegi adatok alapján jelenik meg a táblában
 *              utána pedig a mentett adatok alapján
 * Version 1.0.3.   2009.08.30.
 * Javítva:     Kettős mentés (forrása: a modellbe hibásan került be a beírt adat)
 *
 * Version 1.1.0    2010.02.12.
 * Módosítás:   rögzítés checkbox-al az egyesek helyett.
 *
 *                  2010.03.02.
 * Javítás:     összegsor koordináta rendezés esetén nem volt megfelelő
 *
 *                  2010.03.07.
 * Módosítás:   mentési módszer
 * 
 */
package onyx;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.*;  //MouseAdapter;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import jxl.Workbook;
import jxl.write.Formula;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author  PIRI
 */
public class Etkeztetes extends javax.swing.JPanel implements TableModelListener, ActionListener, ItemListener, FocusListener, ListSelectionListener {

    /** Az étkeztetési adatok megjelenítésére, bevitelére, módosítására alkalmas
    felületet hoz létre */
    public String[] EtkeztetesFEJLEC = new String[34];

    public Etkeztetes() {

        tabla = new Object[0][34];
        initComponents();
        tfEv.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        ev = tfEv.getText();
        Integer t = cbHonap.getSelectedIndex();
//        DecimalFormat dcf = new DecimalFormat("00");
        ho = dcf.format(t);
//        ho = t.toString();
//        if (t < 10) {
//            ho = "0" + ho;
//        }

        //klikk a cellán beír a cellába 1-et, ha üres volt és törli, ha 1 volt benne
        popupmenu = new JPopupMenu();
        JMenuItem pmenu1 = new JMenuItem("Mindennap evett");
        pmenu1.setActionCommand("mindennapevett");
        pmenu1.addActionListener(this);
        popupmenu.add(pmenu1);
        JMenuItem pmenu2 = new JMenuItem("Sor törlés");
        pmenu2.setActionCommand("sortorles");
        pmenu2.addActionListener(this);
        popupmenu.add(pmenu2);
        JMenuItem pmenu3 = new JMenuItem("Adatlap");
        pmenu3.setActionCommand("adatlap");
        pmenu3.addActionListener(this);
        popupmenu.add(pmenu3);
        popupmenu.validate();
        tblEtkeztetes.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 1) {
//                    Object o = 1;
//                    Point p = e.getPoint();
//                    int sor = tblEtkeztetes.convertRowIndexToModel(tblEtkeztetes.rowAtPoint(p));
//                    int oszl = tblEtkeztetes.columnAtPoint(p);
//                    TableModel model = tblEtkeztetes.getModel();
//                    if (oszl > 2 && oszl < Globals.HONAP_HOSSZ[Integer.valueOf(ho)] + 3) {
//                        if (model.getValueAt(sor, oszl) == o) {
//                            model.setValueAt("", sor, oszl);
//                            tabla[sor][oszl] = "0";
//                        } else {
//                            model.setValueAt(o, sor, oszl);
//                            tabla[sor][oszl] = "1";
//                        }
//                    }
//                }

                /*if (e.getClickCount() == 1) {
                Object o = 1;
                Point p = e.getPoint();
                int sor = tblEtkeztetes.rowAtPoint(p);
                int oszl = tblEtkeztetes.columnAtPoint(p); //sor és oszlop a kattintás helyén
                if (oszl > 2 && oszl < Globals.HONAP_HOSSZ[Integer.valueOf(ho)] + 3) {
                if (tblEtkeztetes.getValueAt(sor, oszl) == o) {   //ha 1
                tblEtkeztetes.setValueAt("", sor, oszl);  //akkor törli
                tabla[sor][oszl] = "0";
                //                            tabla[sor][oszl] = "0";
                } else {
                tblEtkeztetes.setValueAt(o, sor, oszl);
                tabla[sor][oszl] = "1";
                //                            tabla[sor][oszl] = "1";
                }  //egyébként 1

                }


                }*/
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblEtkeztetes.rowAtPoint(p);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                    Point p = e.getPoint();
                    aktualissor = tblEtkeztetes.rowAtPoint(p);
                }
            }
        });




    }

    private int dataToCode(int sor) {
        //a sor adatokból egy 32bites egészet állít elő, ami tárolásra kerül az adatbázisban
        int code = 0;
        int h = Globals.HONAP_HOSSZ[Integer.valueOf(ho)];
        for (int i = 0; i < h; i++) {
            if (tabla[sor][i + 3].equals("1")) {
                code |= (int) Math.pow(2, (double) i);
            }
        }
        return code;
    }

    private String[] codeToData(int code) {
        //az adatbázisban tárolt 32 bites egészből egy tömböt állít elő
        //minden elem 1 vagy 0 lehet.
        String[] data = new String[31];
        for (int i = 0; i < 31; i++) {
            if ((code & (int) Math.pow(2, (double) i)) == (int) Math.pow(2, (double) i)) {
                data[i] = "1";
            } else {
                data[i] = "";
            }
        }
        return data;
    }

    private int osszegSor(int sor) {
        /**
         * a megadot sorban lévő elemek összegét adja vissza
         */
        int osszeg = 0;
        Boolean temp;
        Integer ertek;

        for (int i = 3; i < 34; i++) {
            temp = (Boolean) tblEtkeztetes.getModel().getValueAt(sor, i);
            if (temp) {
                osszeg++;
            }
        }
        return osszeg;
    }

    private int osszegOszlop(int oszl) {
        /**a megadott oszlopban lévő elemek összegét adja vissza*/
        int osszeg = 0;
        Boolean temp;
        Integer ertek;

        for (int i = 0; i < tabla.length; i++) {
            temp = (Boolean) tblEtkeztetes.getModel().getValueAt(i, oszl);
            if (temp) {
                osszeg++;
            }
        }
        return osszeg;
    }

    private void osszegTabla() {
        int hoosszesen = 0;
        int temp;
        for (int i = 3; i < 34; i++) {
            temp = osszegOszlop(i);
            tblOsszesen.getModel().setValueAt(temp, 0, i - 3);
            hoosszesen += temp;
        }
        lblHonapOsszesen.setText(String.valueOf(hoosszesen));
    }

    public void honapLezaras() {
        if (JOptionPane.showConfirmDialog(this, "<html><b>Figyelem!</b><br>A lezárás után az adatok<br>nem módosíthatók!<br><br>Kívánja folytatni?", "Lezárás", JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
            param = new String[2];
            param[0] = ev;
            param[1] = ho;
            lek = new SQLMuvelet(28, param);
            if (lek.setUpdate()) {
                JOptionPane.showMessageDialog(this, "A " + ev + "." + ho + ". hónap le lett zárva!", "Lezárás", JOptionPane.INFORMATION_MESSAGE);
            }
            Main.foablak.disableLezaras(Globals.ETKEZTETES);
        }

    }

    private boolean teljesdij(String date, int row) {
        if (!date.equals(null) || !date.isEmpty()) {
            Calendar aktualis = new GregorianCalendar(Integer.valueOf(date.substring(0, 4)), Integer.valueOf(date.substring(6, 7)), Integer.valueOf(date.substring(9, 10)));
            Calendar jegyzoi = new GregorianCalendar();
            if (datum[row][1] != null) {
                jegyzoi.setTime(datum[row][1]);
            } else {
                return false;
            }
            return aktualis.before(jegyzoi);
        }
        return false;
    }

    public void makeigbvnaplo() {
    }

    public Object[] getDatum() {
        Object[] ret = new Object[3];
        ret[0] = tfEv.getText();
//        DecimalFormat dcf = new DecimalFormat("00");
        ret[1] = dcf.format(cbHonap.getSelectedIndex());
        ret[2] = "";
        return ret;
    }

    private void getNevsor() {
        //TODO lekérdezést befejezni

        String kovho = dcf.format(Integer.valueOf(ho) + 1);
        int hossz = 0;
        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(tipus) FROM lezart_idoszak WHERE tipus=0 AND ev=? AND ho=?");
            stmt.setString(1, ev);
            stmt.setString(2, ho);
            res = stmt.executeQuery();
            res.next();
            if (res.getInt(1) == 0) {
                stmt = (PreparedStatement) con.prepareStatement("CREATE VIEW temp AS SELECT ID, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 WHERE ev=? AND ho=?");
                stmt.setString(1, ev);
                stmt.setString(2, ho);
                stmt.executeUpdate();
                stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etk_nevsor");
                res = stmt.executeQuery();
                res.next();
                hossz = res.getInt(1);
                if (hossz > 0) {
                    id = new int[hossz];
                    tabla = new Object[hossz][34];
                    dij = new Double[hossz];
                    datum = new Date[hossz][3];
                    mark = new Boolean[hossz];
                    megjegyzes = new String[hossz];

                    stmt = (PreparedStatement) con.prepareStatement("SELECT ID, iktsz, kiszolgalas, nev, beadas, jegyzoi_ig, megszunes, dij, mark, megjegyzes, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 FROM etk_nevsor AS e LEFT JOIN temp AS t ON e.ID=t.ID");
                    res = stmt.executeQuery();
                    int b = 0;
                    String f = "n";
                    String field = "";
                    while (res.next()) {
                        id[b] = res.getInt(1);
                        tabla[b][0] = res.getString("iktsz");//iktsz
                        tabla[b][1] = res.getString("kiszolgalas");//kiszolgalas
                        tabla[b][2] = res.getString("nev");//nev
                        datum[b][0] = res.getDate("beadas");
                        datum[b][1] = res.getDate("jegyzoi_ig");
                        datum[b][2] = res.getDate("megszunes");
                        dij[b] = res.getDouble("dij");
                        mark[b] = res.getBoolean("mark");
                        megjegyzes[b] = res.getString("megjegyzes");

                        for (int i = 3; i < 34; i++) {
                            field = f + String.valueOf(i - 2).trim();
                            tabla[b][i] = res.getBoolean(field);
                        }
                        b++;

                    }
                } else {
                    new Uzenet("A lista üres!", "Hiba", Uzenet.WARNING);
                }


            }












            stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etkeztetes WHERE ev=? AND ho=?");
            stmt.setString(1, ev);
            stmt.setString(2, ho);
            res = stmt.executeQuery();
            res.next();
            if (res.getInt(1) == 0) {       //még nem volt rögzítve = új hónap
//ügyfelek számának lekérdezése
                stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM ugyfel WHERE etkezes=1 AND statusz='u' AND hidden_etk=0 AND (deleted=0 OR del_date>?)");
                stmt.setDate(1, Date.valueOf(ev + "-" + kovho + "-01"));
                res = stmt.executeQuery();
                res.next();
                hossz = res.getInt(1);
//tömbök előállítása a lekérdezett méretre
                id = new int[hossz];

                tabla = new Object[hossz][34];
                dij = new Double[hossz];
                datum = new Date[hossz][3];
                mark = new Boolean[hossz];
                megjegyzes = new String[hossz];
//ügyfelek adatainak lekérdezése
                stmt = (PreparedStatement) con.prepareStatement("SELECT ID, iktsz, kiszolgalas, nev, beadas, jegyzoi_ig, megszunes, dij, mark, megjegyzes  FROM ugyfel WHERE etkezes=1 AND statusz='u' AND hidden_etk=0 AND (deleted=0 OR del_date>?) ORDER BY nev");
                stmt.setDate(1, Date.valueOf(ev + "-" + kovho + "-01"));
                res = stmt.executeQuery();
                int b = 0;
                while (res.next()) {
                    id[b] = res.getInt(1);
                    tabla[b][0] = res.getString("iktsz");//iktsz
                    tabla[b][1] = res.getString("kiszolgalas");//kiszolgalas
                    tabla[b][2] = res.getString("nev");//nev
                    datum[b][0] = res.getDate("beadas");
                    datum[b][1] = res.getDate("jegyzoi_ig");
                    datum[b][2] = res.getDate("megszunes");
                    dij[b] = res.getDouble("dij");
                    mark[b] = res.getBoolean("mark");
                    megjegyzes[b] = res.getString("megjegyzes");

                    for (int i = 3; i < 34; i++) {
                        tabla[b][i] = false;
                    }
                    b++;

                }


            } else {                      //már volt rögzítve
                stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etkeztetes WHERE ev=? AND ho=?");
                stmt.setString(1, ev);
                stmt.setString(2, ho);
                res = stmt.executeQuery();
                res.next();
                hossz = res.getInt(1);
// tömbök előállítása a lekérdezés szerinti méretre
                id = new int[hossz];

                tabla = new Object[hossz][34];
                dij = new Double[hossz];
                datum = new Date[hossz][3];
                mark = new Boolean[hossz];
                megjegyzes = new String[hossz];
// névsor lekérdezése és tömbökbe töltése
                stmt = (PreparedStatement) con.prepareStatement("SELECT e.ID, iktsz, e.kiszolgalas, nev, beadas, jegyzoi_ig, megszunes, e.dij, mark, megjegyzes, lezarva, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 FROM etkeztetes AS e LEFT JOIN ugyfel AS u ON e.ID=u.ID WHERE ev=? AND ho=? ORDER BY nev");
                stmt.setString(1, ev);
                stmt.setString(2, ho);
                res = stmt.executeQuery();
                int b = 0;
                while (res.next()) {
                    id[b] = res.getInt("ID");
                    lezarva = res.getBoolean("lezarva");
                    dij[b] = res.getDouble("dij");
                    tabla[b][0] = res.getString("iktsz");
                    tabla[b][1] = res.getString("kiszolgalas");
                    tabla[b][2] = res.getString("nev");
                    datum[b][0] = res.getDate("beadas");
                    datum[b][1] = res.getDate("jegyzoi_ig");
                    datum[b][2] = res.getDate("megszunes");
                    mark[b] = res.getBoolean("mark");
                    megjegyzes[b] = res.getString("megjegyzes");
                    for (int i = 3; i < 34; i++) {

                        tabla[b][i] = res.getBoolean(i + 9);
                    }
                    b++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Uzenet("Hiba az adatok betöltése közben", "Étkeztetés", Uzenet.ERROR);
        }







        // <editor-fold defaultstate="collapsed" desc="comment">
            /*lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
        stmt = (PreparedStatement) con.prepareStatement("SELECT ID, iktsz, kiszolgalas, nev, beadas, jegyzoi_ig, megszunes, dij, mark, megjegyzes  FROM ugyfel WHERE etkezes=1 AND statusz='u' AND (deleted=0 OR del_date>?)");
        stmt.setDate(1, Date.valueOf(ev + "-" + ho + "-31"));
        res = stmt.executeQuery();
        } catch (SQLException ex) {
        new Uzenet("Adatbázis hiba (étkezés lista betöltés)", "Hiba", Uzenet.ERROR);
        ex.printStackTrace();
        }
        try {
        res.last();
        int b = res.getRow();
        tabla = new String[b][34];
        id = new int[b];
        dij = new Double[b];
        mark = new Boolean[b];
        megjegyzes = new String[b];
        datum = new Date[b][3];
        res.beforeFirst();
        b = 0;
        while (res.next()) {
        id[b] = res.getInt(1);
        tabla[b][0] = res.getString("iktsz");//iktsz
        tabla[b][1] = res.getString("kiszolgalas");//kiszolgalas
        tabla[b][2] = res.getString("nev");//nev
        datum[b][0] = res.getDate("beadas");
        datum[b][1] = res.getDate("jegyzoi_ig");
        datum[b][2] = res.getDate("megszunes");
        dij[b] = res.getDouble("dij");
        mark[b] = res.getBoolean("mark");
        megjegyzes[b] = res.getString("megjegyzes");
        for (int i = 3; i < 34; i++) {
        tabla[b][i] = "";
        }
        b++;

        }
        stmt.close();
        } catch (Exception ex) {
        new Uzenet("Adatbázis hiba (étkezési lista)", "Hiba", Uzenet.ERROR);
        ex.printStackTrace();
        }
        try {
        stmt = (PreparedStatement) con.prepareStatement("SELECT ID, checksum, lezarva, dij, kiszolgalas, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 FROM etkeztetes WHERE ev=? AND ho=?");
        stmt.setString(1, ev);
        stmt.setString(2, ho);
        res = stmt.executeQuery();
        if (res != null) {
        res.last();
        if (res.getRow() != 0) {
        Main.foablak.enableLezaras(Globals.ETKEZTETES);
        }
        res.beforeFirst();
        int b = 0;
        while (res.next()) {
        b = res.getInt(1);
        if (res.getString(3).equals("1")) {
        lezarva = true;
        } else {
        lezarva = false;
        }
        for (int j = 0; j < id.length; j++) {

        if (id[j] == b) {
        dij[j] = res.getDouble("dij");
        if (res.getBoolean("lezarva")) {
        tabla[j][1] = res.getString("kiszolgalas");
        }
        for (int i = 3; i < 34; i++) {
        tabla[j][i] = res.getBoolean(i + 3) ? "1" : "";
        }
        //                                if (res.getInt("checksum") != dataToCode(j)) {
        //                                    new Uzenet("Adatintegritás hiba (Étkeztetés betöltés)", "Hiba", Uzenet.WARNING);
        //                                    for (int i = 3; i < 34; i++) {
        //                                        tabla[j][i] = "";
        //                                    }
        //                                }
        }
        }
        }
        } //ha üres a lekérdezés, akkor új hónap indul
        } catch (Exception ex) {
        new Uzenet("Adatbázis hiba (Étkeztetés adatok beolvasása)", "Hiba", Uzenet.ERROR);
        ex.printStackTrace();
        }
        lek.close();*/// </editor-fold>
        TableModel model = new DefaultTableModel(tabla, Globals.FEJLEC_ETKEZTETES) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex < 3 ? false : true;
            }
        };
        tblEtkeztetes.setModel(model);
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);

        tblEtkeztetes.setRowSorter(sorter);

        for (int i = 0; i < 34; i++) {
            tblEtkeztetes.getColumnModel().getColumn(i).setMinWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
            tblEtkeztetes.getColumnModel().getColumn(i).setMaxWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
            tblEtkeztetes.getColumnModel().getColumn(i).setPreferredWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
        }
//                tblEtkeztetes.setCellSelectionEnabled(false);
        tblEtkeztetes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblEtkeztetes.getModel().addTableModelListener(this);
        tblEtkeztetes.getSelectionModel().addListSelectionListener(this);
        osszegTabla();
        if ((lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK)))) {
            JOptionPane.showMessageDialog(this, "Az adatok nem módosíthatók!", "Lezárt hónap", JOptionPane.INFORMATION_MESSAGE);
        }
        tfEv.setEnabled(false);
        cbHonap.setEnabled(false);
        btnOk.setEnabled(false);
        log.writeLog("Étkeztetés - " + ev + " " + ho + " hónap megtekintve");
    }

    private void generateExcelSummaryTables(File file) {
//        Map nevsor = new HashMap(2000);
//        Vector adatok = new Vector();
        jxl.write.Number num;
        jxl.write.Label label;
        Formula form;
        NumberFormat nf = new NumberFormat("0");
        WritableCellFormat cf = new WritableCellFormat(nf);
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //1. füzetlap
            WritableSheet sheet = workbook.createSheet("Étkeztetés tanúsítvány", 0);
            //fejlécek kitöltése
            label = new jxl.write.Label(0, 0, "Sorszám");
            sheet.addCell(label);
            label = new jxl.write.Label(1, 0, "Iktsz");
            sheet.addCell(label);
            label = new jxl.write.Label(2, 0, "Ellátott neve");
            sheet.addCell(label);
            label = new jxl.write.Label(3, 0, "Születési év");
            sheet.addCell(label);
            label = new jxl.write.Label(4, 0, "Ellátás kezdete");
            sheet.addCell(label);
            label = new jxl.write.Label(5, 0, "Ellátás vége");
            sheet.addCell(label);

            //2. füzetlap
            WritableSheet sheet2 = workbook.createSheet("Étkeztetés kimutatás", 1);
            //fejlécek kitöltése
            label = new jxl.write.Label(0, 0, "Sorsz.");
            sheet2.addCell(label);
            label = new jxl.write.Label(1, 0, "Iktsz");
            sheet2.addCell(label);
            label = new jxl.write.Label(2, 0, "Név");
            sheet2.addCell(label);
            label = new jxl.write.Label(3, 0, "Szül. éve");
            sheet2.addCell(label);
            label = new jxl.write.Label(4, 0, "JAN");
            sheet2.addCell(label);
            label = new jxl.write.Label(5, 0, "FEB");
            sheet2.addCell(label);
            label = new jxl.write.Label(6, 0, "MÁRC");
            sheet2.addCell(label);
            label = new jxl.write.Label(7, 0, "ÁPR");
            sheet2.addCell(label);
            label = new jxl.write.Label(8, 0, "MÁJ");
            sheet2.addCell(label);
            label = new jxl.write.Label(9, 0, "JÚN");
            sheet2.addCell(label);
            label = new jxl.write.Label(10, 0, "JÚL");
            sheet2.addCell(label);
            label = new jxl.write.Label(11, 0, "AUG");
            sheet2.addCell(label);
            label = new jxl.write.Label(12, 0, "SZEPT");
            sheet2.addCell(label);
            label = new jxl.write.Label(13, 0, "OKT");
            sheet2.addCell(label);
            label = new jxl.write.Label(14, 0, "NOV");
            sheet2.addCell(label);
            label = new jxl.write.Label(15, 0, "DEC");
            sheet2.addCell(label);
            label = new jxl.write.Label(16, 0, "ÖSSZESEN");
            sheet2.addCell(label);
            label = new jxl.write.Label(17, 0, "Egyéb szoc. ell. amiben részesül");
            sheet2.addCell(label);

            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            stmt = (PreparedStatement) con.prepareStatement("SELECT DISTINCT ugyfel.ID, iktsz, nev, YEAR(szul_ido), ellatas, ev, ho, CHECKSUM "
                    + "FROM ugyfel LEFT JOIN etkeztetes ON ugyfel.ID=etkeztetes.ID WHERE ev=? AND CHECKSUM>0 GROUP BY ID, ev, ho");
            stmt.setString(1, tfEv.getText());
//            stmt.setString(2, ho);
            res = stmt.executeQuery();

            Integer sorszam = 0;
            String ellataskezdete;
            Integer honap = 0;
            int current_id = 0;
            int ex_id = 0;
            int row = 0;
            String exrow = "";
            while (res.next()) {
                exrow = String.valueOf(row + 2);
                current_id = res.getInt("ID");
                if (current_id != ex_id) {//ha nem egyezik, akkor új sor (új ügyfél)
                    row++;
                    sorszam++;
                    ex_id = current_id;
                    label = new jxl.write.Label(0, row, sorszam.toString() + ".");
                    sheet.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(1, row, res.getString("iktsz"));
                    sheet.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(2, row, res.getString("nev"));
                    sheet.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(3, row, res.getString(4));//szül. év
                    sheet.addCell(label);
//                    sheet2.addCell(label);
                    ellataskezdete = res.getString(5) == null ? "" : res.getString(5);
                    label = new jxl.write.Label(4, row, ellataskezdete);//ellátás kezdete
                    sheet.addCell(label);

                    label = new jxl.write.Label(0, row, sorszam.toString() + ".");
//                    sheet.addCell(label);
                    sheet2.addCell(label);
                    label = new jxl.write.Label(1, row, res.getString("iktsz"));
//                    sheet.addCell(label);
                    sheet2.addCell(label);
                    label = new jxl.write.Label(2, row, res.getString("nev"));
//                    sheet.addCell(label);
                    sheet2.addCell(label);
                    label = new jxl.write.Label(3, row, res.getString(4));//szül. év
//                    sheet.addCell(label);
                    sheet2.addCell(label);
                    honap = res.getInt("ho");
                    num = new jxl.write.Number(3 + honap, row, res.getDouble("checksum"), cf);//fogyasztás
                    sheet2.addCell(num);
                    form = new Formula(16, row, "SUM(E" + exrow + ":P" + exrow + ")");
                    sheet2.addCell(form);
                } else {
                    ex_id = current_id;
                    honap = res.getInt("ho");
                    num = new jxl.write.Number(3 + honap, row, res.getDouble("checksum"), cf);//fogyasztás
                    sheet2.addCell(num);
                }
            }
            row++;
            exrow = String.valueOf(row);
            label = new jxl.write.Label(0, row, "ÖSSZESEN:");
//                    sheet.addCell(label);
            sheet2.addCell(label);
            form = new Formula(4, row, "SUM(E1:E" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(5, row, "SUM(F1:F" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(6, row, "SUM(G1:G" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(7, row, "SUM(H1:H" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(8, row, "SUM(I1:I" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(9, row, "SUM(J1:J" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(10, row, "SUM(K1:K" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(11, row, "SUM(L1:L" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(12, row, "SUM(M1:M" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(13, row, "SUM(N1:N" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(14, row, "SUM(O1:O" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(15, row, "SUM(P1:P" + exrow + ")");
            sheet2.addCell(form);
            form = new Formula(16, row, "SUM(Q1:Q" + exrow + ")");
            sheet2.addCell(form);



            //3. füzetlap
            WritableSheet sheet3 = workbook.createSheet("Melegedő tanúsítvány", 2);
            //fejlécek kitöltése
            label = new jxl.write.Label(0, 0, "Sorszám");
            sheet3.addCell(label);
            label = new jxl.write.Label(1, 0, "Iktsz");
            sheet3.addCell(label);
            label = new jxl.write.Label(2, 0, "Ellátott neve");
            sheet3.addCell(label);
            label = new jxl.write.Label(3, 0, "Születési év");
            sheet3.addCell(label);
            label = new jxl.write.Label(4, 0, "Ellátás kezdete");
            sheet3.addCell(label);
            label = new jxl.write.Label(5, 0, "Ellátás vége");
            sheet3.addCell(label);

            //4. füzetlap
            WritableSheet sheet4 = workbook.createSheet("Melegedő kimutatás", 3);
            //fejlécek kitöltése
            label = new jxl.write.Label(0, 0, "Sorsz.");
            sheet4.addCell(label);
            label = new jxl.write.Label(1, 0, "Iktsz");
            sheet4.addCell(label);
            label = new jxl.write.Label(2, 0, "Név");
            sheet4.addCell(label);
            label = new jxl.write.Label(3, 0, "Szül. éve");
            sheet4.addCell(label);
            label = new jxl.write.Label(4, 0, "JAN");
            sheet4.addCell(label);
            label = new jxl.write.Label(5, 0, "FEB");
            sheet4.addCell(label);
            label = new jxl.write.Label(6, 0, "MÁRC");
            sheet4.addCell(label);
            label = new jxl.write.Label(7, 0, "ÁPR");
            sheet4.addCell(label);
            label = new jxl.write.Label(8, 0, "MÁJ");
            sheet4.addCell(label);
            label = new jxl.write.Label(9, 0, "JÚN");
            sheet4.addCell(label);
            label = new jxl.write.Label(10, 0, "JÚL");
            sheet4.addCell(label);
            label = new jxl.write.Label(11, 0, "AUG");
            sheet4.addCell(label);
            label = new jxl.write.Label(12, 0, "SZEPT");
            sheet4.addCell(label);
            label = new jxl.write.Label(13, 0, "OKT");
            sheet4.addCell(label);
            label = new jxl.write.Label(14, 0, "NOV");
            sheet4.addCell(label);
            label = new jxl.write.Label(15, 0, "DEC");
            sheet4.addCell(label);
            label = new jxl.write.Label(16, 0, "ÖSSZESEN");
            sheet4.addCell(label);
            label = new jxl.write.Label(17, 0, "Egyéb szoc. ell. amiben részesül");
            sheet4.addCell(label);

            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            stmt = (PreparedStatement) con.prepareStatement("SELECT ugyfel.ID, iktsz, nev, YEAR(szul_ido), melegedo_felvetel, "
                    + "YEAR(datum), MONTH(datum), DAY(datum), szolg FROM ugyfel LEFT JOIN melegedo ON ugyfel.ID=melegedo.ID "
                    + "WHERE YEAR(datum)=? AND szolg>0 GROUP BY ID, YEAR(datum), MONTH(datum), DAY(datum)");
            stmt.setString(1, tfEv.getText());
//            stmt.setString(2, ho);
            res = stmt.executeQuery();

            sorszam = 0;
            current_id = 0;
            ex_id = 0;
            int curr_ho = 0;
            int ex_ho = 0;
            int havi_fogy = 0;
            row = 0;
            exrow = "";
            while (res.next()) {
                exrow = String.valueOf(row + 2);
                current_id = res.getInt("ID");
                if (current_id != ex_id) {//ha nem egyezik, akkor új sor (új ügyfél)
                    row++;
                    sorszam++;
//                    ex_id = current_id;
                    label = new jxl.write.Label(0, row, sorszam.toString() + ".");
                    sheet3.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(1, row, res.getString("iktsz"));
                    sheet3.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(2, row, res.getString("nev"));
                    sheet3.addCell(label);
//                    sheet2.addCell(label);
                    label = new jxl.write.Label(3, row, res.getString(4));//szül. év
                    sheet3.addCell(label);
//                    sheet2.addCell(label);
                    ellataskezdete = res.getString(5) == null ? "" : res.getString(5);
                    label = new jxl.write.Label(4, row, ellataskezdete);//ellátás kezdete
                    sheet3.addCell(label);

                    label = new jxl.write.Label(0, row, sorszam.toString() + ".");
//                    sheet.addCell(label);
                    sheet4.addCell(label);
                    label = new jxl.write.Label(1, row, res.getString("iktsz"));
//                    sheet.addCell(label);
                    sheet4.addCell(label);
                    label = new jxl.write.Label(2, row, res.getString("nev"));
//                    sheet.addCell(label);
                    sheet4.addCell(label);
                    label = new jxl.write.Label(3, row, res.getString(4));//szül. év
//                    sheet.addCell(label);
                    sheet4.addCell(label);
//                    honap=res.getInt("ho");
//                    num = new jxl.write.Number(3+honap, row, res.getDouble("checksum"),cf);//fogyasztás
//                    sheet2.addCell(num);
                    if (havi_fogy > 0 && ex_ho != 0) {
                        num = new jxl.write.Number(3 + ex_ho, row - 1, havi_fogy, cf);//fogyasztás
                        sheet4.addCell(num);
                    }
                    curr_ho = res.getInt(7);
                    havi_fogy = 1;
                    form = new Formula(16, row, "SUM(E" + exrow + ":P" + exrow + ")");
                    sheet4.addCell(form);
                } else {
                    curr_ho = res.getInt(7);
                    if (curr_ho == ex_ho) {
                        havi_fogy++;
                    } else {
                        num = new jxl.write.Number(3 + ex_ho, row, havi_fogy, cf);//fogyasztás
                        sheet4.addCell(num);
                        havi_fogy = 1;
                    }
                }
                ex_ho = curr_ho;
                ex_id = current_id;
            }
            //flush last sum to cell
            num = new jxl.write.Number(3 + ex_ho, row, havi_fogy, cf);//fogyasztás
            sheet4.addCell(num);

            row++;
            exrow = String.valueOf(row);
            label = new jxl.write.Label(0, row, "ÖSSZESEN:");
//                    sheet.addCell(label);
            sheet4.addCell(label);
            form = new Formula(4, row, "SUM(E1:E" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(5, row, "SUM(F1:F" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(6, row, "SUM(G1:G" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(7, row, "SUM(H1:H" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(8, row, "SUM(I1:I" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(9, row, "SUM(J1:J" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(10, row, "SUM(K1:K" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(11, row, "SUM(L1:L" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(12, row, "SUM(M1:M" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(13, row, "SUM(N1:N" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(14, row, "SUM(O1:O" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(15, row, "SUM(P1:P" + exrow + ")");
            sheet4.addCell(form);
            form = new Formula(16, row, "SUM(Q1:Q" + exrow + ")");
            sheet4.addCell(form);



            workbook.write();
            workbook.close();
            new Uzenet("A jelentés elkészült", "Jelentés", Uzenet.INFORMATION);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void generateExcelSummaryTablesMelegedo(File file) {
        Map nevsor = new HashMap(2000);
        Vector adatok = new Vector();
        jxl.write.Number num;
        jxl.write.Label label;
        Formula form;
        NumberFormat nf = new NumberFormat("0");
        WritableCellFormat cf = new WritableCellFormat(nf);
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);


            workbook.write();
            workbook.close();
            new Uzenet("A melegedő jelentés elkészült", "Jelentés", Uzenet.INFORMATION);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void generateSzamlazas(File file) {
        jxl.write.Number num;
        jxl.write.Label label;
        Formula form;
        NumberFormat nf = new NumberFormat("0.00");
        WritableCellFormat cf = new WritableCellFormat(nf);
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("számlázás", 0);
            label = new jxl.write.Label(0, 0, "Iktsz");
            sheet.addCell(label);
            label = new jxl.write.Label(1, 0, "Név");
            sheet.addCell(label);
            label = new jxl.write.Label(2, 0, "Adag");
            sheet.addCell(label);
            label = new jxl.write.Label(3, 0, "Díj");
            sheet.addCell(label);
            label = new jxl.write.Label(4, 0, "Netto");
            sheet.addCell(label);
            label = new jxl.write.Label(5, 0, "ÁFA");
            sheet.addCell(label);
            label = new jxl.write.Label(6, 0, "Brutto");
            sheet.addCell(label);

            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            stmt = (PreparedStatement) con.prepareStatement("SELECT iktsz, nev, checksum, etkeztetes.dij  FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=?");
            stmt.setString(1, ev);
            stmt.setString(2, ho);
            res = stmt.executeQuery();

            int row = 1;
            String exrow;
            while (res.next()) {
                exrow = String.valueOf(row + 1);
                label = new jxl.write.Label(0, row, res.getString("iktsz"));
                sheet.addCell(label);
                label = new jxl.write.Label(1, row, res.getString("nev"));
                sheet.addCell(label);
                num = new jxl.write.Number(2, row, res.getDouble("checksum"));
                sheet.addCell(num);
                num = new jxl.write.Number(3, row, res.getDouble("dij"), cf);
                sheet.addCell(num);
                form = new Formula(6, row, "C" + exrow + "*D" + exrow, cf);
                sheet.addCell(form);
                form = new Formula(5, row, "G" + exrow + "*0.2126", cf);  //27%-os áfa visszaszámolása...
                sheet.addCell(form);
                form = new Formula(4, row, "G" + exrow + "-F" + exrow, cf);
                sheet.addCell(form);
                row++;

            }

            workbook.write();
            workbook.close();

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

        jLabel1 = new javax.swing.JLabel();
        cbHonap = new javax.swing.JComboBox();
        lblAktualisNev = new javax.swing.JLabel();
        lblSorOsszesen = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblOsszesen = new javax.swing.JTable()
        {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                boolean szinez=false;

                String nap=String.valueOf(vColIndex+1);
                nap=nap.length()==1?"0"+nap:nap;
                String datum=ev+"-"+ho+"-"+nap;

                szinez=((settings.isHetvege(datum)&&!settings.isMunkaNap(datum))||settings.isUnnepNap(datum));

                if(szinez&& !isCellSelected(rowIndex, vColIndex)){
                    c.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("weekendcolor").toString())));
                    c.setForeground(Color.black);
                }else{
                    c.setBackground(Color.white);
                    c.setForeground(Color.black);
                }
                int szokoev=Integer.valueOf(ev);
                int hossztemp=Globals.HONAP_HOSSZ[Integer.valueOf(ho)];
                if((szokoev%4==0 || szokoev%100==0) && ho=="02") hossztemp=29;
                if(vColIndex>hossztemp-1){
                    c.setBackground(new Color(236, 233, 216));
                    c.setForeground(new Color(236, 233, 216));
                }
                return c;
            }
        }
        ;
        jLabel3 = new javax.swing.JLabel();
        btnMentes = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        try{
            tfEv = new javax.swing.JFormattedTextField(new MaskFormatter("####"));
            btnOk = new javax.swing.JButton();
            jScrollPane1 = new javax.swing.JScrollPane();
            tblEtkeztetes = new javax.swing.JTable(){
                public Component prepareRenderer(TableCellRenderer renderer,
                    int rowIndex, int vColIndex) {
                    Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                    boolean szinez=false;
                    if(vColIndex>2){
                        String nap=String.valueOf(vColIndex-2);
                        nap=nap.length()==1?"0"+nap:nap;
                        String datum=ev+"-"+ho+"-"+nap;
                        //            if(tblEtkeztetes.getValueAt(rowIndex, vColIndex).equals("1")){
                            //    if(teljesdij(datum, rowIndex))td=true;
                            //    else td=false;
                            //            }
                        szinez=((settings.isHetvege(datum)&&!settings.isMunkaNap(datum))||settings.isUnnepNap(datum));
                    }
                    if(szinez&& !isCellSelected(rowIndex, vColIndex)){
                        c.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("weekendcolor").toString())));
                        c.setForeground(Color.black);
                    }else{
                        c.setBackground(Color.white);
                        c.setForeground(Color.black);
                    }
                    //        if(td){
                        //            c.setBackground(Color.red);
                        //            c.setForeground(Color.black);
                        //        }else{
                        //            c.setBackground(Color.white);
                        //            c.setForeground(Color.black);
                        //        }
                    int szokoev=Integer.valueOf(ev);
                    int hossztemp=Globals.HONAP_HOSSZ[Integer.valueOf(ho)];
                    if((szokoev%4==0 || szokoev%100==0) && ho=="02") hossztemp=29;
                    if(vColIndex>hossztemp+2){
                        c.setBackground(new Color(236, 233, 216));
                    }
                    if(mark[tblEtkeztetes.convertRowIndexToModel(rowIndex)]&&vColIndex<Globals.HONAP_HOSSZ[Integer.valueOf(ho)]+3){
                        c.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
                        if(c instanceof JComponent){
                            JComponent jc=(JComponent)c;
                            jc.setToolTipText(megjegyzes[tblEtkeztetes.convertRowIndexToModel(rowIndex)]);
                        }
                    }else{
                        if(c instanceof JComponent){
                            JComponent jc=(JComponent)c;
                            jc.setToolTipText(null);
                        }
                    }
                    if(isCellSelected(rowIndex, vColIndex)){
                        c.setBackground(Color.blue);
                        c.setForeground(Color.white);
                    }
                    return c;
                }
            };
            jLabel5 = new javax.swing.JLabel();
            lblHonapOsszesen = new javax.swing.JLabel();
            jLabel6 = new javax.swing.JLabel();
            btnPrint = new javax.swing.JButton();
            jComboBox1 = new javax.swing.JComboBox();
            btnSzamlazas = new javax.swing.JButton();
            btnImport = new javax.swing.JButton();
            btnJelentes = new javax.swing.JButton();

            jLabel1.setText("Időszak:");

            cbHonap.setModel(new javax.swing.DefaultComboBoxModel(onyx.Globals.HONAPOK ));
            cbHonap.addItemListener(this);

            lblAktualisNev.setFont(new java.awt.Font("Tahoma", 1, 14));

            lblSorOsszesen.setFont(new java.awt.Font("Tahoma", 1, 12));
            lblSorOsszesen.setForeground(new java.awt.Color(0, 0, 255));

            jLabel2.setText("Összesen:");

            tblOsszesen.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
                },
                new String [] {
                    "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9", "Title 10", "Title 11", "Title 12", "Title 13", "Title 14", "Title 15", "Title 16", "Title 17", "Title 18", "Title 19", "Title 20", "Title 21", "Title 22", "Title 23", "Title 24", "Title 25", "Title 26", "Title 27", "Title 28", "Title 29", "Title 30", "Title 31"
                }
            ));
            tblOsszesen.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
            tblOsszesen.setAutoscrolls(false);
            tblOsszesen.setFocusable(false);
            tblOsszesen.setRowSelectionAllowed(false);
            tblOsszesen.getTableHeader().setResizingAllowed(false);
            tblOsszesen.getTableHeader().setReorderingAllowed(false);
            tblOsszesen.setUpdateSelectionOnSort(false);
            for(int i=0;i<31;i++){
                tblOsszesen.getColumnModel().getColumn(i).setMinWidth(i<9?20:25);
                tblOsszesen.getColumnModel().getColumn(i).setMaxWidth(i<9?20:25);
                tblOsszesen.getColumnModel().getColumn(i).setPreferredWidth(i<9?20:25);
            }
            tblOsszesen.setFont(new Font("Tahoma", Font.PLAIN, 10));
            tblOsszesen.setTableHeader(null);
            jScrollPane2.setViewportView(tblOsszesen);

            jLabel3.setText("Összesen:");

            btnMentes.addActionListener(this);
            btnMentes.setActionCommand("mentes");
            btnMentes.setText("Mentés");
            btnMentes.setEnabled(false);

            jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18));
            jLabel4.setText("Étkeztetés");

            tfEv.addFocusListener(this);
        }catch(Exception e){

        }

        btnOk.addActionListener(this);
        btnOk.setActionCommand("ok");
        btnOk.setText("OK");
        btnOk.setEnabled(false);

        TableModel model=new DefaultTableModel(tabla, Globals.FEJLEC_ETKEZTETES){
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        tblEtkeztetes.setModel(model);
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        tblEtkeztetes.setRowSorter(sorter);
        tblEtkeztetes.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblEtkeztetes.getTableHeader().setReorderingAllowed(false);
        for(int i=0;i<34;i++){
            tblEtkeztetes.getColumnModel().getColumn(i).setMinWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
            tblEtkeztetes.getColumnModel().getColumn(i).setMaxWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
            tblEtkeztetes.getColumnModel().getColumn(i).setPreferredWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
        }
        jScrollPane1.setViewportView(tblEtkeztetes);
        tblEtkeztetes.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jLabel5.setText("Hónap összesen:");

        lblHonapOsszesen.setFont(new java.awt.Font("Tahoma", 1, 14));
        lblHonapOsszesen.setForeground(new java.awt.Color(255, 0, 0));

        jLabel6.setText("Név:");

        btnPrint.addActionListener(this);
        btnPrint.setActionCommand("print");
        btnPrint.setText("Nyomtatás");
        btnPrint.setEnabled(false);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Név", "Iktsz." }));
        jComboBox1.setVisible(false);

        btnSzamlazas.setText("Számlázás");
        btnSzamlazas.setEnabled(false);
        btnSzamlazas.setActionCommand("szamlazas");
        btnSzamlazas.addActionListener(this);

        btnImport.setText("Import");
        btnImport.setEnabled(false);
        btnImport.setActionCommand("import");
        btnImport.addActionListener(this);

        btnJelentes.setText("Jelentés");
        btnJelentes.setActionCommand("jelentes");
        btnJelentes.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1005, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfEv))
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnOk)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(lblAktualisNev, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSorOsszesen, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnJelentes, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnImport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSzamlazas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHonapOsszesen, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 749, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnMentes, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(tfEv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOk)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(lblSorOsszesen, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblAktualisNev, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSzamlazas)
                            .addComponent(btnImport)
                            .addComponent(btnJelentes))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrint)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 522, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(lblHonapOsszesen, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))))
                .addGap(18, 18, 18)
                .addComponent(btnMentes)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnJelentes;
    private javax.swing.JButton btnMentes;
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSzamlazas;
    private javax.swing.JComboBox cbHonap;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAktualisNev;
    private javax.swing.JLabel lblHonapOsszesen;
    private javax.swing.JLabel lblSorOsszesen;
    private javax.swing.JTable tblEtkeztetes;
    private javax.swing.JTable tblOsszesen;
    private javax.swing.JFormattedTextField tfEv;
    // End of variables declaration//GEN-END:variables
    private int[] id;
    private Object[][] tabla;
    private SQLMuvelet lek;
    private ResultSet res;
    private String ev = "00";
    private String ho = "00";
    private String[] param;
    private Object adat;
    private int selectedrowindex;
    private boolean lezarva = false;
    public static final String version = "1.0.2";
    private Connection con;
    private PreparedStatement stmt;
    private Date[][] datum;
    private Double[] dij;
    private Boolean[] mark;
    private String[] megjegyzes;
    private JPopupMenu popupmenu;
    private int aktualissor;
    private int dayofweek;
    private Log log = new Log();
    private Settings settings = new Settings();
    private boolean td;
    private DecimalFormat dcf = new DecimalFormat("00");
    private File docpath;
//TableModelListener implementáció

    public void tableChanged(TableModelEvent e) {

        int sor = e.getFirstRow();
//        int sorview = e.getFirstRow();
        int oszl = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        adat = (Boolean) model.getValueAt(sor, oszl); //kinyerjük a megváltozott adatot

//        if (!adat.toString().equals("")) {
//            if (Integer.valueOf(adat.toString().replace(" ", "")) > 1) {
//                adat = 1;
//                model.setValueAt("1", sor, oszl);
//            }
//
//        }



        tabla[sor][oszl] = adat; //és eltároljuk a tömbben

        lblAktualisNev.setText((String) model.getValueAt(sor, 2));  //cimkék frissítése
        lblSorOsszesen.setText(String.valueOf(osszegSor(sor)));
        osszegTabla();  //összegsor előállítása


        btnMentes.setEnabled(!(lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK)))); //ha változott, akkor mentés gomb aktív, kivéve, ha le van zárva
        Globals.MENTVE = lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK));
        Main.foablak.disableLezaras(Globals.ETKEZTETES); //ha változás van, a lezárás menüpont inaktiválódik mentésig
    }
//ActionListener implementáció

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            String kovho = dcf.format(Integer.valueOf(ho) + 1 > 12 ? 1 : Integer.valueOf(ho) + 1);
            String kovev = Integer.valueOf(ho) + 1 > 12 ? String.valueOf(Integer.valueOf(ev) + 1) : ev;
            int hossz = 0;
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etkeztetes WHERE ev=? AND ho=?");
                stmt.setString(1, ev);
                stmt.setString(2, ho);
                res = stmt.executeQuery();
                res.next();
                if (res.getInt(1) == 0) {       //még nem volt rögzítve = új hónap
//ügyfelek számának lekérdezése
                    stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM ugyfel"
                            + " WHERE etkezes=1 AND statusz='u' AND hidden_etk=0 AND (deleted=0 OR del_date>?)");
                    stmt.setDate(1, Date.valueOf(kovev + "-" + kovho + "-01"));
                    res = stmt.executeQuery();
                    res.next();
                    hossz = res.getInt(1);
//tömbök előállítása a lekérdezett méretre
                    id = new int[hossz];

                    tabla = new Object[hossz][34];
                    dij = new Double[hossz];
                    datum = new Date[hossz][3];
                    mark = new Boolean[hossz];
                    megjegyzes = new String[hossz];
//ügyfelek adatainak lekérdezése
                    stmt = (PreparedStatement) con.prepareStatement("SELECT ID, iktsz, kiszolgalas, nev, "
                            + "beadas, jegyzoi_ig, megszunes, dij, mark, megjegyzes  FROM ugyfel "
                            + "WHERE etkezes=1 AND statusz='u' AND hidden_etk=0 AND (deleted=0 OR del_date>?) ORDER BY nev");
                    stmt.setDate(1, Date.valueOf(kovev + "-" + kovho + "-01"));
                    res = stmt.executeQuery();
                    int b = 0;
                    while (res.next()) {
                        id[b] = res.getInt(1);
                        tabla[b][0] = res.getString("iktsz");//iktsz
                        tabla[b][1] = res.getString("kiszolgalas");//kiszolgalas
                        tabla[b][2] = res.getString("nev");//nev
                        datum[b][0] = res.getDate("beadas");
                        datum[b][1] = res.getDate("jegyzoi_ig");
                        datum[b][2] = res.getDate("megszunes");
                        dij[b] = res.getDouble("dij");
                        mark[b] = res.getBoolean("mark");
                        megjegyzes[b] = res.getString("megjegyzes");

                        for (int i = 3; i < 34; i++) {
                            tabla[b][i] = false;
                        }
                        b++;

                    }


                } else {                      //már volt rögzítve
                    stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etkeztetes WHERE ev=? AND ho=?");
                    stmt.setString(1, ev);
                    stmt.setString(2, ho);
                    res = stmt.executeQuery();
                    res.next();
                    hossz = res.getInt(1);
// tömbök előállítása a lekérdezés szerinti méretre
                    id = new int[hossz];

                    tabla = new Object[hossz][34];
                    dij = new Double[hossz];
                    datum = new Date[hossz][3];
                    mark = new Boolean[hossz];
                    megjegyzes = new String[hossz];
// névsor lekérdezése és tömbökbe töltése
                    stmt = (PreparedStatement) con.prepareStatement("SELECT e.ID, iktsz, e.kiszolgalas, nev,"
                            + " beadas, jegyzoi_ig, megszunes, e.dij, mark, megjegyzes, lezarva, "
                            + "n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16,"
                            + " n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 "
                            + "FROM etkeztetes AS e LEFT JOIN ugyfel AS u ON e.ID=u.ID WHERE ev=? AND ho=? ORDER BY nev");
                    stmt.setString(1, ev);
                    stmt.setString(2, ho);
                    res = stmt.executeQuery();
                    int b = 0;
                    while (res.next()) {
                        id[b] = res.getInt("ID");
                        lezarva = res.getBoolean("lezarva");
                        dij[b] = res.getDouble("dij");
                        tabla[b][0] = res.getString("iktsz");
                        tabla[b][1] = res.getString("kiszolgalas");
                        tabla[b][2] = res.getString("nev");
                        datum[b][0] = res.getDate("beadas");
                        datum[b][1] = res.getDate("jegyzoi_ig");
                        datum[b][2] = res.getDate("megszunes");
                        mark[b] = res.getBoolean("mark");
                        megjegyzes[b] = res.getString("megjegyzes");
                        for (int i = 3; i < 34; i++) {

                            tabla[b][i] = res.getBoolean(i + 9);
                        }
                        b++;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Uzenet("Hiba az adatok betöltése közben", "Étkeztetés", Uzenet.ERROR);
            }







            // <editor-fold defaultstate="collapsed" desc="comment">
            /*lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT ID, iktsz, kiszolgalas, nev, beadas, jegyzoi_ig, megszunes, dij, mark, megjegyzes  FROM ugyfel WHERE etkezes=1 AND statusz='u' AND (deleted=0 OR del_date>?)");
            stmt.setDate(1, Date.valueOf(ev + "-" + ho + "-31"));
            res = stmt.executeQuery();
            } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (étkezés lista betöltés)", "Hiba", Uzenet.ERROR);
            ex.printStackTrace();
            }
            try {
            res.last();
            int b = res.getRow();
            tabla = new String[b][34];
            id = new int[b];
            dij = new Double[b];
            mark = new Boolean[b];
            megjegyzes = new String[b];
            datum = new Date[b][3];
            res.beforeFirst();
            b = 0;
            while (res.next()) {
            id[b] = res.getInt(1);
            tabla[b][0] = res.getString("iktsz");//iktsz
            tabla[b][1] = res.getString("kiszolgalas");//kiszolgalas
            tabla[b][2] = res.getString("nev");//nev
            datum[b][0] = res.getDate("beadas");
            datum[b][1] = res.getDate("jegyzoi_ig");
            datum[b][2] = res.getDate("megszunes");
            dij[b] = res.getDouble("dij");
            mark[b] = res.getBoolean("mark");
            megjegyzes[b] = res.getString("megjegyzes");
            for (int i = 3; i < 34; i++) {
            tabla[b][i] = "";
            }
            b++;

            }
            stmt.close();
            } catch (Exception ex) {
            new Uzenet("Adatbázis hiba (étkezési lista)", "Hiba", Uzenet.ERROR);
            ex.printStackTrace();
            }
            try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT ID, checksum, lezarva, dij, kiszolgalas, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31 FROM etkeztetes WHERE ev=? AND ho=?");
            stmt.setString(1, ev);
            stmt.setString(2, ho);
            res = stmt.executeQuery();
            if (res != null) {
            res.last();
            if (res.getRow() != 0) {
            Main.foablak.enableLezaras(Globals.ETKEZTETES);
            }
            res.beforeFirst();
            int b = 0;
            while (res.next()) {
            b = res.getInt(1);
            if (res.getString(3).equals("1")) {
            lezarva = true;
            } else {
            lezarva = false;
            }
            for (int j = 0; j < id.length; j++) {

            if (id[j] == b) {
            dij[j] = res.getDouble("dij");
            if (res.getBoolean("lezarva")) {
            tabla[j][1] = res.getString("kiszolgalas");
            }
            for (int i = 3; i < 34; i++) {
            tabla[j][i] = res.getBoolean(i + 3) ? "1" : "";
            }
            //                                if (res.getInt("checksum") != dataToCode(j)) {
            //                                    new Uzenet("Adatintegritás hiba (Étkeztetés betöltés)", "Hiba", Uzenet.WARNING);
            //                                    for (int i = 3; i < 34; i++) {
            //                                        tabla[j][i] = "";
            //                                    }
            //                                }
            }
            }
            }
            } //ha üres a lekérdezés, akkor új hónap indul
            } catch (Exception ex) {
            new Uzenet("Adatbázis hiba (Étkeztetés adatok beolvasása)", "Hiba", Uzenet.ERROR);
            ex.printStackTrace();
            }
            lek.close();*/// </editor-fold>
            TableModel model = new DefaultTableModel(tabla, Globals.FEJLEC_ETKEZTETES) {

                Class[] types = new Class[]{
                    java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class, java.lang.Boolean.class,
                    java.lang.Boolean.class
                };

                @Override
                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnIndex < 3 ? false : true;
                }
            };
            tblEtkeztetes.setModel(model);
            RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);

            tblEtkeztetes.setRowSorter(sorter);

            for (int i = 0; i < 34; i++) {
                tblEtkeztetes.getColumnModel().getColumn(i).setMinWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
                tblEtkeztetes.getColumnModel().getColumn(i).setMaxWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
                tblEtkeztetes.getColumnModel().getColumn(i).setPreferredWidth(Globals.OSZLOPMERET_ETKEZTETES[i]);
            }
//                tblEtkeztetes.setCellSelectionEnabled(false);
            tblEtkeztetes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            tblEtkeztetes.getModel().addTableModelListener(this);
            tblEtkeztetes.getSelectionModel().addListSelectionListener(this);
            osszegTabla();
            if ((lezarva && !(Globals.checkJogosultsag(Globals.JOG_FONOK)))) {
                JOptionPane.showMessageDialog(this, "Az adatok nem módosíthatók!", "Lezárt hónap", JOptionPane.INFORMATION_MESSAGE);
            }
            tfEv.setEnabled(false);
            cbHonap.setEnabled(false);
            btnOk.setEnabled(false);
            btnImport.setEnabled(true);
            btnSzamlazas.setEnabled(true);
            btnPrint.setEnabled(true);
            log.writeLog("Étkeztetés - " + ev + " " + ho + " hónap megtekintve");
        }
        if (e.getActionCommand().equals("mentes")) {
            try {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                int temp, hossz, fogytemp;
                lek = new SQLMuvelet();
                con = (Connection) lek.getConnection();
                try {
                    stmt = (PreparedStatement) con.prepareStatement("SELECT COUNT(ID) FROM etkeztetes WHERE ev=? AND ho=?");
                    stmt.setInt(1, Integer.valueOf(ev));
                    stmt.setInt(2, Integer.valueOf(ho));
                    res = stmt.executeQuery();
                    res.next();
                    hossz = res.getInt(1);
                    if (hossz > 0) {
                        stmt = (PreparedStatement) con.prepareStatement("DELETE FROM etkeztetes WHERE ev=? AND ho=?");
                        stmt.setString(1, ev);
                        stmt.setString(2, ho);
                        stmt.executeUpdate();
                    }
//                    if (hossz == 0) {   //még nem volt rögzítve a hónap
                    for (int i = 0; i < tabla.length; i++) {
                        stmt = (PreparedStatement) con.prepareStatement("INSERT INTO etkeztetes (ID, ev, ho, checksum, rogzitve, rogz_id, dij, kiszolgalas, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31) VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        stmt.setInt(1, id[i]);
                        stmt.setString(2, ev);
                        stmt.setString(3, ho);
                        stmt.setInt(5, Globals.getUserID());
                        stmt.setDouble(6, dij[i]);

                        stmt.setString(7, (String) tabla[i][1]);//kiszolgálás
                        temp = 0;
                        for (int j = 3; j < 34; j++) {
                            stmt.setBoolean(j + 5, (Boolean) tabla[i][j]);
                            if ((Boolean) tabla[i][j]) {
                                temp++;
                            }
                        }
                        stmt.setInt(4, temp);//checksum
                        stmt.executeUpdate();
                    }
                    btnMentes.setEnabled(false);
                    Globals.MENTVE = true;
                    Main.foablak.enableLezaras(Globals.ETKEZTETES);
                    log.writeLog("Étkeztetés mentve");


//                   
                    con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Uzenet("Hiba a mentésnél!", "Étkeztetés", Uzenet.ERROR);
                }












                // <editor-fold defaultstate="collapsed" desc="comment">
/*param = new String[4];
                for (int i = 0; i < tabla.length; i++) {//végigmegy minden soron
                hossz = 0;
                try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT checksum FROM etkeztetes WHERE ID=? AND ev=? AND ho=?");
                stmt.setInt(1, id[i]);
                stmt.setString(2, ev);
                stmt.setString(3, ho);
                res = stmt.executeQuery();
                } catch (SQLException ex) {
                new Uzenet("Adatbázis hiba (étkezés mentés) ID:" + id[i], "Hiba", Uzenet.ERROR);
                ex.printStackTrace();
                }

                temp = dataToCode(i);
                fogytemp = 0;
                try {
                res.last();
                hossz = res.getRow();   //a bejegyzések száma
                res.beforeFirst();
                if (hossz > 0) {
                res.next();
                fogytemp = res.getInt(1);
                }
                stmt.close();
                } catch (Exception ex) {
                ex.printStackTrace();
                }
                if (fogytemp == temp) {
                continue;//ha nem változott a fogyasztás, átugorjuk
                }
                if (hossz == 0) {   //ha nincs rögzítve

                try {
                //ha nincs rögzítve
                stmt = (PreparedStatement) con.prepareStatement("INSERT INTO etkeztetes (ID, ev, ho, checksum, rogzitve, rogz_id, dij, kiszolgalas, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, n26, n27, n28, n29, n30, n31) VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(1, id[i]);
                stmt.setString(2, ev);
                stmt.setString(3, ho);
                stmt.setInt(4, dataToCode(i));
                stmt.setInt(5, Globals.getUserID());
                stmt.setDouble(6, dij[i]);
                stmt.setString(7, tabla[i][1]);//kiszolgálás
                for (int j = 3; j < 34; j++) {
                stmt.setBoolean(j + 5, tabla[i][j].equals("1") ? true : false);
                }
                if (stmt.executeUpdate() > 0) {
                btnMentes.setEnabled(false);    //sikeres mentés esetén mentés gomb inaktív
                Globals.MENTVE = true;
                Main.foablak.enableLezaras(Globals.ETKEZTETES); //mentés után a lezárás menüpont aktív
                stmt.close();
                log.writeLog("Étkeztetés - új étkező rögzítve (ID: " + id[i] + " fogyasztás: " + dataToCode(i) + ")");
                }
                } catch (SQLException ex) {
                new Uzenet("Hiba a mentésnél (új rekord beszúrása) ID:" + id[i], "Mentés", Uzenet.ERROR);
                ex.printStackTrace();
                }

                } else {    // ha már volt rögzítve

                try {
                stmt = (PreparedStatement) con.prepareStatement("UPDATE etkeztetes SET checksum=?, rogzitve=NOW(), rogz_id=?, dij=?, kiszolgalas=?, n1=?, n2=?, n3=?, n4=?, n5=?, n6=?, n7=?, n8=?, n9=?, n10=?, n11=?, n12=?, n13=?, n14=?, n15=?, n16=?, n17=?, n18=?, n19=?, n20=?, n21=?, n22=?, n23=?, n24=?, n25=?, n26=?, n27=?, n28=?, n29=?, n30=?, n31=? WHERE ID=? AND ev=? AND ho=?");
                stmt.setInt(1, i);
                stmt.setInt(2, Globals.getUserID());
                stmt.setDouble(3, dij[i]);
                stmt.setString(4, tabla[i][1]);
                stmt.setInt(36, id[i]);
                stmt.setString(37, ev);
                stmt.setString(38, ho);
                for (int j = 3; j < 34; j++) {
                stmt.setBoolean(j + 2, tabla[i][j].equals("1") ? true : false);
                }
                if (stmt.executeUpdate() > 0) {
                btnMentes.setEnabled(false);    //ha sikeres, akkor inaktív mentés gomb
                Globals.MENTVE = true;
                Main.foablak.enableLezaras(Globals.ETKEZTETES); //mentés után a lezárás menüpont aktív
                stmt.close();
                log.writeLog("Étkeztetés - fogyasztás módosítva (ID: " + id[i] + " új fogyasztás: " + dataToCode(i) + ")");

                }
                } catch (Exception ex) {
                new Uzenet("Hiba a mentésnél (rekord frissítése) ID:" + id[i], "Mentés", Uzenet.ERROR);
                ex.printStackTrace();
                }

                }
                }
                lek.close();*/// </editor-fold>
                if (lezarva) {
                    honapLezaras();
                }
            } finally {
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
        if (e.getActionCommand().equals("print")) {
            try {
                tblEtkeztetes.print();
                log.writeLog("Étkeztetés - " + ev + " " + ho + " havi táblázat kinyomtatva");
            } catch (PrinterException ex) {
                new Uzenet("Nyomtatási hiba", "Nyomtatás", Uzenet.WARNING);
                ex.printStackTrace();
            }
        }
        if (e.getActionCommand().equals("mindennapevett")) {
            for (int i = 0; i < Globals.HONAP_HOSSZ[Integer.valueOf(ho)]; i++) {
//                DecimalFormat dcf = new DecimalFormat("00");
                String nap = dcf.format(i + 1);



                String datum = ev + "-" + ho + "-" + nap;
                boolean hetkoznap = !((settings.isHetvege(datum) && !settings.isMunkaNap(datum)) || settings.isUnnepNap(datum));


//                Calendar cal = new GregorianCalendar();
//                cal.set(Integer.valueOf(ev), Integer.valueOf(ho) - 1, i + 1);
//                cal.setFirstDayOfWeek(Calendar.MONDAY);
//                dayofweek = cal.get(Calendar.DAY_OF_WEEK);
//                if ((ho.equals("01") && i == 0) || (ho.equals("03") && i == 14) || (ho.equals("05") && i == 0) || (ho.equals("08") && i == 19) || (ho.equals("10") && i == 22) || (ho.equals("12") && i == 24) || (ho.equals("12") && i == 25)) {
//                    dayofweek = Calendar.SATURDAY;
//                }
                if (hetkoznap) {
//                    tblEtkeztetes.setValueAt("1", tblEtkeztetes.convertRowIndexToModel(aktualissor), i+3);

                    tblEtkeztetes.getModel().setValueAt(true, tblEtkeztetes.convertRowIndexToModel(aktualissor), i + 3);
                    tabla[tblEtkeztetes.convertRowIndexToModel(aktualissor)][i + 3] = true;
                }
            }
        }
        if (e.getActionCommand().equals("sortorles")) {
            for (int i = 0; i < 31; i++) {

                tblEtkeztetes.getModel().setValueAt(false, tblEtkeztetes.convertRowIndexToModel(aktualissor), i + 3);
                tabla[tblEtkeztetes.convertRowIndexToModel(aktualissor)][i + 3] = false;
            }
        }
        if (e.getActionCommand().equals("adatlap")) {
            final JFrame frame = new JFrame("Adatlap");
//            frame.setUndecorated(true);
            Adatlap adatlap = new Adatlap(id[tblEtkeztetes.convertRowIndexToModel(aktualissor)], Globals.UGYFEL);
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
        if (e.getActionCommand().equals("szamlazas")) {
            File file = new File("Számlázás_" + ev + "_" + ho + ".xls");
            JFileChooser chooser = new JFileChooser(Config.settings.getProperty("savepath") + "\\Számlázás");
            FileNameExtensionFilter xlsfilter = new FileNameExtensionFilter("Excel file-ok", "xls");
            chooser.setFileFilter(xlsfilter);
            chooser.setSelectedFile(file);
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                docpath = chooser.getSelectedFile();
                if (docpath.exists()) {
                    Uzenet fileexists = new Uzenet("<html>A file már létezik!<br>Kívánja felülírni?", "Mentés", Uzenet.QUESTION);
                    if (fileexists.getValasz() == Uzenet.YES) {
                        generateSzamlazas(docpath);
                    }

                } else {
                    generateSzamlazas(docpath);
                }
            }
        }
        if (e.getActionCommand().equals("jelentes")) {
            File file = new File("Étkezés jelentés - "+tfEv.getText()+".xls");
            JFileChooser chooser = new JFileChooser(Config.settings.getProperty("savepath"));
            FileNameExtensionFilter xlsfilter = new FileNameExtensionFilter("Excel file-ok", "xls");
            chooser.setFileFilter(xlsfilter);
            chooser.setSelectedFile(file);
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                docpath = chooser.getSelectedFile();
                if (docpath.exists()) {
                    Uzenet fileexists = new Uzenet("<html>A file már létezik!<br>Kívánja felülírni?", "Mentés", Uzenet.QUESTION);
                    if (fileexists.getValasz() == Uzenet.YES) {
                        generateExcelSummaryTables(docpath);
                    }

                } else {
                    generateExcelSummaryTables(docpath);
                }
            }
        }
        if (e.getActionCommand().equals("import")) {
            int idk;
//            final Map kivalaszt=new HashMap();
            final Map dijak = new LinkedHashMap();
            final Map kisz = new LinkedHashMap();
            final Map lista = new LinkedHashMap();
            JCheckBox chb;
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT ID, nev, iktsz, dij, kiszolgalas, mark, hidden_etk "
                        + "FROM ugyfel WHERE etkezes=1 AND deleted=0 AND ID NOT IN (SELECT ID FROM etkeztetes WHERE ev=? AND ho=?) ORDER BY nev");
                stmt.setString(1, ev);
                stmt.setString(2, ho);
                res = stmt.executeQuery();
                while (res.next()) {
                    idk = res.getInt("ID");
                    dijak.put(idk, res.getFloat("dij"));
                    kisz.put(idk, res.getString("kiszolgalas"));
//                    kivalaszt.put(idk, false);
                    chb = new JCheckBox(res.getString("nev") + "(" + res.getString("iktsz") + ")", false);
                    if (res.getBoolean("mark")) {
                        chb.setBackground(new Color(Integer.valueOf(Config.settings.getProperty("markcolor").toString())));
                    }
                    if (res.getBoolean("hidden_etk")) {
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
                                stmt = (PreparedStatement) con.prepareStatement("INSERT INTO etkeztetes SET"
                                        + " ID=?, ev=?, ho=?, checksum='0', rogzitve=NOW(), rogz_id=?, lezarva='0', dij=?, kiszolgalas=?");
                                stmt.setInt(1, k[i]);
                                stmt.setString(2, ev);
                                stmt.setString(3, ho);
                                stmt.setInt(4, Globals.getUserID());
                                stmt.setFloat(5, Float.valueOf(dijak.get(k[i]).toString()));
                                stmt.setString(6, kisz.get(k[i]).toString());
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
            Container cont = fr.getContentPane();
            cont.add(scp, BorderLayout.NORTH);
            cont.add(gombok, BorderLayout.SOUTH);
//            fr.setPreferredSize(new Dimension(400, 750));
//            fr.add(keret);
            fr.setResizable(false);
            fr.setLocation(300, 30);
            fr.validate();
            fr.pack();
            fr.setVisible(true);

        }
    }

//ItemListener implementáció
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == cbHonap) {
            Integer temp;
            temp = cbHonap.getSelectedIndex();
            if (temp != -1 || temp != 0) {
//                ho = temp.toString();
                if (!ev.isEmpty()) {
                    btnOk.setEnabled(true);
                } else {
                    btnOk.setEnabled(false);
                }
//                DecimalFormat dcf = new DecimalFormat("00");
                ho = dcf.format(temp);
//                if (Integer.valueOf(ho) < 10) {
//                    ho = "0" + ho;
//                }
            }
        }

    }
//FocusListener implementáció

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (e.getSource() == tfEv) {
            ev = tfEv.getText();
            if (!ev.equals("    ")) {
                if (Integer.valueOf(ev) > Calendar.getInstance().get(Calendar.YEAR)) {
                    JOptionPane.showMessageDialog(this, "Az év nem lehet nagyobb a jelenleginél!", "Hiba", JOptionPane.ERROR_MESSAGE);
                    ev = "";
                    tfEv.setText("");
                }
            } else {
                ev = "";
            }
            if (ev.isEmpty()) {
                btnOk.setEnabled(false);
            }

        }
    }
//ListSelectionListener implementáció

    public void valueChanged(ListSelectionEvent e) {
//        selectedrowindex = e.getFirstIndex();
        if (!e.getValueIsAdjusting()) {
            selectedrowindex = tblEtkeztetes.convertRowIndexToModel(tblEtkeztetes.getSelectedRow());
            if (selectedrowindex != -1) {
                lblAktualisNev.setText((String) tblEtkeztetes.getModel().getValueAt(selectedrowindex, 2));
                lblSorOsszesen.setText(String.valueOf(osszegSor(selectedrowindex)));
            }


//            if (selectedrowindex != -1) {
//                lblAktualisNev.setText((String) tabla[tblEtkeztetes.convertRowIndexToModel(selectedrowindex)][2]);
//                //                lblAktualisNev.setText((String) tblEtkeztetes.getValueAt(selectedrowindex, 2));
//                lblSorOsszesen.setText(String.valueOf(osszegSor(selectedrowindex)));
//
//            }
        }
    }
}
