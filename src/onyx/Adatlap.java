/*
 * Adatlap.java
 * Version 1.0
 * Created on 2009. február 12., 10:22
 *
 * Version 1.1
 * Updated on 2009. május 8.
 * Hozzáadva:
 *              Nyugdíjas törzsszám (azonosító tab)
 *              Felvétel dátuma (Melegedő)
 *              Étkeztetés beviteli mezői átcsoportosítva
 *              Version változó
 *
 * Javítva:     Városból irányítószám hiba
 *
 * Version 1.2.0
 * Updated on 2009. május 15.
 * Hozzáadva:   Éjjeli menedék (inaktív checkbox)
 *              Szolgáltatás fül (melyik szolgáltatásban vesz részt, és a hozzátartozó adatok)
 *              Lakcím nélküli gomb (beállítható szöveggel)
 *              Szálló címe gomb (settings-ben beállítható tartalom)
 *
 * Javítva:     TAJ ellenőrzés hiba
 *              Dátum formátum eltérés
 * Version 1.2.1 2009.05.17.
 * Hozzáadva:   Utónévből nem beállítása automatikusan és adatbázisba mentése, ha még nincs benne
 *
 * Version 1.2.2 2009.05.31.
 * Hozzáadva:   Szállítási cím tipusa
 *
 * Version 1.2.3 2009.06.07.
 * Hozzáadva:   Átmeneti szálló szolgáltatás
 *              új mentési metódus
 *
 * Version 1.2.4 2009.06.13.
 * Módosítva:   SQL műveletek közvetlen végrehajtása
 */
package onyx;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.*;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author  PIRI
 */
public class Adatlap extends javax.swing.JPanel implements DocumentListener, ItemListener, ActionListener, FocusListener {

