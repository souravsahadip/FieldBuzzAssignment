package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.*;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class FirstFragment extends Fragment {
    private EditText fieldUsername;
    private EditText fieldPassword;
    private Button buttonLogin;
    String auth_token = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        return view;
    }

    void initialize() {
        fieldUsername = getActivity().findViewById(R.id.fieldUsername);
        fieldPassword = getActivity().findViewById(R.id.fieldPassword);
        buttonLogin = getActivity().findViewById(R.id.buttonLogin);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiAuthentication();
            }
        });
    }

    public void apiAuthentication() {
//        String email = mEmailField.getText().toString();
//        String password = mPasswordField.getText().toString();
//        email="1505003.ssd@ugrad.cse.buet.ac.bd";
//        password="r4TpFaw84";
//        Log.d("email",email);
//        Log.d("password",password);

        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject jsonParams = new JSONObject();
        try {
            String username, password;
            // username="1505003.ssd@ugrad.cse.buet.ac.bd";
            // password="r4TpFaw84";
            username = fieldUsername.getText().toString();
            password = fieldPassword.getText().toString();
            jsonParams.put("username", username);
            jsonParams.put("password", password);

            StringEntity entity = new StringEntity(jsonParams.toString());

            client.post(getContext(), "https://recruitment.fisdev.com/api/login/", entity, "application/json",
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("Authentication response", String.valueOf(response));
                            try {
                                auth_token = (String) response.get("token");
                                Log.d("auth_token", auth_token);
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
                            fieldPassword.setError("Invalid Username/Password");
                        }
                    });


        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}