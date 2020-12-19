package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.message.BasicHeader;


public class ThirdFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false);
    }

    void initialize(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        initialize(view, savedInstanceState);
        String message;

        if (getArguments().getString("server_response") != null) {
            message = getArguments().getString("server_response");
            StringBuilder newMessage = new StringBuilder();
            try {
                JSONObject jsonObject = new JSONObject(message);
//                for (Iterator key = jsonObject.keys(); key.hasNext();) {
//                    String key_string=String.valueOf(key.next());
//                    String value =  jsonObject.get(key_string).toString();
//                    newMessage.append(key+"\t"+value+"\n");
//                }
                for (int i = 0; i < jsonObject.names().length(); i++) {
                    String key_string = jsonObject.names().getString(i);
                    String value = jsonObject.get(key_string).toString();
                    newMessage.append(key_string + ":\t" + value + "\n");
                }
                TextView tx = getActivity().findViewById(R.id.textServerResponse);
                tx.setText(newMessage.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}