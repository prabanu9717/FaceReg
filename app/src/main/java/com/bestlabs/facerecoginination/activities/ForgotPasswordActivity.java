package com.bestlabs.facerecoginination.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bestlabs.facerecoginination.NetworkManager.APIClient;
import com.bestlabs.facerecoginination.NetworkManager.APIInterface;
import com.bestlabs.facerecoginination.R;
import com.bestlabs.facerecoginination.models.ForgotPasswordModel;
import com.bestlabs.facerecoginination.models.LoginUserModel;
import com.bestlabs.facerecoginination.others.AlertDialogHelper;
import com.bestlabs.facerecoginination.others.Constants;
import com.bestlabs.facerecoginination.others.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity  extends AppCompatActivity {
    EditText mEmail;
    Button mSubmitBtn;
    private  AlertDialog dialog;
    Context context;
    private ConstraintLayout constraintLayout;

    private APIInterface apiService;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        apiService = APIClient.getClient().create(APIInterface.class);

        initialize();
        clickListners();
    }

    private void initialize() {
        context = ForgotPasswordActivity.this;
        mEmail = findViewById(R.id.edt_email);
        mSubmitBtn = (Button) findViewById(R.id.btn_submit);
        constraintLayout = findViewById(R.id.constraint_layout);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setPadding(10,30,10,30);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.setView(progressBar);
    }

    private void clickListners(){
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                if (email.isEmpty()) {
                    mEmail.setError("Please enter email");
                    return;
                }

                if (!isValidEmail(email)) {
                    mEmail.setError("Invalid Email");
                    return;
                }
                if (email.isEmpty())
                    Toast.makeText(ForgotPasswordActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                else {
                    postForgotPasswordRequest(email);
                }
            }
        });
    }

    // Method to make the POST request
    public void postForgotPasswordRequest(String userName) {
        dialog.show();
        Call<ForgotPasswordModel> call = apiService.forgotPassword(userName);

        call.enqueue(new Callback<ForgotPasswordModel>() {
            @Override
            public void onResponse(Call<ForgotPasswordModel> call, Response<ForgotPasswordModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle successful response
                    Log.e("response", ""+response.body());
                    dialog.dismiss();
                    ForgotPasswordModel result = response.body();
                    if (result.getStatus().equals("success")) {

                        Intent intent = new Intent(getBaseContext(), ResetPasswordActivity.class);
                        intent.putExtra("temp_otp", result.getTemp_otp());
                        finish();
                        startActivity(intent);
                    } else {
                        AlertDialogHelper.showSnackbar(constraintLayout,"Forgot password Failed. Please Try Again" );
                    }
                } else {
                    // Handle unsuccessful response
                    // Example of using the AlertDialogHelper to show an alert dialog
                    Log.e("response", ""+response.errorBody());
                    dialog.dismiss();
                    AlertDialogHelper.showSnackbar(constraintLayout,"Forgot password Failed. Please Try Again" );
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordModel> call, Throwable t) {
                // Handle failure
                Log.e("response", ""+t.getLocalizedMessage());
                dialog.dismiss();
                AlertDialogHelper.showSnackbar(constraintLayout,"Forgot password Failed. Please Try Again" );
            }
        });
    }


    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern p = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = p.matcher(email);
        return matcher.matches();
    }

    // Here is validating password
    private boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 6) {
            return true;
        }
        return false;
    }
}

