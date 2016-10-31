/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package onyx;

import javax.swing.JOptionPane;

/**
 *
 * @author PIRI
 * Version 1.0  2009.05.17.
 */
public class Uzenet {
    public static final int ERROR=1;
    public static final int INFORMATION=2;
    public static final int QUESTION=3;
    public static final int WARNING=4;
    public static final int YES=1;
    public static final int NO=2;
    public static final int QUIT=3;
    private int valasz;
    public static final String version="1.0";


    public Uzenet(String szoveg, String cim, int tipus){
        
        switch(tipus){
            case 1:
                JOptionPane.showMessageDialog(null, szoveg, cim, JOptionPane.ERROR_MESSAGE);
                break;
            case 2:
                JOptionPane.showMessageDialog(null, szoveg, cim, JOptionPane.INFORMATION_MESSAGE);
                break;
            case 3:
                int v=JOptionPane.showConfirmDialog(null, szoveg, cim, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                switch(v){
                    case JOptionPane.YES_OPTION:
                        valasz=YES;
                        break;
                    case JOptionPane.NO_OPTION:
                        valasz=NO;
                        break;
                    case JOptionPane.CLOSED_OPTION:
                        valasz=QUIT;
                        break;
                }
                break;
            case 4:
                JOptionPane.showMessageDialog(null, szoveg, cim, JOptionPane.WARNING_MESSAGE);
                break;
            
        }
    }
    public int getValasz(){
        
        return valasz;
    }
}
