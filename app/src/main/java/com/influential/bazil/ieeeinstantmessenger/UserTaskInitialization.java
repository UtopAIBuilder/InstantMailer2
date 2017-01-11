package com.influential.bazil.ieeeinstantmessenger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.util.HashSet;
import java.util.Set;

public class UserTaskInitialization extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    EditText et2;
    AutoCompleteTextView customitemname;
    String[] Emails;
    ProgressBar progressBar;
    private String EmailId;
    private String Pass;
    private static final int REQUEST_CODE_SELECT = 102;
    private static final int REQUEST_CODE_RESOLUTION = 103;
    private int count = 0;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_task_initialization);
      customitemname = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        customitemname.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, updatedropdown(5)));
        et2 = (EditText) findViewById(R.id.editText2);
        progressBar = (ProgressBar) findViewById(R.id.progressbar1);
        buildGoogleApiClient();
    }

    public void findDatabaseUrl(View view)   // email and password submittion
    {

        count++;
        EmailId = customitemname.getText().toString();
        Pass = et2.getText().toString();
        Mail._user = EmailId;
        Mail._pass = Pass;

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("customitemname",customitemname.getText().toString());
        editor.commit();
        onStart();

    }

    public void buildGoogleApiClient() {
        Log.d("Bz", "Building the client");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (count >= 1) {
            Log.d("Bz", "In onStart connecting");
            progressBar.setVisibility(View.VISIBLE);
            googleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Bz", "In onStop disconnecting");
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        progressBar.setVisibility(View.GONE);
        Log.d("Bz", "We are connected... lets do the background work");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE, "text/plain", "image/png", "application/vnd.google-apps.spreadsheet"})
                .build(googleApiClient);
        try {
            startIntentSenderForResult(intentSender, REQUEST_CODE_SELECT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.d("Bz", "unable to send intent" + e);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        switch (i) {
            case 1:
                Log.d("Bz", "Service Disconnected");
                break;
            case 2:
                Log.d("Bz", "Connection lost");
                break;
            default:
                Log.d("Bz", "Reason unknown");
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("Bz", "connection falied.. result:" + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            return;
        }
        Log.d("Bz", "trying to resolve the connection by restarting the activity");
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.d("Bz", "exception encountered while restarting the actuvity:" + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Bz", "is on activity trigerred on clicking select");
        switch (requestCode) {
            case REQUEST_CODE_SELECT:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    MyTask.url = "https://docs.google.com/spreadsheets/d/" + driveId.getResourceId() + "/gviz/tq?tq=select%20B%2CC";
                    Log.d("Bz", "Selected folder's id:" + driveId.toString());
                    Log.d("Bz", "Selected folder's Resource id:" + driveId.getResourceId());
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                break;
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    Log.d("Bz", "Resolving connection. connecting...");
                    googleApiClient.connect();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    public String[] updatedropdown(int listlength)
    {
        boolean itemalreadyinlist=false;
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        for(int i = 0; i<listlength; i++)
        {
            if (getPreferences(MODE_PRIVATE).getString("customitemname","").equals(getPreferences(MODE_PRIVATE).getString("recentlistitem"+String.valueOf(i),"")))
            {
                itemalreadyinlist=true;
                for(int j = i; j>0; j--)
                {
                    editor.putString("recentlistitem"+String.valueOf(j),getPreferences(MODE_PRIVATE).getString("recentlistitem"+String.valueOf(j-1),""));
                }
                editor.putString("recentlistitem0",getPreferences(MODE_PRIVATE).getString("customitemname",""));
                editor.commit();
                break;
            }
        }

        if( !itemalreadyinlist)
        {
            for(int i = listlength-1; i>0; i--)
            {
                editor.putString("recentlistitem"+String.valueOf(i),getPreferences(MODE_PRIVATE).getString("recentlistitem"+String.valueOf(i-1),""));
            }
            editor.putString("recentlistitem0",getPreferences(MODE_PRIVATE).getString("customitemname",""));
            editor.commit();
        }

        String[] recentlist = new String[listlength];
        for(int i=0;i<listlength;i++)
        {
            recentlist[i] = getPreferences(MODE_PRIVATE).getString("recentlistitem"+String.valueOf(i),"");
        }

        return recentlist;
    }
}

