package ru.network.anonymizer_msgs.service;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface AdminNotifier {

    default String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
