package com.cmb.efx.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class AppUtil {
    public static String requestUUid() {
        StringBuilder accountNumber = new StringBuilder();
        Random random = new Random();
        int count = 0;
        while (count < 3) {
            accountNumber.append(random.nextInt(10));
            ++count;
        }
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatteDate = DateTimeFormatter.ofPattern("ddMMyyyy");
        System.out.println("Current date: " + currentDate);
        return formatteDate.format(currentDate) + accountNumber;
    }
}
