package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

public class SecondFragment extends Fragment {

    String url_test= "https://recruitment.fisdev.com/api/v0/recruiting-entities/";
    TextView textView_token;
    String auth_token="";
    JSONObject applicant_info = new JSONObject();

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
    }

    boolean api__post_info(){
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
//                                Bundle bundle = new Bundle();
//                                bundle.putString("auth_token", auth_token);
//                                NavHostFragment.findNavController(FirstFragment.this)
//                                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
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
        return true;
    }

    boolean api_submit_file(){
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
//                                Bundle bundle = new Bundle();
//                                bundle.putString("auth_token", auth_token);
//                                NavHostFragment.findNavController(FirstFragment.this)
//                                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
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
        return true;
    }

    boolean validateForm(){
        String name,email,phone,full_address,name_of_university,graduation_year,cgpa,experience_in_months,
                current_work_place_name,applying_in,expected_salary;
        UUID tsync_id=UUID.randomUUID();
        UUID cv_file_tsync_id=UUID.randomUUID();
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