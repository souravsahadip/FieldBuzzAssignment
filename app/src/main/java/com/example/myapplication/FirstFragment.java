package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.loopj.android.http.*;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class FirstFragment extends Fragment implements View.OnClickListener{
    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView mText;
    private Button buttonLogin;
    String auth_token="";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_first, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmailField = (EditText)view.findViewById(R.id.idUsername);
        mPasswordField = (EditText) view.findViewById(R.id.idPassword);
        mText = (TextView) view.findViewById(R.id.idTextResponse);
        buttonLogin=view.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);

//        view.findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Bundle bundle = new Bundle();
//                bundle.putString("amount", String.valueOf(2000));
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
//
//            }
//        });
    }

    @Override
    public void onClick(View v)  {
//        String email = mEmailField.getText().toString();
//        String password = mPasswordField.getText().toString();
//        email="1505003.ssd@ugrad.cse.buet.ac.bd";
//        password="r4TpFaw84";
//        Log.d("email",email);
//        Log.d("password",password);

        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonParams = new JSONObject();
        try {
            String email;
            String password;
            email="1505003.ssd@ugrad.cse.buet.ac.bd";
            password="r4TpFaw84";
            jsonParams.put("username", email);
            jsonParams.put("password",password);
            StringEntity entity = new StringEntity(jsonParams.toString());

            client.post(getContext(),"https://recruitment.fisdev.com/api/login/", entity, "application/json",
                    new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                    // Handle resulting parsed JSON response here
                    Log.d("response", String.valueOf(response));
                    try {
                        auth_token= (String) response.get("token");
                        mText.setText(auth_token);
                        Bundle bundle = new Bundle();
                bundle.putString("auth_token", auth_token);
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }
            });


        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }




        ///////////////
//        try {
//                    String email;
//                    String password;
//                    email="1505003.ssd@ugrad.cse.buet.ac.bd";
//                    password="r4TpFaw84";
//                    URL url = new URL("https://recruitment.fisdev.com/api/login/");
//                    HttpURLConnection uc = null;
//                    uc = (HttpURLConnection) url.openConnection();
//                    String line;
//                    StringBuffer jsonString = new StringBuffer();
//                    String payload="{\n"+"\"username\":\""+email+"\",\n\"password\":\""+password+"\"\n}";
//                    Log.d("payload",payload);
//
//                    uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//                    uc.setRequestMethod("POST");
//                    uc.setDoInput(true);
//                    uc.setInstanceFollowRedirects(false);
//                    uc.connect();
//                    OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
//                    writer.write(payload);
//                    writer.close();
//                    try {
//                        BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//                        while((line = br.readLine()) != null){
//                            jsonString.append(line);
//                        }
//                        br.close();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    uc.disconnect();
//                    mText.setText(jsonString);
//                    Log.d("jsonString", String.valueOf(jsonString));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//





    }
}