    /** Creates new form Adatlap */
    public Adatlap(int id, boolean tipus) {
        param = new String[1];
        this.id = id;
        this.tipus = tipus;
        this.addComponentListener(Main.foablak);
        initComponents();
        lblID.setVisible(Main.DEBUG);
        lblID.setText(this.id.toString());
        tfIktsz.setVisible(!tipus);
        for(int i=0;i<tpAdatlap.getTabCount();i++){
            if(tpAdatlap.getTitleAt(i).equals("Beosztás")){
                tpAdatlap.setEnabledAt(i, tipus);
            }
        }        
//személyes adatok lekérdezése


        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT nev, iktsz,felvetel," +
                    " szul_nev, szul_hely, szul_ido, anya_nev, alpolgsag, neme, tel_otthon," +
                    " tel_munka, tel_mobil, email, tk_nev, h_nev, adoszam, taj, szem_szam," +
                    " szig_szam, beadas, ertesites, megszunes, cselekvokepesseg, megjegyzes," +
                    " etkezes, melegedo, melegedo_felvetel, nyugdij_tszam, jovedelem, dij," +
                    " kiszolgalas, ejjeli, szallitasi_cim, atmeneti, atmeneti_felvetel, mark," +
                    " jegyzoi_ig, ellatas, atmeneti_lezaras FROM ugyfel WHERE ID=?");
            stmt.setInt(1, this.id);
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (adatlap betöltés)", "Hiba", Uzenet.ERROR);
        }






        try {
            if (res.next()) {
                tfNev.setText(res.getString("nev"));
                lblNev_cimsor.setText(res.getString("nev"));
                tfIktsz.setText(res.getString("iktsz"));
                lblRogzDatum.setText(res.getDate("felvetel") == null ? "" : res.getDate("felvetel").toString());
                tfSzulNev.setText(res.getString("szul_nev"));
                tfSzulHely.setText(res.getString("szul_hely"));
                ftfSzulDatum.setText(res.getDate("szul_ido") == null ? "" : getDatum(res.getDate("szul_ido").toString()));
                tfAnyaNev.setText(res.getString("anya_nev"));
                tfAllpolg.setText(res.getString("alpolgsag"));
                if (res.getString("neme") != null) {
                    if (res.getString("neme").equals("f")) {
                        rbFerfi.setSelected(true);
                    }
                    if (res.getString("neme").equals("n")) {
                        rbNo.setSelected(true);
                    }
                } else {
                    rbFerfi.setSelected(false);
                    rbNo.setSelected(false);
                }
            }


//kontakt adatok 
            tfTelOtthon.setText(res.getString("tel_otthon"));
            tfTelMunka.setText(res.getString("tel_munka"));
            tfTelMobil.setText(res.getString("tel_mobil"));
            tfEmail.setText(res.getString("email"));
            tfTorvKepvNev.setText(res.getString("tk_nev"));
            tfHozztartNev.setText(res.getString("h_nev"));


//azonosító dokumentumok lekérdezése


            ftfAdoSzam.setText(res.getString("adoszam"));
            ftfTAJ.setText(res.getString("taj"));
            ftfSzemelyiSzam.setText(res.getString("szem_szam"));
            tfSzemIgSzam.setText(res.getString("szig_szam"));
            ftfNyugdTorzsSzam.setText(res.getString("nyugdij_tszam"));


//regisztrációs adatok lekérdezése


            ftfBeadasDatum.setText(getDatum(res.getDate("beadas") == null ? "" : res.getDate("beadas").toString()));
            ftfErtesitesDatum.setText(getDatum(res.getDate("ertesites") == null ? "" : res.getDate("ertesites").toString()));
            ftfMegszunesDatum.setText(getDatum(res.getDate("megszunes") == null ? "" : res.getDate("megszunes").toString()));
            ftfJegyzoiIgazolasDatum.setText(getDatum(res.getDate("jegyzoi_ig") == null ? "" : res.getDate("jegyzoi_ig").toString()));
            ftfEllatasDatum.setText(getDatum(res.getDate("ellatas") == null ? "" : res.getDate("ellatas").toString()));
            tfCselekvokepesseg.setText(res.getString("cselekvokepesseg"));
//megjegyzés lekérdezése

            taMegjegyzes.setText(res.getString("megjegyzes"));
            chbMark.setSelected(res.getBoolean("mark"));

            chbEtkezes.setSelected(res.getBoolean("etkezes"));
            chbMelegedo.setSelected(res.getBoolean("melegedo"));
            chbEjjeliMenedek.setSelected(res.getBoolean("ejjeli"));
            chbAtmenetiSzallo.setSelected(res.getBoolean("atmeneti"));

            ftfMelegedoFelvetelDatum.setText(getDatum(res.getDate("melegedo_felvetel") == null ? "" : res.getDate("melegedo_felvetel").toString()));

            tfJovedelem.setText(String.valueOf(res.getDouble("jovedelem")));
            tfEtkDij.setText(String.valueOf(res.getDouble("dij")));
            String tip = res.getString("kiszolgalas");
            rbEtkTipSzallitas.removeItemListener(this);
            rbEtkTipElvitel.setSelected(false);
            rbEtkTipHelyben.setSelected(false);
            rbEtkTipSzallitas.setSelected(false);
            if (tip != null) {
                if (tip.equals("e")) {
                    rbEtkTipElvitel.setSelected(true);
                }
                if (tip.equals("s")) {
                    rbEtkTipSzallitas.setSelected(true);
                    lblSzallCim.setVisible(true);
                    cbSzallitasiCim.setVisible(true);
                }
                if (tip.equals("h")) {
                    rbEtkTipHelyben.setSelected(true);
                }
            }
            rbEtkTipSzallitas.addItemListener(this);
            lblKategoria.setText(getKategoria());


            if (res.getString("szallitasi_cim") != null) {
                if (res.getString("szallitasi_cim").equals("i")) {
                    cbSzallitasiCim.setSelectedIndex(1);
                }
            } else {
                cbSzallitasiCim.setSelectedIndex(0);
            }
            ftfAtmenetiFelvetelDatum.setText(getDatum(res.getDate("atmeneti_felvetel") == null ? "" : res.getDate("atmeneti_felvetel").toString()));
            ftfAtmenetiLezarasDatum.setText(getDatum(res.getDate("atmeneti_lezaras") == null ? "" : res.getDate("atmeneti_lezaras").toString()));
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


//címadatok lekérdezése

        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT irsz, varos, varosresz, utca, kozterulet, hazszam, jelleg FROM cim WHERE ID=?");
            stmt.setInt(1, this.id);
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (cím betöltés)", "Hiba", Uzenet.ERROR);
        }

        int a = 0;
        try {
            if (res != null) {
                while (res.next()) {
                    String jelleg = res.getString("jelleg");
                    String[] tempstring = new String[1];
                    if (jelleg.equals("a")) {
                        a = 1;
                    }
                    if (jelleg.equals("i")) {
                        a = 2;
                    }
                    if (jelleg.equals("t")) {
                        a = 3;
                    }
                    if (jelleg.equals("h")) {
                        a = 4;
                    }
                    switch (a) {
                        case 1:     //állandó cím
                            tfAllIrsz.setText(res.getString("irsz"));
                            tfAllVaros.setText(res.getString("varos"));
                            tfAllUtca.setText(res.getString("utca"));
                            tfAllHazSzam.setText(res.getString("hazszam"));
                            tempstring[0] = res.getString("varosresz");
                            cbAllVarosResz.setModel(new DefaultComboBoxModel(tempstring));
                            cbAllKozterulet.setSelectedItem(res.getString("kozterulet"));
                            chbAllandoLakcim.setSelected(true);
                            break;
                        case 2:     //ideiglenes cím
                            tfIdeiglIrsz.setText(res.getString("irsz"));
                            tfIdeiglVaros.setText(res.getString("varos"));
                            tfIdeiglUtca.setText(res.getString("utca"));
                            tfIdeiglHazSzam.setText(res.getString("hazszam"));
                            tempstring[0] = res.getString("varosresz");
                            cbIdeigVarosResz.setModel(new DefaultComboBoxModel(tempstring));
                            cbIdeiglKozterulet.setSelectedItem(res.getString("kozterulet"));
                            chbIdeiglenesLakcim.setSelected(true);
                            break;
                        case 3:     //törvényes képviselő címe
                            tfTorvKepvIrsz.setText(res.getString("irsz"));
                            tfTorvKepvVaros.setText(res.getString("varos"));
                            tfTorvKepvUtca.setText(res.getString("utca"));
                            tfTorvKepvHazSzam.setText(res.getString("hazszam"));
                            tempstring[0] = res.getString("varosresz");
                            cbTorvKepvVarosResz.setModel(new DefaultComboBoxModel(tempstring));
                            cbTorvKepvKozterulet.setSelectedItem(res.getString("kozterulet"));
                            chbTorvKepviselo.setSelected(true);
                            break;
                        case 4:     //hozzátartozó címe
                            tfHozztartIrsz.setText(res.getString("irsz"));
                            tfHozztartVaros.setText(res.getString("varos"));
                            tfHozztartUtca.setText(res.getString("utca"));
                            tfHozztartHazSzam.setText(res.getString("hazszam"));
                            tempstring[0] = res.getString("varosresz");
                            cbHozztartVarosResz.setModel(new DefaultComboBoxModel(tempstring));
                            cbHozztartKozterulet.setSelectedItem(res.getString("kozterulet"));
                            chbHozzatartozo.setSelected(true);
                            break;
                    }

                }
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//beosztási adatok lekérdezése
        if (tipus) {
            try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT beosztas, alk_minoseg, besorolas, br_ber, heti_ora, feor, mnyp_belepes, onyp_belepes, onyp_dij, munkaszerzodes, munkakori_leiras, cv, bizonyitvany, alkalmassag, reg_kartya_szam FROM beosztasi_adat WHERE ID=?");
                stmt.setInt(1, this.id);
                res = stmt.executeQuery();
            } catch (SQLException ex) {
                new Uzenet("Adatbázis hiba (beosztási adatok betöltése)", "Hiba", Uzenet.ERROR);
            }

            try {
                if (res.next()) {
                    tfBeosztas.setText(res.getString("beosztas"));
                    tfAlkMinoseg.setText(res.getString("alk_minoseg"));
                    tfBesorolas.setText(res.getString("besorolas"));
                    tfBruttoBer.setText(String.valueOf(res.getDouble("br_ber")));
                    tfHetiOraszam.setText(String.valueOf(res.getInt("heti_ora")));
                    tfFEOR.setText(res.getString("feor"));
                    ftfMnypDatum.setText(res.getDate("mnyp_belepes") == null ? "" : getDatum(res.getDate("mnyp_belepes").toString()));
                    ftfOnypDatum.setText(res.getDate("onyp_belepes") == null ? "" : getDatum(res.getDate("onyp_belepes").toString()));
                    tfOnypDij.setText(String.valueOf(res.getDouble("onyp_dij")));
                    chbMunkaszerzodes.setSelected(res.getBoolean("munkaszerzodes"));
                    chbMunkakori.setSelected(res.getBoolean("munkakori"));
                    chbCV.setSelected(res.getBoolean("cv"));
                    if (Integer.valueOf(res.getInt("bizonyitvany")) != null) {
                        if (res.getInt("bizonyitvany") > 0) {
                            chbBizonyitvany.setSelected(true);
                            tfBizDarab.setText(String.valueOf(res.getInt("bizonyitvany")));
                        }
                    }
                    ftfAlkalmassagDatum.setText(res.getDate("alkalmassag") == null ? "" : getDatum(res.getDate("alkalmassag").toString()));
                    tfRegKartya.setText(res.getString("reg_kartya_szam"));
                }
                lek.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        log.writeLog("Adatlap megtekintése: " + tfNev.getText());
        btnMentes.setEnabled(false);
        Globals.MENTVE = true;
    }

    public Adatlap(boolean uj, boolean tipus) {
        id = -1;
        this.tipus = tipus;
        this.addComponentListener(Main.foablak);
        initComponents();
        if (Main.DEBUG) {
            lblID.setText(id.toString());
        }
        tfIktsz.setVisible(!tipus);
        for(int i=0;i<tpAdatlap.getTabCount();i++){
            if(tpAdatlap.getTitleAt(i).equals("Beosztás")){
                tpAdatlap.setEnabledAt(i, tipus);
            }
        }
        if(tipus){
            tfIktsz.requestFocusInWindow();
        }else{
            tfNev.requestFocusInWindow();
        }
        
    }

    private boolean Mentes(boolean b) { //uj verzió
        try {
            //uj verzió

            uj = false;
            param = new String[35];
            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            //új mentése, id megszerzése
            if (id == -1) {
                stmt = (PreparedStatement) con.prepareStatement("INSERT INTO ugyfel (iktsz, nev, statusz, felvetel) VALUES (?,?,?,NOW())");
                uj = true;
                if (!checkNev()) {
                    return false; //üres iktsz
                }
                if (!tipus) {
                    if (!checkIktsz()) {
                        return false; //üres iktsz
                    }
                }
                stmt.setString(1, tfIktsz.getText());
                stmt.setString(2, tfNev.getText());
                stmt.setString(3, tipus ? "d" : "u");
                if (stmt.executeUpdate() == 0) {
                    return false;
                }
                stmt.close();
//az ID autoincrement mező,
//ezért először beszúrjuk az alapadatokat, majd lekérdezzük az ID-t
                stmt = (PreparedStatement) con.prepareStatement("SELECT ID, felvetel FROM ugyfel WHERE iktsz=? AND nev=? AND statusz=?");
                stmt.setString(1, tfIktsz.getText());
                stmt.setString(2, tfNev.getText());
                stmt.setString(3, tipus ? "d" : "u");
                res = stmt.executeQuery();
                try {
                    res.next();
                    id = res.getInt(1);
                    lblID.setText(id.toString());
                    lblRogzDatum.setText(res.getString(2).substring(0, 10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stmt.close();
            }
//innentől azonos a mentés az új és meglévő adatok számára

//nem mentése
            stmt = (PreparedStatement) con.prepareStatement("REPLACE INTO nem SET nev=?, nem=?");
            stmt.setString(1, tfNev.getText().substring(tfNev.getText().lastIndexOf(32) + 1));
            stmt.setString(2, rbFerfi.isSelected() ? "f" : "n");
//            if (stmt.executeUpdate() == 0) {
//                return false;
//            }
            stmt.executeUpdate();
            stmt.close();


            stmt = (PreparedStatement) con.prepareStatement("UPDATE ugyfel SET " +
                    "nev=?, iktsz=?, szul_nev=?, szul_hely=?, szul_ido=?, anya_nev=?," +
                    " alpolgsag=?, neme=?, tk_nev=?, h_nev=?, tel_otthon=?, tel_munka=?," +
                    " tel_mobil=?, email=?,  adoszam=?, taj=?, szem_szam=?, szig_szam=?," +
                    " nyugdij_tszam=?, cselekvokepesseg=?, megjegyzes=?," +
                    " etkezes=?, melegedo=?, ejjeli=?, atmeneti=?, jovedelem=?, " +
                    "dij=?, kiszolgalas=?, szallitasi_cim=?, beadas=?, ertesites=?," +
                    " megszunes=?, jegyzoi_ig=?, melegedo_felvetel=?, atmeneti_felvetel=?," +
                    " rogzitve= NOW(), rogz_id=?, mark=?, kategoria=?, ellatas=?, atmeneti_lezaras=? WHERE ID=?");


            stmt.setInt(1, id);
//név, iktsz
            stmt.setString(1, tfNev.getText());
            stmt.setString(2, tfIktsz.getText());
//születési adatok
            stmt.setString(3, tfSzulNev.getText());
            stmt.setString(4, tfSzulHely.getText());
            if (ftfSzulDatum.getText().isEmpty()) {
                stmt.setNull(5, java.sql.Types.DATE);
            } else {
                stmt.setDate(5, Date.valueOf(ftfSzulDatum.getText()));
            }
            stmt.setString(6, tfAnyaNev.getText());
            stmt.setString(7, tfAllpolg.getText());
            stmt.setString(8, rbFerfi.isSelected() ? "f" : "n");
//kontakt adatok
            stmt.setString(9, tfTorvKepvNev.getText());
            stmt.setString(10, tfHozztartNev.getText());
//tel és email adatok
            stmt.setString(11, tfTelOtthon.getText());
            stmt.setString(12, tfTelMunka.getText());
            stmt.setString(13, tfTelMobil.getText());
            stmt.setString(14, tfEmail.getText());
//azonosítók
            stmt.setString(15, ftfAdoSzam.getText().contains("_") ? "" : ftfAdoSzam.getText());
            stmt.setString(16, ftfTAJ.getText().contains("_") ? "" : ftfTAJ.getText());
            stmt.setString(17, ftfSzemelyiSzam.getText().contains("_") ? "" : ftfSzemelyiSzam.getText());
            stmt.setString(18, tfSzemIgSzam.getText());
            stmt.setString(19, ftfNyugdTorzsSzam.getText().contains("_") ? "" : ftfNyugdTorzsSzam.getText());

//egyéb adatok
            stmt.setString(20, tfCselekvokepesseg.getText());
            stmt.setString(21, taMegjegyzes.getText());


//szolgáltatás
            stmt.setBoolean(22, chbEtkezes.isSelected() ? igaz : hamis);
            stmt.setBoolean(23, chbMelegedo.isSelected() ? igaz : hamis);
            stmt.setBoolean(24, chbEjjeliMenedek.isSelected() ? igaz : hamis);
            stmt.setBoolean(25, chbAtmenetiSzallo.isSelected() ? igaz : hamis);
//étkezés
            stmt.setDouble(26, Double.valueOf(tfJovedelem.getText().isEmpty() ? "0" : tfJovedelem.getText()));
            stmt.setDouble(27, Double.valueOf(tfEtkDij.getText().isEmpty() ? "0" : tfEtkDij.getText()));
            stmt.setString(28, "");
            stmt.setString(29, "a");
            if (rbEtkTipElvitel.isSelected()) {
                stmt.setString(28, "e");
            }
            if (rbEtkTipSzallitas.isSelected()) {
                stmt.setString(28, "s");
                stmt.setString(29, cbSzallitasiCim.getSelectedItem().equals("Állandó") ? "a" : "i");
            }
            if (rbEtkTipHelyben.isSelected()) {
                stmt.setString(28, "h");
            }
            if (ftfBeadasDatum.getText().isEmpty()) {
                stmt.setNull(30, java.sql.Types.DATE);
            } else {
                stmt.setDate(30, Date.valueOf(ftfBeadasDatum.getText()));
            }
            if (ftfErtesitesDatum.getText().isEmpty()) {
                stmt.setNull(31, java.sql.Types.DATE);
            } else {
                stmt.setDate(31, Date.valueOf(ftfErtesitesDatum.getText()));
            }
            if (ftfMegszunesDatum.getText().isEmpty()) {
                stmt.setNull(32, java.sql.Types.DATE);
            } else {
                stmt.setDate(32, Date.valueOf(ftfMegszunesDatum.getText()));
            }
            if (ftfJegyzoiIgazolasDatum.getText().isEmpty()) {
                stmt.setNull(33, java.sql.Types.DATE);
            } else {
                stmt.setDate(33, Date.valueOf(ftfJegyzoiIgazolasDatum.getText()));
            }

//melegedő
            if (ftfMelegedoFelvetelDatum.getText().isEmpty()) {
                stmt.setNull(34, java.sql.Types.DATE);
            } else {
                stmt.setDate(34, Date.valueOf(ftfMelegedoFelvetelDatum.getText()));
            }

//átmeneti szálló
            if (ftfAtmenetiFelvetelDatum.getText().isEmpty()) {
                stmt.setNull(35, java.sql.Types.DATE);
            } else {
                stmt.setDate(35, Date.valueOf(ftfAtmenetiFelvetelDatum.getText()));
            }
            stmt.setInt(36, Globals.getUserID());
            stmt.setBoolean(37, chbMark.isSelected());
            stmt.setString(38, lblKategoria.getText());
            if (ftfEllatasDatum.getText().isEmpty()) {
                stmt.setNull(39, java.sql.Types.DATE);
            } else {
                stmt.setDate(39, Date.valueOf(ftfEllatasDatum.getText()));
            }
            if (ftfAtmenetiLezarasDatum.getText().isEmpty()) {
                stmt.setNull(40, java.sql.Types.DATE);
            } else {
                stmt.setDate(40, Date.valueOf(ftfAtmenetiLezarasDatum.getText()));
            }
            stmt.setInt(41, id);





            if (stmt.executeUpdate() == 0) {
                return false;
            }
            stmt.close();
//lakcím adatok mentése
            if (chbAllandoLakcim.isSelected()) {
                param[1] = tfAllIrsz.getText();
                param[2] = tfAllVaros.getText();
                if (cbAllVarosResz.isEnabled()) {
                    param[3] = cbAllVarosResz.getSelectedIndex() == -1 ? "" : cbAllVarosResz.getSelectedItem().toString();
                } else {
                    param[3] = "";
                }
                param[4] = tfAllUtca.getText();
                param[5] = cbAllKozterulet.getSelectedItem().toString();
                param[6] = tfAllHazSzam.getText();
                param[7] = "a";
                if (!isParamEmpty(6, param)) {
                    if (!saveCim(param)) {
                        return false;
                    }
                }
            }
            if (chbIdeiglenesLakcim.isSelected()) {
                param[1] = tfIdeiglIrsz.getText();
                param[2] = tfIdeiglVaros.getText();
                if (cbIdeigVarosResz.isEnabled()) {
                    param[3] = cbIdeigVarosResz.getSelectedItem().toString();
                } else {
                    param[3] = "";
                }
                param[4] = tfIdeiglUtca.getText();
                param[5] = cbIdeiglKozterulet.getSelectedItem().toString();
                param[6] = tfIdeiglHazSzam.getText();
                param[7] = "i";
                if (!isParamEmpty(6, param)) {
                    if (!saveCim(param)) {
                        return false;
                    }
                }
            }
            if (chbTorvKepviselo.isSelected()) {
                if (tfTorvKepvNev.getText().isEmpty() || tfTorvKepvNev.getText().equals(" ")) {
                    JOptionPane.showMessageDialog(this, "Adja meg a törv. képviselő nevét!", "Hiba!", JOptionPane.ERROR_MESSAGE);
                    return false;
                } else {
                    param[1] = tfTorvKepvIrsz.getText();
                    param[2] = tfTorvKepvVaros.getText();
                    if (cbTorvKepvVarosResz.isEnabled()) {
                        param[3] = cbTorvKepvVarosResz.getSelectedItem().toString();
                    } else {
                        param[3] = "";
                    }
                    param[4] = tfTorvKepvUtca.getText();
                    param[5] = cbTorvKepvKozterulet.getSelectedItem().toString();
                    param[6] = tfTorvKepvHazSzam.getText();
                    param[7] = "t";
                    if (!isParamEmpty(6, param)) {
                        if (!saveCim(param)) {
                            return false;
                        }
                    }
                }
            }
            if (chbHozzatartozo.isSelected()) {
                if (tfHozztartNev.getText().isEmpty() || tfHozztartNev.getText().equals(" ")) {
                    JOptionPane.showMessageDialog(this, "Adja meg a hozzátartozó nevét!", "Hiba!", JOptionPane.ERROR_MESSAGE);
                    return false;
                } else {
                    param[1] = tfHozztartIrsz.getText();
                    param[2] = tfHozztartVaros.getText();
                    if (cbHozztartVarosResz.isEnabled()) {
                        param[3] = cbHozztartVarosResz.getSelectedItem().toString();
                    } else {
                        param[3] = "";
                    }
                    param[4] = tfHozztartUtca.getText();
                    param[5] = cbHozztartKozterulet.getSelectedItem().toString();
                    param[6] = tfHozztartHazSzam.getText();
                    param[7] = "h";
                    if (!isParamEmpty(6, param)) {
                        if (!saveCim(param)) {
                            return false;
                        }
                    }
                }
            }
//ha dolgozó, akkor a beosztási adatok mentése
            if (tipus) {
                stmt = (PreparedStatement) con.prepareStatement("REPLACE INTO beosztasi_adat (ID, beosztas, alk_minoseg, besorolas, br_ber, heti_ora, feor, mnyp_belepes, onyp_belepes, onyp_dij, munkaszerzodes, munkakori_leiras, cv, bizonyitvany, alkalmassag, reg_kartya_szam) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                stmt.setInt(1, id);
                stmt.setString(2, tfBeosztas.getText());
                stmt.setString(3, tfAlkMinoseg.getText());
                stmt.setString(4, tfBesorolas.getText());
                stmt.setDouble(5, Double.valueOf(tfBruttoBer.getText().isEmpty() ? "0" : tfBruttoBer.getText()));
                stmt.setInt(6, Integer.valueOf(tfHetiOraszam.getText().isEmpty() ? "0" : tfHetiOraszam.getText()));
                stmt.setString(7, tfFEOR.getText());
                if (ftfMnypDatum.getText().isEmpty()) {
                    stmt.setNull(8, java.sql.Types.DATE);
                } else {
                    stmt.setDate(8, Date.valueOf(ftfMnypDatum.getText()));
                }
                if (ftfOnypDatum.getText().isEmpty()) {
                    stmt.setNull(9, java.sql.Types.DATE);
                } else {
                    stmt.setDate(9, Date.valueOf(ftfOnypDatum.getText()));
                }
                stmt.setDouble(10, Double.valueOf(tfOnypDij.getText()));
                stmt.setBoolean(11, chbMunkaszerzodes.isSelected());
                stmt.setBoolean(12, chbMunkakori.isSelected());
                stmt.setBoolean(13, chbCV.isSelected());
                if (chbBizonyitvany.isSelected()) {
                    stmt.setInt(14, Integer.valueOf(tfBizDarab.getText()));
                } else {
                    stmt.setInt(14, 0);
                }
                if (ftfAlkalmassagDatum.getText().isEmpty()) {
                    stmt.setNull(15, java.sql.Types.DATE);
                } else {
                    stmt.setDate(15, Date.valueOf(ftfAlkalmassagDatum.getText()));
                }
                stmt.setString(16, tfRegKartya.getText());
                if (stmt.executeUpdate() == 0) {
                    return false;
                }
                stmt.close();
            }
            return true;
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba", "Hiba", Uzenet.ERROR);
            ex.printStackTrace();
        }
        return true;
    }
//        return true;

    private boolean checkNev() {
        if (tfNev.getText().isEmpty() || tfNev.getText().equals(" ")) {
            new Uzenet("A név nem lehet üres!", "Hiba!", Uzenet.ERROR);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIktsz() {
        if (tfIktsz.getText().isEmpty() || tfIktsz.getText().equals(" ")) {
            new Uzenet("Az iktatószám nem lehet üres!", "Hiba!", Uzenet.ERROR);
            return false;
        } else {
            return true;
        }
    }

    private String[] irszToVaros(String irsz) {
//        param = new String[1];
//        param[0] = irsz;

        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT varos, vresz FROM helyseg WHERE irsz=?");
            stmt.setString(1, irsz);
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (adatlap betöltés)", "Hiba", Uzenet.ERROR);
        }

//        lek = new SQLMuvelet(4, param);
        eredmeny = new String[2];
//        res = lek.getLekerdezes();
        try {
            while (res.next()) {
                eredmeny[0] = res.getString(1);
                eredmeny[1] = res.getString(2);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        lek.close();
        return eredmeny;
    }

    private String[] varosToIrsz(String varos) {

//        param = new String[1];
//        param[0] = varos;

        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT irsz FROM helyseg WHERE varos=?");
            stmt.setString(1, varos);
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (adatlap betöltés)", "Hiba", Uzenet.ERROR);
        }

//        lek = new SQLMuvelet(5, param);
//        res = lek.getLekerdezes();
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
        lek.close();
        return eredmeny;
    }

    private String varosToIrsz(String varos, String vresz) {
//        param = new String[2];
//        param[0] = varos;
//        param[1] = vresz;
//        lek = new SQLMuvelet(6, param);
//        res = lek.getLekerdezes();

        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("SELECT irsz FROM helyseg WHERE varos=? AND vresz=?");
            stmt.setString(1, varos);
            stmt.setString(2, vresz);
            res = stmt.executeQuery();
        } catch (SQLException ex) {
            new Uzenet("Adatbázis hiba (adatlap betöltés)", "Hiba", Uzenet.ERROR);
        }

        String eredm = new String();
        try {
            res.next();
            eredm = res.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lek.close();
        return eredm;
    }

    private boolean saveCim(String[] p) {
        /** Cím mentése vagy frissítése, attól függően, hogy új 
         * vagy meglévő adatokat mentünk
         */
        if (uj) {
            try {
//                lek = new SQLMuvelet(11, p);
                stmt = (PreparedStatement) con.prepareStatement("INSERT INTO cim (ID, irsz, varos, varosresz, utca, kozterulet, hazszam, jelleg) VALUES (?,?,?,?,?,?,?,?)");
                stmt.setInt(1, id);
                stmt.setString(2, p[1]);
                stmt.setString(3, p[2]);
                stmt.setString(4, p[3]);
                stmt.setString(5, p[4]);
                stmt.setString(6, p[5]);
                stmt.setString(7, p[6]);
                stmt.setString(8, p[7]);
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException ex) {
                new Uzenet("Hiba a cím mentése közben!", "Hiba", Uzenet.ERROR);
            }

        } else {
//            lek = new SQLMuvelet(35, p);
//            res = lek.getLekerdezes();//leellenőrizzük, hogy van-e már ilyen bejegyzés
            try {
                //leellenőrizzük, hogy van-e már ilyen bejegyzés
                stmt = (PreparedStatement) con.prepareStatement("SELECT * FROM cim WHERE ID=? AND jelleg=?");
                stmt.setInt(1, id);
                stmt.setString(2, p[7]);
                res = stmt.executeQuery();
                if (res.next()) {
                    stmt.close();
//                    lek = new SQLMuvelet(14, p);//ha van, akkor update
                    stmt = (PreparedStatement) con.prepareStatement("UPDATE cim SET irsz=?, varos=?, varosresz=?, utca=?, kozterulet=?, hazszam=? WHERE ID=? AND jelleg=?");
                    stmt.setString(1, p[1]);
                    stmt.setString(2, p[2]);
                    stmt.setString(3, p[3]);
                    stmt.setString(4, p[4]);
                    stmt.setString(5, p[5]);
                    stmt.setString(6, p[6]);
                    stmt.setInt(7, id);
                    stmt.setString(8, p[7]);
                    if (stmt.executeUpdate() == 0) {
                        stmt.close();
                        return false;
                    }
                } else {
                    stmt.close();
//                    lek = new SQLMuvelet(11, p);//ha nincs, akkor beszúrjuk az új adatokat
                    stmt = (PreparedStatement) con.prepareStatement("INSERT INTO cim (ID, irsz, varos, varosresz, utca, kozterulet, hazszam, jelleg) VALUES (?,?,?,?,?,?,?,?)");
                    stmt.setInt(1, id);
                    stmt.setString(2, p[1]);
                    stmt.setString(3, p[2]);
                    stmt.setString(4, p[3]);
                    stmt.setString(5, p[4]);
                    stmt.setString(6, p[5]);
                    stmt.setString(7, p[6]);
                    stmt.setString(8, p[7]);
                    if (stmt.executeUpdate() == 0) {
                        stmt.close();
                        return false;
                    }

                }
            } catch (SQLException ex) {
                new Uzenet("Adatbázis hiba!(cím)", "Hiba", Uzenet.ERROR);
                ex.printStackTrace();
            }
        }
        return true;

    }

    private boolean isParamEmpty(int maxindex, String[] param) {
        int content = 0;
        for (int i = 1; i < maxindex + 1; i++) {
            if (!(param[i].isEmpty() || param[i].equals(Globals.URES_DATUM) || param[i].equals("0") || param[i].equals("magyar"))) {
                content++;
            }
        }
        return content > 0 ? hamis : igaz;

    }

    private String getDatum(String s) {
        if (s == null) {
            return "";
//        }
//        if (s.equals(Globals.URES_DATUM)) {
//            return "";
        } else {
            return s;
        }
    }

    private String getKategoria() {
        String kategoria = "";
        Double jovedelem = new Double(tfJovedelem.getText().isEmpty() ? "0" : tfJovedelem.getText());
        Double minnyugdij = new Double(Config.settings.getProperty("min_nyugdij"));
        double hatar1 = (new Double(Config.settings.getProperty("kat1")) / 100) * minnyugdij;
        double hatar2 = (new Double(Config.settings.getProperty("kat2")) / 100) * minnyugdij;
        if (jovedelem <= hatar1) {
            kategoria = "A";
        } else {
            if (jovedelem > hatar2) {
                kategoria = "C";
            } else {
                kategoria = "B";
            }
        }
        return kategoria;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngpNem = new javax.swing.ButtonGroup();
        btngpEtkTipus = new javax.swing.ButtonGroup();
        lblNev_cimsor = new javax.swing.JLabel();
        lblRogz = new javax.swing.JLabel();
        lblID = new javax.swing.JLabel();
        tpAdatlap = new javax.swing.JTabbedPane();
        pnlSzulAdat = new javax.swing.JPanel();
        lblNev = new javax.swing.JLabel();
        tfNev = new javax.swing.JTextField();
        lblSzulNev = new javax.swing.JLabel();
        lblSzulHely = new javax.swing.JLabel();
        lblSzulDatum = new javax.swing.JLabel();
        tfSzulNev = new javax.swing.JTextField();
        tfSzulHely = new javax.swing.JTextField();
        lblAnyjaNeve = new javax.swing.JLabel();
        tfAnyaNev = new javax.swing.JTextField();
        lblNeme = new javax.swing.JLabel();
        lblAllpolg = new javax.swing.JLabel();
        tfAllpolg = new javax.swing.JTextField();
        rbFerfi = new javax.swing.JRadioButton();
        rbNo = new javax.swing.JRadioButton();
        ftfSzulDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        pnlLakcim = new javax.swing.JPanel();
        chbAllandoLakcim = new javax.swing.JCheckBox();
        tfAllIrsz = new javax.swing.JTextField();
        tfAllVaros = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tfAllUtca = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        cbAllKozterulet = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        tfAllHazSzam = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        chbIdeiglenesLakcim = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        tfIdeiglIrsz = new javax.swing.JTextField();
        tfIdeiglVaros = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        tfIdeiglUtca = new javax.swing.JTextField();
        cbIdeiglKozterulet = new javax.swing.JComboBox();
        tfIdeiglHazSzam = new javax.swing.JTextField();
        cbAllVarosResz = new javax.swing.JComboBox();
        cbIdeigVarosResz = new javax.swing.JComboBox();
        btnSzallo = new javax.swing.JButton();
        btnLakcimNelkuli = new javax.swing.JButton();
        pnlErtesites = new javax.swing.JPanel();
        chbTorvKepviselo = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        tfTorvKepvIrsz = new javax.swing.JTextField();
        tfTorvKepvVaros = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        tfTorvKepvUtca = new javax.swing.JTextField();
        cbTorvKepvKozterulet = new javax.swing.JComboBox();
        tfTorvKepvHazSzam = new javax.swing.JTextField();
        chbHozzatartozo = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        tfHozztartIrsz = new javax.swing.JTextField();
        tfHozztartVaros = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        tfHozztartUtca = new javax.swing.JTextField();
        cbHozztartKozterulet = new javax.swing.JComboBox();
        tfHozztartHazSzam = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        tfTorvKepvNev = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        tfHozztartNev = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        tfTelOtthon = new javax.swing.JTextField();
        tfTelMobil = new javax.swing.JTextField();
        tfTelMunka = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        cbTorvKepvVarosResz = new javax.swing.JComboBox();
        cbHozztartVarosResz = new javax.swing.JComboBox();
        pnlAzonosito = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        tfSzemIgSzam = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        try{
            MaskFormatter fs=new MaskFormatter("# ###### ####");
            fs.setPlaceholderCharacter('_');
            ftfSzemelyiSzam = new javax.swing.JFormattedTextField(fs);
            try{
                MaskFormatter ft=new MaskFormatter("### ### ###");
                ft.setPlaceholderCharacter('_');
                ftfTAJ = new javax.swing.JFormattedTextField(ft);
                try{
                    MaskFormatter fa=new MaskFormatter("##########");
                    fa.setPlaceholderCharacter('_');
                    ftfAdoSzam = new javax.swing.JFormattedTextField(fa);
                    try{
                        MaskFormatter f=new MaskFormatter("###'-#####'-#");
                        f.setPlaceholderCharacter('_');
                        ftfNyugdTorzsSzam = new javax.swing.JFormattedTextField(f);
                        jLabel60 = new javax.swing.JLabel();
                        pnlBeosztas = new javax.swing.JPanel();
                        jLabel1 = new javax.swing.JLabel();
                        tfBeosztas = new javax.swing.JTextField();
                        jLabel2 = new javax.swing.JLabel();
                        tfAlkMinoseg = new javax.swing.JTextField();
                        jLabel3 = new javax.swing.JLabel();
                        tfBesorolas = new javax.swing.JTextField();
                        jLabel4 = new javax.swing.JLabel();
                        tfBruttoBer = new javax.swing.JTextField();
                        jLabel5 = new javax.swing.JLabel();
                        tfHetiOraszam = new javax.swing.JTextField();
                        jLabel6 = new javax.swing.JLabel();
                        tfFEOR = new javax.swing.JTextField();
                        jLabel7 = new javax.swing.JLabel();
                        ftfMnypDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel53 = new javax.swing.JLabel();
                        ftfOnypDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel54 = new javax.swing.JLabel();
                        tfOnypDij = new javax.swing.JTextField();
                        chbMunkaszerzodes = new javax.swing.JCheckBox();
                        chbMunkakori = new javax.swing.JCheckBox();
                        chbBizonyitvany = new javax.swing.JCheckBox();
                        tfBizDarab = new javax.swing.JTextField();
                        jLabel55 = new javax.swing.JLabel();
                        chbCV = new javax.swing.JCheckBox();
                        jLabel56 = new javax.swing.JLabel();
                        ftfAlkalmassagDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel57 = new javax.swing.JLabel();
                        tfRegKartya = new javax.swing.JTextField();
                        jPanel1 = new javax.swing.JPanel();
                        jLabel52 = new javax.swing.JLabel();
                        tfCselekvokepesseg = new javax.swing.JTextField();
                        jLabel51 = new javax.swing.JLabel();
                        jScrollPane1 = new javax.swing.JScrollPane();
                        taMegjegyzes = new javax.swing.JTextArea();
                        chbMark = new javax.swing.JCheckBox();
                        pnlEgyeb = new javax.swing.JPanel();
                        chbEtkezes = new javax.swing.JCheckBox();
                        jLabel43 = new javax.swing.JLabel();
                        tfEtkDij = new javax.swing.JTextField();
                        jLabel44 = new javax.swing.JLabel();
                        jLabel45 = new javax.swing.JLabel();
                        rbEtkTipElvitel = new javax.swing.JRadioButton();
                        rbEtkTipSzallitas = new javax.swing.JRadioButton();
                        rbEtkTipHelyben = new javax.swing.JRadioButton();
                        jLabel46 = new javax.swing.JLabel();
                        tfJovedelem = new javax.swing.JTextField();
                        jLabel47 = new javax.swing.JLabel();
                        jLabel48 = new javax.swing.JLabel();
                        jLabel49 = new javax.swing.JLabel();
                        jLabel50 = new javax.swing.JLabel();
                        jTextField49 = new javax.swing.JTextField();
                        jTextField50 = new javax.swing.JTextField();
                        jComboBox11 = new javax.swing.JComboBox();
                        jComboBox12 = new javax.swing.JComboBox();
                        ftfBeadasDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        ftfErtesitesDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        ftfMegszunesDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        chbMelegedo = new javax.swing.JCheckBox();
                        jLabel61 = new javax.swing.JLabel();
                        ftfMelegedoFelvetelDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel62 = new javax.swing.JLabel();
                        lblKategoria = new javax.swing.JLabel();
                        chbEjjeliMenedek = new javax.swing.JCheckBox();
                        cbSzallitasiCim = new javax.swing.JComboBox();
                        lblSzallCim = new javax.swing.JLabel();
                        chbAtmenetiSzallo = new javax.swing.JCheckBox();
                        jLabel63 = new javax.swing.JLabel();
                        ftfJegyzoiIgazolasDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel64 = new javax.swing.JLabel();
                        ftfAtmenetiFelvetelDatum = new javax.swing.JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
                        jLabel65 = new javax.swing.JLabel();
                        ftfAtmenetiLezarasDatum = new javax.swing.JFormattedTextField();
                        jLabel66 = new javax.swing.JLabel();
                        ftfEllatasDatum = new javax.swing.JFormattedTextField();
                        btnVissza = new javax.swing.JButton();
                        btnMentes = new javax.swing.JButton();
                        tfIktsz = new javax.swing.JTextField();
                        jLabel58 = new javax.swing.JLabel();
                        lblRogzDatum = new javax.swing.JLabel();
                        jLabel59 = new javax.swing.JLabel();
                        btnPrint = new javax.swing.JButton();

                        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

                        lblNev_cimsor.setFont(new java.awt.Font("Tahoma", 1, 14));
                        lblNev_cimsor.setText("Név");

                        lblRogz.setText("Rögzítve:");

                        lblID.setText("ID");
                        lblID.setVisible(Main.DEBUG);

                        lblNev.setText("Név:");

                        tfNev.getDocument().addDocumentListener(this);
                        tfNev.addFocusListener(this);

                        lblSzulNev.setText("Születési név:");

                        lblSzulHely.setText("Születési hely:");

                        lblSzulDatum.setText("Születési idő:");

                        tfSzulNev.getDocument().addDocumentListener(this);

                        tfSzulHely.getDocument().addDocumentListener(this);

                        lblAnyjaNeve.setText("Anyja neve:");

                        tfAnyaNev.getDocument().addDocumentListener(this);

                        lblNeme.setText("Neme:");

                        lblAllpolg.setText("Állampolgársága:");

                        tfAllpolg.setText("magyar");
                        tfAllpolg.getDocument().addDocumentListener(this);

                        rbFerfi.addItemListener(this);
                        btngpNem.add(rbFerfi);
                        rbFerfi.setText("férfi");

                        rbNo.addItemListener(this);
                        btngpNem.add(rbNo);
                        rbNo.setText("nő");

                        ftfSzulDatum.getDocument().addDocumentListener(this);
                        ftfSzulDatum.setToolTipText("ÉÉÉÉ-HH-NN");

                        javax.swing.GroupLayout pnlSzulAdatLayout = new javax.swing.GroupLayout(pnlSzulAdat);
                        pnlSzulAdat.setLayout(pnlSzulAdatLayout);
                        pnlSzulAdatLayout.setHorizontalGroup(
                            pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSzulAdatLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblAllpolg)
                                    .addComponent(lblAnyjaNeve)
                                    .addComponent(lblNeme)
                                    .addComponent(lblSzulDatum)
                                    .addComponent(lblSzulHely)
                                    .addComponent(lblSzulNev)
                                    .addComponent(lblNev))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbFerfi)
                                    .addComponent(rbNo)
                                    .addComponent(tfAllpolg, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(ftfSzulDatum, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tfAnyaNev, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                                        .addComponent(tfSzulHely, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tfSzulNev, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tfNev, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(105, Short.MAX_VALUE))
                        );
                        pnlSzulAdatLayout.setVerticalGroup(
                            pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSzulAdatLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblNev))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfSzulNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSzulNev))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfSzulHely, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSzulHely))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblSzulDatum)
                                    .addComponent(ftfSzulDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfAnyaNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblAnyjaNeve))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(rbFerfi)
                                    .addComponent(lblNeme))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbNo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSzulAdatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfAllpolg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblAllpolg))
                                .addContainerGap(254, Short.MAX_VALUE))
                        );

                        tfNev.getAccessibleContext().setAccessibleDescription("");
                        tfSzulNev.getAccessibleContext().setAccessibleDescription("");
                        tfSzulHely.getAccessibleContext().setAccessibleDescription("");
                        tfAnyaNev.getAccessibleContext().setAccessibleDescription("");
                        tfAllpolg.getAccessibleContext().setAccessibleDescription("");
                        rbFerfi.getAccessibleContext().setAccessibleDescription("");
                        ftfSzulDatum.getAccessibleContext().setAccessibleDescription("");

                        tpAdatlap.addTab("Születési", pnlSzulAdat);

                        chbAllandoLakcim.addItemListener(this);
                        chbAllandoLakcim.setText("Állandó lakcím");

                        tfAllIrsz.getDocument().addDocumentListener(this);
                        tfAllIrsz.addFocusListener(this);
                        tfAllIrsz.setEnabled(false);

                        tfAllVaros.getDocument().addDocumentListener(this);
                        tfAllVaros.addFocusListener(this);
                        tfAllVaros.setEnabled(false);

                        jLabel8.setText("Irsz.:");

                        jLabel9.setText("Város:");

                        jLabel10.setText("Városrész:");

                        tfAllUtca.getDocument().addDocumentListener(this);
                        tfAllUtca.setEnabled(false);

                        jLabel11.setText("Utca:");

                        cbAllKozterulet.setModel(new javax.swing.DefaultComboBoxModel(Globals.KOZTERULET));
                        cbAllKozterulet.setEnabled(false);

                        jLabel12.setText("Közterület:");

                        tfAllHazSzam.getDocument().addDocumentListener(this);
                        tfAllHazSzam.setEnabled(false);

                        jLabel13.setText("Házszám:");

                        chbIdeiglenesLakcim.addItemListener(this);
                        chbIdeiglenesLakcim.setText("Ideiglenes lakcím");

                        jLabel14.setText("Irsz.:");

                        jLabel15.setText("Város:");

                        jLabel16.setText("Városrész:");

                        tfIdeiglIrsz.getDocument().addDocumentListener(this);
                        tfIdeiglIrsz.addFocusListener(this);
                        tfIdeiglIrsz.setEnabled(false);

                        tfIdeiglVaros.getDocument().addDocumentListener(this);
                        tfIdeiglVaros.addFocusListener(this);
                        tfIdeiglVaros.setEnabled(false);

                        jLabel17.setText("Utca:");

                        jLabel18.setText("Közterület:");

                        jLabel19.setText("Házszám:");

                        tfIdeiglUtca.getDocument().addDocumentListener(this);
                        tfIdeiglUtca.setEnabled(false);

                        cbIdeiglKozterulet.setModel(new javax.swing.DefaultComboBoxModel(Globals.KOZTERULET));
                        cbIdeiglKozterulet.setEnabled(false);

                        tfIdeiglHazSzam.getDocument().addDocumentListener(this);
                        tfIdeiglHazSzam.setEnabled(false);

                        cbAllVarosResz.addItemListener(this);
                        cbAllVarosResz.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
                        cbAllVarosResz.setEnabled(false);

                        cbIdeigVarosResz.addItemListener(this);
                        cbIdeigVarosResz.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
                        cbIdeigVarosResz.setEnabled(false);

                        btnSzallo.addActionListener(this);
                        btnSzallo.setActionCommand("szallo");
                        btnSzallo.setText("Szálló");
                        btnSzallo.setEnabled(false);

                        btnLakcimNelkuli.addActionListener(this);
                        btnLakcimNelkuli.setActionCommand("lakcimnelkuli");
                        btnLakcimNelkuli.setText("Lakcím nélküli");
                        btnLakcimNelkuli.setEnabled(false);

                        javax.swing.GroupLayout pnlLakcimLayout = new javax.swing.GroupLayout(pnlLakcim);
                        pnlLakcim.setLayout(pnlLakcimLayout);
                        pnlLakcimLayout.setHorizontalGroup(
                            pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlLakcimLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addComponent(chbAllandoLakcim)
                                                .addGap(30, 30, 30)
                                                .addComponent(btnLakcimNelkuli))
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addComponent(jLabel8)
                                                .addGap(20, 20, 20)
                                                .addComponent(jLabel9)
                                                .addGap(82, 82, 82)
                                                .addComponent(jLabel10))))
                                    .addGroup(pnlLakcimLayout.createSequentialGroup()
                                        .addGap(27, 27, 27)
                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(tfAllUtca, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlLakcimLayout.createSequentialGroup()
                                                    .addComponent(tfAllIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(tfAllVaros, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jLabel11))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(cbAllKozterulet, 0, 97, Short.MAX_VALUE)
                                                    .addComponent(jLabel12))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(tfAllHazSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                                                    .addComponent(jLabel13)))
                                            .addComponent(cbAllVarosResz, 0, 162, Short.MAX_VALUE)))
                                    .addGroup(pnlLakcimLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addComponent(chbIdeiglenesLakcim)
                                                .addGap(18, 18, 18)
                                                .addComponent(btnSzallo, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addComponent(jLabel14)
                                                .addGap(20, 20, 20)
                                                .addComponent(jLabel15)
                                                .addGap(82, 82, 82)
                                                .addComponent(jLabel16))
                                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(tfIdeiglUtca, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlLakcimLayout.createSequentialGroup()
                                                            .addComponent(tfIdeiglIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(tfIdeiglVaros, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addComponent(jLabel17))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(pnlLakcimLayout.createSequentialGroup()
                                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(cbIdeiglKozterulet, 0, 97, Short.MAX_VALUE)
                                                            .addComponent(jLabel18))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(tfIdeiglHazSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                                            .addComponent(jLabel19)))
                                                    .addComponent(cbIdeigVarosResz, 0, 160, Short.MAX_VALUE))))))
                                .addGap(74, 74, 74))
                        );
                        pnlLakcimLayout.setVerticalGroup(
                            pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlLakcimLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(chbAllandoLakcim)
                                    .addComponent(btnLakcimNelkuli, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addGap(1, 1, 1)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfAllIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfAllVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbAllVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfAllUtca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbAllKozterulet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfAllHazSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(chbIdeiglenesLakcim)
                                    .addComponent(btnSzallo, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel16))
                                .addGap(1, 1, 1)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfIdeiglIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfIdeiglVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbIdeigVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel17))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlLakcimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfIdeiglUtca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbIdeiglKozterulet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfIdeiglHazSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(224, Short.MAX_VALUE))
                        );

                        chbAllandoLakcim.getAccessibleContext().setAccessibleDescription("");
                        tfAllIrsz.getAccessibleContext().setAccessibleDescription("");
                        tfAllVaros.getAccessibleContext().setAccessibleDescription("");
                        tfAllUtca.getAccessibleContext().setAccessibleDescription("");
                        cbAllKozterulet.getAccessibleContext().setAccessibleDescription("");
                        tfAllHazSzam.getAccessibleContext().setAccessibleDescription("");
                        chbIdeiglenesLakcim.getAccessibleContext().setAccessibleDescription("");
                        tfIdeiglIrsz.getAccessibleContext().setAccessibleDescription("");
                        tfIdeiglVaros.getAccessibleContext().setAccessibleDescription("");
                        tfIdeiglUtca.getAccessibleContext().setAccessibleDescription("");
                        cbIdeiglKozterulet.getAccessibleContext().setAccessibleDescription("");
                        tfIdeiglHazSzam.getAccessibleContext().setAccessibleDescription("");

                        tpAdatlap.addTab("Lakcím", pnlLakcim);

                        chbTorvKepviselo.addItemListener(this);
                        chbTorvKepviselo.setText("Törvényes képviselő");

                        jLabel20.setText("Irsz.:");

                        jLabel21.setText("Város:");

                        jLabel22.setText("Városrész:");

                        tfTorvKepvIrsz.getDocument().addDocumentListener(this);
                        tfTorvKepvIrsz.addFocusListener(this);
                        tfTorvKepvIrsz.setEnabled(false);

                        tfTorvKepvVaros.getDocument().addDocumentListener(this);
                        tfTorvKepvVaros.addFocusListener(this);
                        tfTorvKepvVaros.setEnabled(false);

                        jLabel23.setText("Utca:");

                        jLabel24.setText("Közterület:");

                        jLabel25.setText("Házszám:");

                        tfTorvKepvUtca.getDocument().addDocumentListener(this);
                        tfTorvKepvUtca.setEnabled(false);

                        cbTorvKepvKozterulet.setModel(new javax.swing.DefaultComboBoxModel(Globals.KOZTERULET));
                        cbTorvKepvKozterulet.setEnabled(false);

                        tfTorvKepvHazSzam.getDocument().addDocumentListener(this);
                        tfTorvKepvHazSzam.setEnabled(false);

                        chbHozzatartozo.addItemListener(this);
                        chbHozzatartozo.setText("Hozzátartozó");

                        jLabel26.setText("Irsz.:");

                        jLabel27.setText("Város:");

                        jLabel28.setText("Városrész:");

                        tfHozztartIrsz.getDocument().addDocumentListener(this);
                        tfHozztartIrsz.addFocusListener(this);
                        tfHozztartIrsz.setEnabled(false);

                        tfHozztartVaros.getDocument().addDocumentListener(this);
                        tfHozztartVaros.addFocusListener(this);
                        tfHozztartVaros.setEnabled(false);

                        jLabel29.setText("Utca:");

                        jLabel30.setText("Közterület:");

                        jLabel31.setText("Házszám:");

                        tfHozztartUtca.getDocument().addDocumentListener(this);
                        tfHozztartUtca.setEnabled(false);

                        cbHozztartKozterulet.setModel(new javax.swing.DefaultComboBoxModel(Globals.KOZTERULET));
                        cbHozztartKozterulet.setEnabled(false);

                        tfHozztartHazSzam.getDocument().addDocumentListener(this);
                        tfHozztartHazSzam.setEnabled(false);

                        jLabel32.setText("Neve:");

                        tfTorvKepvNev.getDocument().addDocumentListener(this);
                        tfTorvKepvNev.setEnabled(false);

                        jLabel33.setText("Neve:");

                        tfHozztartNev.getDocument().addDocumentListener(this);
                        tfHozztartNev.setEnabled(false);

                        jLabel34.setText("Otthoni:");

                        jLabel35.setText("Mobil:");

                        jLabel36.setText("Munkahelyi:");

                        jLabel37.setText("Telefon:");

                        tfTelOtthon.getDocument().addDocumentListener(this);

                        tfTelMobil.getDocument().addDocumentListener(this);

                        tfTelMunka.getDocument().addDocumentListener(this);

                        jLabel38.setText("E-mail:");

                        tfEmail.getDocument().addDocumentListener(this);

                        cbTorvKepvVarosResz.addItemListener(this);
                        cbTorvKepvVarosResz.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
                        cbTorvKepvVarosResz.setEnabled(false);

                        cbHozztartVarosResz.addItemListener(this);
                        cbHozztartVarosResz.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
                        cbHozztartVarosResz.setEnabled(false);

                        javax.swing.GroupLayout pnlErtesitesLayout = new javax.swing.GroupLayout(pnlErtesites);
                        pnlErtesites.setLayout(pnlErtesitesLayout);
                        pnlErtesitesLayout.setHorizontalGroup(
                            pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlErtesitesLayout.createSequentialGroup()
                                            .addContainerGap()
                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                    .addGap(21, 21, 21)
                                                    .addComponent(jLabel33)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(tfHozztartNev, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(chbTorvKepviselo)
                                                .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                    .addGap(21, 21, 21)
                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                            .addComponent(jLabel20)
                                                            .addGap(20, 20, 20)
                                                            .addComponent(jLabel21)
                                                            .addGap(82, 82, 82)
                                                            .addComponent(jLabel22))
                                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                            .addComponent(jLabel32)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(tfTorvKepvNev, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                    .addComponent(tfTorvKepvUtca, javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlErtesitesLayout.createSequentialGroup()
                                                                        .addComponent(tfTorvKepvIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(tfTorvKepvVaros, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(cbTorvKepvKozterulet, 0, 97, Short.MAX_VALUE)
                                                                        .addComponent(jLabel24))
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(tfTorvKepvHazSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                                                                        .addComponent(jLabel25)))
                                                                .addComponent(cbTorvKepvVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addGap(175, 175, 175))))
                                                .addComponent(chbHozzatartozo))
                                            .addGap(15, 15, 15))
                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlErtesitesLayout.createSequentialGroup()
                                                    .addGap(27, 27, 27)
                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                            .addComponent(jLabel26)
                                                            .addGap(20, 20, 20)
                                                            .addComponent(jLabel27)
                                                            .addGap(82, 82, 82)
                                                            .addComponent(jLabel28))
                                                        .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                    .addComponent(tfHozztartUtca, javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlErtesitesLayout.createSequentialGroup()
                                                                        .addComponent(tfHozztartIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(tfHozztartVaros, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                .addComponent(jLabel29))
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(cbHozztartKozterulet, 0, 97, Short.MAX_VALUE)
                                                                        .addComponent(jLabel30))
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(tfHozztartHazSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                                                                        .addComponent(jLabel31)))
                                                                .addComponent(cbHozztartVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addGap(9, 9, 9))))
                                                .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                                    .addGap(28, 28, 28)
                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel36)
                                                        .addComponent(jLabel34)
                                                        .addComponent(jLabel35)
                                                        .addComponent(jLabel38))
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(tfTelMobil)
                                                            .addComponent(tfTelOtthon)
                                                            .addComponent(tfTelMunka, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                            .addGap(181, 181, 181)))
                                    .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jLabel37)))
                                .addContainerGap())
                        );
                        pnlErtesitesLayout.setVerticalGroup(
                            pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlErtesitesLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(chbTorvKepviselo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel32)
                                    .addComponent(tfTorvKepvNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel20)
                                    .addComponent(jLabel21)
                                    .addComponent(jLabel22))
                                .addGap(1, 1, 1)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfTorvKepvIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfTorvKepvVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbTorvKepvVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel25))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfTorvKepvUtca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbTorvKepvKozterulet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfTorvKepvHazSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(chbHozzatartozo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel33)
                                    .addComponent(tfHozztartNev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel27)
                                    .addComponent(jLabel28))
                                .addGap(1, 1, 1)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfHozztartIrsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfHozztartVaros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbHozztartVarosResz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel29)
                                    .addComponent(jLabel30)
                                    .addComponent(jLabel31))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfHozztartUtca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbHozztartKozterulet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfHozztartHazSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel37)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel34)
                                    .addComponent(tfTelOtthon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel35)
                                    .addComponent(tfTelMobil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel36)
                                    .addComponent(tfTelMunka, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlErtesitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel38)
                                    .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                        );

                        chbTorvKepviselo.getAccessibleContext().setAccessibleDescription("");
                        tfTorvKepvIrsz.getAccessibleContext().setAccessibleDescription("");
                        tfTorvKepvVaros.getAccessibleContext().setAccessibleDescription("");
                        tfTorvKepvUtca.getAccessibleContext().setAccessibleDescription("");
                        cbTorvKepvKozterulet.getAccessibleContext().setAccessibleDescription("31");
                        tfTorvKepvHazSzam.getAccessibleContext().setAccessibleDescription("");
                        chbHozzatartozo.getAccessibleContext().setAccessibleDescription("");
                        tfHozztartIrsz.getAccessibleContext().setAccessibleDescription("");
                        tfHozztartVaros.getAccessibleContext().setAccessibleDescription("");
                        tfHozztartUtca.getAccessibleContext().setAccessibleDescription("");
                        cbHozztartKozterulet.getAccessibleContext().setAccessibleDescription("40");
                        tfHozztartHazSzam.getAccessibleContext().setAccessibleDescription("");
                        tfTorvKepvNev.getAccessibleContext().setAccessibleDescription("");
                        tfHozztartNev.getAccessibleContext().setAccessibleDescription("");
                        tfTelOtthon.getAccessibleContext().setAccessibleDescription("");
                        tfTelMobil.getAccessibleContext().setAccessibleDescription("");
                        tfTelMunka.getAccessibleContext().setAccessibleDescription("");
                        tfEmail.getAccessibleContext().setAccessibleDescription("");

                        tpAdatlap.addTab("Értesítés", pnlErtesites);

                        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                        jLabel39.setText("Személyi szám:");

                        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                        jLabel40.setText("Személyi igazolvány:");

                        tfSzemIgSzam.getDocument().addDocumentListener(this);

                        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                        jLabel41.setText("TAJ:");

                        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                        jLabel42.setText("Adószám:");

                        ftfSzemelyiSzam.getDocument().addDocumentListener(this);
                        ftfSzemelyiSzam.setToolTipText("X XXXXXX XXXX");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    ftfSzemelyiSzam.setFocusLostBehavior(JFormattedTextField.COMMIT);
                    ftfSzemelyiSzam.addFocusListener(this);

                    ftfTAJ.getDocument().addDocumentListener(this);
                    ftfTAJ.setToolTipText("XXX XXX XXX");
                }catch(Exception e){
                    e.printStackTrace();
                }
                ftfTAJ.setFocusLostBehavior(JFormattedTextField.COMMIT);
                ftfTAJ.addFocusListener(this);

                ftfAdoSzam.getDocument().addDocumentListener(this);
                ftfAdoSzam.setToolTipText("XXXXXXXXXX");
            }catch(Exception e){
                e.printStackTrace();
            }
            ftfAdoSzam.setFocusLostBehavior(JFormattedTextField.COMMIT);
            ftfAdoSzam.addFocusListener(this);

            ftfNyugdTorzsSzam.getDocument().addDocumentListener(this);
            ftfNyugdTorzsSzam.setToolTipText("XXX-XXXXX-X");
        }catch(Exception e){
            e.printStackTrace();
        }
        ftfNyugdTorzsSzam.setFocusLostBehavior(JFormattedTextField.COMMIT);
        ftfNyugdTorzsSzam.addFocusListener(this);

        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel60.setText("Folyósítási törzsszám:");

        javax.swing.GroupLayout pnlAzonositoLayout = new javax.swing.GroupLayout(pnlAzonosito);
        pnlAzonosito.setLayout(pnlAzonositoLayout);
        pnlAzonositoLayout.setHorizontalGroup(
            pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAzonositoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 118, Short.MAX_VALUE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ftfNyugdTorzsSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(ftfTAJ, 0, 0, Short.MAX_VALUE)
                    .addComponent(tfSzemIgSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(ftfSzemelyiSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(ftfAdoSzam, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                .addGap(183, 183, 183))
        );
        pnlAzonositoLayout.setVerticalGroup(
            pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAzonositoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(ftfSzemelyiSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfSzemIgSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftfTAJ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(ftfAdoSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAzonositoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftfNyugdTorzsSzam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel60))
                .addContainerGap(343, Short.MAX_VALUE))
        );

        tfSzemIgSzam.getAccessibleContext().setAccessibleDescription("");
        ftfSzemelyiSzam.getAccessibleContext().setAccessibleDescription("");
        ftfTAJ.getAccessibleContext().setAccessibleDescription("");
        ftfAdoSzam.getAccessibleContext().setAccessibleDescription("");

        tpAdatlap.addTab("Azonosító", pnlAzonosito);

        jLabel1.setText("Beosztás:");

        tfBeosztas.getDocument().addDocumentListener(this);

        jLabel2.setText("Alkalmazás minősége:");

        tfAlkMinoseg.getDocument().addDocumentListener(this);

        jLabel3.setText("Besorolás:");

        tfBesorolas.getDocument().addDocumentListener(this);

        jLabel4.setText("Bruttó bér:");

        tfBruttoBer.setText("0");
        tfBruttoBer.getDocument().addDocumentListener(this);

        jLabel5.setText("Heti óraszám:");

        tfHetiOraszam.setText("0");
        tfHetiOraszam.getDocument().addDocumentListener(this);

        jLabel6.setText("FEOR:");

        tfFEOR.getDocument().addDocumentListener(this);

        jLabel7.setText("Mnyp. belépés:");

        ftfMnypDatum.getDocument().addDocumentListener(this);
        ftfMnypDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel53.setText("Önyp. belépés:");

        ftfOnypDatum.getDocument().addDocumentListener(this);
        ftfOnypDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel54.setText("Önyp. díja:");

        tfOnypDij.setText("0");
        tfOnypDij.getDocument().addDocumentListener(this);

        chbMunkaszerzodes.addItemListener(this);
        chbMunkaszerzodes.setText("Munkaszerződés");

        chbMunkakori.addItemListener(this);
        chbMunkakori.setText("Munkaköri leírás");

        chbBizonyitvany.addItemListener(this);
        chbBizonyitvany.setText("Bizonyítvány");

        tfBizDarab.setText("0");
        tfBizDarab.getDocument().addDocumentListener(this);
        tfBizDarab.setEnabled(false);

        jLabel55.setText("db");

        chbCV.addItemListener(this);
        chbCV.setText("CV");

        jLabel56.setText("Alkalmasság dátuma:");

        ftfAlkalmassagDatum.getDocument().addDocumentListener(this);
        ftfAlkalmassagDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel57.setText("Reg. Kártya száma:");

        tfRegKartya.getDocument().addDocumentListener(this);

        javax.swing.GroupLayout pnlBeosztasLayout = new javax.swing.GroupLayout(pnlBeosztas);
        pnlBeosztas.setLayout(pnlBeosztasLayout);
        pnlBeosztasLayout.setHorizontalGroup(
            pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBeosztasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbCV)
                    .addGroup(pnlBeosztasLayout.createSequentialGroup()
                        .addComponent(chbBizonyitvany)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfBizDarab, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel55))
                    .addComponent(chbMunkakori)
                    .addComponent(chbMunkaszerzodes)
                    .addGroup(pnlBeosztasLayout.createSequentialGroup()
                        .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel53)
                            .addComponent(jLabel54))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfAlkMinoseg, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                            .addComponent(tfBeosztas, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                            .addGroup(pnlBeosztasLayout.createSequentialGroup()
                                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tfOnypDij, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(ftfOnypDatum, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                    .addComponent(ftfMnypDatum, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                    .addComponent(tfFEOR, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(tfHetiOraszam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(tfBesorolas, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(tfBruttoBer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(ftfAlkalmassagDatum, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                    .addComponent(tfRegKartya, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                                .addGap(161, 161, 161))))
                    .addComponent(jLabel56)
                    .addComponent(jLabel57))
                .addContainerGap())
        );
        pnlBeosztasLayout.setVerticalGroup(
            pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBeosztasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfBeosztas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfAlkMinoseg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfBesorolas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(tfBruttoBer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfHetiOraszam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tfFEOR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ftfMnypDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(ftfOnypDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(tfOnypDij, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(ftfAlkalmassagDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(tfRegKartya, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(chbMunkaszerzodes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chbMunkakori)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBeosztasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chbBizonyitvany)
                    .addComponent(tfBizDarab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel55))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chbCV)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        tfBeosztas.getAccessibleContext().setAccessibleDescription("");
        tfAlkMinoseg.getAccessibleContext().setAccessibleDescription("");
        tfBesorolas.getAccessibleContext().setAccessibleDescription("");
        tfBruttoBer.getAccessibleContext().setAccessibleDescription("");
        tfHetiOraszam.getAccessibleContext().setAccessibleDescription("");
        tfFEOR.getAccessibleContext().setAccessibleDescription("");
        ftfMnypDatum.getAccessibleContext().setAccessibleDescription("");
        ftfOnypDatum.getAccessibleContext().setAccessibleDescription("");
        tfOnypDij.getAccessibleContext().setAccessibleDescription("");
        chbMunkaszerzodes.getAccessibleContext().setAccessibleDescription("");
        chbMunkakori.getAccessibleContext().setAccessibleDescription("");
        tfBizDarab.getAccessibleContext().setAccessibleDescription("");
        chbCV.getAccessibleContext().setAccessibleDescription("");
        ftfAlkalmassagDatum.getAccessibleContext().setAccessibleDescription("");
        tfRegKartya.getAccessibleContext().setAccessibleDescription("");

        tpAdatlap.addTab("Beosztás", pnlBeosztas);

        jLabel52.setText("Cselekvőképesség mértéke:");

        tfCselekvokepesseg.getDocument().addDocumentListener(this);

        jLabel51.setText("Megjegyzés:");

        taMegjegyzes.getDocument().addDocumentListener(this);
        taMegjegyzes.setColumns(20);
        taMegjegyzes.setFont(new java.awt.Font("Tahoma", 0, 12));
        taMegjegyzes.setRows(5);
        taMegjegyzes.setEditable(true);
        jScrollPane1.setViewportView(taMegjegyzes);

        chbMark.setText("Megjelölés");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(38, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbMark)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfCselekvokepesseg, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52)
                    .addComponent(tfCselekvokepesseg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chbMark)
                .addContainerGap(305, Short.MAX_VALUE))
        );

        tfCselekvokepesseg.getAccessibleContext().setAccessibleDescription("");

        tpAdatlap.addTab("Egyéb", jPanel1);

        chbEtkezes.addItemListener(this);
        chbEtkezes.setText("Étkezés");

        jLabel43.setText("Díja:");
        jLabel43.setEnabled(false);

        tfEtkDij.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfEtkDij.setText("0");
        tfEtkDij.getDocument().addDocumentListener(this);
        tfEtkDij.setEnabled(false);

        jLabel44.setText("Ft");
        jLabel44.setEnabled(false);

        jLabel45.setText("Tipusa:");
        jLabel45.setEnabled(false);

        rbEtkTipElvitel.addItemListener(this);
        btngpEtkTipus.add(rbEtkTipElvitel);
        rbEtkTipElvitel.setText("Elvitellel");
        rbEtkTipElvitel.setEnabled(false);

        rbEtkTipSzallitas.addItemListener(this);
        btngpEtkTipus.add(rbEtkTipSzallitas);
        rbEtkTipSzallitas.setText("Szállítással");
        rbEtkTipSzallitas.setEnabled(false);

        rbEtkTipHelyben.addItemListener(this);
        btngpEtkTipus.add(rbEtkTipHelyben);
        rbEtkTipHelyben.setText("Helyben");
        rbEtkTipHelyben.setEnabled(false);

        jLabel46.setText("Jövedelem:");
        jLabel46.setEnabled(false);

        tfJovedelem.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tfJovedelem.setText("0");
        tfJovedelem.getDocument().addDocumentListener(this);
        tfJovedelem.setEnabled(false);
        tfJovedelem.addFocusListener(this);

        jLabel47.setText("Ft");
        jLabel47.setEnabled(false);

        jLabel48.setText("Beadás dátuma:");
        jLabel48.setEnabled(false);

        jLabel49.setText("Értesítés dátuma:");
        jLabel49.setEnabled(false);

        jLabel50.setText("Megszűnés dátuma:");
        jLabel50.setEnabled(false);

        jTextField49.setText("jTextField49");

        jTextField50.setText("jTextField50");

        jComboBox11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        ftfBeadasDatum.getDocument().addDocumentListener(this);
        ftfBeadasDatum.setToolTipText("ÉÉÉÉ-HH-NN");
        ftfBeadasDatum.setEnabled(false);

        ftfErtesitesDatum.getDocument().addDocumentListener(this);
        ftfErtesitesDatum.setToolTipText("ÉÉÉÉ-HH-NN");
        ftfErtesitesDatum.setEnabled(false);

        ftfMegszunesDatum.getDocument().addDocumentListener(this);
        ftfMegszunesDatum.setToolTipText("ÉÉÉÉ-HH-NN");
        ftfMegszunesDatum.setEnabled(false);

        chbMelegedo.addItemListener(this);
        chbMelegedo.setText("Nappali Melegedő");

        jLabel61.setText("Felvétel dátuma:");
        jLabel61.setEnabled(false);

        ftfMelegedoFelvetelDatum.getDocument().addDocumentListener(this);
        ftfMelegedoFelvetelDatum.setToolTipText("ÉÉÉÉ-HH-NN");
        ftfMelegedoFelvetelDatum.setEnabled(false);

        jLabel62.setText("Kategória:");
        jLabel62.setEnabled(false);

        lblKategoria.setFont(new java.awt.Font("Tahoma", 1, 12));
        lblKategoria.setEnabled(false);

        chbEjjeliMenedek.setText("Éjjeli Menedék");
        chbEjjeliMenedek.addItemListener(this);

        cbSzallitasiCim.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Állandó", "Ideiglenes"}));
        cbSzallitasiCim.setVisible(rbEtkTipSzallitas.isSelected());
        cbSzallitasiCim.addItemListener(this);

        lblSzallCim.setText("Cím:");
        lblSzallCim.setVisible(rbEtkTipSzallitas.isEnabled());

        chbAtmenetiSzallo.setText("Átmeneti szálló");
        chbAtmenetiSzallo.addItemListener(this);

        jLabel63.setText("Jegyzői igazolás dátuma:");
        jLabel63.setEnabled(false);

        ftfJegyzoiIgazolasDatum.setEnabled(false);
        ftfJegyzoiIgazolasDatum.getDocument().addDocumentListener(this);
        ftfJegyzoiIgazolasDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel64.setText("Felvétel dátuma:");
        jLabel64.setEnabled(false);

        ftfAtmenetiFelvetelDatum.setEnabled(false);
        ftfAtmenetiFelvetelDatum.getDocument().addDocumentListener(this);
        ftfAtmenetiFelvetelDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel65.setText("Lezárás dátuma:");
        jLabel65.setEnabled(false);

        ftfAtmenetiLezarasDatum.setEnabled(false);
        ftfAtmenetiLezarasDatum.getDocument().addDocumentListener(this);
        ftfAtmenetiLezarasDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        jLabel66.setText("Ellátás dátuma:");
        jLabel66.setEnabled(false);

        ftfEllatasDatum.setEnabled(false);
        ftfEllatasDatum.getDocument().addDocumentListener(this);
        ftfEllatasDatum.setToolTipText("ÉÉÉÉ-HH-NN");

        javax.swing.GroupLayout pnlEgyebLayout = new javax.swing.GroupLayout(pnlEgyeb);
        pnlEgyeb.setLayout(pnlEgyebLayout);
        pnlEgyebLayout.setHorizontalGroup(
            pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEgyebLayout.createSequentialGroup()
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEgyebLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(chbEtkezes))
                    .addGroup(pnlEgyebLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlEgyebLayout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbEtkTipElvitel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbEtkTipSzallitas)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rbEtkTipHelyben)
                                .addGap(15, 15, 15)
                                .addComponent(lblSzallCim)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(pnlEgyebLayout.createSequentialGroup()
                                .addComponent(jLabel46)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfJovedelem)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel47)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel43)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfEtkDij, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel44)
                                .addGap(16, 16, 16)))
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlEgyebLayout.createSequentialGroup()
                                .addComponent(cbSzallitasiCim, 0, 66, Short.MAX_VALUE)
                                .addGap(19, 19, 19))
                            .addGroup(pnlEgyebLayout.createSequentialGroup()
                                .addComponent(jLabel62)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblKategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlEgyebLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel63)
                            .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel50, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel49, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel48, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ftfEllatasDatum, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(ftfBeadasDatum, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ftfErtesitesDatum, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ftfMegszunesDatum, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ftfJegyzoiIgazolasDatum, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                            .addComponent(ftfMelegedoFelvetelDatum)
                            .addComponent(ftfAtmenetiLezarasDatum, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(ftfAtmenetiFelvetelDatum, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField50, 0, 0, Short.MAX_VALUE)
                            .addComponent(jTextField49, 0, 0, Short.MAX_VALUE))
                        .addGap(62, 62, 62)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jComboBox12, 0, 0, Short.MAX_VALUE)
                            .addComponent(jComboBox11, 0, 0, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(pnlEgyebLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbMelegedo)
                    .addComponent(chbEjjeliMenedek)
                    .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel61, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(chbAtmenetiSzallo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel64)
                    .addComponent(jLabel65))
                .addGap(197, 197, 197))
            .addGroup(pnlEgyebLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel66)
                .addContainerGap(323, Short.MAX_VALUE))
        );
        pnlEgyebLayout.setVerticalGroup(
            pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEgyebLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chbEtkezes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(tfJovedelem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblKategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel62)
                    .addComponent(jLabel43)
                    .addComponent(tfEtkDij, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44)
                    .addComponent(jLabel47))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbEtkTipElvitel)
                    .addComponent(jLabel45)
                    .addComponent(rbEtkTipSzallitas)
                    .addComponent(rbEtkTipHelyben)
                    .addComponent(cbSzallitasiCim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSzallCim))
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEgyebLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlEgyebLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel48)
                            .addComponent(ftfBeadasDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel49)
                            .addComponent(ftfErtesitesDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel50)
                            .addComponent(ftfMegszunesDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel63)
                            .addComponent(ftfJegyzoiIgazolasDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel66)
                    .addComponent(ftfEllatasDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(chbMelegedo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel61)
                    .addComponent(ftfMelegedoFelvetelDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(chbEjjeliMenedek)
                .addGap(18, 18, 18)
                .addComponent(chbAtmenetiSzallo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel64)
                    .addComponent(ftfAtmenetiFelvetelDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEgyebLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(ftfAtmenetiLezarasDatum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        chbEtkezes.getAccessibleContext().setAccessibleDescription("");
        tfEtkDij.getAccessibleContext().setAccessibleDescription("");
        rbEtkTipElvitel.getAccessibleContext().setAccessibleDescription("");
        tfJovedelem.getAccessibleContext().setAccessibleDescription("");
        ftfBeadasDatum.getAccessibleContext().setAccessibleDescription("");
        ftfErtesitesDatum.getAccessibleContext().setAccessibleDescription("");
        ftfMegszunesDatum.getAccessibleContext().setAccessibleDescription("");

        tpAdatlap.addTab("Szolgáltatás", pnlEgyeb);

        btnVissza.addActionListener(this);
        btnVissza.setActionCommand("vissza");
        btnVissza.setText("Vissza");

        btnMentes.addActionListener(this);
        btnMentes.setActionCommand("mentes");
        btnMentes.setText("Mentés");
        btnMentes.setEnabled(false);

        tfIktsz.setFont(new java.awt.Font("Tahoma", 1, 14));
        tfIktsz.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tfIktsz.getDocument().addDocumentListener(this);

        jLabel58.setText("Iktatószám:");

        lblRogzDatum.setText(" ");

        jLabel59.setBackground(new java.awt.Color(102, 102, 102));
        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setText("Adatlap");
        jLabel59.setOpaque(true);

        btnPrint.setText("Export");
        btnPrint.addActionListener(this);
        btnPrint.setActionCommand("export");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel58)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfIktsz, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblNev_cimsor, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblRogz)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblRogzDatum, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                                    .addComponent(lblID, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnPrint)
                        .addGap(192, 192, 192)
                        .addComponent(btnMentes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnVissza))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tpAdatlap, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel59)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNev_cimsor, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                    .addComponent(lblRogz)
                    .addComponent(lblRogzDatum, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(tfIktsz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblID))
                .addGap(12, 12, 12)
                .addComponent(tpAdatlap, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVissza)
                    .addComponent(btnMentes)
                    .addComponent(btnPrint))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLakcimNelkuli;
    private javax.swing.JButton btnMentes;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSzallo;
    private javax.swing.JButton btnVissza;
    private javax.swing.ButtonGroup btngpEtkTipus;
    private javax.swing.ButtonGroup btngpNem;
    private javax.swing.JComboBox cbAllKozterulet;
    private javax.swing.JComboBox cbAllVarosResz;
    private javax.swing.JComboBox cbHozztartKozterulet;
    private javax.swing.JComboBox cbHozztartVarosResz;
    private javax.swing.JComboBox cbIdeigVarosResz;
    private javax.swing.JComboBox cbIdeiglKozterulet;
    private javax.swing.JComboBox cbSzallitasiCim;
    private javax.swing.JComboBox cbTorvKepvKozterulet;
    private javax.swing.JComboBox cbTorvKepvVarosResz;
    private javax.swing.JCheckBox chbAllandoLakcim;
    private javax.swing.JCheckBox chbAtmenetiSzallo;
    private javax.swing.JCheckBox chbBizonyitvany;
    private javax.swing.JCheckBox chbCV;
    private javax.swing.JCheckBox chbEjjeliMenedek;
    private javax.swing.JCheckBox chbEtkezes;
    private javax.swing.JCheckBox chbHozzatartozo;
    private javax.swing.JCheckBox chbIdeiglenesLakcim;
    private javax.swing.JCheckBox chbMark;
    private javax.swing.JCheckBox chbMelegedo;
    private javax.swing.JCheckBox chbMunkakori;
    private javax.swing.JCheckBox chbMunkaszerzodes;
    private javax.swing.JCheckBox chbTorvKepviselo;
    private javax.swing.JFormattedTextField ftfAdoSzam;
    private javax.swing.JFormattedTextField ftfAlkalmassagDatum;
    private javax.swing.JFormattedTextField ftfAtmenetiFelvetelDatum;
    private javax.swing.JFormattedTextField ftfAtmenetiLezarasDatum;
    private javax.swing.JFormattedTextField ftfBeadasDatum;
    private javax.swing.JFormattedTextField ftfEllatasDatum;
    private javax.swing.JFormattedTextField ftfErtesitesDatum;
    private javax.swing.JFormattedTextField ftfJegyzoiIgazolasDatum;
    private javax.swing.JFormattedTextField ftfMegszunesDatum;
    private javax.swing.JFormattedTextField ftfMelegedoFelvetelDatum;
    private javax.swing.JFormattedTextField ftfMnypDatum;
    private javax.swing.JFormattedTextField ftfNyugdTorzsSzam;
    private javax.swing.JFormattedTextField ftfOnypDatum;
    private javax.swing.JFormattedTextField ftfSzemelyiSzam;
    private javax.swing.JFormattedTextField ftfSzulDatum;
    private javax.swing.JFormattedTextField ftfTAJ;
    private javax.swing.JComboBox jComboBox11;
    private javax.swing.JComboBox jComboBox12;
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
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField49;
    private javax.swing.JTextField jTextField50;
    private javax.swing.JLabel lblAllpolg;
    private javax.swing.JLabel lblAnyjaNeve;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblKategoria;
    private javax.swing.JLabel lblNeme;
    private javax.swing.JLabel lblNev;
    private javax.swing.JLabel lblNev_cimsor;
    private javax.swing.JLabel lblRogz;
    private javax.swing.JLabel lblRogzDatum;
    private javax.swing.JLabel lblSzallCim;
    private javax.swing.JLabel lblSzulDatum;
    private javax.swing.JLabel lblSzulHely;
    private javax.swing.JLabel lblSzulNev;
    private javax.swing.JPanel pnlAzonosito;
    private javax.swing.JPanel pnlBeosztas;
    private javax.swing.JPanel pnlEgyeb;
    private javax.swing.JPanel pnlErtesites;
    private javax.swing.JPanel pnlLakcim;
    private javax.swing.JPanel pnlSzulAdat;
    private javax.swing.JRadioButton rbEtkTipElvitel;
    private javax.swing.JRadioButton rbEtkTipHelyben;
    private javax.swing.JRadioButton rbEtkTipSzallitas;
    private javax.swing.JRadioButton rbFerfi;
    private javax.swing.JRadioButton rbNo;
    private javax.swing.JTextArea taMegjegyzes;
    private javax.swing.JTextField tfAlkMinoseg;
    private javax.swing.JTextField tfAllHazSzam;
    private javax.swing.JTextField tfAllIrsz;
    private javax.swing.JTextField tfAllUtca;
    private javax.swing.JTextField tfAllVaros;
    private javax.swing.JTextField tfAllpolg;
    private javax.swing.JTextField tfAnyaNev;
    private javax.swing.JTextField tfBeosztas;
    private javax.swing.JTextField tfBesorolas;
    private javax.swing.JTextField tfBizDarab;
    private javax.swing.JTextField tfBruttoBer;
    private javax.swing.JTextField tfCselekvokepesseg;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfEtkDij;
    private javax.swing.JTextField tfFEOR;
    private javax.swing.JTextField tfHetiOraszam;
    private javax.swing.JTextField tfHozztartHazSzam;
    private javax.swing.JTextField tfHozztartIrsz;
    private javax.swing.JTextField tfHozztartNev;
    private javax.swing.JTextField tfHozztartUtca;
    private javax.swing.JTextField tfHozztartVaros;
    private javax.swing.JTextField tfIdeiglHazSzam;
    private javax.swing.JTextField tfIdeiglIrsz;
    private javax.swing.JTextField tfIdeiglUtca;
    private javax.swing.JTextField tfIdeiglVaros;
    private javax.swing.JTextField tfIktsz;
    private javax.swing.JTextField tfJovedelem;
    private javax.swing.JTextField tfNev;
    private javax.swing.JTextField tfOnypDij;
    private javax.swing.JTextField tfRegKartya;
    private javax.swing.JTextField tfSzemIgSzam;
    private javax.swing.JTextField tfSzulHely;
    private javax.swing.JTextField tfSzulNev;
    private javax.swing.JTextField tfTelMobil;
    private javax.swing.JTextField tfTelMunka;
    private javax.swing.JTextField tfTelOtthon;
    private javax.swing.JTextField tfTorvKepvHazSzam;
    private javax.swing.JTextField tfTorvKepvIrsz;
    private javax.swing.JTextField tfTorvKepvNev;
    private javax.swing.JTextField tfTorvKepvUtca;
    private javax.swing.JTextField tfTorvKepvVaros;
    private javax.swing.JTabbedPane tpAdatlap;
    // End of variables declaration//GEN-END:variables
    private Integer id;
    private boolean tipus;
    private String[] eredmeny;
    private String[] param;
    private SQLMuvelet lek;
    private boolean uj;
    private ResultSet res;
    private String igen = "1";
    private String nem = "0";
    private boolean igaz = true;
    private boolean hamis = false;
    private int hibasadat = 0;
    char[] temp;
    int sum;
    int checksum;
    public static String version = "1.2.4";
    Log log = new Log();
    PreparedStatement stmt;
    com.mysql.jdbc.Connection con;

