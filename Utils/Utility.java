package com.ayush.imagesteganographylibrary.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class Utility {

    // Задаем константу размера квадратного блока
    private static final int SQUARE_BLOCK_SIZE = 512;
    private final static String TAG = Utility.class.getName();

    /**
     * Метод определяет количество изображений, на которое разделится изначальное
     *
     * @return : оличество квадратных блоков {Integer}
     * @parameter : количество пикселей {Integer}
     */
    public static int squareBlockNeeded(int pixels) {
        int result;

        int quadratic = SQUARE_BLOCK_SIZE * SQUARE_BLOCK_SIZE;
        int divisor = pixels / (quadratic);
        int remainder = pixels % (quadratic);

        result = divisor + (remainder > 0 ? 1 : 0);

        return result;
    }

    /**
     * Метод разделяет одно изображение на несколько других размером ( SQUARE_BLOCK_SIZE * SQUARE_BLOCK_SIZE )
     *
     * @return : Список изображений {List}
     * @parameter : Изображение {Bitmap}
     */
    public static List<Bitmap> splitImage(Bitmap bitmap) {

        // Для высоты и ширины маленьких кусочков изображений
        int chunkHeight, chunkWidth;

        // Для хранения всех кусков изображенияTo store all the small image chunks in bitmap format in this list
        ArrayList<Bitmap> chunkedImages = new ArrayList<>();

        // Представим матрицу из квадратных блоков
        // Это ее столбцы и строки
        int rows = bitmap.getHeight() / SQUARE_BLOCK_SIZE;
        int cols = bitmap.getWidth() / SQUARE_BLOCK_SIZE;

        int chunk_height_mod = bitmap.getHeight() % SQUARE_BLOCK_SIZE;
        int chunk_width_mod = bitmap.getWidth() % SQUARE_BLOCK_SIZE;

        if (chunk_height_mod > 0)
            rows++;
        if (chunk_width_mod > 0)
            cols++;


        // x, y - координаты пикселя в куске изображения
        int y_coordinate = 0;

        for (int x = 0; x < rows; x++) {

            int x_coordinate = 0;

            for (int y = 0; y < cols; y++) {

                chunkHeight = SQUARE_BLOCK_SIZE;
                chunkWidth = SQUARE_BLOCK_SIZE;

                if (y == cols - 1 && chunk_width_mod > 0)
                    chunkWidth = chunk_width_mod;

                if (x == rows - 1 && chunk_height_mod > 0)
                    chunkHeight = chunk_height_mod;

                // Добавление куска изображения в список
                chunkedImages.add(Bitmap.createBitmap(bitmap, x_coordinate, y_coordinate, chunkWidth, chunkHeight));
                x_coordinate += SQUARE_BLOCK_SIZE;

            }

            y_coordinate += SQUARE_BLOCK_SIZE;

        }

        // возврат списка
        return chunkedImages;
    }

    /**
     * Данный метод объединяет изображения из списка в одно целое
     *
     * @return : Целое изображение {Bitmap}
     * @parameter : List {Bitmap} - список изображений, Original Height {Integer} - изначальная высота, Original Width {Integer} - изначальная ширина
     */
    public static Bitmap mergeImage(List<Bitmap> images, int original_height, int original_width) {

        // вычисление количества строк и столбцов матрицы
        int rows = original_height / SQUARE_BLOCK_SIZE;
        int cols = original_width / SQUARE_BLOCK_SIZE;

        int chunk_height_mod = original_height % SQUARE_BLOCK_SIZE;
        int chunk_width_mod = original_width % SQUARE_BLOCK_SIZE;

        if (chunk_height_mod > 0)
            rows++;
        if (chunk_width_mod > 0)
            cols++;

        // создание объекта bitmap, который может вместить в себя полученное изображение
        Log.d(TAG, "Size width " + original_width + " size height " + original_height);
        Bitmap bitmap = Bitmap.createBitmap(original_width, original_height, Bitmap.Config.ARGB_4444);

        // создание canvas
        Canvas canvas = new Canvas(bitmap);

        int count = 0;

        for (int irows = 0; irows < rows; irows++) {
            for (int icols = 0; icols < cols; icols++) {

                // рисовка всех кусков изображения
                canvas.drawBitmap(images.get(count), (SQUARE_BLOCK_SIZE * icols), (SQUARE_BLOCK_SIZE * irows), new Paint());
                count++;

            }
        }

        // возврат bitmap
        return bitmap;
    }

    /**
     * Данный метод конвертирует массив byte в массив integer.
     *
     * @return : Integer Array - массив типа integer
     * @parameter : b {the byte array} - массив типа byte
     */

    public static int[] byteArrayToIntArray(byte[] b) {

        Log.v("Size byte array", b.length + "");

        int size = b.length / 3;

        Log.v("Size Int array", size + "");

        System.runFinalization();
        System.gc();

        Log.v("FreeMemory", Runtime.getRuntime().freeMemory() + "");
        int[] result = new int[size];
        int offset = 0;
        int index = 0;

        while (offset < b.length) {
            result[index++] = byteArrayToInt(b, offset);
            offset = offset + 3;
        }

        return result;
    }

    /**
     * Преобразование массива byte в int.
     *
     * @return : Integer
     * @parameter :  b {the byte array} - массив типа byte
     */
    public static int byteArrayToInt(byte[] b) {

        return byteArrayToInt(b, 0);

    }

    /**
     * Преобразуйте массивa byte в int, начиная с заданного смещения
     *
     * @return :  Integer
     * @parameter :  b {the byte array} - массив byte, offset {integer} - смещение
     */
    private static int byteArrayToInt(byte[] b, int offset) {

        int value = 0x00000000;

        for (int i = 0; i < 3; i++) {
            int shift = (3 - 1 - i) * 8;
            value |= (b[i + offset] & 0x000000FF) << shift;
        }

        value = value & 0x00FFFFFF;

        return value;
    }

    /**
     * Преобразвание целочисленного масиива, представляющего значения [argb], в массив байтов,
     * представляющий значения [rgb]
     *
     * @return : массив байтов, представляющий значения [rgb].
     * @parameter : целочисленный масиив, представляющий значения [argb].
     */
    public static byte[] convertArray(int[] array) {

        byte[] newarray = new byte[array.length * 3];

        for (int i = 0; i < array.length; i++) {

            newarray[i * 3] = (byte) ((array[i] >> 16) & 0xFF);
            newarray[i * 3 + 1] = (byte) ((array[i] >> 8) & 0xFF);
            newarray[i * 3 + 2] = (byte) ((array[i]) & 0xFF);

        }

        return newarray;
    }

    /**
     * Данный метод проверяет строку на пустоту
     *
     * @return : true или false {boolean}
     * @parameter : String - строка
     */
    public static boolean isStringEmpty(String str) {
        boolean result = true;

        if (str == null) ;
        else {
            str = str.trim();
            if (str.length() > 0 && !str.equals("undefined"))
                result = false;
        }

        return result;
    }
}
