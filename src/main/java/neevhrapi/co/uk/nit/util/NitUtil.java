package neevhrapi.co.uk.nit.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NitUtil {

    public static Date convertStringToDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            return null; // or throw an exception
        }
    }



}