//DocumentListener implementáció
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

//ItemListener implementáció
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == chbBizonyitvany) {
            tfBizDarab.setEnabled(chbBizonyitvany.isSelected());
        }
        if (e.getSource() == chbEtkezes) {
            tfEtkDij.setEnabled(chbEtkezes.isSelected());
            tfJovedelem.setEnabled(chbEtkezes.isSelected());
            rbEtkTipElvitel.setEnabled(chbEtkezes.isSelected());
            rbEtkTipHelyben.setEnabled(chbEtkezes.isSelected());
            rbEtkTipSzallitas.setEnabled(chbEtkezes.isSelected());
            ftfBeadasDatum.setEnabled(chbEtkezes.isSelected());
            ftfErtesitesDatum.setEnabled(chbEtkezes.isSelected());
            ftfMegszunesDatum.setEnabled(chbEtkezes.isSelected());
            ftfJegyzoiIgazolasDatum.setEnabled(chbEtkezes.isSelected());
            ftfEllatasDatum.setEnabled(chbEtkezes.isSelected());
            jLabel43.setEnabled(chbEtkezes.isSelected());
            jLabel44.setEnabled(chbEtkezes.isSelected());
            jLabel45.setEnabled(chbEtkezes.isSelected());
            jLabel46.setEnabled(chbEtkezes.isSelected());
            jLabel47.setEnabled(chbEtkezes.isSelected());
            jLabel48.setEnabled(chbEtkezes.isSelected());
            jLabel49.setEnabled(chbEtkezes.isSelected());
            jLabel50.setEnabled(chbEtkezes.isSelected());
            jLabel62.setEnabled(chbEtkezes.isSelected());
            jLabel63.setEnabled(chbEtkezes.isSelected());
            jLabel66.setEnabled(chbEtkezes.isSelected());
            lblKategoria.setEnabled(chbEtkezes.isSelected());
            if (!chbEtkezes.isSelected()) {
                tfEtkDij.setText("0");
                tfJovedelem.setText("0");
                rbEtkTipElvitel.setSelected(false);
                rbEtkTipHelyben.setSelected(false);
                rbEtkTipSzallitas.setSelected(false);
                ftfBeadasDatum.setText("");
                ftfErtesitesDatum.setText("");
                ftfMegszunesDatum.setText("");
                ftfJegyzoiIgazolasDatum.setText("");
            }

        }
        if (e.getSource() == chbMelegedo) {
            ftfMelegedoFelvetelDatum.setEnabled(chbMelegedo.isSelected());
            jLabel61.setEnabled(chbMelegedo.isSelected());
            if (!chbMelegedo.isSelected()) {
                ftfMelegedoFelvetelDatum.setText("");
            }
        }
        if (e.getSource() == chbTorvKepviselo) {
            tfTorvKepvNev.setEnabled(chbTorvKepviselo.isSelected());
            tfTorvKepvIrsz.setEnabled(chbTorvKepviselo.isSelected());
            tfTorvKepvVaros.setEnabled(chbTorvKepviselo.isSelected());
            tfTorvKepvUtca.setEnabled(chbTorvKepviselo.isSelected());
            tfTorvKepvHazSzam.setEnabled(chbTorvKepviselo.isSelected());
            cbTorvKepvKozterulet.setEnabled(chbTorvKepviselo.isSelected());
            cbTorvKepvVarosResz.setEnabled(chbTorvKepviselo.isSelected());
            if (!chbTorvKepviselo.isSelected()) {
                tfTorvKepvIrsz.setText("");
                tfTorvKepvVaros.setText("");
                tfTorvKepvUtca.setText("");
                tfTorvKepvHazSzam.setText("");
                cbTorvKepvVarosResz.removeAllItems();
            }
        }
        if (e.getSource() == chbHozzatartozo) {
            tfHozztartNev.setEnabled(chbHozzatartozo.isSelected());
            tfHozztartIrsz.setEnabled(chbHozzatartozo.isSelected());
            tfHozztartVaros.setEnabled(chbHozzatartozo.isSelected());
            tfHozztartUtca.setEnabled(chbHozzatartozo.isSelected());
            tfHozztartHazSzam.setEnabled(chbHozzatartozo.isSelected());
            cbHozztartKozterulet.setEnabled(chbHozzatartozo.isSelected());
            cbHozztartVarosResz.setEnabled(chbHozzatartozo.isSelected());
            if (!chbHozzatartozo.isSelected()) {
                tfHozztartIrsz.setText("");
                tfHozztartVaros.setText("");
                tfHozztartUtca.setText("");
                tfHozztartHazSzam.setText("");
                cbHozztartVarosResz.removeAllItems();
            }
        }
        if (e.getSource() == chbIdeiglenesLakcim) {
            tfIdeiglIrsz.setEnabled(chbIdeiglenesLakcim.isSelected());
            tfIdeiglVaros.setEnabled(chbIdeiglenesLakcim.isSelected());
            tfIdeiglUtca.setEnabled(chbIdeiglenesLakcim.isSelected());
            tfIdeiglHazSzam.setEnabled(chbIdeiglenesLakcim.isSelected());
            cbIdeiglKozterulet.setEnabled(chbIdeiglenesLakcim.isSelected());
            cbIdeigVarosResz.setEnabled(chbIdeiglenesLakcim.isSelected());
            btnSzallo.setEnabled(chbIdeiglenesLakcim.isSelected());
            if (!chbIdeiglenesLakcim.isSelected()) {
                tfIdeiglIrsz.setText("");
                tfIdeiglVaros.setText("");
                tfIdeiglUtca.setText("");
                tfIdeiglHazSzam.setText("");
                cbIdeigVarosResz.removeAllItems();
            }
        }
        if (e.getSource() == chbAllandoLakcim) {
            tfAllIrsz.setEnabled(chbAllandoLakcim.isSelected());
            tfAllVaros.setEnabled(chbAllandoLakcim.isSelected());
            tfAllUtca.setEnabled(chbAllandoLakcim.isSelected());
            tfAllHazSzam.setEnabled(chbAllandoLakcim.isSelected());
            cbAllKozterulet.setEnabled(chbAllandoLakcim.isSelected());
            cbAllVarosResz.setEnabled(chbAllandoLakcim.isSelected());
            btnLakcimNelkuli.setEnabled(chbAllandoLakcim.isSelected());
            if (!chbAllandoLakcim.isSelected()) {
                tfAllIrsz.setText("");
                tfAllVaros.setText("");
                tfAllUtca.setText("");
                tfAllHazSzam.setText("");
                cbAllVarosResz.removeAllItems();
            }
        }
        if (e.getSource() == cbAllVarosResz) {
            String irsz = varosToIrsz(tfAllVaros.getText(), cbAllVarosResz.getSelectedItem().toString());
            tfAllIrsz.setText(irsz);
        }
        if (e.getSource() == cbIdeigVarosResz) {
            String irsz = varosToIrsz(tfIdeiglVaros.getText(), cbIdeigVarosResz.getSelectedItem().toString());
            tfIdeiglIrsz.setText(irsz);
        }
        if (e.getSource() == cbHozztartVarosResz) {
            String irsz = varosToIrsz(tfHozztartVaros.getText(), cbHozztartVarosResz.getSelectedItem().toString());
            tfHozztartIrsz.setText(irsz);
        }
        if (e.getSource() == cbTorvKepvVarosResz) {
            String irsz = varosToIrsz(tfTorvKepvVaros.getText(), cbTorvKepvVarosResz.getSelectedItem().toString());
            tfTorvKepvIrsz.setText(irsz);
        }
        if (e.getSource() == rbEtkTipSzallitas) {
//            cbSzallitasiCim.setVisible(rbEtkTipSzallitas.isSelected());
//            lblSzallCim.setVisible(rbEtkTipSzallitas.isSelected());

            if (rbEtkTipSzallitas.isSelected()) {
                if (chbAllandoLakcim.isSelected() || chbIdeiglenesLakcim.isSelected()) {
                    cbSzallitasiCim.setVisible(true);
                    lblSzallCim.setVisible(true);
                } else {
                    new Uzenet("A szállításhoz meg kell adni egy címet!", "Figyelmeztetés", Uzenet.WARNING);
                    rbEtkTipElvitel.setSelected(false);
                    cbSzallitasiCim.setVisible(false);
                    lblSzallCim.setVisible(false);
                }
            } else {
                cbSzallitasiCim.setVisible(false);
                lblSzallCim.setVisible(false);
            }
        }
        if (e.getSource() == chbAtmenetiSzallo) {
            jLabel64.setEnabled(chbAtmenetiSzallo.isSelected());
            jLabel65.setEnabled(chbAtmenetiSzallo.isSelected());
            ftfAtmenetiFelvetelDatum.setEnabled(chbAtmenetiSzallo.isSelected());
            ftfAtmenetiLezarasDatum.setEnabled(chbAtmenetiSzallo.isSelected());
            if (!chbAtmenetiSzallo.isSelected()) {
                ftfAtmenetiFelvetelDatum.setText("");
            }
        }
        btnMentes.setEnabled(true);
        Globals.MENTVE = false;
    }
