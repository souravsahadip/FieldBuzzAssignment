package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.message.BasicHeader;


public class SecondFragment extends Fragment {

    public static final int PICKFILE_RESULT_CODE = 1;
    private static final int PICK_PDF_FILE = 2;

    UUID tsync_id=UUID.randomUUID();
    UUID cv_file_tsync_id=UUID.randomUUID();
    String url_test= "https://recruitment.fisdev.com/api/v0/recruiting-entities/";
    TextView textView_token;
    String auth_token="";
    JSONObject applicant_info = new JSONObject();
    Button buttonChooseFile;
     Uri fileUri;
     String filePath;
    File file;
    JSONObject cv_file_json;
    String cv_file_name;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments().getString("auth_token")!=null)
            auth_token= getArguments().getString("auth_token");
        textView_token=view.findViewById(R.id.textView_token);
        buttonChooseFile =  view.findViewById(R.id.button_chooseFile);
        textView_token.setText(auth_token);
        validateForm();

        view.findViewById(R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);

                api__post_info();

            }
        });

        buttonChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//                chooseFile.setType("*/*");
//                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
//                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_FILE);
            }
        });
    }

    void api__post_info(){
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            StringEntity entity = new StringEntity(applicant_info.toString());
            Header[] headers= new Header[1];
            String token="Token "+ auth_token;
            headers[0]=new BasicHeader("Authorization", token);

            client.post(getContext(),url_test, headers,entity, "application/json",
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("response", String.valueOf(response));
                            try {
                                String message= (String) response.get("message");
                                textView_token.setText(message);
                                 cv_file_json= (JSONObject) response.get("cv_file");
                                int cv_file_id= 0;
                                cv_file_id = cv_file_json.getInt("id");
                                String cv_file_tsync_id= (String) cv_file_json.get("tsync_id");
                                api_submit_file(cv_file_id,cv_file_tsync_id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        }
                    });


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    boolean api_submit_file(int cv_file_id, String cv_file_tsync_id){

        AsyncHttpClient client = new AsyncHttpClient();

        try {
            String url_file_submit="https://recruitment.fisdev.com/api/file-object/"+cv_file_id+"/";
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
             file = new File(path, "/" + "cv.pdf");
            file.setWritable(true);
            FileUtils.copyToFile(inputStream,file);
            //file=copyInputStreamToFile(inputStream,file);
            Log.d("fileGetPath", String.valueOf(file.length()));
            String boundary="----WebKitFormBoundaryHFfQGfqvJGRfItxt";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
           // builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            builder.addBinaryBody("file",new FileInputStream(file),
//                    ContentType.DEFAULT_BINARY, file.getName());
            builder.setBoundary(boundary);
            builder.addBinaryBody("file",file,
                    ContentType.parse("application/pdf"), cv_file_name);
            HttpEntity entity = builder.build();
            Header[] headers= new Header[2];
            String token="Token "+ auth_token;
            headers[0]=new BasicHeader("Authorization", token);
            headers[1]=new BasicHeader("Content-Type", "multipart/form-data; boundary="+boundary);

            client.put(getContext(),url_file_submit, headers,entity, "multipart/form-data",
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("responseFile", String.valueOf(response));
                            try {
                                String message= (String) response.get("message");
                                textView_token.setText(message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable t,JSONObject errorResponse) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            super.onFailure(statusCode, headers, String.valueOf(errorResponse), t);
                            Log.d("Failed: ", ""+statusCode);
                            Log.d("Error : ", "" + t);
                            Log.d("errorResponse : ", "" + errorResponse);
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                fileUri = resultData.getData();
                cv_file_name=getPath(fileUri);
                Log.d("cv_file_name",cv_file_name);
            }
        }
    }


    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor =getContext().getContentResolver().query(uri, null, null, null, null);

        if(cursor == null){
            Log.d("cursor","null");
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            String displayName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            Log.d("Display Name: ", displayName);
            Log.d("cursor",cursor.toString());
            path = displayName;
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    boolean validateForm(){

        JSONObject cv_file=new JSONObject();
        long unixTime = System.currentTimeMillis();

        try {
            cv_file.put("tsync_id",cv_file_tsync_id);
            applicant_info.put("tsync_id",tsync_id);
            applicant_info.put("name","Sourav Saha Dip");
            applicant_info.put("email","1505003.ssd@ugrad.cse.buet.ac.bd");
            applicant_info.put("phone","01843427662");
            applicant_info.put("full_address","Khulna");
            applicant_info.put("name_of_university","BUET");
            applicant_info.put("graduation_year",2021);
            applicant_info.put("cgpa",3.18);
            applicant_info.put("experience_in_months",0);
            applicant_info.put("current_work_place_name","None");
            applicant_info.put("applying_in","Frontend");
            applicant_info.put("expected_salary",50000);
            applicant_info.put("field_buzz_reference","None");
            applicant_info.put("github_project_url","https://github.com/souravsahadip/FieldBuzzAssignment");
            applicant_info.put("cv_file",cv_file);
            applicant_info.put("on_spot_update_time",unixTime);
            applicant_info.put("on_spot_creation_time",unixTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}