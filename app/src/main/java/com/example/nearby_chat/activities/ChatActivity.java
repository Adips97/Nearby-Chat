package com.example.nearby_chat.activities;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.nearby_chat.R;
import com.example.nearby_chat.adapters.ChatAdapter;
import com.example.nearby_chat.constants.Constant;
import com.example.nearby_chat.models.Message;
import com.example.nearby_chat.models.UserProfile;
import com.example.nearby_chat.utils.DatabaseUtils;
import com.example.nearby_chat.utils.FileUtils;
import com.example.nearby_chat.utils.ImageUtils;
import com.example.nearby_chat.utils.PermissionUtils;

import static com.example.nearby_chat.constants.Constant.FIREBASE_STORAGE_REFERENCE;
import static com.example.nearby_chat.constants.Constant.NEARBY_CHAT;


public class ChatActivity extends AppCompatActivity {

    public static final String PARTNER_USER_PROFILE = "PARTNER_USER_PROFILE";

    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    private static final int RECORD_AUDIO = 3;

    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final String[] RECORD_AUDIO_PERMISSION = new String[]{Manifest.permission.RECORD_AUDIO};

    private String conversationId;

    private List<Message> messages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;

    private final TextWatcher editMessageTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() == 0) {
                messageSendButton.setEnabled(false);
            } else {
                messageSendButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private String imagePath;
    private String imageUrl;
    private Uri imageUri;
    private Bitmap resizedImage;
    private ImageButton messageAtachImageButton;
    private ListView messageListView;
    private ProgressBar progressBar;
    private final ChildEventListener messageListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            if (message != null) {
                chatAdapter.add(message);
                messageListView.setSelection(messages.size() - 1);
            } else {
                Log.w(Constant.NEARBY_CHAT, "No messages");
            }

            if (progressBar.getVisibility() == View.VISIBLE) {
                hideProgressBar();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "loadPost:onCancelled", databaseError.toException());
        }
    };

    private UserProfile conversationPartner;
    private ImageButton messageRecordButton;
    private MediaRecorder mediaRecorder;
    private boolean recording;
    private String recordPath;
    private String recordUrl;
