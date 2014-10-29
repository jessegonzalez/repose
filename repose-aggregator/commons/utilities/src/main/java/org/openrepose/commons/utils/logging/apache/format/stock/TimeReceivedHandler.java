package org.openrepose.commons.utils.logging.apache.format.stock;

import org.openrepose.commons.utils.logging.apache.format.FormatterLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeReceivedHandler implements FormatterLogic {

    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response) {
        return new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
    }
}