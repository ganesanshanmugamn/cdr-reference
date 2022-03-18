package com.vmware.logger.futureutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FutureUtils {


    public static void main(String[] args) {
        List<CompletableFuture<String>> list = new ArrayList<>();

        list.add(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of Future 1";
        }));

        list.add(CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("test");
        }));

        list.add(null);
        list.removeAll(Collections.singletonList(null));

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        for (CompletableFuture<String> stringCompletableFuture : list) {
            try {
                System.out.println(stringCompletableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
               RuntimeException r =(RuntimeException) e.getCause();
                System.out.println(r.getMessage());
            }
        }


    }


}
