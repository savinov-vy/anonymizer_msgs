package ru.network.anonymizer_msgs.service;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface AdminNotifier {
    public static final Long ADMIN_ID = 5888671303L;

    default String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
