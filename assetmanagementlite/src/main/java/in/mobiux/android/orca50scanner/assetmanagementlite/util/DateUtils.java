package in.mobiux.android.orca50scanner.assetmanagementlite.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.mobiux.android.orca50scanner.assetmanagementlite.activity.CheckOutActivity;

public class DateUtils {


    public static void setDate(Calendar calendar, TextView textView) {
        textView.setText(formattedDate(calendar));
    }

    public static void setTime(Calendar calendar, TextView textView) {
        textView.setText(formattedTime(calendar));
    }

    public static String formattedDate(Calendar calendar) {
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String selectedDate = dateFormat.format(date);
        return selectedDate;
    }

    public static String formattedTime(Calendar calendar) {
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        String selectedTime = dateFormat.format(date);
        return selectedTime;
    }

    public static void openDatePicker(Calendar calendar, TextView textView) {

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, m);
                calendar.set(Calendar.DAY_OF_MONTH, d);

                setDate(calendar, textView);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(textView.getContext(), onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public static void openTimePicker(Calendar calendar, TextView textView) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                setTime(calendar, textView);
            }
        };

        TimePickerDialog pickerDialog = new TimePickerDialog(textView.getContext(), onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        pickerDialog.show();
    }
}