//ActionListener implementáció

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnVissza) {  //vissza gomb
            if (btnMentes.isEnabled()) {  //ha volt nem mentett változás, rákérdez a mentésre
                if (JOptionPane.showConfirmDialog(this, "Az adatok nem lettek mentve, folytatja mentés nélkül?", "Mentés!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                    Globals.MENTVE = true;
                    this.setVisible(false);
                }
            } else {
                Globals.ENTERPRESSED = false; //ha enterrel választották ki a listából, az enter lenyomását töröljük
                this.setVisible(false); //eltüntetjük az ablakot -> esemény keletkezik, ami a főablakból törli az objektumot
            }
        }
        if (e.getSource() == btnMentes) {
            if (!(rbFerfi.isSelected() || rbNo.isSelected())) {
                new Uzenet("Nincs bejelölve a nem!", "Mentés", Uzenet.WARNING);
            } else {

                if (chbEtkezes.isSelected() && (!rbEtkTipElvitel.isSelected()&&!rbEtkTipHelyben.isSelected()&&!rbEtkTipSzallitas.isSelected())) {
                    new Uzenet("Nincs bejelölve a típus az Étkeztetésnél!", "Mentés", Uzenet.WARNING);
                } else {
                    if (hibasadat == 0) {
                        if (Mentes(true)) {
                            btnMentes.setEnabled(false);   //sikeres mentés után a mentés gomb ismét inaktív
                            Globals.MENTVE = true;
                            if (!log.writeLog("Mentve: " + tfNev.getText())) {
                                new Uzenet("Hiba a log írásakor!", "Hiba", Uzenet.ERROR);
                            }
                        } else {
                            new Uzenet("Hiba az adatok mentése közben!", "Mentés", Uzenet.ERROR);
//                    JOptionPane.showMessageDialog(this, "Hiba az adatok mentése közben!", "Mentés", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        new Uzenet("Nem lehet menteni, még van hibás adat!", "Mentés", Uzenet.WARNING);
                    }
                }
            }
        }
        if (e.getActionCommand().equals("lakcimnelkuli")) {

            tfAllUtca.setText(Config.settings.getProperty("cim_nelkuli"));
            tfAllIrsz.setText(Config.settings.getProperty("cim_nelkuli_irsz"));
            tfAllVaros.setText(Config.settings.getProperty("cim_nelkuli_varos"));
        }
        if (e.getActionCommand().equals("szallo")) {

            tfIdeiglIrsz.setText(Config.settings.getProperty("irsz"));
            tfIdeiglVaros.setText(Config.settings.getProperty("varos"));
            tfIdeiglUtca.setText(Config.settings.getProperty("utca"));
            tfIdeiglHazSzam.setText(Config.settings.getProperty("hazszam"));
            cbIdeigVarosResz.removeItemListener(this);
            cbIdeigVarosResz.addItem(Config.settings.getProperty("vresz"));
            cbIdeigVarosResz.addItemListener(this);
            cbIdeigVarosResz.setSelectedItem(Config.settings.getProperty("vresz"));
//            cbIdeiglKozterulet.addItem(Config.settings.getProperty("kozterulet"));
            cbIdeiglKozterulet.setSelectedItem(Config.settings.getProperty("kozterulet"));
        }
        if (e.getActionCommand().equals("export")) {
            String[] adatok = new String[24];
            String iktsz = tfIktsz.getText().replaceAll("/", "-");
            String filenev = "Adatlap - " + tfNev.getText() + " " + "(" + iktsz + ").pdf";
            adatok[0] = tfIktsz.getText();
            adatok[1] = tfNev.getText();
            adatok[2] = tfSzulNev.getText().isEmpty() ? "u.a." : tfSzulNev.getText();
            if (chbAllandoLakcim.isSelected()) {
                String vresz = cbAllVarosResz.getSelectedItem().equals("") ? "" : "-" + cbAllVarosResz.getSelectedItem();
                adatok[3] = tfAllIrsz.getText() + ", " + tfAllVaros.getText() + vresz + ", " + tfAllUtca.getText() + " " + cbAllKozterulet.getSelectedItem().toString() + " " + tfAllHazSzam.getText();
            } else {
                adatok[3] = "-";
            }
            if (chbIdeiglenesLakcim.isSelected()) {
                String vresz=cbIdeigVarosResz.getSelectedItem().equals("")?"":"-"+cbIdeigVarosResz.getSelectedItem();
                adatok[4]=tfIdeiglIrsz.getText()+", "+tfIdeiglVaros.getText()+vresz+", "+ tfIdeiglUtca.getText()+" "+cbIdeiglKozterulet.getSelectedItem().toString()+" "+tfIdeiglHazSzam.getText();
            }else adatok[4]="-";
            adatok[5]=tfAllpolg.getText();
            adatok[6]=tfSzulHely.getText()+", "+ftfSzulDatum.getText().replaceAll("-", ".");
            if(chbTorvKepviselo.isSelected()){
                String vresz=cbTorvKepvVarosResz.getSelectedIndex()==-1?"":"-"+cbTorvKepvVarosResz.getSelectedItem();
                adatok[7]=tfTorvKepvIrsz.getText()+", "+tfTorvKepvVaros.getText()+vresz+", "+ tfTorvKepvUtca.getText()+" "+cbTorvKepvKozterulet.getSelectedItem().toString()+" "+tfTorvKepvHazSzam.getText();
            }else adatok[7]="";
            if(chbHozzatartozo.isSelected()){
                String vresz=cbHozztartVarosResz.getSelectedIndex()==-1?"":"-"+cbHozztartVarosResz.getSelectedItem();
                adatok[8]=tfHozztartNev.getText()+", "+tfHozztartIrsz.getText()+", "+tfHozztartVaros.getText()+vresz+", "+ tfHozztartUtca.getText()+" "+cbHozztartKozterulet.getSelectedItem().toString()+" "+tfHozztartHazSzam.getText();
            }else adatok[8]="";
            adatok[9]=tfCselekvokepesseg.getText();
            adatok[10]=tfSzemIgSzam.getText();
            adatok[11]=ftfTAJ.getText().contains("_")?"":ftfTAJ.getText();
            adatok[12]=ftfBeadasDatum.getText().replaceAll("-", ".");
            adatok[13]="";
            adatok[14]=ftfErtesitesDatum.getText().replaceAll("-", ".");
            adatok[15]=taMegjegyzes.getText();
            adatok[16]=ftfMegszunesDatum.getText().isEmpty()?"határozatlan":ftfMegszunesDatum.getText().replaceAll("-", ".");
            adatok[17]=lblKategoria.getText();
            adatok[18]=tfEtkDij.getText()+" Ft/adag";
            adatok[19]=tfAnyaNev.getText();
            adatok[20]=ftfNyugdTorzsSzam.getText();
            adatok[21]=tfTorvKepvNev.getText();
            adatok[22]=tfJovedelem.getText();
            adatok[23]=ftfEllatasDatum.getText();
            new AdatlapExport(filenev, adatok);
        }
    }
//FocusListener implementáció

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
        if (e.getSource() == tfIdeiglIrsz) {
            if (!tfIdeiglIrsz.getText().isEmpty()) {
                String[] varos = new String[2];
                varos = irszToVaros(tfIdeiglIrsz.getText());
                tfIdeiglVaros.setText(varos[0]);
                cbIdeigVarosResz.addItem(varos[1]);
                cbIdeigVarosResz.setSelectedItem(varos[1]);
                cbIdeigVarosResz.setEnabled(true);
            }
        }
        if (e.getSource() == tfHozztartIrsz) {
            if (!tfHozztartIrsz.getText().isEmpty()) {
                String[] varos = new String[2];
                varos = irszToVaros(tfHozztartIrsz.getText());
                tfHozztartVaros.setText(varos[0]);
                cbHozztartVarosResz.addItem(varos[1]);
                cbHozztartVarosResz.setSelectedItem(varos[1]);
                cbHozztartVarosResz.setEnabled(true);
            }
        }
        if (e.getSource() == tfTorvKepvIrsz) {
            if (!tfTorvKepvIrsz.getText().isEmpty()) {
                String[] varos = new String[2];
                varos = irszToVaros(tfTorvKepvIrsz.getText());
                tfTorvKepvVaros.setText(varos[0]);
                cbTorvKepvVarosResz.addItem(varos[1]);
                cbTorvKepvVarosResz.setSelectedItem(varos[1]);
                cbTorvKepvVarosResz.setEnabled(true);
            }
        }
        if (e.getSource() == tfAllVaros) {
            if (tfAllIrsz.getText().isEmpty()) {
                cbAllVarosResz.removeItemListener(this);
                cbAllVarosResz.removeAllItems();
                cbAllVarosResz.setEnabled(false);
                String[] irsz;
//                String[] v = new String[1];
//                v[0] = tfAllVaros.getText();
                irsz = varosToIrsz(tfAllVaros.getText());
                if (irsz.length > 1) {     //ha több bejegyzés is van, akkor van városrész
//                    lek = new SQLMuvelet(7, v);
//                    res = lek.getLekerdezes();

                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        stmt = (PreparedStatement) con.prepareStatement("SELECT vresz FROM helyseg WHERE varos=?");
                        stmt.setString(1, tfAllVaros.getText());
                        res = stmt.executeQuery();
                    } catch (SQLException ex) {
                        new Uzenet("Adatbázis hiba (nem betöltés)", "Hiba", Uzenet.ERROR);
                    }

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
        if (e.getSource() == tfIdeiglVaros) {
            if (tfIdeiglIrsz.getText().isEmpty()) {
                cbIdeigVarosResz.removeItemListener(this);
                cbIdeigVarosResz.removeAllItems();
                cbIdeigVarosResz.setEnabled(false);
                String[] irsz;
//                String[] v = new String[1];
//                v[0] = tfIdeiglVaros.getText();
                irsz = varosToIrsz(tfIdeiglVaros.getText());
                if (irsz.length > 1) {     //ha több bejegyzés is van, akkor van városrész
//                    lek = new SQLMuvelet(7, v);
//                    res = lek.getLekerdezes();

                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        stmt = (PreparedStatement) con.prepareStatement("SELECT vresz FROM helyseg WHERE varos=?");
                        stmt.setString(1, tfIdeiglVaros.getText());
                        res = stmt.executeQuery();
                    } catch (SQLException ex) {
                        new Uzenet("Adatbázis hiba (nem betöltés)", "Hiba", Uzenet.ERROR);
                    }

                    try {
                        while (res.next()) {      //combobox feltöltése a városrészek neveivel
                            cbIdeigVarosResz.addItem(res.getString(1));
                        }
                    } catch (SQLException ev) {
                        ev.printStackTrace();
                    }

                    cbIdeigVarosResz.setEnabled(true);
                    cbIdeigVarosResz.requestFocusInWindow();  //a fókusz a combobox-on marad
                    cbIdeigVarosResz.addItemListener(this);
                } else {
                    tfIdeiglIrsz.setText(irsz[0]);
                    tfIdeiglUtca.requestFocusInWindow();
                }
            }
        }
        if (e.getSource() == tfHozztartVaros) {
            if (tfHozztartIrsz.getText().isEmpty()) {
                cbHozztartVarosResz.removeItemListener(this);
                cbHozztartVarosResz.removeAllItems();
                cbHozztartVarosResz.setEnabled(false);
                String[] irsz;
//                String[] v = new String[1];
//                v[0] = tfHozztartVaros.getText();
                irsz = varosToIrsz(tfHozztartVaros.getText());
                if (irsz.length > 1) {     //ha több bejegyzés is van, akkor van városrész
//                    lek = new SQLMuvelet(7, v);
//                    res = lek.getLekerdezes();

                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        stmt = (PreparedStatement) con.prepareStatement("SELECT vresz FROM helyseg WHERE varos=?");
                        stmt.setString(1, tfHozztartVaros.getText());
                        res = stmt.executeQuery();
                    } catch (SQLException ex) {
                        new Uzenet("Adatbázis hiba (nem betöltés)", "Hiba", Uzenet.ERROR);
                    }

                    try {
                        while (res.next()) {      //combobox feltöltése a városrészek neveivel
                            cbHozztartVarosResz.addItem(res.getString(1));
                        }
                    } catch (SQLException ev) {
                        ev.printStackTrace();
                    }

                    cbHozztartVarosResz.setEnabled(true);
                    cbHozztartVarosResz.requestFocusInWindow();  //a fókusz a combobox-on marad
                    cbHozztartVarosResz.addItemListener(this);

                } else {
                    tfHozztartIrsz.setText(irsz[0]);
                    tfHozztartUtca.requestFocusInWindow();
                }
            }
        }
        if (e.getSource() == tfTorvKepvVaros) {
            if (tfTorvKepvIrsz.getText().isEmpty()) {
                cbTorvKepvVarosResz.removeItemListener(this);
                cbTorvKepvVarosResz.removeAllItems();
                cbTorvKepvVarosResz.setEnabled(false);
                String[] irsz;
//                String v = new String();
//                v = tfTorvKepvVaros.getText();
                irsz = varosToIrsz(tfTorvKepvVaros.getText());
                if (irsz.length > 1) {     //ha több bejegyzés is van, akkor van városrész
//                    lek = new SQLMuvelet(7, v);
//                    res = lek.getLekerdezes();

                    lek = new SQLMuvelet();
                    con = (Connection) lek.getConnection();
                    try {
                        stmt = (PreparedStatement) con.prepareStatement("SELECT vresz FROM helyseg WHERE varos=?");
                        stmt.setString(1, tfTorvKepvVaros.getText());
                        res = stmt.executeQuery();
                    } catch (SQLException ex) {
                        new Uzenet("Adatbázis hiba (nem betöltés)", "Hiba", Uzenet.ERROR);
                    }

                    try {
                        while (res.next()) {      //combobox feltöltése a városrészek neveivel
                            cbTorvKepvVarosResz.addItem(res.getString(1));
                        }
                    } catch (SQLException ev) {
                        ev.printStackTrace();
                    }

                    cbTorvKepvVarosResz.setEnabled(true);
                    cbTorvKepvVarosResz.requestFocusInWindow();  //a fókusz a combobox-on marad
                    cbTorvKepvVarosResz.addItemListener(this);

                } else {
                    tfTorvKepvIrsz.setText(irsz[0]);
                    tfTorvKepvUtca.requestFocusInWindow();
                }
            }
        }
        if (e.getSource() == tfNev) {
            lblNev_cimsor.setText(tfNev.getText());
//            String p = new String;
//            p = tfNev.getText().substring(tfNev.getText().lastIndexOf(32) + 1);
//            SQLMuvelet neme = new SQLMuvelet(36, p);
            ResultSet r = null;

            lek = new SQLMuvelet();
            con = (Connection) lek.getConnection();
            try {
                stmt = (PreparedStatement) con.prepareStatement("SELECT nem FROM nem WHERE nev=?");
                stmt.setString(1, tfNev.getText().substring(tfNev.getText().lastIndexOf(32) + 1));
                r = stmt.executeQuery();
            } catch (SQLException ex) {
                new Uzenet("Adatbázis hiba (nem betöltés)", "Hiba", Uzenet.ERROR);
            }

            try {
                if (r.next()) {
                    String n = r.getString(1);
                    if (n.equals("f")) {
                        rbFerfi.setSelected(true);
                    }
                    if (n.equals("n")) {
                        rbNo.setSelected(true);
                    }
                }
            } catch (SQLException ex) {
                new Uzenet("<html>Hiba az adatbázis művelet végrehajtása közben!<br>Hiba:<br> " + ex.getMessage(), "Hiba", Uzenet.ERROR);
            }
        }
        if (e.getSource() == ftfSzemelyiSzam) {
            // 1-9 számjegyek helyükkel szorozva, összeadva, majd az összeg 11-el osztva
            // ( 1*[1]+2*[2]+...+9*[9])/11
            // a maradék a 10. számjegy (modulo10 checksum)

            if (!ftfSzemelyiSzam.getText().contains("_")) {
                temp = new char[11];
                String szemszam = ftfSzemelyiSzam.getText().substring(0, 1) + ftfSzemelyiSzam.getText().substring(2, 8) + ftfSzemelyiSzam.getText().substring(9);
                sum = 0;
                checksum = 0;
                szemszam.getChars(0, 11, temp, 0);
                for (int i = 0; i < 10; i++) {
                    sum = sum + (((Integer.valueOf(temp[i])) - 48) * (i + 1));
                }
                checksum = sum % 11;
                if (Integer.valueOf(temp[10]) - 48 != checksum) {
                    ftfSzemelyiSzam.setBackground(Color.RED);
                    hibasadat++;
                } else {
                    if (ftfSzemelyiSzam.getBackground() == Color.RED) {
                        ftfSzemelyiSzam.setBackground(Color.WHITE);
                        hibasadat--;
                    }
                }
            } else {
                ftfSzemelyiSzam.setText("");
            }
        }
        if (e.getSource() == ftfAdoSzam) {
            //1996 évi XX. törvény az adószám előállításáról
            //http://index.hu/gazdasag/magyar/adoszam05020/
            //[1] mindig 8 (magánszemély adószáma)
            //[2]-[6] = 1867.01.01-től a születésig eltelt napok száma
            //[10] = modulo10 ellenőrző összeg: 1*[1]+2*[2]+...+9*[9])/11 maradéka
            if (!ftfAdoSzam.getText().contains("_")) {
                temp = new char[10];
                sum = 0;
                checksum = 0;
                ftfAdoSzam.getText().getChars(0, 10, temp, 0);
                if (temp[0] == 56) {    //az első számjegy 8-as
                    for (int i = 0; i < 9; i++) {
                        sum = sum + ((temp[i] - 48) * (i + 1));
                    }
                    checksum = sum % 11;
                    if (temp[9] - 48 != checksum) {
                        ftfAdoSzam.setBackground(Color.RED);
                        hibasadat++;
                    } else {
                        if (ftfAdoSzam.getBackground() == Color.RED) {
                            ftfAdoSzam.setBackground(Color.WHITE);
                            hibasadat--;
                        }
                    }
                } else {
                    ftfAdoSzam.setBackground(Color.RED);
                    hibasadat++;

                    new Uzenet("Magánszemély adószáma 8-assal kezdődik!", "HIBA!", Uzenet.ERROR);
                }
            } else {
                ftfAdoSzam.setText("");
            }
        }
        if (e.getSource() == ftfTAJ) {
            //{[([1]+[3]+[5]+[7])*3]+[([2]+[4]+[6]+[8])*7]}%10=[9]
            if (!ftfTAJ.getText().contains("_")) {
                String s = ftfTAJ.getText();
                temp = new char[9];
                int j = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) != 32) {
                        temp[j] = s.charAt(i);
                        j++;
                    }
                }
                checksum = ((((temp[0] + temp[2] + temp[4] + temp[6]) - 192) * 3) + (((temp[1] + temp[3] + temp[5] + temp[7]) - 192) * 7)) % 10;
                if (temp[8] - 48 != checksum) {
                    ftfTAJ.setBackground(Color.RED);
                    hibasadat++;
                } else {
                    if (ftfTAJ.getBackground() == Color.RED) {
                        ftfTAJ.setBackground(Color.WHITE);
                        hibasadat--;
                    }
                }
            } else {
                ftfTAJ.setText("");
            }

        }
        if (e.getSource() == tfJovedelem) {
            int napokszama=Integer.parseInt(Config.settings.getProperty("dij_napok_szama").toString());
            Double szazalek=Double.parseDouble(Config.settings.getProperty("dij_jov_max_terheles").toString())/100;
            Double jovedelem = new Double(tfJovedelem.getText().isEmpty() ? "0" : tfJovedelem.getText());

            Double dij = (jovedelem / napokszama)*szazalek;
            Double maxdij = Double.parseDouble(Config.settings.getProperty("max_ter_dij"));
            if (dij > maxdij) {
                dij = maxdij;
            }
            tfEtkDij.setText(dij.toString().substring(0, dij.toString().lastIndexOf(".") + 2));
            lblKategoria.setText(getKategoria());
        }


    }
}
