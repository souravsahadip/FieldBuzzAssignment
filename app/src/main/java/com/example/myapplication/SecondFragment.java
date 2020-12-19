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
import androidx.navigation.fragment.NavHostFragment;

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
import java.util.UUID;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.message.BasicHeader;


public class SecondFragment extends Fragment {

    public static final int PICKFILE_RESULT_CODE = 1;
    private static final int PICK_PDF_FILE = 2;

    UUID tsync_id = UUID.randomUUID();
    UUID cv_file_tsync_id = UUID.randomUUID();
    String url_test = "https://recruitment.fisdev.com/api/v1/recruiting-entities/";
    TextView textView_token;
    String auth_token = "";
    JSONObject applicant_info = new JSONObject();
    Uri fileUri;
    JSONObject cv_file_json;
    String cv_file_name;
    boolean hasChosenCV = false, isFirstSubmit = true;
    long creation_time = 0;
    Button btnChoosefile, btnSubmit;
    EditText fieldName, fieldEmail, fieldPhone, fieldFullAddress, fieldUniversity, fieldGraduation,
            fieldCgpa, fieldExperience, fieldCurWork, fieldSalary, fieldReference, fieldGithub;
    RadioGroup fieldApplying;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    void initialize(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView_token = view.findViewById(R.id.textView_token);
        btnChoosefile = view.findViewById(R.id.button_chooseFile);
        btnSubmit = view.findViewById(R.id.button_submit);
        fieldName = view.findViewById(R.id.fieldName);
        fieldEmail = view.findViewById(R.id.fieldEmail);
        fieldPhone = view.findViewById(R.id.fieldPhone);
        fieldFullAddress = view.findViewById(R.id.fieldFullAddress);
        fieldUniversity = view.findViewById(R.id.fieldUniversity);
        fieldGraduation = view.findViewById(R.id.fieldGraduation);
        fieldCgpa = view.findViewById(R.id.fieldCgpa);
        fieldExperience = view.findViewById(R.id.fieldExperience);
        fieldCurWork = view.findViewById(R.id.fieldCurrentWorkplace);
        fieldSalary = view.findViewById(R.id.fieldSalary);
        fieldReference = view.findViewById(R.id.fieldReference);
        fieldGithub = view.findViewById(R.id.fieldGithub);
        fieldApplying = view.findViewById(R.id.fieldApplying);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        initialize(view, savedInstanceState);

        if (getArguments().getString("auth_token") != null)
            auth_token = getArguments().getString("auth_token");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = validateForm(view);
                if (isValid == true)
                    api__post_info();
            }
        });

        btnChoosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_FILE);
            }
        });
    }

    void api__post_info() {
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            StringEntity entity = new StringEntity(applicant_info.toString());
            Header[] headers = new Header[1];
            String token = "Token " + auth_token;
            headers[0] = new BasicHeader("Authorization", token);

            client.post(getContext(), url_test, headers, entity, "application/json",
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("response", String.valueOf(response));
                            TextView tx = getActivity().findViewById(R.id.textHttperror);
                            tx.setText("Success posting applicant info");
                            try {
                                String message = (String) response.get("message");
                                cv_file_json = (JSONObject) response.get("cv_file");
                                int cv_file_id = 0;
                                cv_file_id = cv_file_json.getInt("id");
                                String cv_file_tsync_id = (String) cv_file_json.get("tsync_id");
                                api_submit_file(cv_file_id, cv_file_tsync_id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            TextView tx = getActivity().findViewById(R.id.textHttperror);
                            tx.setText("Failed posting applicant info");
                        }
                    });


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    boolean api_submit_file(int cv_file_id, String cv_file_tsync_id) {

        AsyncHttpClient client = new AsyncHttpClient();

        try {
            String url_file_submit = "https://recruitment.fisdev.com/api/file-object/" + cv_file_id + "/";
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            File outputDir = getActivity().getCacheDir(); // context being the Activity pointer
            File file = File.createTempFile("prefix", "extension", outputDir);
            file.setWritable(true);
            FileUtils.copyToFile(inputStream, file);
            Log.d("fileGetPath", String.valueOf(file.length()));
            String boundary = "----WebKitFormBoundaryHFfQGfqvJGRfItxt";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setBoundary(boundary);
            builder.addBinaryBody("file", file, ContentType.parse("application/pdf"), cv_file_name);
            HttpEntity entity = builder.build();
            Header[] headers = new Header[2];
            String token = "Token " + auth_token;
            headers[0] = new BasicHeader("Authorization", token);
            headers[1] = new BasicHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

            client.put(getContext(), url_file_submit, headers, entity, "multipart/form-data",
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            Log.d("responseFile", String.valueOf(response));
                            TextView tx = getActivity().findViewById(R.id.textHttperror);
                            tx.setText("Success submitting cv file");
                            try {
                                String message = String.valueOf(response);
                                Bundle bundle = new Bundle();
                                bundle.putString("server_response", message);
                                NavHostFragment.findNavController(SecondFragment.this)
                                        .navigate(R.id.action_SecondFragment_to_ThirdFragment, bundle);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject errorResponse) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            super.onFailure(statusCode, headers, String.valueOf(errorResponse), t);
                            Log.d("Failed: ", "" + statusCode);
                            Log.d("Error : ", "" + t);
                            Log.d("errorResponse : ", "" + errorResponse);
                            TextView tx = getActivity().findViewById(R.id.textHttperror);
                            tx.setText("Failed submitting cv file");
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
                cv_file_name = getPath(fileUri);
                hasChosenCV = true;
                Log.d("cv_file_name", cv_file_name);
                TextView tx = getActivity().findViewById(R.id.textChosenfile);
                tx.setText(cv_file_name);
            }
        }
    }


    public String getPath(Uri uri) {

        String path = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor == null) {
            Log.d("cursor", "null");
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            String displayName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            Log.d("Display Name: ", displayName);
            Log.d("cursor", cursor.toString());
            path = displayName;
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    boolean validateForm(@NonNull View view) {

        JSONObject new_applicant_info = new JSONObject();
        boolean isValid = true;

        try {

            String name = fieldName.getText().toString();
            if (name.length() > 256 || name.length() < 1) {
                isValid = false;
                if (name.length() > 256)
                    fieldName.setError("Name too long");
                else fieldName.setError("Name too short");
            }

            String email = fieldEmail.getText().toString();
            if (email.length() > 256 || email.length() < 1 || isEmail(email) == false) {
                isValid = false;
                if (isEmail(email) == false)
                    fieldEmail.setError("Invalid Email");
                else if (email.length() > 256)
                    fieldEmail.setError("Email too long");
                else fieldEmail.setError("Email can not be blank");

            }

            String phone = fieldPhone.getText().toString();
            if (phone.length() > 14 || phone.length() < 1) {
                isValid = false;
                if (phone.length() > 14)
                    fieldPhone.setError("Max 14 digits");
                else fieldPhone.setError("too short");
            }

            String address = fieldFullAddress.getText().toString();
            if (address.length() > 512) {
                isValid = false;
                fieldFullAddress.setError("Address too long");
            }

            String university = fieldUniversity.getText().toString();
            if (university.length() > 256 || university.length() < 1) {
                isValid = false;
                if (university.length() > 256)
                    fieldUniversity.setError("university name too long");
                else fieldUniversity.setError("university name too short");
            }

            String gradYear = fieldGraduation.getText().toString();
            if (gradYear.length() < 1) {
                isValid = false;
                fieldGraduation.setError("Can not be blank");
            } else {
                try {
                    int gradYear_int = Integer.parseInt(gradYear);
                    if (gradYear_int < 2015 || gradYear_int > 2020) {
                        isValid = false;
                        fieldGraduation.setError("Graduation year must be between 2015 and 2020");
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    fieldGraduation.setError("Graduation year must be between 2015 and 2020");
                }
            }

            String cgpa = fieldCgpa.getText().toString();
            if (cgpa.length() > 0) {
                try {
                    double cgpa_double = Double.parseDouble(cgpa);
                    if (cgpa_double < 2 || cgpa_double > 4) {
                        isValid = false;
                        fieldCgpa.setError("CGPA  must be between 2.0 and 4.0");
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    fieldCgpa.setError("CGPA  must be between 2.0 and 4.0");
                }
            }

            String exp = fieldExperience.getText().toString();
            if (exp.length() > 0) {
                try {
                    int expMonth = Integer.parseInt(exp);
                    if (expMonth < 0 || expMonth > 100) {
                        isValid = false;
                        fieldExperience.setError("month should be an integer between 0-100");
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    fieldExperience.setError("month should be an integer between 0-100");
                }
            }

            String curWorkplace = fieldCurWork.getText().toString();
            if (curWorkplace.length() > 256) {
                isValid = false;
                fieldCurWork.setError("Max 256 characters");
            }

            int radioButtonID = fieldApplying.getCheckedRadioButtonId();
            if (radioButtonID <= 0) {
                isValid = false;
                RadioButton lastRadioBtn = (RadioButton) getActivity().findViewById(R.id.radio2);
                lastRadioBtn.setError("Select a department");
            }

            String salary = fieldSalary.getText().toString();
            if (salary.length() < 1) {
                isValid = false;
                fieldSalary.setError("Can not be blank");
            } else {
                try {
                    int salary_int = Integer.parseInt(salary);
                    if (salary_int < 15000 || salary_int > 60000) {
                        isValid = false;
                        fieldSalary.setError("Salary must be between 15000 and 60000");
                    }
                } catch (NumberFormatException e) {
                    isValid = false;
                    fieldSalary.setError("Salary must be between 15000 and 60000");
                }
            }

            String reference = fieldReference.getText().toString();
            if (reference.length() > 256) {
                isValid = false;
                fieldReference.setError("Max 256 characters");
            }

            String githubUrl = fieldGithub.getText().toString();
            if (githubUrl.length() > 512 || githubUrl.length() < 1 || urlValidator(githubUrl) == false) {
                isValid = false;
                if (urlValidator(githubUrl) == false)
                    fieldGithub.setError("Enter a valid github Url");
                else fieldGithub.setError("Invalid Length");
            }

            if (hasChosenCV == false) {
                isValid = false;
                btnChoosefile.setError("must attach a pdf file");
            }


            //////////

            if (isValid == true) {
                JSONObject cv_file = new JSONObject();
                long unixTime = System.currentTimeMillis();
                if (isFirstSubmit == true) {
                    isFirstSubmit = false;
                    creation_time = unixTime;
                }

                View radioButton = fieldApplying.findViewById(radioButtonID);
                int idx = fieldApplying.indexOfChild(radioButton);
                RadioButton r = (RadioButton) fieldApplying.getChildAt(idx);
                String applyingIn = r.getText().toString();

                cv_file.put("tsync_id", cv_file_tsync_id);
                applicant_info.put("tsync_id", tsync_id);
                applicant_info.put("name", name);
                applicant_info.put("email", email);
                applicant_info.put("phone", phone);
                if (address.length() > 0)
                    applicant_info.put("full_address", address);
                applicant_info.put("name_of_university", university);
                applicant_info.put("graduation_year", Integer.parseInt(gradYear));
                try {
                    applicant_info.put("cgpa", Double.parseDouble(cgpa));
                } catch (NumberFormatException e) {
                    // applicant_info.put("cgpa",Double.parseDouble(cgpa));
                }
                try {
                    applicant_info.put("experience_in_months", Integer.parseInt(exp));
                } catch (NumberFormatException e) {
                    // applicant_info.put("experience_in_months",Integer.parseInt(exp));
                }

                if (curWorkplace.length() > 0)
                    applicant_info.put("current_work_place_name", curWorkplace);
                applicant_info.put("applying_in", applyingIn);
                applicant_info.put("expected_salary", Integer.parseInt(salary));
                if (reference.length() > 0)
                    applicant_info.put("field_buzz_reference", reference);
                applicant_info.put("github_project_url", githubUrl);
                applicant_info.put("cv_file", cv_file);
                applicant_info.put("on_spot_update_time", unixTime);
                applicant_info.put("on_spot_creation_time", creation_time);

//                applicant_info.put("tsync_id",tsync_id);
//                applicant_info.put("name","Sourav Saha Dip");
//                applicant_info.put("email","1505003.ssd@ugrad.cse.buet.ac.bd");
//                applicant_info.put("phone","01843427662");
//                applicant_info.put("full_address","Khulna");
//                applicant_info.put("name_of_university","BUET");
//                applicant_info.put("graduation_year",2021);
//                applicant_info.put("cgpa",3.18);
//                applicant_info.put("experience_in_months",0);
//                applicant_info.put("current_work_place_name","None");
//                applicant_info.put("applying_in","Frontend");
//                applicant_info.put("expected_salary",50000);
//                applicant_info.put("field_buzz_reference","None");
//                applicant_info.put("github_project_url","https://github.com/souravsahadip/FieldBuzzAssignment");
//                applicant_info.put("cv_file",cv_file);
//                applicant_info.put("on_spot_update_time",unixTime);
//                applicant_info.put("on_spot_creation_time",creation_time);
            } else Log.e("invalid", "invalid");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    boolean isEmail(String email) {
        String pattern = "^[\\w_!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+$";
        return Pattern.matches(pattern, email);
    }

    public boolean urlValidator(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException exception) {
            return false;
        } catch (MalformedURLException exception) {
            return false;
        }
    }

}