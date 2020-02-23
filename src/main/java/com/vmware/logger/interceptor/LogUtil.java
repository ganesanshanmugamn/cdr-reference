package com.vmware.logger.interceptor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static String getCurrentHeapSize() {
        return "Current Heap: "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
                + " mb \t";
    }

    public static String getMaxHeapSize() {
        return "Max Heap: "
                + Runtime.getRuntime().maxMemory() / (1024 * 1024)
                + " mb \t";
    }

    public static String getFreeHeapSize() {
        return "Free Heap: "
                + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) / (1024 * 1024)
                + " mb";
    }

    public static String getTimeTaken(long startTime) {
        return "\t Client call Time taken millisec: " + (System.currentTimeMillis() - startTime);
    }

    public static String getSingleLineErrorTrace(Exception e) {
        if (e == null || ExceptionUtils.getStackTrace(e) == null) {
            return "";
        }
        return ExceptionUtils.getStackTrace(e).replaceAll("\n", ":::");
    }


    public static String getOrderIdFromReqJson(String jsonreq) {
        String orderId = "";

        try {
            JsonObject jsonObject = new JsonParser().parse(jsonreq).getAsJsonObject();
            orderId = jsonObject.getAsJsonObject("order").get("id").getAsString();
        } catch (Exception e1) {
            logger.info("Exception details: " + LogUtil.getSingleLineErrorTrace(e1));
        }
        return orderId;
    }

    public static String getcorrelationIdFromReq(HttpServletRequest request) {
        String transId = "";
        try {
            transId = request.getHeader("correlation-id");
            if (transId == null || transId.trim().length() == 0) {
                transId = request.getHeader("transactionId");
            }
        } catch (Exception e) {
            logger.info("Exception details: " + LogUtil.getSingleLineErrorTrace(e));
        }
        if (transId == null || transId.trim().length() == 0) {
            transId = "";
        }
        return transId;
    }


}
