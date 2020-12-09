package com.ayush.imagesteganographylibrary.Text;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Utils.Utility;

import java.util.List;

/**
 * В этом классе все методы класса EncodeDecode используются для декодирования секретного сообщения в изображении.
 * Все задачи будут выполняться в фоновом режиме.
 */
public class TextDecoding extends AsyncTask<ImageSteganography, Void, ImageSteganography> {


    private final static String TAG = TextDecoding.class.getName();

    private final ImageSteganography result;

    private final TextDecodingCallback textDecodingCallback;
    private ProgressDialog progressDialog;

    public TextDecoding(Activity activity, TextDecodingCallback textDecodingCallback) {
        super();
        this.progressDialog = new ProgressDialog(activity);
        this.textDecodingCallback = textDecodingCallback;
        //making result object
        this.result = new ImageSteganography();
    }

    //установка диалога прогресса, если нужно
    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    //предварительное выполнение метода
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //setting parameters of progress dialog
        if (progressDialog != null) {
            progressDialog.setMessage("Loading, Please Wait...");
            progressDialog.setTitle("Decoding Message");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }
    }

    @Override
    protected void onPostExecute(ImageSteganography imageSteganography) {
        super.onPostExecute(imageSteganography);

        //закрыть диалоговое окно прогресса
        if (progressDialog != null)
            progressDialog.dismiss();

        //отправка результата в обратный вызов
        textDecodingCallback.onCompleteTextEncoding(result);
    }

    @Override
    protected ImageSteganography doInBackground(ImageSteganography... imageSteganographies) {

        //Если он еще не декодирован
        if (imageSteganographies.length > 0) {

            ImageSteganography imageSteganography = imageSteganographies[0];

            //получение растрового изображения из файла
            Bitmap bitmap = imageSteganography.getImage();

            //return null if bitmap is null
//            if (bitmap == null)
//                return null;

            //разделение изображений
            List<Bitmap> srcEncodedList = Utility.splitImage(bitmap);

            //расшифровка зашифрованного заархивированного сообщения
            String decoded_message = EncodeDecode.decodeMessage(srcEncodedList);

            Log.d(TAG, "Decoded_Message : " + decoded_message);

            //текст декодирован = true
            if (!Utility.isStringEmpty(decoded_message)) {
                result.setDecoded(true);
            }

            //расшифровка закодированного сообщения
            String decrypted_message = ImageSteganography.decryptMessage(decoded_message, imageSteganography.getSecret_key());
            Log.d(TAG, "Decrypted message : " + decrypted_message);

            //Если decrypted_message имеет значение null, это означает, что секретный ключ неверен, в противном случае секретный ключ правильный.
            if (!Utility.isStringEmpty(decrypted_message)) {

                //предоставленный секретный ключ правильный
                result.setSecretKeyWrong(false);


                result.setMessage(decrypted_message);


                //освобождение памяти
                for (Bitmap bitm : srcEncodedList)
                    bitm.recycle();

                //Java Garbage Collector
                System.gc();
            }
        }

        return result;
    }
}
