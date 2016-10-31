/*
 * FoAblak_2.java
 *
 * Created on 2009. március 15., 14:57
 */
package onyx;

import com.lowagie.tools.Executable;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author  PIRI
 */
public class FoAblak extends javax.swing.JFrame implements ActionListener, ComponentListener {

    /** Creates new form FoAblak_2 */
    public FoAblak() {

        initComponents();
        Boolean l;
        do {
            l = getLogin();
            if (l == null) {
                System.exit(0);
            }
            if (!l) {
                log.writeLog("Sikertelen bejelentkezés. Felhasználónév: " + username);
            }
        } while (!l);
        setTitle("Onyx - " + Globals.getUserName());
        log.writeLog("Bejelentkezve: " + username);
        beallit();
        updateDataBase();
    }

    private Boolean getLogin() {
        if (Main.DEBUG) {
            //DEBUG mód, teljes jogosultság, beléptetés nélkül működik minden
            Globals.setUser(-1, "DEBUG", "DEBUG", 63);
            mnTeszt.setVisible(true);
            return true;
        }
        if (Globals.ELSO_INDITAS) {
            Globals.setUser(-1, "ELSŐ INDÍTÁS", "elso inditas", 24);
            return true;
        }
        lg = new Login();    //loginablak megjelenítése


        lg.addAncestorListener(new AncestorListener() {     //a fókusz beállításához szükséges

            public void ancestorAdded(AncestorEvent event) {
                lg.setFocus();      //ha megjelent a dialog, a felhasználónév megkapja a fókuszt
            }

            public void ancestorRemoved(AncestorEvent event) {
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        });


        int showConfirmDialog = JOptionPane.showConfirmDialog(null, lg, "Bejelentkezés", JOptionPane.OK_CANCEL_OPTION); //Dialog-ba ágyazva
        if (showConfirmDialog == JOptionPane.CANCEL_OPTION || showConfirmDialog == JOptionPane.CLOSED_OPTION) {
            return null;    //bezárták az ablakot v. cancelt nyomtak
        }
        if (showConfirmDialog == JOptionPane.OK_OPTION) {   //OK gomb
            if (lg.getUserName().equals("") || lg.getUserName().equals(" ") || lg.getUserName() == null) {  //üres a felhasználónév
                JOptionPane.showMessageDialog(this, "A felhasználónév nem lehet üres!", "Hiba!", JOptionPane.ERROR_MESSAGE);    //hibaüzenet
                return false;   //nem jó a login
            }
            String[] param = new String[1];   //login info begyüjtése egyelemű tömbbe
            param[0] = username = lg.getUserName();      //felhasználónév
            if (!username.equals("DEBUG")) {
                //            System.out.println(param[0]);
                lek = new SQLMuvelet(0, param);    //a 0. SQL mondat meghívása a felh.név paraméterrel
                res = lek.getLekerdezes();  //lekérdezés végrehajtása
                try {
                    if (res == null) { //nincs a megadott felh. az adatbázisban, hibaüzenet
                        JOptionPane.showMessageDialog(this, "Nincs ilyen felhasználó!", "Hiba!", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    while (res.next()) {
                        if (!(res.getString("pw").equals(lg.getPassword()))) {   //a tárolt jelszó és a megadott nem egyezik, hibaüzenet
                            JOptionPane.showMessageDialog(this, "Hibás jelszó!", "Hiba!", JOptionPane.ERROR_MESSAGE);
                            return false;   //nem jó a login
                        }
                        Globals.setUser(res.getInt("ID"), res.getString("nev"), lg.getUserName(), res.getInt("rights"));    //minden passzol, felhasználó adatainak és jogainak beállítása
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                if ("onyx".equals(lg.getPassword())) {
                    Globals.setUser(-1, "DEBUG", "DEBUG", 63);
                } else {
                    new Uzenet("Rossz jelszó! A program kilép!", "DEBUG", Uzenet.ERROR);
                    System.exit(0);
                }

            }
            return true;    //jó a login
        }
        return false;   //rossz a login
    }

    private void showNevsor(boolean tipus) {
        //névsor táblázat megmutatása
        //tipus: false= ügyfél, true= dolgozó
        cleanUpWorkarea();
        ugyfelnevsor = new Nevsor(tipus);
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(ugyfelnevsor);
        ugyfelnevsor.setOpaque(true);
        pnlWorkArea.validate();
    }

    private void beallit() {
        //jogosultságok szerint engedélyezi a megfelelő menüpontokat
        int jog = Globals.getJogok();
        if ((jog & 1) == 1) {
            mnRegisztracio.setEnabled(true);
        }
        if ((jog & 2) == 2) {
            mnEtkeztetes.setEnabled(true);
        }
        if ((jog & 4) == 4) {
            mnMelegedo.setEnabled(true);
        }
        if ((jog & 8) == 8) {
            mnDolgozok.setEnabled(true);
        }
        if ((jog & 16) == 16) {
            mnManagement.setEnabled(true);
        }
    }

    public void showAdatlap(int id, boolean tipus) {
        /**adatlap megmutatása*/
        final JFrame frame = new JFrame("Adatlap");
//        frame.setUndecorated(true);
        adatlap = new Adatlap(id, Globals.UGYFEL);
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










        /*ugyfelnevsor.setEnabledNevsor(false);
        adatlap=new Adatlap(id,tipus);
        pnlWorkArea.add(adatlap);
        adatlap.setVisible(true);
        pnlWorkArea.validate();*/

    }

    public void showAdatlap(boolean uj, boolean tipus) {
        final JFrame frame = new JFrame("Adatlap");
        frame.setUndecorated(true);
        adatlap = new Adatlap(uj, tipus);
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



        /*ugyfelnevsor.setEnabledNevsor(false);
        adatlap = new Adatlap(uj, tipus);
        pnlWorkArea.add(adatlap);
        adatlap.setVisible(true);
        pnlWorkArea.validate();*/
    }

    private void showManagement() {
        cleanUpWorkarea();
        management = new Management();
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(management);
        management.setVisible(true);
        pnlWorkArea.validate();
        if (!log.writeLog("Funkció: management")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void showEtkezes() {
        cleanUpWorkarea();
        etkezes = new Etkeztetes();
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(etkezes);
        etkezes.setVisible(true);
        pnlWorkArea.validate();
        if (!log.writeLog("Funkció: étkeztetés")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void showMelegedo() {
        cleanUpWorkarea();
        melegedo = new Melegedo();
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(melegedo);
        melegedo.setVisible(true);
        pnlWorkArea.validate();
        if (!log.writeLog("Funkció: melegedő")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void cleanUpWorkarea() {
        pnlWorkArea.removeAll();
        pnlWorkArea.validate();
        pnlWorkArea.repaint();
        disableLezaras(Globals.ETKEZTETES);
        disableLezaras(Globals.MELEGEDO);
    }

    public void enableLezaras(int tipus) {
        switch (tipus) {
            case 0: //étkeztetés
                miLezaras_etk.setEnabled(true);
            case 1: //melegedő
                miLezaras_mel.setEnabled(true);
        }
    }

    public void disableLezaras(int tipus) {
        switch (tipus) {
            case 0: //étkeztetés
                miLezaras_etk.setEnabled(false);
            case 1: //melegedő
                miLezaras_mel.setEnabled(false);
        }
    }

    private void settings() {
        cleanUpWorkarea();
        Settings settings = new Settings();
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(settings);
        settings.setVisible(true);
        pnlWorkArea.validate();
        if (!log.writeLog("Funkció: beállítások")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void prop() {
        FileOutputStream out = null;
        try {
            Properties settings = new Properties();
            settings.setProperty("max_ter_dij", "0");
            settings.setProperty("min_nyugdij", "0");
            settings.setProperty("kat1", "0");
            settings.setProperty("kat2", "0");
            settings.setProperty("irsz", "");
            settings.setProperty("varos", "");
            settings.setProperty("vresz", "");
            settings.setProperty("utca", "");
            settings.setProperty("kozterulet", "");
            settings.setProperty("hazszam", "");
            settings.setProperty("cim_nelkuli", "");
            out = new FileOutputStream("D:\\properties.prop");
            settings.store(out, "no comments");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showAboutBox() {
        JOptionPane.showMessageDialog(this, "<html><b>Onyx" + Main.version + "(20100805)" + "</b><br> Étkeztetés verzió:" + Etkeztetes.version + "<br> Melegedő verzió:" + Melegedo.version, "Névjegy", JOptionPane.INFORMATION_MESSAGE);
        if (!log.writeLog("Névjegy megtekintése")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void showUtemterv() {
        cleanUpWorkarea();
        utemterv = new Utemterv();
        pnlWorkArea.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnlWorkArea.setOpaque(true);
        pnlWorkArea.add(utemterv);
        utemterv.setVisible(true);
        pnlWorkArea.validate();
        if (!log.writeLog("Funkció: ütemterv")) {
            new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
        }
    }

    private void showNaplo(int tipus) {
        switch (tipus) {
            case 1:
                naplo = new Naplo(Naplo.ETKEZES_IGBV);
                break;
            case 2:
                naplo = new Naplo(Naplo.MELEGEDO_ESEMENY);
                break;
        }

        if (JOptionPane.showConfirmDialog(null, naplo, "Időszak", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            if (naplo.getEv() == null || naplo.getHonapIndex() == null || naplo.getNap() == null) {
                new Uzenet("Hiányos dátum!", "Hiba!", Uzenet.ERROR);
            } else {
                switch (tipus) {
                    case 1:
                        naplo.makeNaplo(Naplo.ETKEZES_IGBV);
                        break;
                    case 2:
                        naplo.makeNaplo(Naplo.MELEGEDO_ESEMENY);
                        break;
                }

            }

        }

    }

    public Object[] checkMelegedo() {
        Object[] ret = new Object[3];
        if (melegedo != null) {
            ret = melegedo.getDatum();
            return ret;
        } else {
            return null;
        }
    }

    public Object[] checkEtkezes() {
        Object[] ret = new Object[3];
        if (etkezes != null) {
            ret = etkezes.getDatum();
            return ret;
        } else {
            return null;
        }
    }

    private void updateDataBase() {
        lek = new SQLMuvelet();
        con = (Connection) lek.getConnection();
        try {
            stmt = (PreparedStatement) con.prepareStatement("DESCRIBE ugyfel ellatas");
            res = stmt.executeQuery();
            if (!res.last()) {
                stmt.execute("ALTER TABLE ugyfel ADD COLUMN (ellatas date, atmeneti_lezaras date)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

        pnlWorkArea = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnBeallitas = new javax.swing.JMenu();
        miJelszoCsere = new javax.swing.JMenuItem();
        mnSQL = new javax.swing.JMenu();
        miSQLBackup = new javax.swing.JMenuItem();
        miSQLRestore = new javax.swing.JMenuItem();
        mnFunkcio = new javax.swing.JMenu();
        mnRegisztracio = new javax.swing.JMenu();
        miNevsorUgyf = new javax.swing.JMenuItem();
        mnEtkeztetes = new javax.swing.JMenu();
        miAdatrogzEtk = new javax.swing.JMenuItem();
        miLezaras_etk = new javax.swing.JMenuItem();
        miIgbvNaplo = new javax.swing.JMenuItem();
        miUtemterv = new javax.swing.JMenuItem();
        mnMelegedo = new javax.swing.JMenu();
        miAdatrogzMel = new javax.swing.JMenuItem();
        miLezaras_mel = new javax.swing.JMenuItem();
        miEsemenyNaplo = new javax.swing.JMenuItem();
        miOsszesito = new javax.swing.JMenuItem();
        mnDolgozok = new javax.swing.JMenu();
        miNevsorDolg = new javax.swing.JMenuItem();
        mnManagement = new javax.swing.JMenu();
        miManagement = new javax.swing.JMenuItem();
        mnKilepes = new javax.swing.JMenu();
        miKilep = new javax.swing.JMenuItem();
        mnHelp = new javax.swing.JMenu();
        miHelp = new javax.swing.JMenuItem();
        mnAbout = new javax.swing.JMenuItem();
        mnTeszt = new javax.swing.JMenu();
        mnProp = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Onyx");

        pnlWorkArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        javax.swing.GroupLayout pnlWorkAreaLayout = new javax.swing.GroupLayout(pnlWorkArea);
        pnlWorkArea.setLayout(pnlWorkAreaLayout);
        pnlWorkAreaLayout.setHorizontalGroup(
            pnlWorkAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1113, Short.MAX_VALUE)
        );
        pnlWorkAreaLayout.setVerticalGroup(
            pnlWorkAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 673, Short.MAX_VALUE)
        );

        mnBeallitas.setText("Beállítások");

        miJelszoCsere.addActionListener(this);
        miJelszoCsere.setActionCommand("settings");
        miJelszoCsere.setText("Beállítások");
        mnBeallitas.add(miJelszoCsere);

        mnSQL.setText("Adatbázis");

        miSQLBackup.setText("Biztonsági mentés");
        miSQLBackup.setActionCommand("sqlbackup");
        miSQLBackup.addActionListener(this);
        mnSQL.add(miSQLBackup);

        miSQLRestore.setText("Helyreállítás");
        miSQLRestore.setEnabled(false);
        mnSQL.add(miSQLRestore);

        mnBeallitas.add(mnSQL);

        jMenuBar1.add(mnBeallitas);

        mnFunkcio.setText("Funkció");

        mnRegisztracio.setText("Regisztráció");
        mnRegisztracio.setEnabled(false);

        miNevsorUgyf.setText("Névsor");
        miNevsorUgyf.addActionListener(this);
        miNevsorUgyf.setActionCommand("ugyfelnevsor");
        mnRegisztracio.add(miNevsorUgyf);

        mnFunkcio.add(mnRegisztracio);
        mnRegisztracio.getAccessibleContext().setAccessibleDescription("1");

        mnEtkeztetes.setText("Étkeztetés");
        mnEtkeztetes.setEnabled(false);

        miAdatrogzEtk.addActionListener(this);
        miAdatrogzEtk.setActionCommand("etkeztetes");
        miAdatrogzEtk.setText("Adatrögzítés");
        mnEtkeztetes.add(miAdatrogzEtk);

        miLezaras_etk.addActionListener(this);
        miLezaras_etk.setActionCommand("lezaras_etk");
        miLezaras_etk.setText("Lezárás");
        miLezaras_etk.setEnabled(false);
        mnEtkeztetes.add(miLezaras_etk);

        miIgbvNaplo.setActionCommand("igbvnaplo");
        miIgbvNaplo.addActionListener(this);
        miIgbvNaplo.setText("Igénybevételi napló");
        mnEtkeztetes.add(miIgbvNaplo);

        miUtemterv.setText("Ütemterv");
        miUtemterv.setActionCommand("utemterv");
        miUtemterv.addActionListener(this);
        mnEtkeztetes.add(miUtemterv);

        mnFunkcio.add(mnEtkeztetes);
        mnEtkeztetes.getAccessibleContext().setAccessibleDescription("2");

        mnMelegedo.setText("Melegedő");
        mnMelegedo.setEnabled(false);

        miAdatrogzMel.addActionListener(this);
        miAdatrogzMel.setActionCommand("melegedo");
        miAdatrogzMel.setText("Adatrögzítés");
        mnMelegedo.add(miAdatrogzMel);

        miLezaras_mel.addActionListener(this);
        miLezaras_mel.setActionCommand("lezaras_mel");
        miLezaras_mel.setText("Lezárás");
        miLezaras_mel.setEnabled(false);
        mnMelegedo.add(miLezaras_mel);

        miEsemenyNaplo.setActionCommand("esemenynaplo");
        miEsemenyNaplo.addActionListener(this);
        miEsemenyNaplo.setText("Eseménynapló");
        mnMelegedo.add(miEsemenyNaplo);

        miOsszesito.setText("Összesítő");
        miOsszesito.setActionCommand("osszesito");
        miOsszesito.addActionListener(this);
        mnMelegedo.add(miOsszesito);

        mnFunkcio.add(mnMelegedo);
        mnMelegedo.getAccessibleContext().setAccessibleDescription("4");

        mnDolgozok.setText("Dolgozók");
        mnDolgozok.setEnabled(false);

        miNevsorDolg.addActionListener(this);
        miNevsorDolg.setActionCommand("dolgozonevsor");
        miNevsorDolg.setText("Névsor");
        mnDolgozok.add(miNevsorDolg);

        mnFunkcio.add(mnDolgozok);
        mnDolgozok.getAccessibleContext().setAccessibleDescription("8");

        mnManagement.setText("Management");
        mnManagement.setEnabled(false);

        miManagement.setText("Management");
        miManagement.addActionListener(this);
        miManagement.setActionCommand("management");
        mnManagement.add(miManagement);

        mnFunkcio.add(mnManagement);
        mnManagement.getAccessibleContext().setAccessibleDescription("16");

        jMenuBar1.add(mnFunkcio);

        mnKilepes.setText("Kilépés");

        miKilep.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        miKilep.setText("Kilépés");
        miKilep.addActionListener(this);
        miKilep.setActionCommand("exit");
        mnKilepes.add(miKilep);

        jMenuBar1.add(mnKilepes);

        mnHelp.setText("Segítség");

        miHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        miHelp.setText("Súgó");
        miHelp.setActionCommand("help");
        mnHelp.add(miHelp);

        mnAbout.setText("Névjegy");
        mnAbout.setActionCommand("about");
        mnAbout.addActionListener(this);
        mnHelp.add(mnAbout);

        jMenuBar1.add(mnHelp);

        mnTeszt.setText("Teszt");

        mnProp.setText("prop");
        mnProp.setActionCommand("prop");
        mnProp.addActionListener(this);
        mnTeszt.add(mnProp);

        jMenuBar1.add(mnTeszt);
        mnTeszt.setVisible(false);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlWorkArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlWorkArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1157)/2, (screenSize.height-778)/2, 1157, 778);
    }// </editor-fold>//GEN-END:initComponents
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new FoAblak().setVisible(true);
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem miAdatrogzEtk;
    private javax.swing.JMenuItem miAdatrogzMel;
    private javax.swing.JMenuItem miEsemenyNaplo;
    private javax.swing.JMenuItem miHelp;
    private javax.swing.JMenuItem miIgbvNaplo;
    private javax.swing.JMenuItem miJelszoCsere;
    private javax.swing.JMenuItem miKilep;
    private javax.swing.JMenuItem miLezaras_etk;
    private javax.swing.JMenuItem miLezaras_mel;
    private javax.swing.JMenuItem miManagement;
    private javax.swing.JMenuItem miNevsorDolg;
    private javax.swing.JMenuItem miNevsorUgyf;
    private javax.swing.JMenuItem miOsszesito;
    private javax.swing.JMenuItem miSQLBackup;
    private javax.swing.JMenuItem miSQLRestore;
    private javax.swing.JMenuItem miUtemterv;
    private javax.swing.JMenuItem mnAbout;
    private javax.swing.JMenu mnBeallitas;
    private javax.swing.JMenu mnDolgozok;
    private javax.swing.JMenu mnEtkeztetes;
    private javax.swing.JMenu mnFunkcio;
    private javax.swing.JMenu mnHelp;
    private javax.swing.JMenu mnKilepes;
    private javax.swing.JMenu mnManagement;
    private javax.swing.JMenu mnMelegedo;
    private javax.swing.JMenuItem mnProp;
    private javax.swing.JMenu mnRegisztracio;
    private javax.swing.JMenu mnSQL;
    private javax.swing.JMenu mnTeszt;
    private javax.swing.JPanel pnlWorkArea;
    // End of variables declaration//GEN-END:variables
    private Nevsor ugyfelnevsor;
    private Adatlap adatlap;
    private Management management;
    private Melegedo melegedo;
    private Etkeztetes etkezes;
    private SQLMuvelet lek;
    private ResultSet res;
    private Log log = new Log();
    private String username;
    private Naplo naplo;
    private Utemterv utemterv;
    private Connection con;
    private PreparedStatement stmt;
    Login lg;
//ComponentListener implementáció

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
        //ha nem látható az adatlap, akkor eltávolitjuk a panelről
        if (e.getSource() == adatlap) {
//            pnlWorkArea.remove(adatlap);
            /*boolean b = ugyfelnevsor.getTipus();
            pnlWorkArea.removeAll();
            pnlWorkArea.validate();
            pnlWorkArea.repaint();
            showNevsor(b);*/
//            ugyfelnevsor.setEnabledNevsor(true);
//            ugyfelnevsor.nevsorFrissit();
        }
        if (e.getSource() == management) {
            cleanUpWorkarea();
        }
        if (e.getSource() == ugyfelnevsor) {
            boolean b = ugyfelnevsor.getTipus();
            cleanUpWorkarea();
            showNevsor(b);
        }
        if (utemterv != null) {
            if (e.getSource() == utemterv.dp) {
                utemterv.disposeDatePicker();
            }
        }
    }
//ActionListener implementáció

    public void actionPerformed(ActionEvent e) {
        if (!Globals.MENTVE) {
            if (JOptionPane.showConfirmDialog(this, "<html>Az adatok nem lettek mentve!<br>Folytatja mentés nélkül?", "Mentés", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                Globals.MENTVE = true;
            }
        }
        if (Globals.MENTVE) {
            if (e.getActionCommand().equals("exit")) {
                if (!log.writeLog("Kilépés")) {
                    new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                }
                System.exit(0);
            }
            if (e.getActionCommand().equals("ugyfelnevsor")) {
                showNevsor(Globals.UGYFEL);
                if (!log.writeLog("Funkció: regisztráció")) {
                    new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                }
            }
            if (e.getActionCommand().equals("dolgozonevsor")) {
                showNevsor(Globals.DOLGOZO);
                if (!log.writeLog("Funkció: dolgozók")) {
                    new Uzenet("Hiba a log írásakor", "Hiba", Uzenet.ERROR);
                }
            }
            if (e.getActionCommand().equals("management")) {
                showManagement();
            }
            if (e.getActionCommand().equals("melegedo")) {
                showMelegedo();
            }
            if (e.getActionCommand().equals("etkeztetes")) {
                showEtkezes();
            }
            if (e.getActionCommand().equals("settings")) {
                settings();
            }
            if (e.getActionCommand().equals("lezaras_etk")) {
                etkezes.honapLezaras();
            }
            if (e.getActionCommand().equals("lezaras_mel")) {
                melegedo.honapLezaras();
            }
            if (e.getActionCommand().equals("prop")) {
                prop();
            }
            if (e.getActionCommand().equals("about")) {
                showAboutBox();
            }
            if (e.getActionCommand().equals("igbvnaplo")) {
                showNaplo(Naplo.ETKEZES_IGBV);
            }
            if (e.getActionCommand().equals("esemenynaplo")) {
                showNaplo(Naplo.MELEGEDO_ESEMENY);
            }
            if (e.getActionCommand().equals("osszesito"));
            if (e.getActionCommand().equals("sqlbackup")) {
                SQLMuvelet.dbExport();
            }
            if (e.getActionCommand().equals("utemterv")) {
                showUtemterv();
            }
            if (e.getActionCommand().equals("help")) {
                try {
                    Process proc = Executable.openDocument(new File("c:\\Onyx\\OFK.pdf"), true);
            proc.destroy();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
