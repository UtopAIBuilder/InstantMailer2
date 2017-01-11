package com.influential.bazil.ieeeinstantmessenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private int count=0;

   EditText messageEditText,subjectEditText;

    private String Message="N/A";
    private String Subject="N/A";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageEditText= (EditText) findViewById(R.id.editText);
        subjectEditText= (EditText) findViewById(R.id.editText2);

    }

    public void SendEmail(View view)
    {
            Message = messageEditText.getText().toString();
            Subject = subjectEditText.getText().toString();
             new Mail(Subject,Message);
            Intent intent=new Intent(this,MyTask.class);
            startActivity(intent);



    }


    }










