/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package onyx;

/**
 *
 * @author PIRI
 * version 1.0
 */
public class Log{

    SQLMuvelet lek;
    String[] param;
    public static final String version="1.0";

    public Log(){

    }

    public boolean writeLog(String esemeny){
        param=new String[2];
        param[0]=esemeny;
        param[1]=String.valueOf(Globals.getUserID());
        lek=new SQLMuvelet(38, param);
        if(lek.setUpdate())return true;
        else return false;
    }

}
