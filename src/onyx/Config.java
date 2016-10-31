/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onyx;

import java.io.*;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author PIRI
 * v1.1
 * hozzáadva:   settings Property object
 *              config file beolvasás
 *              verziószám
 *
 */
public class Config {

    private String filenev;
    private String[] sql_parancsok;
    private String[] conf;  //ebbe kerülnek a config fileban tárolt adatbázis adatok
    private boolean validcfg = true;
    public static Properties settings=null;
    public static Properties ini;
    public static final String version="1.1";
    

    public Config() {
        getsettings();
        Globals.setAdatbazis("jdbc:mysql://" + settings.getProperty("dbpath")+":"+settings.getProperty("dbport") + "/" +
                settings.getProperty("dbname"), settings.getProperty("dbuser"), settings.getProperty("dbpass"));
        setSQL();
        Globals.setSQLParancsok(sql_parancsok);


        
//        if (Globals.configpath == null) {
//        //alapértelmezett filenév beállítása, ha nincs megadva
//        filenev = Globals.DEFAULT_CONFIG_PATH + "onyx.cfg";
//        if(ReadConf(filenev)){
//        Globals.setAdatbazis("jdbc:mysql://" + conf[0] + "/" + conf[1], conf[2], conf[3]);
//        setSQL();
//        Globals.setSQLParancsok(sql_parancsok);
//        }
//        } else {
//        filenev = Globals.configpath + "onyx.cfg";
//        if (ReadConf(filenev)) {
//        Globals.setAdatbazis("jdbc:mysql://" + conf[0] + "/" + conf[1], conf[2], conf[3]);
//        setSQL();
//        Globals.setSQLParancsok(sql_parancsok);
//        }
//        }
        
    }

