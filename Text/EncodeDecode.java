package com.ayush.imagesteganographylibrary.Text;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.ayush.imagesteganographylibrary.Utils.Utility;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class EncodeDecode {

    private static final String TAG = EncodeDecode.class.getName();
    //start and end message constants
    private static final String END_MESSAGE_COSTANT = "#!@";
    private static final String START_MESSAGE_COSTANT = "@!#";
    private static final int[] binary = {16, 8, 0};
    private static final byte[] andByte = {(byte) 0xC0, 0x30, 0x0C, 0x03};
    private static final int[] toShift = {6, 4, 2, 0};

    /**
     * Данный метод - основа двухбитного кодирования
     *
     * @return : byte encoded pixel array
     * @parameter :  integer_pixel_array {The integer RGB array}
     * @parameter : image_columns {int} - столбцы
     * @parameter : image_rows {int} - строки
     * @parameter : messageEncodingStatus {MessageEncodingStatus} - статус закодированности
     * @parameter : progressHandler {ProgressHandler} - интерфейс отображающий прогресс     *
     */

    private static byte[] encodeMessage(int[] integer_pixel_array, int image_columns, int image_rows,
                                        MessageEncodingStatus messageEncodingStatus, ProgressHandler progressHandler) {

        //  RGB каналлы
        int channels = 3;

        int shiftIndex = 4;


        byte[] result = new byte[image_rows * image_columns * channels];

        int resultIndex = 0;

        for (int row = 0; row < image_rows; row++) {

            for (int col = 0; col < image_columns; col++) {

                //2D матрица в 1D
                int element = row * image_columns + col;

                byte tmp;

                for (int channelIndex = 0; channelIndex < channels; channelIndex++) {

                    if (!messageEncodingStatus.isMessageEncoded()) {

                        // Сдвиг целочисленного значения на 2 влево и замена двух младших цифр значениями message_byte_array.
                        tmp = (byte) ((((integer_pixel_array[element] >> binary[channelIndex]) & 0xFF) & 0xFC) | ((messageEncodingStatus.getByteArrayMessage()[messageEncodingStatus.getCurrentMessageIndex()] >> toShift[(shiftIndex++)
                                % toShift.length]) & 0x3));// 6

                        if (shiftIndex % toShift.length == 0) {

                            messageEncodingStatus.incrementMessageIndex();

                            if (progressHandler != null)
                                progressHandler.increment(1);

                        }

                        if (messageEncodingStatus.getCurrentMessageIndex() == messageEncodingStatus.getByteArrayMessage().length) {

                            messageEncodingStatus.setMessageEncoded();

                            if (progressHandler != null)
                                progressHandler.finished();

                        }
                    } else {
                        // копирование целого числа в массив результатов
                        tmp = (byte) ((((integer_pixel_array[element] >> binary[channelIndex]) & 0xFF)));
                    }

                    result[resultIndex++] = tmp;

                }

            }

        }


        return result;

    }

    /**
     * Этот метод реализует вышеуказанный метод в списке фрагментов изображения.
     *
     * @return : Encoded list of chunk images
     * @parameter : splitted_images {List<Bitmap>} - фрагменты
     * @parameter : encrypted_message {string} - зашифрованное сообщение
     * @parameter : progressHandler {Progress bar handler} - прогрессбар
     */
    public static List<Bitmap> encodeMessage(List<Bitmap> splitted_images,
                                             String encrypted_message, ProgressHandler progressHandler) {



        List<Bitmap> result = new ArrayList<>(splitted_images.size());


        //Добавление констант начального и конечного сообщения к зашифрованному сообщению
        encrypted_message = encrypted_message + END_MESSAGE_COSTANT;
        encrypted_message = START_MESSAGE_COSTANT + encrypted_message;


        //получение байтового массива из строки
        byte[] byte_encrypted_message = encrypted_message.getBytes(Charset.forName("ISO-8859-1"));


        MessageEncodingStatus message = new MessageEncodingStatus(byte_encrypted_message, encrypted_message);


        if (progressHandler != null) {
            progressHandler.setTotal(encrypted_message.getBytes(Charset.forName("ISO-8859-1")).length);
        }

        //журнал для получения байтовой длины сообщения
        Log.i(TAG, "Message length " + byte_encrypted_message.length);

        for (Bitmap bitmap : splitted_images) {

            if (!message.isMessageEncoded()) {


                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                //Создание одномерного целочисленного массива пикселей
                int[] oneD = new int[width * height];
                bitmap.getPixels(oneD, 0, width, 0, 0, width, height);


                int density = bitmap.getDensity();

                //кодирование изображения
                byte[] encodedImage = encodeMessage(oneD, width, height, message, progressHandler);

                int[] oneDMod = Utility.byteArrayToIntArray(encodedImage);


                Bitmap encoded_Bitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);
                encoded_Bitmap.setDensity(density);

                int masterIndex = 0;

                //установка значений пикселей вышеупомянутого растрового изображения
                for (int j = 0; j < height; j++)
                    for (int i = 0; i < width; i++) {

                        encoded_Bitmap.setPixel(i, j, Color.argb(0xFF,
                                oneDMod[masterIndex] >> 16 & 0xFF,
                                oneDMod[masterIndex] >> 8 & 0xFF,
                                oneDMod[masterIndex++] & 0xFF));

                    }

                result.add(encoded_Bitmap);

            } else {

                result.add(bitmap.copy(bitmap.getConfig(), false));
            }
        }

        return result;
    }

    /**
     * Это метод декодирования 2-битного кодирования.
     *
     * @return : Void
     * @parameter : byte_pixel_array {The byte array image}
     * @parameter : image_columns {int}
     * @parameter : image_rows {int}
     * @parameter : messageDecodingStatus {MessageDecodingStatus}
     */
    private static void decodeMessage(byte[] byte_pixel_array, int image_columns,
                                      int image_rows, MessageDecodingStatus messageDecodingStatus) {

        //зашифрованное сообщение
        Vector<Byte> byte_encrypted_message = new Vector<>();

        int shiftIndex = 4;

        byte tmp = 0x00;


        for (byte aByte_pixel_array : byte_pixel_array) {



            tmp = (byte) (tmp | ((aByte_pixel_array << toShift[shiftIndex
                    % toShift.length]) & andByte[shiftIndex++ % toShift.length]));

            if (shiftIndex % toShift.length == 0) {
                //добавление значения временного байта
                byte_encrypted_message.addElement(tmp);


                //преобразование байтового значения в строку
                byte[] nonso = {byte_encrypted_message.elementAt(byte_encrypted_message.size() - 1)};
                String str = new String(nonso, Charset.forName("ISO-8859-1"));

                if (messageDecodingStatus.getMessage().endsWith(END_MESSAGE_COSTANT)) {

                    Log.i("TEST", "Decoding ended");

                    //исправление декодирования ISO-8859-1
                    byte[] temp = new byte[byte_encrypted_message.size()];

                    for (int index = 0; index < temp.length; index++)
                        temp[index] = byte_encrypted_message.get(index);


                    String stra = new String(temp, Charset.forName("ISO-8859-1"));


                    messageDecodingStatus.setMessage(stra.substring(0, stra.length() - 1));


                    messageDecodingStatus.setEnded();

                    break;
                } else {
                    //добавить декодированное сообщение к исходному сообщению
                    messageDecodingStatus.setMessage(messageDecodingStatus.getMessage() + str);

                    //Если там не было сообщения, а были только константы начального и конечного сообщения
                    if (messageDecodingStatus.getMessage().length() == START_MESSAGE_COSTANT.length()
                            && !START_MESSAGE_COSTANT.equals(messageDecodingStatus.getMessage())) {

                        messageDecodingStatus.setMessage("");
                        messageDecodingStatus.setEnded();

                        break;
                    }
                }

                tmp = 0x00;
            }

        }

        if (!Utility.isStringEmpty(messageDecodingStatus.getMessage()))
            //удаление начальных и конечных констант формы сообщения

            try {
                messageDecodingStatus.setMessage(messageDecodingStatus.getMessage().substring(START_MESSAGE_COSTANT.length(), messageDecodingStatus.getMessage()
                        .length()
                        - END_MESSAGE_COSTANT.length()));
            } catch (Exception e) {
                e.printStackTrace();
            }


    }

    /**
     * Этот метод берет список закодированных изображений фрагментов и декодирует его.
     *
     * @return : encrypted message {String}
     * @parameter : encodedImages {List<Bitmap>} - список закодированных фрагментов изображения
     */

    public static String decodeMessage(List<Bitmap> encodedImages) {


        MessageDecodingStatus messageDecodingStatus = new MessageDecodingStatus();

        for (Bitmap bit : encodedImages) {
            int[] pixels = new int[bit.getWidth() * bit.getHeight()];

            bit.getPixels(pixels, 0, bit.getWidth(), 0, 0, bit.getWidth(),
                    bit.getHeight());

            byte[] b;

            b = Utility.convertArray(pixels);

            decodeMessage(b, bit.getWidth(), bit.getHeight(), messageDecodingStatus);

            if (messageDecodingStatus.isEnded())
                break;
        }

        return messageDecodingStatus.getMessage();
    }

    /**
     * Рассчет необходимого количества пикселей.
     *
     * @return : The number of pixel {integer}
     * @parameter : message {String} - сообщение
     */
    public static int numberOfPixelForMessage(String message) {
        int result = -1;
        if (message != null) {
            message += END_MESSAGE_COSTANT;
            message = START_MESSAGE_COSTANT + message;
            result = message.getBytes(Charset.forName("ISO-8859-1")).length * 4 / 3;
        }

        return result;
    }


    public interface ProgressHandler {

        void setTotal(int tot);

        void increment(int inc);

        void finished();
    }

    private static class MessageDecodingStatus {

        private String message;
        private boolean ended;

        MessageDecodingStatus() {
            message = "";
            ended = false;
        }

        boolean isEnded() {
            return ended;
        }

        void setEnded() {
            this.ended = true;
        }

        String getMessage() {
            return message;
        }

        void setMessage(String message) {
            this.message = message;
        }


    }

    private static class MessageEncodingStatus {
        private boolean messageEncoded;
        private int currentMessageIndex;
        private byte[] byteArrayMessage;
        private String message;

        MessageEncodingStatus(byte[] byteArrayMessage, String message) {
            this.messageEncoded = false;
            this.currentMessageIndex = 0;
            this.byteArrayMessage = byteArrayMessage;
            this.message = message;
        }

        void incrementMessageIndex() {
            currentMessageIndex++;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        boolean isMessageEncoded() {
            return messageEncoded;
        }

        void setMessageEncoded() {
            this.messageEncoded = true;
        }

        int getCurrentMessageIndex() {
            return currentMessageIndex;
        }

        public void setCurrentMessageIndex(int currentMessageIndex) {
            this.currentMessageIndex = currentMessageIndex;
        }

        byte[] getByteArrayMessage() {
            return byteArrayMessage;
        }

        public void setByteArrayMessage(byte[] byteArrayMessage) {
            this.byteArrayMessage = byteArrayMessage;
        }
    }

}