FirebaseStorage firebaseStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // spinner
        firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);

        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);

        conversationPartner = (UserProfile) getIntent().getSerializableExtra(PARTNER_USER_PROFILE);


        messageEditView = (EditText) findViewById(R.id.message_edit);
        messageEditView.addTextChangedListener(editMessageTextWatcher);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setEnabled(false);
        messageSendButton.setOnClickListener(v -> sendMessage());

        messageAtachImageButton = (ImageButton) findViewById(R.id.message_attach_image);
        messageAtachImageButton.setOnClickListener(v -> showImageAttachementDialog());


        recording = false;
        messageRecordButton = (ImageButton) findViewById(R.id.message_record_audio);
        messageRecordButton.setOnClickListener(v -> voiceRecordingAction());

        messages = new ArrayList<>();

        conversationId = getConversationId(conversationPartner.getId());

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .addChildEventListener(messageListener);

        chatAdapter = new ChatAdapter(this, messages);
        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);
        TextView namaLawan = (TextView) findViewById(R.id.namaLawan);
        namaLawan.setText(conversationPartner.getUserName());

        // set conversation title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        loadProfileImage();

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(ChatActivity.this , VisitProfileActivity.class);
                conversationPartner.setAvatar(null);
                i.putExtra(ChatActivity.PARTNER_USER_PROFILE, conversationPartner);
            startActivity(i);
                }
            });


        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initializeMediaRecord(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void sendMessage() {
        Message newMessage = new Message();

        // text message
        if (imageUrl == null && recordUrl == null) {

            String textContent = messageEditView.getText().toString();
            newMessage.setType(Message.Type.TEXT);
            newMessage.setContent(textContent);

            messageEditView.setText("");
        }
        // image message
        else if(imageUrl != null) {

            newMessage.setType(Message.Type.IMAGE);
            newMessage.setContent(imageUrl);

            imageUrl = null;
        }
        else if(recordUrl != null){
            newMessage.setType(Message.Type.SOUND);
            newMessage.setContent(recordUrl);

            recordUrl = null;
        }
        else{
            Log.e(Constant.NEARBY_CHAT, "Unknow message type");
        }

        newMessage.setDate(new Date());
        newMessage.setSenderId(DatabaseUtils.getCurrentUUID());

        String id = DatabaseUtils.getMessagesByConversationId(conversationId)
                .push()
                .getKey();

        newMessage.setId(id);

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .child(id)
                .setValue(newMessage);

       // userLastMessage.setText((CharSequence) newMessage);



    }

    private void sendImage(Bitmap image) {

        StorageReference storageReference = DatabaseUtils.getStorageDatabase().getReference(imagePath);
        DatabaseUtils.savePictureOnline(image, storageReference, taskSnapshot -> {
            Log.w(Constant.NEARBY_CHAT, "Image uploaded, now sending message");
            // send a image message
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                imageUrl = e.toString();
                sendMessage();
            });

        }, e -> Log.w(Constant.NEARBY_CHAT, e.getMessage()));
    }

    private void sendRecord(){
        StorageReference storageReference = DatabaseUtils.getStorageDatabase().getReference(recordPath);
        DatabaseUtils.saveRecordOnline(recordPath, storageReference, taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                recordUrl = e.toString();
                sendMessage();
            });
        }, e -> Log.w(Constant.NEARBY_CHAT, e.getMessage()));

    }

    private void showImageAttachementDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasWritePermission(this)) {
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_PERMISSION, GALLERY);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasWritePermission(this)) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, GALLERY);
        }
    }

    private void takePhotoFromCamera() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasCameraPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION[0], WRITE_EXTERNAL_PERMISSION[0]}, CAMERA);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasCameraPermission(this) ||
                PermissionUtils.hasWritePermission(this)) {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".my.package.name.provider",
                    FileUtils.createFileWithExtension("jpg"));

            takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePhotoIntent, CAMERA);
        }
    }

    private void voiceRecordingAction(){

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasAudioRecordPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_PERMISSION[0], RECORD_AUDIO_PERMISSION[0]}, RECORD_AUDIO);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasAudioRecordPermission(this)
                || PermissionUtils.hasWritePermission(this)) {

            if(!recording){
                Toast.makeText(ChatActivity.this, "Started voice recording", Toast.LENGTH_SHORT).show();
                messageRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_24px));

                initializeMediaRecord();
                startRecordingAudio();
            }
            else{
                Toast.makeText(ChatActivity.this, "Stopped voice recording", Toast.LENGTH_SHORT).show();

                messageRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_black_24px));
                stopRecordingAudio();
                sendRecord();
            }
            recording = !recording;
        }
    }

    private void startRecordingAudio(){
        File audioFile = FileUtils.createFileWithExtension("3gpp");
        recordUrl = null;
        recordPath = audioFile.getAbsolutePath();
        mediaRecorder.setOutputFile(recordPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio(){

        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();

            mediaRecorder = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imagePath = saveImage(image);

                    sendImage(resizedImage);

                    Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePath = saveImage(image);
                Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                sendImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery();

                } else {
                    Toast.makeText(this, "GALLERY DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();

                } else {
                    Toast.makeText(this, "CAMERA DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    voiceRecordingAction();
                } else {
                    Toast.makeText(this, "RECORD AUDIO DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }


        }
    }

    public String saveImage(Bitmap myBitmap) {

        File file = FileUtils.createFileWithExtension("jpg");
        resizedImage = ImageUtils.resizeImage(myBitmap);
        ByteArrayOutputStream bytes = ImageUtils.compressImage(resizedImage);

        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()},
                    new String[]{"image/jpeg"}, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", "File Saved::--->" + file.getAbsolutePath());

        return file.getAbsolutePath();
    }


    private String getConversationId(String partnerId) {

        String myId = DatabaseUtils.getCurrentUUID();
        if (myId.compareTo(partnerId) < 0) {
            return myId + "-" + partnerId;
        } else {
            return partnerId + "-" + myId;
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

        messageListView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.getMessagesByConversationId(conversationId).removeEventListener(messageListener);
    }
    @NonNull
    private StorageReference getStorageReference() {
        return firebaseStorage.getReference("profile/" + conversationPartner.getId() + ".jpeg");
    }

    private void loadProfileImage() {
        ImageView profileImage = (ImageView) findViewById(R.id.fotolawan);
        StorageReference reference = getStorageReference();

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            conversationPartner.setAvatar(avatar);
            profileImage.setImageBitmap(avatar);


        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.w(NEARBY_CHAT, "loadProfileImage: ", exception);
        });

    }
}