    private boolean ReadConf(String filenev) {  //beolvassa a config.cfg fileból a DB elérési adatait

        if (new File(filenev).exists()) { //ellenőrzi, hogy megvan-e

            conf = new String[4]; //jelenleg 4 soros a file

            try {
                BufferedReader in = new BufferedReader(new FileReader(filenev));
                for (int i = 0; i < 4; i++) {
                    conf[i] = in.readLine();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            validcfg = true;
        } else {
            JOptionPane.showMessageDialog(null, "A config file nem található", "Hiba", JOptionPane.ERROR_MESSAGE);
            return false;
        // hibaüzenet, ha nincs meg a file
        }
        return true;
    }

    private void getsettings() {
        FileInputStream in=null;
        try {
            ini=new Properties();
            FileInputStream iniload=new FileInputStream("onyx.ini");
            try{
                ini.load(iniload);
            }catch(FileNotFoundException ex){
                new Uzenet("Az ini file nem található!", "Hiba!", Uzenet.ERROR);
                ex.printStackTrace();
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                try{
                iniload.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }


            settings = new Properties();
            if(Globals.intezmeny.equals("KE")){
            in = new FileInputStream(ini.getProperty("configpath"));//Kecskemét
            }
            if(Globals.intezmeny.equals("MA")){
            in = new FileInputStream(ini.getProperty("configpath_ma"));//Madaras
            }
            if(Globals.intezmeny.equals("BA")){
            in = new FileInputStream(ini.getProperty("configpath_ba"));//Bacsalmas
            }
            try {
                settings.load(in);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    private void setSQL(){
        sql_parancsok=new String[39];
//login
        sql_parancsok[0]="SELECT ID, rights, nev, pw FROM ugyfel WHERE login_name=?";
//ügyfélnévsor
        sql_parancsok[1]="SELECT ID, nev, iktsz, felvetel, szul_ido FROM ugyfel WHERE statusz='u' AND deleted=0";
//dolgozoi névsor
        sql_parancsok[2]="SELECT u.ID, nev, szul_ido, beosztas FROM ugyfel AS u LEFT JOIN beosztasi_adat AS b ON u.ID=b.ID WHERE statusz='d' AND deleted=0";
//management lista
        sql_parancsok[3]="SELECT ID, nev, szul_ido, login_name, pw, rights FROM ugyfel WHERE statusz='d' AND deleted=0";
//irányítószám -> város
        sql_parancsok[4]="SELECT varos, vresz FROM helyseg WHERE irsz=?";
//város -> irányítószám
        sql_parancsok[5]="SELECT 	irsz FROM helyseg WHERE varos=?";
//város, városrész -> irányítószám
        sql_parancsok[6]="SELECT irsz FROM helyseg WHERE varos=? AND vresz=?";
//városrész
        sql_parancsok[7]="SELECT vresz FROM helyseg WHERE varos=?";
//új név mentése
        sql_parancsok[8]="INSERT INTO ugyfel (iktsz, nev, statusz, felvetel) VALUES (?,?,?,?)";
//ID lekérdezése első mentés után
        sql_parancsok[9]="SELECT ID FROM ugyfel WHERE iktsz=? AND nev=? AND statusz=? AND felvetel=?";
//személyes adatok mentése
        sql_parancsok[10]="UPDATE ugyfel SET nev=?, iktsz=?, szul_nev=?, szul_hely=?, szul_ido=?, anya_nev=?, alpolgsag=?, neme=?, tel_otthon=?, tel_munka=?, tel_mobil=?, email=?, tk_nev=?, h_nev=?, adoszam=?, taj=?, szem_szam=?, szig_szam=?, beadas=?, ertesites=?, megszunes=?, cselekvokepesseg=?, megjegyzes=?, etkezes=?, melegedo=?, rogzitve=?, rogz_id=?, melegedo_felvetel=?, nyugdij_tszam=?, jovedelem=?, dij=?, kiszolgalas=?, ejjeli=?, szallitasi_cim=?, atmeneti=?, atmeneti_felvetel=? WHERE ID=?";
//cím mentése
        sql_parancsok[11]="INSERT INTO cim (ID, irsz, varos, varosresz, utca, kozterulet, hazszam, jelleg) VALUES (?,?,?,?,?,?,?,?)";
//étkezési adatok mentése(egyéb fül)
        sql_parancsok[12]="INSERT INTO etkezesi_adat (ID, dij, tipus, jovedelem, idoszak) VALUES (?,?,?,?,?)";
//beosztási adatok mentése
        sql_parancsok[13]="REPLACE INTO beosztasi_adat (ID, beosztas, alk_minoseg, besorolas, br_ber, heti_ora, feor, mnyp_belepes, onyp_belepes, onyp_dij, munkaszerzodes, munkakori_leiras, cv, bizonyitvany, alkalmassag, reg_kartya_szam) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//cím frissítése
        sql_parancsok[14]="UPDATE cim SET irsz=?, varos=?, varosresz=?, utca=?, kozterulet=?, hazszam=? WHERE ID=? AND jelleg=?";
//személyes adatok lekérdezése
        sql_parancsok[15]="SELECT nev, iktsz,felvetel, szul_nev, szul_hely, szul_ido, anya_nev, alpolgsag, neme, tel_otthon, tel_munka, tel_mobil, email, tk_nev, h_nev, adoszam, taj, szem_szam, szig_szam, beadas, ertesites, megszunes, cselekvokepesseg, megjegyzes, etkezes, melegedo, melegedo_felvetel, nyugdij_tszam, jovedelem, dij, kiszolgalas, ejjeli, szallitasi_cim, atmeneti, atmeneti_felvetel FROM ugyfel WHERE ID=?";
//cím adatok lekérdezése
        sql_parancsok[16]="SELECT irsz, varos, varosresz, utca, kozterulet, hazszam, jelleg FROM cim WHERE ID=?";
//étkezési adatok lekérdezése
        sql_parancsok[17]="SELECT dij, tipus, jovedelem FROM etkezesi_adat WHERE ID=? AND (idoszak=(SELECT MAX(idoszak) FROM etkezesi_adat))";
//beosztási adatok lekérdezése
        sql_parancsok[18]="SELECT beosztas, alk_minoseg, besorolas, br_ber, heti_ora, feor, mnyp_belepes, onyp_belepes, onyp_dij, munkaszerzodes, munkakori_leiras, cv, bizonyitvany, alkalmassag, reg_kartya_szam FROM beosztasi_adat WHERE ID=?";
//név törlése a névsorból
        sql_parancsok[19]="UPDATE ugyfel SET deleted=1, del_date=? WHERE ID=?";
//login adatok frissítése
        sql_parancsok[20]="UPDATE ugyfel SET login_name=?, pw=?, rights=? WHERE ID=?";
//jelszó lekérdezés
        sql_parancsok[21]="SELECT * FROM ugyfel WHERE ID=? AND pw=?";
//új jelszó mentése
        sql_parancsok[22]="UPDATE ugyfel SET pw=? WHERE ID=?";
//étkeztetés lista lekérdezése
        sql_parancsok[23]="SELECT ID, iktsz, kiszolgalas, nev  FROM ugyfel WHERE etkezes=1 AND statusz='u' AND (deleted=0 OR del_date>?)";
//fogyasztás lekérdezése az adott hónapra
        sql_parancsok[24]="SELECT ID, fogyasztas, lezarva FROM etkeztetes WHERE ev=? AND ho=?";
//új fogyasztás mentése
        sql_parancsok[25]="INSERT INTO etkeztetes (ID, ev, ho, fogyasztas, rogzitve, rogz_id) VALUES (?,?,?,?,?,?)";
//fogyasztás frissítése
        sql_parancsok[26]="UPDATE etkeztetes SET fogyasztas=?, rogzitve=?, rogz_id=? WHERE ID=? AND ev=? AND ho=?";
//fogyasztás bejegyzés ellenőrző lekérdezés
        sql_parancsok[27]="SELECT fogyasztas FROM etkeztetes WHERE ID=? AND ev=? AND ho=?";
//hónap lezárása (étkeztetés)
        sql_parancsok[28]="UPDATE etkeztetes SET lezarva=1 WHERE ev=? AND ho=?";
//melegedő lista lekérdezése
        sql_parancsok[29]="SELECT ID, iktsz, nev FROM ugyfel WHERE melegedo=1 AND statusz='u' AND (deleted=0 OR del_date>?)";
//szolgáltatások lekérdezése az adott napra
        sql_parancsok[30]="SELECT ID, szolg, lezarva FROM melegedo WHERE datum=?";
//uj szolgáltatás igénybevétel beszúrása
        sql_parancsok[31]="INSERT INTO melegedo (ID, datum, szolg, rogzitve, rogz_id) VALUES (?, ?, ?, ?, ?)";
//szolgáltatás igénybevétel frissítése
        sql_parancsok[32]="UPDATE melegedo SET szolg=?, rogzitve=?, rogz_id=? WHERE ID=? AND datum=?";
//szolgáltatás bejegyzés ellenőrző lekérdezés
        sql_parancsok[33]="SELECT szolg FROM melegedo WHERE ID=? AND datum=?";
//hónap lezárása (melegedő)
        sql_parancsok[34]="UPDATE melegedo SET lezarva=1 WHERE datum>=? AND datum<=?";
//cím ellenőrzése
        sql_parancsok[35]="SELECT * FROM cim WHERE ID=? AND jelleg=?";
//settings betöltése
        sql_parancsok[36]="SELECT nem FROM nem WHERE nev=?";
//settings mentése
        sql_parancsok[37]="REPLACE INTO nem SET nev=?, nem=?";
//log bejegyzés
        sql_parancsok[38]="INSERT INTO log SET esemeny=?, id=?, ido=NOW()";
    }
    public boolean validConfig(){
        return validcfg;
    }
}
