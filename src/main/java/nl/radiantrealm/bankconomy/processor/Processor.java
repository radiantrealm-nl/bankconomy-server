package nl.radiantrealm.bankconomy.processor;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.ApplicationService;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Processor implements ApplicationService {
    private final Logger logger = Logger.getLogger(Processor.class);

    private static final AtomicInteger processID = new AtomicInteger(0);
    private static final Map<Integer, Process> processMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> task;

    public Processor() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        ApplicationService.super.start();
        task = executorService.scheduleWithFixedDelay(this::handleNextProcess, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        ApplicationService.super.stop();
        task.cancel(false);
    }

    protected void handleNextProcess() {
        if (processMap.isEmpty()) return;

        int nextProcessID = Collections.min(processMap.keySet());
        Process process = processMap.remove(nextProcessID);

        try {
            ProcessResult result = process.handler().handle(process);
            process.consumer().accept(result);
        } catch (Exception e) {
            String error = String.format("Unexpected error in %s whilst processing request.", process.handler().getClass().getSimpleName());
            process.consumer().accept(ProcessResult.error(error, e));
            logger.error(error, e);
        }
    }

    public static void createProcess(ProcessType processType, JsonObject object, Consumer<ProcessResult> consumer) throws IllegalArgumentException {
        int nextProcessID = processID.incrementAndGet();

        processMap.put(nextProcessID, new Process(
                nextProcessID,
                processType.handler,
                object,
                consumer
        ));
    }
}
