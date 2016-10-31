/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onyx;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.tools.Executable;
import java.awt.Color;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Az adatlapból a jogszabályban meghatározott tartalmú
 * Nyilvántartási lapot hoz létre
 * @author PIRI
 */
public class AdatlapExport {

    private File docpath;
    private String[] header = {"\"HÍRÖS DIÁK\" ALAPÍTVÁNY ÉTKEZTETÉSI SZOLGÁLAT", "KECSKEMÉT, CSABAY G. KRT. 1.", "TEL:76-504-149/200\n"};
    private String[] title = {"\nNyilvántartás\n", "a személyes gondoskodást nyújtó szociális ellátásra várakozókról,", "illetve az ellátást igénybevevőkről\n"};
    private String[] items = {"Nyilvántartási szám: ", "Név: ", "Születési név: ", "Lakóhely: ", "Tartózkodási hely: ", "Állampolgárság (státusz): ", "Születési hely, idő: ", "Törvényes képviselő neve, címe: ", "Más hozzátartozó neve, címe:", "Cselekvőképesség mértéke: ", "Személyi igazolvány száma: ", "TAJ-száma: ", "Kérelem beadásának dátuma: ", "Soron kívüli elhelyezés iránti igény elbírálásának időpontja: ", "Értesítés időpontja: ", "Egyéb, az igénybevétellel kapcsolatos megjegyzés: ", "A jogviszony megszűnésének módja, időpontja: ", "Mélt. Kat.: ", "Fizetendő díj: "};

