/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Naplo.java
 *
 * Created on 2010.01.08., 9:22:57
 *
 * Version 1.0
 */
package onyx;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author PIRI
 *
 * A különböző szolgáltatások naplóit előállító osztály
 */
public class Naplo extends javax.swing.JPanel {

    public static int ETKEZES_IGBV = 1;
    public static int MELEGEDO_ESEMENY = 2;
    private File docpath;
    private String datum;
    private SQLMuvelet lek;
    private Connection con;
    private java.sql.PreparedStatement stmt;
    private ResultSet res;
    private int letszam;
    private String[][] tabla;
    private String[][] tablaA;
    private String[][] tablaB;
    private String[][] tablaC;
    private Integer[] osszesen;
    private PdfPCell cell;
    private Paragraph parheader;
    private Paragraph partitle;
    private PdfPTable tablazat;
    private String filenev;
    private File file;
    private JFileChooser chooser;
    private FileNameExtensionFilter pdffilter;
    private int haviosszes;

    /** Creates new form Naplo */
    public Naplo(int tipus) {
        FontFactory.register("c:\\windows\\fonts\\times.ttf");
        FontFactory.register("c:\\windows\\fonts\\timesbd.ttf");
        initComponents();

        Object[] obj = new Object[3];


        switch (tipus) {
            case 1:     //étkezés igbv napló
                obj = Main.foablak.checkEtkezes();
                lblTitle.setText("Igénybevételi napló");
                tfEv.setText(obj != null ? (String) obj[0] : String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                cbHonap.setSelectedIndex(obj != null ? Integer.valueOf((String) obj[1]) : 0);
                tfNap.setText("--");
                tfNap.setEnabled(false);
                break;
            case 2:     //melegedő eseménynapló
                obj = Main.foablak.checkMelegedo();
                lblTitle.setText("Eseménynapló");
                tfEv.setText(obj != null ? (String) obj[0] : String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                cbHonap.setSelectedIndex(obj != null ? Integer.valueOf((String) obj[1]) : 0);
                tfNap.setText((String) (obj != null ? obj[2] : ""));
                break;
        }


    }

    /** Az Év mezőben beállított értékkel tér vissza */
    public String getEv() {
        if (tfEv.getText().replace(" ", "").isEmpty()) {
            return null;
        }
        return tfEv.getText();
    }

    /** A kiválasztott hónap indexével tér vissza. =0, ha nincs kiválasztva semmi */
    public Integer getHonapIndex() {
        int retval = cbHonap.getSelectedIndex();
        if (retval == 0) {
            return null;
        }
        return retval;
    }

    /** A kiválasztott hónap nevével tér vissza*/
    public String getHonap() {
        if (cbHonap.getSelectedIndex() == 0) {
            return null;
        }
        return (String) cbHonap.getSelectedItem();
    }

    /** A Nap mezőben beállított értékkel tér vissza */
    public String getNap() {
        if (tfNap.getText().replace(" ", "").isEmpty()) {
            return null;
        }
        return tfNap.getText();
    }

    private String replace(String str, String pattern, String replace) {
        int start = 0;
        int index = 0;
        StringBuffer result = new StringBuffer();

        while ((index = str.indexOf(pattern, start)) >= 0) {
            result.append(str.substring(start, index));
            result.append(replace);
            start = index + pattern.length();
        }
        result.append(str.substring(start));
        return result.toString();
    }

    /** A napló elkészítését végzi, betölti a szükséges adatokat az adatbázisból,
     * bekéri a mentendő napló filenevét, majd meghívja a dokumentumot előállító rutint.*/
    public void makeNaplo(int tipus) {


        switch (tipus) {
            case 1:     //étkezés igbv napló
                datum = getEv() + ". " + getHonap();
                lek = new SQLMuvelet();
                con = lek.getConnection();
                String s = "";
                boolean create = false;

                filenev = Config.settings.getProperty("etk_naplo_nev") + "_" + getEv() + "_" + getHonap() + ".pdf";
                file = new File(filenev);
                chooser = new JFileChooser(Config.settings.getProperty("savepath") + "\\Naplók");
                pdffilter = new FileNameExtensionFilter("Pdf dokumentumok", "pdf");
                chooser.setFileFilter(pdffilter);
                chooser.setSelectedFile(file);
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    docpath = chooser.getSelectedFile();
                    if (docpath.exists()) {
                        Uzenet fileexists = new Uzenet("<html>A file már létezik!<br>Kívánja felülírni?", "Mentés", Uzenet.QUESTION);
                        if (fileexists.getValasz() == Uzenet.YES) {
                            create = true;
                        }

                    } else {
                        create = true;
                    }
                } else {
                    create = false;
                }

                if (Integer.parseInt(getEv()) < 2010) {
                    for (int k = 0; k < 3; k++) {
                        try {
                            stmt = con.prepareStatement("SELECT COUNT(etkeztetes.ID) FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0 AND ugyfel.kategoria=?");
                            stmt.setInt(1, Integer.valueOf(getEv()));
                            stmt.setInt(2, getHonapIndex());
                            switch (k) {
                                case 0:
                                    s = "A";
                                    break;
                                case 1:
                                    s = "B";
                                    break;
                                case 2:
                                    s = "C";
                            }
                            stmt.setString(3, s);
                            res = stmt.executeQuery();
                            res.next();
                            letszam = res.getInt(1);
                            if (letszam == 0) {
                                new Uzenet("A \"" + s + "\" kategóriában 0 a létszám!", "Figyelem!", Uzenet.WARNING);
                            } else {
                                tabla = new String[32][letszam];
                                osszesen = new Integer[31];
                                for (int i = 0; i < 31; i++) {
                                    osszesen[i] = 0;
                                }
                                stmt = con.prepareStatement("SELECT ugyfel.nev, n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,n13,n14,n15,n16,n17,n18,n19,n20,n21,n22,n23,n24,n25,n26,n27,n28,n29,n30,n31 FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND  n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0 AND ugyfel.kategoria=? ORDER BY ugyfel.nev");
                                stmt.setInt(1, Integer.valueOf(getEv()));
                                stmt.setInt(2, getHonapIndex());
                                switch (k) {
                                    case 0:
                                        s = "A";
                                        break;
                                    case 1:
                                        s = "B";
                                        break;
                                    case 2:
                                        s = "C";
                                }
                                stmt.setString(3, s);
                                res = stmt.executeQuery();
                                for (int i = 0; i < letszam; i++) {
                                    res.next();
                                    tabla[0][i] = res.getString(1);
                                    for (int j = 1; j < 32; j++) {
                                        if (res.getInt(j + 1) == 1) {
                                            tabla[j][i] = "X";
                                            osszesen[j - 1] = osszesen[j - 1] + 1;
                                        } else {
                                            tabla[j][i] = "";
                                        }
                                    }

                                }
                                String temppath = "";
                                if (create) {

//                                temppath.replace(".pdf", "_");
                                    switch (k) {
                                        case 0:
                                            temppath = docpath.getPath();
                                            temppath = temppath.substring(0, temppath.length() - 4);
                                            temppath = temppath + "_A.pdf";
//                                        temppath.concat("A.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "A");
                                            break;
                                        case 1:
                                            temppath = docpath.getPath();
                                            temppath = temppath.substring(0, temppath.length() - 6);
                                            temppath = temppath + "_B.pdf";
//                                        temppath.concat("B.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "B");
                                            break;
                                        case 2:
                                            temppath = docpath.getPath();
                                            temppath = temppath.substring(0, temppath.length() - 6);
                                            temppath = temppath + "_C.pdf";
//                                        temppath.concat("C.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "C");
                                            break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (Integer.parseInt(getEv()) == 2010) {
                    try {
                        stmt = con.prepareStatement("SELECT COUNT(etkeztetes.ID) FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0");
                        stmt.setInt(1, Integer.valueOf(getEv()));
                        stmt.setInt(2, getHonapIndex());
                        res = stmt.executeQuery();
                        res.next();
                        letszam = res.getInt(1);
                        if (letszam == 0) {
                            new Uzenet("Ez a hónap még nem lett rögzítve!", "Figyelem!", Uzenet.WARNING);
                        } else {
                            tabla = new String[39][letszam];
                            osszesen = new Integer[31];
                            Integer napiosszes = new Integer(0);
                            for (int i = 0; i < 31; i++) {
                                osszesen[i] = 0;
                            }
                            stmt = con.prepareStatement("SELECT ugyfel.nev, n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,n13,n14,n15,n16,n17,n18,n19,n20,n21,n22,n23,n24,n25,n26,n27,n28,n29,n30,n31 FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND  n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0 ORDER BY ugyfel.nev");
                            stmt.setInt(1, Integer.valueOf(getEv()));
                            stmt.setInt(2, getHonapIndex());
                            res = stmt.executeQuery();
                            for (int i = 0; i < letszam; i++) {
                                res.next();
                                tabla[0][i] = res.getString(1);
                                napiosszes = 0;
                                for (int j = 1; j < 32; j++) {
                                    if (res.getInt(j + 1) == 1) {
                                        tabla[j][i] = "3";
                                        osszesen[j - 1] = osszesen[j - 1] + 1;
//                                        napiosszes = napiosszes + 1;
                                        napiosszes++;
                                    } else {
                                        tabla[j][i] = "";
                                    }
                                }
                                for (int j = 32; j < 39; j++) {
                                    tabla[j][i] = "";
                                }
                                tabla[34][i] = napiosszes.toString();

                            }

                            if (create) {
//                                createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, null);
                                String[] title = {"Otthonközeli ellátásra vonatkozó igénybevételi napló"};
                                createDocument(3, PageSize.A4.rotate(), Globals.NAPLO_HEADER, title, datum, null);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (Integer.parseInt(getEv()) >=2011) {
                    for (int k = 0; k < 3; k++) {
                        try {
                            stmt = con.prepareStatement("SELECT COUNT(etkeztetes.ID) FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0 AND etkeztetes.kiszolgalas=?");
                            stmt.setInt(1, Integer.valueOf(getEv()));
                            stmt.setInt(2, getHonapIndex());
                            switch (k) {
                                case 0:
                                    s = "s";    //szállítás
                                    break;
                                case 1:
                                    s = "e";    //elvitel
                                    break;
                                case 2:
                                    s = "h";    //helyben
                            }
                            stmt.setString(3, s);
                            res = stmt.executeQuery();
                            res.next();
                            letszam = res.getInt(1);
                            if (letszam == 0) {
                                new Uzenet("A \"" + s + "\" típusban 0 a létszám!", "Figyelem!", Uzenet.WARNING);
                            } else {
                                tabla = new String[33][letszam];
                                osszesen = new Integer[32];
                                haviosszes=0;
                                Integer napiosszes=new Integer(0);
                                for (int i = 0; i < 31; i++) {
                                    osszesen[i] = 0;
                                }
                                stmt = con.prepareStatement("SELECT ugyfel.nev, n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,n13,n14,n15,n16,n17,n18,n19,n20,n21,n22,n23,n24,n25,n26,n27,n28,n29,n30,n31 FROM etkeztetes LEFT JOIN ugyfel ON etkeztetes.ID=ugyfel.ID WHERE ev=? AND ho=? AND  n1+n2+n3+n4+n5+n6+n7+n8+n9+n10+n11+n12+n13+n14+n15+n16+n17+n18+n19+n20+n21+n22+n23+n24+n25+n26+n27+n28+n29+n30+n31>0 AND etkeztetes.kiszolgalas=? ORDER BY ugyfel.nev");
                                stmt.setInt(1, Integer.valueOf(getEv()));
                                stmt.setInt(2, getHonapIndex());
                                switch (k) {
                                    case 0:
                                        s = "s";
                                        break;
                                    case 1:
                                        s = "e";
                                        break;
                                    case 2:
                                        s = "h";
                                }
                                stmt.setString(3, s);
                                res = stmt.executeQuery();
                                for (int i = 0; i < letszam; i++) {
                                    napiosszes=new Integer(0);
                                    res.next();
                                    tabla[0][i] = res.getString(1);
                                    for (int j = 1; j < 32; j++) {
                                        if (res.getInt(j + 1) == 1) {
                                            tabla[j][i] = "X";
//                                            osszesen[j - 1] = osszesen[j - 1] + 1;
                                            osszesen[j-1]++;
                                            haviosszes++;
                                            napiosszes++;
                                        } else {
                                            tabla[j][i] = "";
                                        }
                                    }
                                    tabla[32][i]=napiosszes.toString();
                                    osszesen[31]=haviosszes;
                                }
                                String temppath = "";
                                if (create) {

//                                temppath.replace(".pdf", "_");
                                    switch (k) {
                                        case 0:
                                            temppath = docpath.getPath();
                                            temppath = replace(temppath, ".pdf", "_szállítás.pdf");
//                                            temppath = temppath.substring(0, temppath.length() - 4);
//                                            temppath = temppath + "_szállítás.pdf";
//                                        temppath.concat("A.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "Szállítás");
                                            break;
                                        case 1:
                                            temppath = docpath.getPath();
                                            temppath = replace(temppath, "_szállítás.pdf", "_elvitel.pdf");
//                                            temppath = temppath.substring(0, temppath.length() - 6);
//                                            temppath = temppath + "_elvitel.pdf";
//                                        temppath.concat("B.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "Elvitel");
                                            break;
                                        case 2:
                                            temppath = docpath.getPath();
                                            temppath = replace(temppath, "_elvitel.pdf", "_helyben.pdf");
//                                            temppath = temppath.substring(0, temppath.length() - 6);
//                                            temppath = temppath + "_helyben.pdf";
//                                        temppath.concat("C.pdf");
                                            docpath = new File(temppath);
                                            createDocument(tipus, PageSize.A4.rotate(), Globals.NAPLO_HEADER, Globals.ETKEZES_IGBV_TITLE, datum, "Helyben");
                                            break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;


            case 2:     //melegedő eseménynapló

                Calendar cal = new GregorianCalendar(Integer.valueOf(getEv()), getHonapIndex() - 1, Integer.valueOf(getNap()));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
                SimpleDateFormat sqlformatter = new SimpleDateFormat("yyyy-MM-dd");
                datum = formatter.format(cal.getTime());
                String sqldatum = sqlformatter.format(cal.getTime());
                lek = new SQLMuvelet();
                con = lek.getConnection();
                try {
                    stmt = con.prepareStatement("SELECT COUNT(ID) FROM melegedo WHERE datum=? AND szolg01+szolg02+szolg03+szolg04+szolg05+szolg06+szolg07+szolg08+szolg09+szolg10+szolg11+szolg12+szolg13>0");
                    stmt.setDate(1, Date.valueOf(sqldatum));
                    res = stmt.executeQuery();
                    res.next();
                    letszam = res.getInt(1);
                    if (letszam == 0) {
                        new Uzenet("Ez a nap még nem lett rögzítve!", "Figyelem!", Uzenet.WARNING);
                    } else {
                        tabla = new String[18][letszam];
                        stmt = con.prepareStatement("SELECT nev, szul_ido, szolg01, szolg02, szolg03, szolg04, szolg05, szolg06, szolg07, szolg08, szolg09, szolg10, szolg11, szolg12, szolg13 FROM melegedo LEFT JOIN ugyfel ON melegedo.ID=ugyfel.ID WHERE datum=? AND szolg01+szolg02+szolg03+szolg04+szolg05+szolg06+szolg07+szolg08+szolg09+szolg10+szolg11+szolg12+szolg13>0 ORDER BY nev");
                        stmt.setDate(1, Date.valueOf(sqldatum));
                        res = stmt.executeQuery();
                        for (int i = 0; i < letszam; i++) {
                            res.next();
                            tabla[0][i] = String.valueOf(i + 1) + ".";
                            tabla[1][i] = datum;
                            tabla[2][i] = res.getString("nev");
                            tabla[3][i] = res.getString("szul_ido") == null ? "" : res.getString("szul_ido").substring(0, 4);
                            for (int j = 4; j < 17; j++) {
                                tabla[j][i] = res.getInt(j - 1) == 1 ? "X" : "";
                            }
                            tabla[17][i] = "";    //aláírás mező
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                filenev = Config.settings.getProperty("melegedo_naplo_nev") + "_" + getEv() + "_" + getHonap() + "_" + getNap() + ".pdf";
                file = new File(filenev);
                chooser = new JFileChooser(Config.settings.getProperty("savepath") + "\\Naplók");
                pdffilter = new FileNameExtensionFilter("Pdf dokumentumok", "pdf");
                chooser.setFileFilter(pdffilter);
                chooser.setSelectedFile(file);
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    docpath = chooser.getSelectedFile();
                    if (docpath.exists()) {
                        Uzenet fileexists = new Uzenet("<html>A file már létezik!<br>Kívánja felülírni?", "Mentés", Uzenet.QUESTION);
                        if (fileexists.getValasz() == Uzenet.YES) {
                            createDocument(tipus, PageSize.A4, Globals.NAPLO_HEADER, Globals.MELEGEDO_ESEMENY_TITLE, datum, null);
                        }

                    } else {
                        createDocument(tipus, PageSize.A4, Globals.NAPLO_HEADER, Globals.MELEGEDO_ESEMENY_TITLE, datum, null);
                    }
                }
                break;
        }
    }

    private void createDocument(int tipus, Rectangle pagesize, String[] header, String[] title, String datum, String kat) {
        /** A paraméterekben meghatározott tartalmú dokumentumot állít elő és menti azt a megadott útvonalon. */
        Document doc = new Document(pagesize);    // A4-es lap
        doc.setMargins(25, 25, 25, 30);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(docpath));
            doc.open();
            Font times_plain = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.NORMAL);
            Font times_bold = FontFactory.getFont("Times New Roman Bold", BaseFont.CP1250, 12, Font.BOLD);
            Font footerfont = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 9, Font.NORMAL);
            Font tablafont = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 10, Font.NORMAL);
            if (tipus == 1) {
                HeaderFooter footer = new HeaderFooter(new Phrase(kat + " kategória   " + datum + "  -  ", footerfont), new Phrase(".oldal", footerfont));
                footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
                footer.setBorder(HeaderFooter.NO_BORDER);
                doc.setFooter(footer);
            } else {
                HeaderFooter footer = new HeaderFooter(new Phrase(datum + "  -  ", footerfont), new Phrase(".oldal", footerfont));
                footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
                footer.setBorder(HeaderFooter.NO_BORDER);
                doc.setFooter(footer);
            }
            switch (tipus) {
                case 1:      //étkeztetés igbv napló (<2010 vagy =2011 vagy =2012)



                    parheader = new Paragraph(null, times_bold);
                    for (int i = 0; i < header.length; i++) {
                        parheader.add(header[i] + "\n");
                        parheader.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    doc.add(parheader);
                    partitle = new Paragraph(null, times_bold);
                    for (int i = 0; i < title.length; i++) {
                        partitle.add(title[i] + "\n");
                        partitle.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    if (Integer.parseInt(getEv()) < 2010) {
                        partitle.add(kat + " kategória");
                    }
                    if (Integer.parseInt(getEv()) == 2011 ||Integer.parseInt(getEv()) == 2012) {
                        partitle.add(kat);
                    }
                    doc.add(partitle);
                    if (tipus == 1) {
                        Paragraph datumsor = new Paragraph("\n\nDátum:  " + datum + "\n", times_plain);
                        datumsor.setAlignment(Paragraph.ALIGN_LEFT);
                        doc.add(datumsor);
                    }
                    tablazat = new PdfPTable(Globals.ETK_NAPLO_TABLA_OSZLOPMERET_1);
                    tablazat.setHeaderRows(1);
                    tablazat.setWidthPercentage(100);
                    tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    tablazat.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                    tablazat.setSpacingBefore(15);

                    for (int i = 0; i < 33; i++) {      //Header előállítása
                        cell = new PdfPCell(new Phrase(Globals.ETKEZES_IGBV_TABLEHEADER[i], times_bold));
                        if (i == 0) {
                            tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                        }
                        if (i > 0) {
                            tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        }
                        tablazat.addCell(cell);
                    }
                    for (int i = 0; i < letszam; i++) { //táblázat feltöltése
                        for (int j = 0; j < 33; j++) {
                            cell = new PdfPCell(new Phrase(tabla[j][i], tablafont));
                            if (j == 0) {
                                tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                            }
                            if (j > 0) {
                                tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            }

                            tablazat.addCell(cell);
//                            tablazat.addCell(new Phrase(tabla[j][i], times_plain));
                        }
                    }
                    tablazat.addCell(new Phrase("Összesen:", tablafont));
                    for (int i = 0; i < 31; i++) {
                        cell = new PdfPCell(new Phrase(osszesen[i].toString(), tablafont));
                        cell.setRotation(90);
                        tablazat.addCell(cell);
                    }
                    cell = new PdfPCell(new Phrase("", tablafont));
                    tablazat.addCell(cell);
                    doc.add(tablazat);




                    break;
                case 2:     //melegedő eseménynapló



                    parheader = new Paragraph(null, times_bold);
                    for (int i = 0; i < header.length; i++) {
                        parheader.add(header[i] + "\n");
                        parheader.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    doc.add(parheader);
                    partitle = new Paragraph(null, times_bold);
                    for (int i = 0; i < title.length; i++) {
                        partitle.add(title[i] + "\n");
                        partitle.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    doc.add(partitle);

                    tablazat = new PdfPTable(Globals.MELEGEDO_NAPLO_TABLA_OSZLOPMERET);
                    tablazat.setWidthPercentage(100);
                    tablazat.setHeaderRows(2);
                    tablazat.setSpacingBefore(15);
                    cell = new PdfPCell(new Phrase(Globals.MELEGEDO_ESEMENY_TABLEHEADER1[0], footerfont));
                    cell.setColspan(4);
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    tablazat.addCell(cell);
                    cell = new PdfPCell(new Phrase(Globals.MELEGEDO_ESEMENY_TABLEHEADER1[1], footerfont));
                    cell.setColspan(14);
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    tablazat.addCell(cell);
                    for (int i = 0; i < 18; i++) {
                        cell = new PdfPCell(new Phrase(Globals.MELEGEDO_ESEMENY_TABLEHEADER2[i], footerfont));
                        if (i > 3 && i < 17) {
                            tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                            cell.setRotation(90);
                            tablazat.addCell(cell);
                        } else {
                            tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setRotation(0);
                            tablazat.addCell(cell);
                        }
                    }
                    for (int i = 0; i < letszam; i++) {
                        for (int j = 0; j < 18; j++) {

                            cell = new PdfPCell(new Phrase(tabla[j][i], footerfont));
                            if (j < 3) {
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                            }
                            if (j > 3) {
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            }

                            tablazat.addCell(cell);
                        }
                    }

                    doc.add(tablazat);

                    break;
                case 3:     //otthonközeli igbv napló  (=2010)
                    parheader = new Paragraph(null, times_bold);
                    for (int i = 0; i < header.length; i++) {
                        parheader.add(header[i] + "\n");
                        parheader.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    doc.add(parheader);
                    partitle = new Paragraph(null, times_bold);
                    for (int i = 0; i < title.length; i++) {
                        partitle.add(title[i] + "\n");
                        partitle.setAlignment(Paragraph.ALIGN_CENTER);
                    }
                    doc.add(partitle);

                    tablazat = new PdfPTable(Globals.ETK_NAPLO_TABLA_OSZLOPMERET);
                    tablazat.setHeaderRows(1);
                    tablazat.setWidthPercentage(100);
                    tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    tablazat.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                    tablazat.setSpacingBefore(15);


                    cell = new PdfPCell(new Phrase("Ellátott neve"));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    tablazat.addCell(cell);
                    PdfPTable nested = new PdfPTable(31);
                    cell = new PdfPCell(new Phrase(getEv() + ". " + getHonap()));
                    cell.setColspan(31);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    nested.addCell(cell);
                    cell = new PdfPCell(new Phrase("Ellátási napok dátuma"));
                    cell.setColspan(31);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    nested.addCell(cell);
                    for (int i = 1; i < 32; i++) {
                        cell = new PdfPCell(new Phrase(String.valueOf(i)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        nested.addCell(cell);
                    }
                    cell = new PdfPCell(nested);
                    cell.setColspan(31);
                    tablazat.addCell(cell);
                    nested = new PdfPTable(7);
                    cell = new PdfPCell(new Phrase("Havonta összesen"));
                    cell.setColspan(7);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    nested.addCell(cell);
                    for (int i = 1; i < 8; i++) {
                        cell = new PdfPCell(new Phrase(String.valueOf(i)));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        nested.addCell(cell);
                    }
                    cell = new PdfPCell(nested);
                    cell.setColspan(7);
                    tablazat.addCell(cell);


                    /*for (int i = 0; i < 32; i++) {      //Header előállítása
                    cell = new PdfPCell(new Phrase(Globals.ETKEZES_IGBV_TABLEHEADER[i], times_bold));
                    if (i == 0) {
                    tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                    }
                    if (i > 0) {
                    tablazat.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    }
                    tablazat.addCell(cell);
                    }*/
                    for (int i = 0; i < letszam; i++) { //táblázat feltöltése
                        for (int j = 0; j < 39; j++) {
                            cell = new PdfPCell(new Phrase(tabla[j][i], tablafont));
                            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            if (j == 0) {
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            }
                            if (j > 0) {
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            }

                            tablazat.addCell(cell);
//                            tablazat.addCell(new Phrase(tabla[j][i], times_plain));
                        }
                    }
                    nested = new PdfPTable(2);
                    cell = new PdfPCell(new Phrase("Ellátási napok összesített igénybevétele kód szerint", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    nested.addCell(cell);
                    PdfPTable nested2 = new PdfPTable(1);
                    cell = new PdfPCell(new Phrase("1:É+HS", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("2:É+INE", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("3:É", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("4:HS", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("5:INE", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("6:É+INE+SZF", footerfont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(new Phrase("7:INE+SZF", tablafont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(15f);
                    nested2.addCell(cell);
                    cell = new PdfPCell(nested2);
                    nested.addCell(cell);
                    cell = new PdfPCell(nested);
                    tablazat.addCell(cell);
                    nested = new PdfPTable(31);
                    for (int i = 0; i < 7; i++) {
                        if (i == 2) {
                            for (int j = 0; j < 31; j++) {
                                cell = new PdfPCell(new Phrase(osszesen[j].toString(), footerfont));
                                cell.setMinimumHeight(15f);
                                nested.addCell(cell);
                            }
                        } else {
                            for (int j = 0; j < 31; j++) {
                                cell = new PdfPCell(new Phrase(""));
                                cell.setMinimumHeight(15f);
                                nested.addCell(cell);
                            }
                        }
                    }
                    cell = new PdfPCell(nested);
                    cell.setColspan(31);
                    tablazat.addCell(cell);
                    cell = new PdfPCell(new Phrase(""));
                    cell.setColspan(7);
                    tablazat.addCell(cell);

                    doc.add(tablazat);




                    break;

            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        doc.close();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfEv = new javax.swing.JTextField();
        cbHonap = new javax.swing.JComboBox();
        tfNap = new javax.swing.JTextField();

        lblTitle.setBackground(new java.awt.Color(153, 153, 153));
        lblTitle.setFont(new java.awt.Font("Tahoma", 1, 14));
        lblTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle.setText("Napló");
        lblTitle.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        lblTitle.setOpaque(true);

        jLabel2.setText("Időszak:");

        tfEv.setText("2010");

        cbHonap.setModel(new javax.swing.DefaultComboBoxModel(Globals.HONAPOK));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfEv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfNap, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfEv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbHonap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfNap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHonap;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextField tfEv;
    private javax.swing.JTextField tfNap;
    // End of variables declaration//GEN-END:variables
}
