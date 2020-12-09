package com.ayush.imagesteganographylibrary.Text;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.ayush.imagesteganographylibrary.Utils.Utility;

import java.util.List;

/**
 *  Все методы в классе EncodeDecode используются для кодирования секретного сообщения в изображении.
 *  Все задачи будут выполняться в фоновом режиме.
 */
public class TextEncoding extends AsyncTask<ImageSteganography, Integer, ImageSteganography> {


    private static final String TAG = TextEncoding.class.getName();

    private final ImageSteganography result;
    //Интерфейс обратного вызова для AsyncTask
    private final TextEncodingCallback callbackInterface;
    private int maximumProgress;
    private final ProgressDialog progressDialog;

    public TextEncoding(Activity activity, TextEncodingCallback callbackInterface) {
        super();
        this.progressDialog = new ProgressDialog(activity);
        this.callbackInterface = callbackInterface;
        //создание объекта результата
        this.result = new ImageSteganography();
    }

    //предварительное выполнение метода
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //настройка параметров диалога прогресса
        if (progressDialog != null) {
            progressDialog.setMessage("Loading, Please Wait...");
            progressDialog.setTitle("Encoding Message");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(ImageSteganography textStegnography) {
        super.onPostExecute(textStegnography);

        // закрытие диалога прогресса
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        //Отправка результата в интерфейс обратного вызова
        callbackInterface.onCompleteTextEncoding(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        //обновление диалога прогресса
        if (progressDialog != null) {
            progressDialog.incrementProgressBy(values[0]);
        }
    }

    @Override
    protected ImageSteganography doInBackground(ImageSteganography... imageSteganographies) {

        maximumProgress = 0;

        if (imageSteganographies.length > 0) {

            ImageSteganography textStegnography = imageSteganographies[0];


            Bitmap bitmap = textStegnography.getImage();


            int originalHeight = bitmap.getHeight();
            int originalWidth = bitmap.getWidth();

            //разделение растрового изображения
            List<Bitmap> src_list = Utility.splitImage(bitmap);

            //кодирование зашифрованного сжатого сообщения в изображение

            List<Bitmap> encoded_list = EncodeDecode.encodeMessage(src_list, textStegnography.getEncrypted_message(), new EncodeDecode.ProgressHandler() {

                //Progress Handler
                @Override
                public void setTotal(int tot) {
                    maximumProgress = tot;
                    progressDialog.setMax(maximumProgress);
                    Log.d(TAG, "Total Length : " + tot);
                }

                @Override
                public void increment(int inc) {
                    publishProgress(inc);
                }

                @Override
                public void finished() {
                    Log.d(TAG, "Message Encoding end....");
                    progressDialog.setIndeterminate(true);
                }
            });

            //освобождение памяти
            for (Bitmap bitm : src_list)
                bitm.recycle();

            //Java Garbage collector
            System.gc();

            //объединение разделенного закодированного изображения
            Bitmap srcEncoded = Utility.mergeImage(encoded_list, originalHeight, originalWidth);

            //Установка закодированного изображения для результата
            result.setEncoded_image(srcEncoded);
            result.setEncoded(true);
        }

        return result;
    }
}