    public AdatlapExport(String filenev, String[] adatok) {
        Uzenet print = new Uzenet("Akarja a nyilvántartó lapot nyomtatni is?", "Nyomtatás", Uzenet.QUESTION);
        FontFactory.register("c:\\windows\\fonts\\times.ttf");
        FontFactory.register("c:\\windows\\fonts\\timesbd.ttf");
        File file = new File(filenev);
        JFileChooser chooser = new JFileChooser(Config.settings.getProperty("savepath") + "\\Adatlapok");
        FileNameExtensionFilter pdffilter = new FileNameExtensionFilter("Pdf dokumentumok", "pdf");
        chooser.setFileFilter(pdffilter);
        chooser.setSelectedFile(file);
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            docpath = chooser.getSelectedFile();
            if (docpath.exists()) {
                Uzenet fileexists = new Uzenet("<html>A file már létezik!<br>Kívánja felülírni?", "Mentés", Uzenet.QUESTION);
                if (fileexists.getValasz() == Uzenet.YES) {
                    createDocument2(adatok);
                }

            } else {
                createDocument2(adatok);
            }
            if (print.getValasz() == Uzenet.YES) {
                printDocument(docpath);
            }

        }


    }

    private void createDocument(String[] adatok) {
        Document doc = new Document(PageSize.A4, 72, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(docpath));
            doc.open();
            Font times_plain = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.NORMAL);
            Font times_bold = FontFactory.getFont("Times New Roman Bold", BaseFont.CP1250, 12, Font.BOLD);
            Paragraph parheader = new Paragraph(null, times_bold);
            for (int i = 0; i < header.length; i++) {
                parheader.add(header[i] + "\n");
                parheader.setAlignment(Paragraph.ALIGN_CENTER);
            }
            doc.add(parheader);
            Paragraph partitle = new Paragraph(null, times_bold);
            for (int i = 0; i < title.length; i++) {
                partitle.add(title[i] + "\n");
                partitle.setAlignment(Paragraph.ALIGN_CENTER);
            }
            doc.add(partitle);
            Chunk tag1, tag2, tag3;
            Paragraph body = new Paragraph();
            for (int i = 0; i < items.length; i++) {
                tag1 = new Chunk(String.valueOf(i + 1) + ". ", times_plain);
                tag2 = new Chunk(items[i], times_plain);
                tag3 = new Chunk(adatok[i] + "\n", times_bold);
                body.add(tag1);
                body.add(tag2);
                body.add(tag3);
                body.setAlignment(Paragraph.ALIGN_LEFT);
                body.setLeading((float) 5, (float) 1.5);
            }
            doc.add(body);

        } catch (DocumentException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        doc.close();
    }

    private void printDocument(File filenev) {
        try {
            Process proc = Executable.printDocument(filenev, true);
            proc.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDocument2(String[] adatok) {
        Document doc = new Document(PageSize.A4, 20, 20, 20, 25);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(docpath));
            doc.open();
            Font times_plain = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.NORMAL);
            Font times_bold = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.BOLD);
            Font times_plain_white = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.NORMAL, new Color(255, 255, 255));
            Font times_bold_white = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 12, Font.BOLD, new Color(255, 255, 255));
            Font times_small = FontFactory.getFont("Times New Roman", BaseFont.CP1250, 8, Font.BOLD);
            Image headerimage = Image.getInstance("Silencio_header.jpg");
            headerimage.setAlignment(Image.ALIGN_CENTER);
            headerimage.scalePercent(24f);
            doc.add(headerimage);
            Paragraph parheader = new Paragraph("Nyilvántartás", times_bold);
            parheader.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(parheader);
            parheader = new Paragraph();
            Phrase ph1 = new Phrase("Szociális Étkezéshez", times_bold);

            parheader.add(ph1);
            parheader.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(parheader);
            float[] colw = {50, 50};
            PdfPTable table = new PdfPTable(colw);
            table.setSpacingBefore(8f);
            table.setWidthPercentage(100f);
            PdfPCell cell;
            Phrase ph = new Phrase("I. Jogosult Adatai", times_bold_white);
            cell = new PdfPCell(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("nyilvántartási sz.: " + adatok[0], times_plain_white);
            cell = new PdfPCell(ph);
//            cell.setFixedHeight(18f);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("Név:   " + adatok[1] + "\nSzül. hely, idő:   " + adatok[6]
                    + "\nTAJ szám  :" + adatok[11] + "    Szig.sz.:  " + adatok[10]
                    + "\nLakóhely:  " + adatok[3] + "\nÁllampolgárság,  bevándorolt,"
                    + "  menekült v.  letelepedett ", times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("Születési név:   " + adatok[2] + "\nAnyja neve:   " + adatok[19]
                    + "\nNyugdíjas törzsszám:  " + adatok[20] + "\nTartózkodási hely:  "
                    + adatok[4] + "\njogállása:  " + adatok[5], times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("II. Jogosult tartására köteles személy, törvényes képviselő adatai", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            String adat = adatok[21].trim().isEmpty() ? "............."
                    + "..........................................................." : adatok[21];
            ph = new Phrase("Név:   " + adat, times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            adat = adatok[7].trim().isEmpty() ? "............."
                    + "..................................................." : adatok[7];
            ph = new Phrase("Lakcím:  " + adat, times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("Szül. hely, idő:   ............."
                    + "..........................................", times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("Telefonszám:  ............."
                    + "..........................................", times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("III. A jogosultsági feltételek és változások adatai", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("A személyes gondoskodást nyújtó szociális intézményi jogviszony keletkezését a következők alapozhatják meg:\n" +
                    "   1. A lakóhely szerinti illetékes települési önkormányzat képviselő testületének határozata\n" +
                    "   2. A bíróság ideiglenes intézkedést tartalmazó végzés\n" +
                    "   3. A bírói ítélet\n" +
                    "   4. Az intézményvezető intézkedése\n" +
                    "   5. Az 1993. évi III. törvény 94/D §szerinti esetben a megállapodás.\n" +
                    "                                           Igen/ Nem       ( A megfelelő rész aláhúzandó )\n" +
                    "\n" +
                    "Tényleges felvétel, a vonatkozó dokumentum száma, egyéb okmányok: nyugdíjas igazolvány, törzslap, számlakivonat", times_plain);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("IV. A szociális ellátás megállapítása, változása, megszüntetése", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            adat=adatok[16].trim().equals("határozatlan")?"...................................":adatok[16];
            ph = new Phrase("Kérelem beadásának dátuma:  "+adatok[12]+"\nSzociális ellátás" +
                    " megállapításának(értesítés) ideje:  "+adatok[14]+"     Térítési díj összege:"
                    +adatok[18]+"\nVáltozások oka:..........................................................." +
                    ".................   Térítési díj összege:.............Ft/adag\nSzociális ellátás megszüntetésének ideje:  "+
                    adat+"   Megszűnés oka:.................................................", times_plain);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("V. A jogosultság és a térítési díj megállapításához szükséges jövedelmi adatok", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            adat=adatok[22].trim().isEmpty()?".........................................":adatok[22];
            ph = new Phrase("Összes (nettó) havi jövedelem:  "+adat, times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("Egy főre eső családi jövedelem:  ......................", times_plain);
            cell = new PdfPCell(ph);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("VI. Az 1993. évi III. törvény 3§ bekezdés szerinti személy adatai:", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("A Magyarországon tartózkodás jogcíme:  ......................." +
                    "................................................\nHozzátartozó esetén a rokoni kapcsolat:" +
                    ".......................................................................", times_plain);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("VII. Az ellátás igénybe vételének és megszűnésének időpontja:", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            adat=adatok[23].trim().isEmpty()?".........................................":adatok[23];
            ph = new Phrase("Az ellátás igénybe vételének időpontja ( első napja) :  " +adat+
                    "\nA jogviszony megszűnésének módja, időpontja:   " +adatok[16], times_plain);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("VIII. A térítési díj fizetési kötelezettség teljesítésére, annak elmaradására és követelés behajtására, valamint elévülésére vonatkozó adatok", times_bold_white);
            cell.addElement(ph);
//            cell.setFixedHeight(18f);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(2);
            cell.setBackgroundColor(Color.gray);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            ph = new Phrase("\n", times_small);
            cell = new PdfPCell(ph);
            cell.setColspan(2);
            cell.setNoWrap(false);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
//            ph = new Phrase("IX. Egyéb feljegyzések", times_bold_white);
//            cell.addElement(ph);
////            cell.setFixedHeight(18f);
//            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
//            cell.setColspan(2);
//            cell.setBackgroundColor(Color.gray);
//            cell.setBorder(Rectangle.NO_BORDER);
//            table.addCell(cell);
            doc.add(table);


            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

