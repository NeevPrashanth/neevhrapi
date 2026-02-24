package neevhrapi.co.uk.nit.util;

import neevhrapi.co.uk.nit.domains.MessageResponse;
import neevhrapi.co.uk.nit.domains.Task;
import neevhrapi.co.uk.nit.domains.projectmgt.TimesheetEntryDTO;
import neevhrapi.co.uk.nit.domains.projectmgt.WeekTimesheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
