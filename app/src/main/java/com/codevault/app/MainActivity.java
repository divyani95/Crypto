package com.codevault.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;


public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;

    private TextView text2TextView;
    private Uri fileUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text2TextView = findViewById(R.id.text2TextView);
        imageView = findViewById(R.id.imageView);

        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                // Calculate the bottom y coordinate of the TextView
                int[] text2TextViewLocation = new int[2];
                text2TextView.getLocationOnScreen(text2TextViewLocation);
                float text2TextViewBottom = text2TextViewLocation[1] + text2TextView.getHeight();

                // Calculate the top y coordinate of the ImageView
                int[] imageViewLocation = new int[2];
                imageView.getLocationOnScreen(imageViewLocation);
                float imageViewTop = imageViewLocation[1];

                // Check if the touch is in the empty area above the ImageView
                if (y < imageViewTop && y > text2TextViewBottom) {
                    // Clear the text in text2TextView
                    text2TextView.setText("");

                    // Reset the ImageView to its initial state
                    imageView.setImageResource(0);

                    return true; // Consume the touch event
                }

                return false; // Let the touch event pass through
            }
        });

        Button selectFileButton = findViewById(R.id.selectFileButton);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileSelector();
            }
        });

        Button encryptButton = findViewById(R.id.encryptButton);
        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptFile();
            }
        });

        Button decryptButton = findViewById(R.id.decryptButton);
        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decryptFile();
            }
        });

        // Feedback Button
        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedbackForm();
            }
        });

        // Inside onCreate method, add this code to set OnClickListener for Reset button
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the text in text2TextView
                text2TextView.setText("");

                // Reset the ImageView to its initial state
                imageView.setImageResource(0);
            }
        });
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                fileUri = data.getData();
                String filePath = fileUri.getPath();
                text2TextView.setText(filePath);
            }
        }
    }

    private void openFeedbackForm() {
        String url = "https://zal7mj471qm.typeform.com/to/Vdo3h3M7"; // URL of the feedback form
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    private void encryptFile() {
        // Show a dialog to get the password from the user
        showPasswordDialog(true);
    }

    private void decryptFile() {
        // Show a dialog to get the password from the user
        showPasswordDialog(false);
    }
    private void performEncryption() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] fileData = getBytesFromInputStream(inputStream);
            String key = "thisisaverysecurekeyfour"; // 16, 24, or 32 bytes
            byte[] encryptedBytes = aesEncrypt(fileData, key);
            String encryptedFileData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
            // Assume encryptedFileData is a String containing the encrypted file data
            text2TextView.setText(encryptedFileData);
            Toast.makeText(this, "Encryption successful", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPasswordDialog(final boolean isEncrypt) {
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");
        builder.setView(passwordEditText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordEditText.getText().toString();
                if ("1234".equals(password)) {
                    if (isEncrypt) {
                        performEncryption();
                    } else {
                        performDecryption();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void performDecryption() {
        try {
            String encryptedFileData = text2TextView.getText().toString();
            String key = "thisisaverysecurekeyfour"; // 16, 24, or 32 bytes
            byte[] decryptedFileData = aesDecrypt(encryptedFileData, key);
            // Decode the byte array to a Bitmap
            Bitmap decryptedImage = BitmapFactory.decodeByteArray(decryptedFileData, 0, decryptedFileData.length);
            // Set the Bitmap to the ImageView
            imageView.setImageBitmap(decryptedImage);
            Toast.makeText(this, "Decryption successful", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] aesEncrypt(byte[] data, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private byte[] aesDecrypt(String encryptedData, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
    }

    private String saveFile(byte[] data) throws Exception {
        File file = new File(getFilesDir(), "decrypted_file");
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
        return file.getAbsolutePath();
    }
}
