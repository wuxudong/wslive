package com.github.wuxudong.wslive;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;


public class SocketIOServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketIOServer.class);

    private com.corundumstudio.socketio.SocketIOServer server;

    public void start() throws InterruptedException {
        Configuration config = new Configuration();

        SocketConfig socketConfig = config.getSocketConfig();
        socketConfig.setReuseAddress(true);

        config.setHostname("127.0.0.1");
        config.setPort(9092);

        server = new com.corundumstudio.socketio.SocketIOServer(config);

        server.start();
    }

    public void broadcast(File file) throws IOException {

        byte[] data = FileUtils.readFileToByteArray(file);

        logger.info("broadcast " + file.getName() + " length " + data.length);

        server.getBroadcastOperations().sendEvent("msg", data);
    }

    public void shutdown() {
        server.stop();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        SocketIOServer socketIOServer = new SocketIOServer();
        socketIOServer.start();


        File processing = new File("processing");
        File processed = new File("processed");

        cleanDirectory(processing);

        cleanDirectory(processed);

        int fps = 25;

        ScreenshotMonitor monitor = new ScreenshotMonitor();
        Consumer<File> fileConsumer = new Consumer<File>() {
            int count = 0;

            ImageDiffer imageDiffer = new ImageDiffer();

            @Override
            public void accept(File file) {
                try {
                    if (count++ % fps == 0 || imageDiffer.significantDiff(file)) {
                        socketIOServer.broadcast(file);
                    } else {
                        logger.info("ignore pic cause no significant diff " + file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        monitor.start(processing, processed, fileConsumer);

        ScreenshotWorker worker = new ScreenshotWorker();
        worker.start("rtmp://13603.liveplay.myqcloud.com/live/13603_01ac087b7a?txSecret=f67c91e22bd4f077a1c6d814c5f5332f&txTime=5A2C087F", processing, fps);

    }

    private static void cleanDirectory(File processing) throws IOException {
        if (processing.exists()) {
            FileUtils.cleanDirectory(processing);
            FileUtils.forceDelete(processing);
        }
        processing.mkdirs();
    }
}
