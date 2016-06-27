package com.tohure.betagcm.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.tohure.betagcm.Constantes;
import com.tohure.betagcm.MainActivity;
import com.tohure.betagcm.R;

import java.util.ArrayList;
import java.util.List;

public class AWSHelper {

    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences sharedPreferences;
    private static String userMail;
    private static String token_gcm;
    private static Context context;
    private static AmazonSNSClient snsClient;
    private static ProgressDialog progressDialog;

    public static void initVariables(Context applicationContext, String token) {
        //context aplication
        context = applicationContext;

        //progress dialog main
        progressDialog = MainActivity.progressDialog;

        //token gcm
        token_gcm = token;

        //get aplications preferences and de userEmail
        final String preferences = context.getString(R.string.preferences);
        sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE);
        userMail = sharedPreferences.getString(context.getString(R.string.user_mail), "chuaman@gruporpp.com.pe");

        AWSSaveInCognito();
    }

    public static void AWSSaveInCognito() {

        // the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(context, Constantes.IDENTITY_POOL, Regions.US_EAST_1);

        //Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(context, Regions.US_EAST_1, credentialsProvider);

        //Create a record in a dataset and synchronize with the server
        Dataset dataset = syncClient.openOrCreateDataset("RPP_Notifications_Beta");
        dataset.put("GCM_Token", token_gcm);
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                Log.i("thr","Cognito dataset");
                new AWSCreateEndpointTask().execute();
            }

            @Override
            public void onFailure(DataStorageException dse) {
                Log.e("thr cognito",dse.toString());
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                List<Record> resolvedRecords = new ArrayList<Record>();
                for (SyncConflict conflict : conflicts) {
                    /* resolved by taking remote records */
                    resolvedRecords.add(conflict.resolveWithRemoteRecord());

                    /* alternately take the local records */
                    //resolvedRecords.add(conflict.resolveWithLocalRecord());

                    /* or customer logic, say concatenate strings */
                    // String newValue = conflict.getRemoteRecord().getValue()
                    //     + conflict.getLocalRecord().getValue();
                    // resolvedRecords.add(conflict.resolveWithValue(newValue);
                }
                dataset.resolve(resolvedRecords);
                Log.i("thr cognito","resolve");
                // return true so that synchronize() is retried after conflicts are resolved
                return true;
            }
        });
    }

    static class AWSCreateEndpointTask extends AsyncTask<Void, Void,  CreatePlatformEndpointResult> {

        @Override
        protected CreatePlatformEndpointResult doInBackground(Void... params) {
            snsClient = new AmazonSNSClient(credentialsProvider);
            snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

            try {
                CreatePlatformEndpointRequest request = new CreatePlatformEndpointRequest();

                request.setCustomUserData(userMail);
                request.setToken(token_gcm);
                request.setPlatformApplicationArn(Constantes.ARN_APLICATION);

                return snsClient.createPlatformEndpoint(request);

            }catch(Exception ex){
                Log.e("thr exep",ex.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(CreatePlatformEndpointResult result) {

            if(result != null) {
                String endpointArn = result.getEndpointArn();
                editor = sharedPreferences.edit();
                editor.putString(context.getString(R.string.end_point), endpointArn).apply();
                Log.i("thr endpoint",endpointArn);

                new AWSSubscribeToTopicTask().execute();
            }
        }
    }

    static class AWSSubscribeToTopicTask extends AsyncTask<Void, Void, SubscribeResult>{

        @Override
        protected SubscribeResult doInBackground(Void... params) {

            String endpointArn = sharedPreferences.getString(context.getString(R.string.end_point), "null");

            if (endpointArn.equals("null")){
                return null;
            }else{
                try {
                    SubscribeRequest subRequest = new SubscribeRequest(Constantes.ARN_TOPIC, "application", endpointArn);
                    return snsClient.subscribe(subRequest);
                } catch (Exception e) {
                    Log.e("thr topic",e.getMessage());
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(SubscribeResult subscribeResult) {

            if (subscribeResult != null){
                String subscriptionArn = subscribeResult.getSubscriptionArn();

                editor.putString(context.getString(R.string.topic_subscription), subscriptionArn).apply();

                if (progressDialog.isShowing()){

                    SharedPreferences defaultValues = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = defaultValues.edit();
                    editor.putBoolean(context.getString(R.string.first_launch), false);
                    editor.apply();

                    progressDialog.dismiss();
                    Toast.makeText(context,"Registro terminado",Toast.LENGTH_LONG).show();

                    MainActivity.showThanks();
                    //TODO: lanzar servicio propiamente dicho
                }

                Log.i("thr topic",subscriptionArn);
            }else{
                Log.e("thr topic","failed");
            }
        }
    }
}