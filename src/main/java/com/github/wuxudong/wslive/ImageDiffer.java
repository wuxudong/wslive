package com.github.wuxudong.wslive;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class ImageDiffer {
    private Color[][] lastData = null;

    public boolean significantDiff(File image) throws IOException {

        long diff = 0;

        boolean result = true;
        Color[][] data = convertTo2DWithoutUsingGetRGB(ImageIO.read(image));

        if (lastData != null) {

            if (lastData.length == data.length && lastData.length != 0 && data.length != 0 && lastData[0].length == data[0].length) {
                int rows = lastData.length;
                int cols = lastData[0].length;

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        Color p1 = lastData[i][j];
                        Color p2 = data[i][j];
                        diff += Math.abs(p1.getRed() - p2.getRed()) + Math.abs(p1.getGreen() - p2.getGreen()) + Math.abs(p1.getBlue() - p2.getBlue());
                    }
                }
            }

            System.out.println("diff is " + diff);
            if (diff > 8 * lastData.length * lastData[0].length) {
                result = true;
            } else {
                result = false;
            }
        }

        lastData = data;
        return result;
    }

    private Color[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        Color[][] result = new Color[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int alpha = (((int) pixels[pixel] & 0xff)); // alpha
                int blue = ((int) pixels[pixel + 1] & 0xff); // blue
                int green = (((int) pixels[pixel + 2] & 0xff)); // green
                int red = (((int) pixels[pixel + 3] & 0xff)); // red
                result[row][col] = new Color(red, green, blue, alpha);
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int blue = ((int) pixels[pixel] & 0xff); // blue
                int green = (((int) pixels[pixel + 1] & 0xff)); // green
                int red = (((int) pixels[pixel + 2] & 0xff)); // red
                result[row][col] = new Color(red, green, blue);
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

}
