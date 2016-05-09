package me.geso.nanojsampler;

import javax.management.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Nanojsampler {
    private final Map<StackTraceElement, Integer> map = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        int sleepMillisec = Integer.parseInt(agentArgs);
        Nanojsampler nanojsampler = new Nanojsampler();
        nanojsampler.initJmx();

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    nanojsampler.poll();
                    Thread.sleep(sleepMillisec);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        thread.setName("nanojsampler");
        thread.start();

        System.out.println("done");
    }

    public void initJmx() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("nanojsampler:type=report");
        NanojsamplerReport nanojsamplerMBean = new NanojsamplerReport(map);
        mbs.registerMBean(nanojsamplerMBean, objectName);
    }

    public void poll() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
            process(threadEntry);
        }
    }

    private void process(Map.Entry<Thread, StackTraceElement[]> threadEntry) {
        Thread thread = threadEntry.getKey();
        StackTraceElement[] traceElements = threadEntry.getValue();

        // ignore waiting threads
        if (thread.getState() == Thread.State.TIMED_WAITING || thread.getState() == Thread.State.WAITING) {
            return;
        }

        for (StackTraceElement traceElement : traceElements) {
            map.compute(traceElement,
                    (key2, oldValue) -> oldValue == null ? 1 : oldValue + 1);
        }
    }

    public interface NanojsamplerReportMBean {
        List<String> getReport();
    }

    public static class NanojsamplerReport implements NanojsamplerReportMBean {
        private final Map<StackTraceElement, Integer> map;

        public NanojsamplerReport(Map<StackTraceElement, Integer> map) {
            this.map = map;
        }

        @Override
        public List<String> getReport() {
            return this.map.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                    .map(NanojsamplerReport::convertEntry)
                    .collect(Collectors.toList());
        }

        private static String convertEntry(Map.Entry<StackTraceElement, Integer> stackTraceElementIntegerEntry) {
            StackTraceElement element = stackTraceElementIntegerEntry.getKey();
            Integer value = stackTraceElementIntegerEntry.getValue();

            return value + "\t" + element.getClassName() + "." + element.getMethodName() + ":" + element.getFileName() + ":" + element.getLineNumber();
        }
    }
}
