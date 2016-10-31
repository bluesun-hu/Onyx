/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onyx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;

/**
 *
 * @author PIRI
 * v1.1
 * javítva: ő,ű betű kezelés
 *
 * hozzáadva:   verziószám
 */
public class SQLMuvelet {

    private int index;
    private String[] parameters;
    private Connection con;
    public static final String version = "1.1";

    public SQLMuvelet(int idx, String[] param) {
        index = idx;
        parameters = param;
    }

    public SQLMuvelet(int idx) {
        index = idx;
    }

    public SQLMuvelet() {
    }

    public Connection getConnection() {
        con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(Globals.ADATBAZIS + "?user=" + Globals.DBUSER + "&password=" + Globals.DBPASS + "&useUnicode=true&characterEncoding=utf8&characterSetResults=utf8");
            PreparedStatement pstmt = con.prepareStatement("SET NAMES 'utf8'");
            pstmt.execute();
        } catch (ClassNotFoundException c) {
            new Uzenet("Adatbázis meghajtó hiba (Class not found)", "Hiba", Uzenet.ERROR);
        } catch (SQLException e) {
            new Uzenet("Adatbázis kapcsolódási hiba", "Hiba", Uzenet.ERROR);
        }
        return con;
    }

    public ResultSet getLekerdezes() {
        con = null;
        ResultSet eredmeny;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            con = DriverManager.getConnection(Globals.ADATBAZIS + "?user=" + Globals.DBUSER + "&password=" + Globals.DBPASS + "&useUnicode=true&characterEncoding=utf8&characterSetResults=utf8");

            PreparedStatement pstmt = con.prepareStatement("SET NAMES 'utf8'");
            pstmt.execute();
            pstmt = con.prepareStatement(Globals.getSQLParancs(index));
            switch (index) {
                case 0:     //login lekérdezés
                    pstmt.setString(1, parameters[0]);
                    break;
                case 1:     //ugyfélnévsor
                    break;
                case 2:     //dolgozói névsor

                case 3:     //management lista

                    break;
                case 4:     //irsz->varos
                    pstmt.setString(1, parameters[0]);
                    break;
                case 5:     //város -> irsz
                    pstmt.setString(1, parameters[0]);
                    break;
                case 6:     //vresz -> irsz
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;
                case 7:     //városrész
                    pstmt.setString(1, parameters[0]);
                    break;

                case 9:     //ID lekérdezés első mentés után
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setString(4, parameters[3]);
                    break;
                case 15:    //születési adatok lekérdezése
                case 16:    //címadatok lekérdezése
                case 17:    //étkezési adatok lekérdezése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    break;

                case 23:    //étkezési lista lekérdezése
                    pstmt.setString(1, parameters[0]);
//            pstmt.setString(2, parameters[0]);
                    break;
                case 29:    //melegedő lista lekérdezése
                    pstmt.setString(1, parameters[0]);
//            pstmt.setString(2, parameters[0]);
                    break;
                case 18:    //étkezési névsor lekérdezése
                case 30:    //melegedő szolgáltatások lekérdezése
                    pstmt.setString(1, parameters[0]);
                    break;
                case 21:    //jelszó ellenőrzés
                case 33:    //szolgáltatás bejegyzés ellenőrző lekérdezés
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    break;
                case 24:    //fogyasztás lekérdezése az adott hónapra
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;
                case 27:    //fogyasztás bejegyzés ellenőrző lekérdezés
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    break;
                case 35:    //cím ellenőrzése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[7]);  //jelleg
                    break;
                case 36:    //utónév-nem páros lekérdezése
                    pstmt.setString(1, parameters[0]);
                    break;


                default:
                    if (Main.DEBUG) {
                        JOptionPane.showMessageDialog(null, "Nincs elég SQL mondat!\n index:" + index, "Hiba", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
            }
            eredmeny = pstmt.executeQuery();
            return eredmeny;
        } catch (Exception e) {
            System.out.println(index);
            e.printStackTrace();

        }
        return null;
    }

    public boolean setUpdate() {
        con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
//    con=DriverManager.getConnection(Globals.ADATBAZIS,Globals.DBUSER,Globals.DBPASS);
            con = DriverManager.getConnection(Globals.ADATBAZIS + "?user=" + Globals.DBUSER + "&password=" + Globals.DBPASS + "&useUnicode=true&characterEncoding=utf8&characterSetResults=utf8");
            PreparedStatement pstmt = con.prepareStatement("SET NAMES 'utf8'");
            pstmt.execute();
            pstmt = con.prepareStatement(Globals.getSQLParancs(index));
            switch (index) {
                case 8:     //új név mentése
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setString(4, parameters[3]);
                    break;
                case 10:       //személyes adatok mentése
                    pstmt.setString(1, parameters[1]);//név
                    pstmt.setString(2, parameters[2]);//iktsz
                    pstmt.setString(3, parameters[3]);//szül név
                    pstmt.setString(4, parameters[4]);//szül hely
                    if (parameters[5].equals(Globals.URES_DATUM)) {
                        pstmt.setNull(5, java.sql.Types.DATE);
                    } else {
                        pstmt.setString(5, parameters[5]);//szül dátum
                    }
                    pstmt.setString(6, parameters[6]);//anya név
                    pstmt.setString(7, parameters[7]);//állpolg
                    pstmt.setString(8, parameters[8]);//neme
                    pstmt.setString(9, parameters[9]);//tel otthon
                    pstmt.setString(10, parameters[10]);//tel munka
                    pstmt.setString(11, parameters[11]);//tel mobil
                    pstmt.setString(12, parameters[12]);//email
                    pstmt.setString(13, parameters[13]);//törv képv név
                    pstmt.setString(14, parameters[14]);//hozz tart név
                    pstmt.setString(15, parameters[15]);//adószám
                    pstmt.setString(16, parameters[16]);//TAJ
                    pstmt.setString(17, parameters[17]);//szem szám
                    pstmt.setString(18, parameters[18]);//szig szám
                    if (parameters[19].equals(Globals.URES_DATUM)) {
                        pstmt.setNull(19, java.sql.Types.DATE);
                    } else {
                        pstmt.setString(19, parameters[19]);//beadás dátum
                    }
                    if (parameters[20].equals(Globals.URES_DATUM)) {
                        pstmt.setNull(20, java.sql.Types.DATE);
                    } else {
                        pstmt.setString(20, parameters[20]);//értesítés dátum
                    }
                    if (parameters[21].equals(Globals.URES_DATUM)) {
                        pstmt.setNull(21, java.sql.Types.DATE);
                    } else {
                        pstmt.setString(21, parameters[21]);//megszűnés dátum
                    }
                    pstmt.setString(22, parameters[22]);//cselekvőképesség
                    pstmt.setString(23, parameters[23]);//megjegyzés
                    pstmt.setString(24, parameters[24]);//étkezés
                    pstmt.setString(25, parameters[25]);//melegedő
                    pstmt.setString(26, parameters[26]);//rögzítés ideje
                    pstmt.setString(27, parameters[27]);//rögzítő ID-je
                    if (parameters[28].equals(Globals.URES_DATUM)) {
                        pstmt.setNull(28, java.sql.Types.DATE);
                    } else {
                        pstmt.setString(28, parameters[28]);//melegedő felvétel dátuma
                    }
                    pstmt.setString(29, parameters[29]);//nyugdíjas törzsszám
                    pstmt.setString(30, parameters[30]);//jövedelem
                    pstmt.setString(31, parameters[31]);//étkezés díja
                    pstmt.setString(32, parameters[32]);//kiszolgálás módja
                    pstmt.setString(33, parameters[33]);//éjjeli menedék
                    pstmt.setString(34, parameters[34]);//szállítási cím tipusa
                    pstmt.setString(35, parameters[35]);//átmeneti szálló
                    pstmt.setString(36, parameters[36]);//átmeneti szálló felvétel dátuma
                    pstmt.setInt(37, Integer.valueOf(parameters[0]));//
                    break;
                case 11:    //cím mentése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setString(4, parameters[3]);
                    pstmt.setString(5, parameters[4]);
                    pstmt.setString(6, parameters[5]);
                    pstmt.setString(7, parameters[6]);
                    pstmt.setString(8, parameters[7]);
                    break;

                case 12:    //étkezési adatok mentése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setString(4, parameters[3]);
                    pstmt.setString(5, parameters[4]);
                    break;

                case 13:    //beosztási adatok mentése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setString(4, parameters[3]);
                    pstmt.setString(5, parameters[4]);
                    pstmt.setString(6, parameters[5]);
                    pstmt.setString(7, parameters[6]);
                    pstmt.setString(8, parameters[7]);
                    pstmt.setString(9, parameters[8]);
                    pstmt.setString(10, parameters[9]);
                    pstmt.setString(11, parameters[10]);
                    pstmt.setString(12, parameters[11]);
                    pstmt.setString(13, parameters[12]);
                    pstmt.setString(14, parameters[13]);
                    pstmt.setString(15, parameters[14]);
                    pstmt.setString(16, parameters[15]);
                    break;
                case 14:    //cím adatok frissítése
                    pstmt.setString(1, parameters[1]);
                    pstmt.setString(2, parameters[2]);
                    pstmt.setString(3, parameters[3]);
                    pstmt.setString(4, parameters[4]);
                    pstmt.setString(5, parameters[5]);
                    pstmt.setString(6, parameters[6]);
                    pstmt.setInt(7, Integer.valueOf(parameters[0]));
                    pstmt.setString(8, parameters[7]);
                    break;

                case 19:    //törlés a névsorból
                    pstmt.setString(1, parameters[0]);
                    pstmt.setInt(2, Integer.valueOf(parameters[1]));
                    break;
                case 20:    //login frissítése
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    pstmt.setInt(3, Integer.valueOf(parameters[2]));
                    pstmt.setInt(4, Integer.valueOf(parameters[3]));
                    break;
                case 22:    //új jelszó mentése
                    pstmt.setString(1, parameters[1]);
                    pstmt.setInt(2, Integer.valueOf(parameters[0]));
                    break;
                case 25:    //új fogyasztás mentése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setString(3, parameters[2]);
                    pstmt.setInt(4, Integer.valueOf(parameters[3]));
                    pstmt.setString(5, Globals.getDatumIdoMost());
                    pstmt.setInt(6, Globals.getUserID());
                    break;
                case 26:    //fogyasztás frissítése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, Globals.getDatumIdoMost());
                    pstmt.setInt(3, Globals.getUserID());
                    pstmt.setInt(4, Integer.valueOf(parameters[1]));
                    pstmt.setString(5, parameters[2]);
                    pstmt.setString(6, parameters[3]);
                    break;
                case 28:    //hónap lezárása (étkeztetés)
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;
                case 31:    //uj szolgáltatás igénybevétel rögzítése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, parameters[1]);
                    pstmt.setInt(3, Integer.valueOf(parameters[2]));
                    pstmt.setString(4, Globals.getDatumIdoMost());
                    pstmt.setInt(5, Globals.getUserID());
                    break;
                case 32:    //szolgáltatás igénybevétel frissítése
                    pstmt.setInt(1, Integer.valueOf(parameters[0]));
                    pstmt.setString(2, Globals.getDatumIdoMost());
                    pstmt.setInt(3, Globals.getUserID());
                    pstmt.setInt(4, Integer.valueOf(parameters[1]));
                    pstmt.setString(5, parameters[2]);
                    break;
                case 34:    //hónap lezárása (melegedő)
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;
                case 37:    //
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;
                case 38:    //log bejegyzés
                    pstmt.setString(1, parameters[0]);
                    pstmt.setString(2, parameters[1]);
                    break;

                default:
                    if (Main.DEBUG) {
                        JOptionPane.showMessageDialog(null, "Nincs elég SQL mondat!\n index:" + index, "Hiba", JOptionPane.ERROR_MESSAGE);
                    }
            }
            if (pstmt.executeUpdate() != 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.out.println(index);
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void dbExport() {
        String path = Config.settings.getProperty("db_backup_path");
        Calendar cal = Calendar.getInstance();
        DecimalFormat dcf = new DecimalFormat("00");
        String fname = path + "\\" + cal.get(Calendar.YEAR) + "-" +
                dcf.format(cal.get(Calendar.MONTH) + 1) + "-" +
                dcf.format(cal.get(Calendar.DATE)) + ".sql";
        String dumpCommand = "mysqldump -u" + Config.settings.getProperty("dbuser") +
                " -p" + Config.settings.getProperty("dbpass") + " " +
                Config.settings.getProperty("dbname") + " -r \"" + fname+"\"\nexit";

        File tempfile = new File(Globals.onyxpath+ "tempdump.bat");

        try {
            Writer out = new BufferedWriter(new FileWriter(tempfile));
            out.write(dumpCommand);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] command={"cmd.exe","/c","start","/min",tempfile.getAbsolutePath()};
        try {
            Runtime p = Runtime.getRuntime();
            Process pr=p.exec(command);
            pr.waitFor();
            new Uzenet("Az adatbázis biztonsági mentése megtörtént!", "Mentés", Uzenet.INFORMATION);
            tempfile.delete();

        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
}
