/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onyx;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.JOptionPane;

/**
 *
 * @author PIRI
 */
public class Main {

    public static Boolean DEBUG = false;
    public static FoAblak foablak;
    public static final String version = "1.1.0";
    static final String tryvariable="try";

    /**
     * paraméterek: -d (=debug)
     *              -f=[path] (=config file elérési útvonal)
     */
    public static void main(String[] args) {
        if (new File("elso").exists()) {
            try {
                Runtime bat = Runtime.getRuntime();
                bat.exec("cmd /c start /min dbinit.bat");
                Globals.ELSO_INDITAS = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean a = new File("elso").delete();
        }
        Globals.configpath = null;
        Globals.intezmeny="KE";
        Globals.NAPLO_HEADER[0]="\"SILENCIO\" Szociális Ellátó Központ";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                DEBUG = true;
            }
            if (args[i].startsWith("-f=")) {
                Globals.configpath = args[i].substring(3);
            }
            if (args[i].equals("-ma")) {
                Globals.intezmeny="MA";
                Globals.NAPLO_HEADER[0]="\"SILENCIO\" Szociális Ellátó Központ";
            }
            if (args[i].equals("-ba")) {
                Globals.intezmeny="BA";
                Globals.NAPLO_HEADER[0]="\"Hírös Diák\" Alapítvány";
            }

        }

        try {
            //  System L&F beállítása
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
        Config a = new Config();
        if (!a.validConfig()) {
            System.exit(0);
        }

        ToolTipManager.sharedInstance().setDismissDelay(5000);
        ToolTipManager.sharedInstance().setReshowDelay(200);
        ToolTipManager.sharedInstance().setInitialDelay(500);


        foablak = new FoAblak();
        foablak.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (!Globals.MENTVE) {
                    if (JOptionPane.showConfirmDialog(null, "<html>Az adatok nem lettek mentve!<br>Folytatja mentés nélkül?", "Mentés", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                        Globals.MENTVE = true;
                    }else Globals.MENTVE=false;
                }

                if (Globals.MENTVE) {
                    new Log().writeLog("Kilépés");
                    System.exit(0);
                }
            }
        });
        foablak.setSize(1024, 768);
        foablak.setExtendedState(Frame.MAXIMIZED_BOTH);
        foablak.setVisible(true);


    }
}
