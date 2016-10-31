/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onyx;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author PIRI
 */
public abstract class Globals {

    private static int userid;
    private static String username;
    private static int jogok;
    private static String[] sqlparancs;
    public static int JOG_REGISZTRACIO = 1;
    public static int JOG_ETKEZTETES = 2;
    public static int JOG_MELEGEDO = 4;
    public static int JOG_DOLGOZO = 8;
    public static int JOG_MANAGEMENT = 16;
    public static int JOG_FONOK = 32;
    public static int ETKEZTETES=0;
    public static int MELEGEDO=1;
    public static int[] OSZLOPMERET_UGYFEL = {70, 220, 70,50,50,50,50};
    public static int[] OSZLOPMERET_DOLGOZO = {170, 70, 120};
    public static int[] OSZLOPMERET_MANAGEMENT = {35, 155, 75, 75, 80, 80, 75, 75, 65, 90, 80};
    public static int[] OSZLOPMERET_MELEGEDO = {70, 155, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 0, 0};
    public static int[] OSZLOPMERET_ETKEZTETES = {70, 28, 150, 20, 20, 20, 20, 20, 20, 20, 20, 20, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25};
    public static int[] OSZLOPMERET_UNNEPNAP = {40,70,35};
    public static int[] HONAP_HOSSZ = {31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static float[] ETK_NAPLO_TABLA_OSZLOPMERET={14.75f, 2.25f, 2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f,2.25f};
    public static float[] ETK_NAPLO_TABLA_OSZLOPMERET_1={14.85f, 2.65f, 2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f,2.65f, 3.00f};
    public static float[] MELEGEDO_NAPLO_TABLA_OSZLOPMERET={5.0f,9.0f,26.0f,5.5f,3.0f,3.0f,3.0f,3.0f,3.0f,3.8f,3.8f,3.8f,3.8f,3.8f,3.8f,3.8f,3.8f,14.5f};
    public static boolean UJ = true;
    public static boolean UGYFEL = false;
    public static boolean DOLGOZO = true;
    public static boolean ENTERPRESSED = false;
    public static boolean MENTVE=true;
    public static boolean ELSO_INDITAS=false;
    public static String ADATBAZIS;
    public static String DBUSER;
    public static String DBPASS;
    public static String configpath;
    public static String DEFAULT_CONFIG_PATH = "C:\\Onyx\\";
    public static String DBUGYFEL = "u";
    public static String DBDOLGOZO = "d";
    public static String URES_DATUM = "1900-01-01";
    public static final String onyxpath="C:\\Onyx\\";
    public static final String[] HONAPOK = {"","Január", "Február", "Március", "Április", "Május", "Június", "Július", "Augusztus", "Szeptember", "Október", "November", "December"};
    public static final String[] NAPOK = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    public static final String[] FEJLEC_DOLGOZO = {"Név", "Szül.idő", "Beosztás"};
    public static final String[] FEJLEC_UGYFEL = {"Iktsz.", "Név", "Szül.idő","É","NM","ÉM","ÁSZ"};
    public static final String[] FEJLEC_MANAGEMENT = {"ID", "Név", "Szül. idő", "Felh. név", "Jelszó", "Regisztráció", "Étkeztetés", "Melegedő", "Dolgozók", "Management", "Főnök"};
    public static final String[] FEJLEC_MELEGEDO = {"<html><br>Iktsz.", "<html><br>Név", "Tisztálkodás", "<html>Mosás-<br>Szárítás", "<html>Étel-<br>melegítés", "<html>Szoc.<br>ügyintézés", "<html>Szociális-<br>mentális", "Étkeztetés", "Ruhapótlás", "Postacím", "<html>Csomag-<br>megőrzés", "<html>Sz.idős <br>program", "<html>Eü.<br>ellátás"};
    public static final String[] FEJLEC_ETKEZTETES = {"Iktsz.", "Tip", "Név", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    public static final String[] FEJLEC_UNNEPNAP={"Év","Hónap","Nap"};
    public static final String[] KOZTERULET = {"utca", "út", "tér", "körút", "köz", "sugárút", "tanya",""};
    public static String[] ETKEZES_IGBV_TITLE ={ "4. sz. melléklet az 1/2000.(I.7.)SzCsM rendelethez","Étkeztetésre vonatkozó igénybevételi napló"};
    public static String[] ETKEZES_IGBV_TABLEHEADER = {"Ellátott neve:", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "Ö.:"};
    public static String[] MELEGEDO_ESEMENY_TITLE = {"11. sz. melléklet az 1/2000.(I.7.)SzCsM rendelethez","Nappali melegedők eseménynaplója az ellátásban részesítettekről és a nyújtott szolgáltatásokról","(sorszámozott és hitelesített)"};
    public static String[] MELEGEDO_ESEMENY_TABLEHEADER1 = {"Az ellátott adatai", "Események, szolgáltatások"};
    public static String[] MELEGEDO_ESEMENY_TABLEHEADER2 = {"Sorsz.", "Dátum", "Név", "Szül. év", "Tisztálkodás", "Mosás, szárítás", "Ételmelegítés", "Szociális ügyintézés", "Szociális, mentális", "Étkeztetés", "Ruhapótlás", "Postacím adása", "Csomagmegőrzés", "Szabadidős programok", "Eü. ellátás","","","Egyéb"};
    public static String[]NAPLO_HEADER={""};
    private static String userloginname;
    public static String intezmeny;


    private Globals(){}

    static private Globals _instance;
    //TODO átalakítani nem abstract-ra!!
    public Globals getInstance(){
        if(_instance==null)
            _instance=new Globals(){};

        return _instance;
    }



    public static void setUser(int userid, String username, String userloginname, int jogok) {
        Globals.userid = userid;
        Globals.username = username;
        Globals.jogok = jogok;
        Globals.userloginname=userloginname;
    }

    public static int getJogok() {
        return jogok;
    }

    public static int getUserID() {
        return userid;
    }
    public static String getUserName(){
        return username;
    }
    public static String getUserLoginName(){
        return userloginname;
    }

    public static void setSQLParancsok(String[] sql) {
        sqlparancs = sql;
    }

    public static String getSQLParancs(int idx) {
        return sqlparancs[idx];
    }

    public static void setAdatbazis(String dburl, String user, String pass) {
        ADATBAZIS = dburl;
        DBUSER = user;
        DBPASS = pass;
    }

    public static boolean checkJogosultsag(int jog) {
        //jog= az ellenőrizni kívánt jog, összevetésre kerül a jelenlegi felhasználó jogaival
        //ha megfelelő, true-val tér vissza
        return ((Globals.jogok & jog) == jog);
    }

    public static String getDatumIdoMost() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dat = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
        return dat.format(cal.getTime());

    }

    public static String getDatumMa() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dat = new SimpleDateFormat("yyyy-MM-dd");
        return dat.format(cal.getTime());
    }
}
