package ru.edu.pr05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("=== Unit-проверки ===");
        runUnitTests();
        System.out.println("\n=== Data-driven ===");
        runDataDriven();
        System.out.println("\nИТОГО: passed=" + passed + ", failed=" + failed);
        if (failed > 0) System.exit(1);
    }

    private static void pass(String name) { passed++; System.out.println("PASS " + name); }
    private static void fail(String name, String reason) { failed++; System.out.println("FAIL " + name + " : " + reason); }

    private static void expectEquals(String name, Object actual, Object expected) {
        if (expected == null ? actual == null : expected.equals(actual)) pass(name);
        else fail(name, "expected=" + expected + ", actual=" + actual);
    }

    private static void expectException(String name, Runnable r, Class<? extends Exception> cls) {
        try {
            r.run();
            fail(name, "no exception thrown");
        } catch (Exception e) {
            if (cls.isInstance(e)) pass(name);
            else fail(name, "wrong exception: " + e.getClass().getSimpleName());
        }
    }

    private static void runUnitTests() {
        LocalDate today = LocalDate.now();
        LocalDate past = today.minusDays(5);
        
        BusinessTrip t = new BusinessTrip("T1", "Conference", past, past.plusDays(3));
        expectEquals("initial status", t.getStatus(), TripStatus.DRAFT);
        
        t.submit();
        expectEquals("after submit", t.getStatus(), TripStatus.SUBMITTED);
        
        t.approve();
        expectEquals("after approve", t.getStatus(), TripStatus.APPROVED);
        
        // Тест на завершение (дата уже прошла)
        t.complete();
        expectEquals("after complete", t.getStatus(), TripStatus.COMPLETED);

        // Тесты ошибок
        BusinessTrip t2 = new BusinessTrip("T2", "Meeting", today, today.plusDays(2));
        t2.submit();
        t2.approve();
        expectException("submit from APPROVED", t2::submit, IllegalStateException.class);

        BusinessTrip t3 = new BusinessTrip("T3", "Meeting", today, today.plusDays(2));
        t3.submit();
        expectException("reject short reason", () -> t3.reject("bad"), IllegalArgumentException.class);
        t3.reject("This is a valid reason");
        expectEquals("after reject", t3.getStatus(), TripStatus.REJECTED);

        // Часть Б: duration
        expectEquals("duration 5 days", new BusinessTrip("T4", "Trip", past, past.plusDays(5)).getDurationDays(), 5L);
    }

    private static void runDataDriven() throws IOException {
        Path path = Path.of("data", "sample.csv");
        if (!Files.exists(path)) return;
        List<String> lines = Files.readAllLines(path);
        int n = 0;
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#") || line.startsWith("id;")) continue;
            n++;
            String[] p = line.split(";", -1);
            try {
                LocalDate today = LocalDate.now();
                LocalDate start = today.plusDays(Long.parseLong(p[2]));
                LocalDate end = today.plusDays(Long.parseLong(p[3]));
                BusinessTrip t = new BusinessTrip(p[0], p[1], start, end);
                String action = p[4];
                if (action.equals("submit")) t.submit();
                else if (action.equals("approve")) { t.submit(); t.approve(); }
                else if (action.equals("cancel")) t.cancel();
                else if (action.equals("complete")) { t.submit(); t.approve(); t.complete(); }
                
                expectEquals("data/" + n, t.getStatus().name(), p[5]);
            } catch (Exception e) {
                if (p[5].equals("ERROR")) pass("data/" + n);
                else fail("data/" + n, e.getMessage());
            }
        }
        System.out.println("Data-driven: " + passed + "/" + (passed + failed) + " passed");
    }
}