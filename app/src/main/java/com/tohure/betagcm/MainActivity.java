package com.tohure.betagcm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tohure.betagcm.helpers.AWSHelper;
import com.tohure.betagcm.helpers.NetworkHelper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static SharedPreferences savedValues;
    public static ProgressDialog progressDialog;
    private EditText email;
    private LinearLayout register_box;
    private static LinearLayout thanks_box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String preferences = getString(R.string.preferences);
        savedValues = getSharedPreferences(preferences, Context.MODE_PRIVATE);
        initUI();
    }

    private void initUI() {
        register_box = (LinearLayout) findViewById(R.id.registro_box);
        thanks_box = (LinearLayout) findViewById(R.id.thanks_box);
        email = (EditText) findViewById(R.id.edtMail);
        Button registro_button = (Button) findViewById(R.id.btn_regis);
        registro_button.setOnClickListener(this);
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_regis){
            iniciarRegistro();
            progressDialog = ProgressDialog.show(this, "", "Creando cuenta. Espere por favor...", true);
        }
    }

    private void iniciarRegistro() {
        String mail = String.valueOf(email.getText());
        if (isValidEmail(mail)){
            SharedPreferences defaultValues = PreferenceManager.getDefaultSharedPreferences(this);
            if(defaultValues.getBoolean(getString(R.string.first_launch), true)){
                if (NetworkHelper.haveNetworkConnection(this)){
                    SharedPreferences.Editor editor_saved = savedValues.edit();
                    editor_saved.putString(getString(R.string.user_mail), mail);
                    editor_saved.apply();
                    register_box.setVisibility(View.GONE);
                    new RegisterPhoneTask().execute();
                    Log.i("thr", "Entro 0");
                }else{
                    Toast.makeText(this,"Por favor conéctese a una red antes de continuar",Toast.LENGTH_SHORT).show();
                }
            }else{
                Log.i("thr", "Entro 0.1");
            }
        }else{
            Toast.makeText(this,"Por favor ingrese un DNI correcto",Toast.LENGTH_SHORT).show();
        }
    }

    public static void showThanks(){ thanks_box.setVisibility(View.VISIBLE); }

    class RegisterPhoneTask extends AsyncTask<Object, Object, Boolean> {

        private GoogleCloudMessaging gcm;
        String token = null;

        @Override
        protected Boolean doInBackground(Object... objects) {
            Log.i("thr", "Entro 1");
            try {
                if (gcm == null){ gcm = GoogleCloudMessaging.getInstance(getApplicationContext()); }
                token = gcm.register(getString(R.string.project_number));

                Log.i("Registration Successful", token);
                return true;
            }
            catch (IOException e) {
                Log.i("Registration Error", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean && token != null){
                AWSHelper.initVariables(getApplicationContext(),token);
            }
        }
    }
}