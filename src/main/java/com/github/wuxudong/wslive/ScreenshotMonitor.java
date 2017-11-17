package com.github.wuxudong.wslive;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ScreenshotMonitor {

    AtomicBoolean running = new AtomicBoolean(true);

    public void start(File unprocessed, File processed, Consumer<File> consumer) {

        Consumer<File> markAlreadyProcessed = file -> {
            try {
                FileUtils.moveFileToDirectory(file, processed, false);
            } catch (IOException e) {
            }
        };

        new Thread(() -> {
            while (running.get()) {
                Arrays.stream(unprocessed.listFiles((dir, name) -> name.startsWith("screenshot-"))).sorted(Comparator.comparing(this::getIndex)).forEach(consumer.andThen(markAlreadyProcessed));
                try {
                    Thread.sleep(10l);
                } catch (InterruptedException e) {
                }
            }
        }).start();


    }

    private Integer getIndex(File o1) {
        return Integer.valueOf(o1.getName().substring("screenshot-".length(), o1.getName().indexOf('.')));
    }

    public void stop() {
        running.set(false);
    }
}
