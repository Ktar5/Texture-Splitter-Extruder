package com.ktar5.texturesplitter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Unpacker {
    public static final int SCALE = 1;
    private Map<Integer, Image> tileImages;
    private Image originalImage;
    private File sourceFile, outputDirectory;
    private int tileWidth, tileHeight;
    private int paddingVertical, paddingHorizontal;
    private int offsetLeft, offsetUp;
    private int columns, rows, dimensionX, dimensionY;

    public Unpacker(File sourceFile, File outputDirectory, int paddingVertical, int paddingHorizontal,
                    int offsetLeft, int offsetUp, int tileWidth, int tileHeight) {
        this.sourceFile = sourceFile;
        this.outputDirectory = outputDirectory;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.offsetLeft = offsetLeft;
        this.offsetUp = offsetUp;

        this.paddingHorizontal = paddingHorizontal;
        this.paddingVertical = paddingVertical;
        tileImages = new HashMap<>();
        try {
            final BufferedImage readImage = ImageIO.read(sourceFile);
            columns = (readImage.getWidth() - getOffsetLeft()) / (getTileWidth() + getPaddingHorizontal());
            rows = (readImage.getHeight() - getOffsetUp()) / (getTileHeight() + getPaddingVertical());
            originalImage = SwingFXUtils.toFXImage(readImage, null);
            this.dimensionX = columns * (getTileWidth() + 2);
            this.dimensionY = rows * (getTileHeight() + 2);
            getTilesetImages(readImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Extrusion Complete");
        alert.setHeaderText(null);
        alert.setContentText("The extrusion process is complete!");
        alert.showAndWait();
    }

    public void getTilesetImages(BufferedImage image) throws IOException {
        int index = 0;

        Map<Integer, BufferedImage> imageMap = new HashMap<>();

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                BufferedImage subImage = image.getSubimage(
                        getOffsetLeft() + ((getPaddingHorizontal() + getTileWidth()) * col),
                        getOffsetUp() + ((getPaddingVertical() + getTileHeight()) * row),
                        getTileWidth(), getTileHeight());
                index++;

                System.out.println("Unpacking image: " + index);

                File outputFile = new File(outputDirectory, "output_" + String.format("%04d", index) + ".png");
                subImage = extrude(subImage);

                ImageIO.write(subImage, "png", outputFile);
                imageMap.put(index, subImage);
            }
        }

        BufferedImage result = new BufferedImage(dimensionX, dimensionY, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        int x = 0, y = 0;
        for (BufferedImage bufferedImage : imageMap.values()) {
            g.drawImage(bufferedImage, x, y, null);

            //Add
            x += bufferedImage.getWidth();
            if (x >= result.getWidth()) {
                x = 0;
                y += bufferedImage.getHeight();
            }
        }
        ImageIO.write(result, "png", new File(outputDirectory, "result.png"));

    }

    public BufferedImage extrude(BufferedImage input) {
        Image image = SwingFXUtils.toFXImage(input, null);
        PixelReader pixelReader = image.getPixelReader();

        int width = input.getWidth() + 2;
        int height = input.getHeight() + 2;

        //Copy from source to destination pixel by pixel
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        Color color = pixelReader.getColor(0, 0);
        pixelWriter.setColor(0, 0, color);

        color = pixelReader.getColor(input.getWidth() - 1, 0);
        pixelWriter.setColor(width - 1, 0, color);

        color = pixelReader.getColor(0, input.getHeight() - 1);
        pixelWriter.setColor(0, height - 1, color);

        color = pixelReader.getColor(input.getWidth() - 1, input.getHeight() - 1);
        pixelWriter.setColor(width - 1, height - 1, color);


        for (int x = 0; x < input.getWidth(); x++) {
            color = pixelReader.getColor(x, 0);
            pixelWriter.setColor(x + 1, 0, color);
            color = pixelReader.getColor(x, input.getHeight() - 1);
            pixelWriter.setColor(x + 1, height - 1, color);
        }

        for (int y = 0; y < input.getHeight(); y++) {
            color = pixelReader.getColor(0, y);
            pixelWriter.setColor(0, y + 1, color);

            color = pixelReader.getColor(input.getWidth() - 1, y);
            pixelWriter.setColor(width - 1, y + 1, color);
        }

//        for (int y = 0; y < input.getHeight(); y++) {
//            for (int x = 0; x < input.getWidth(); x++) {
//                color = pixelReader.getColor(x, y);
//                pixelWriter.setColor(x + 1, y + 1, color);
//            }
//        }

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                pixelWriter.setArgb(x + 1, y + 1, argb);
            }
        }

        return SwingFXUtils.fromFXImage(writableImage, null);
    }


}
