package com.github.wuxudong.wslive;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenshotWorker {

    AtomicBoolean running = new AtomicBoolean(true);

    private Executor executor = Executors.newCachedThreadPool();

    public void start(String url, File targetDir, int fps) {
        try {
            String shpath = "ffmpeg -i " + url +
                    " -f image2 -vcodec mjpeg  -s 480x640 -vf fps=" + fps + " " +
                    targetDir.getAbsolutePath() + "/screenshot-%d.jpg";
            Process ps = Runtime.getRuntime().exec(shpath);

            executor.execute(new StreamGobbler(ps.getInputStream(), s -> System.out.println(s)));

            executor.execute(() -> {
                while (running.get()) {

                    try {
                        Thread.sleep(10l);
                    } catch (InterruptedException e) {
                    }
                }

                ps.destroy();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running.set(false);
    }


}
