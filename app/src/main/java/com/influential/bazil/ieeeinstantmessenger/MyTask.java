package com.influential.bazil.ieeeinstantmessenger;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyTask extends AppCompatActivity implements AdapterView.OnItemClickListener,View.OnClickListener {
    protected  static String url;
    ListView list;
    ArrayAdapter<String> adapter;  // declaring adapter
    private String name;
    ProgressBar pb2;
    private String email;
    CheckBox c;
    JSONArray rows;
    private int count=0;
    int flag =0;
    int [] temp;    // temp array contains selected mail ids to whom mail have to be send
    public MyTask()
    {

     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_async_tasks);
        pb2= (ProgressBar) findViewById(R.id.progressbar2);
        pb2.setVisibility(View.VISIBLE);
        list= (ListView) findViewById(R.id.listview);
        c= (CheckBox) findViewById(R.id.checkBox);
        c.setOnClickListener(this);         // to select or deselect all mails.... with a single click
        list.setOnItemClickListener(this);
        list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>()));
        new MyAsyncTask().execute(url);
    }
    public void SelectedMails(View view)
    {
        new MyAsyncTask().execute();          // redirecting to doInbackground method
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // for selecting a single mail
        TextView t= (TextView) view;                                                //  i.e., temp[i]==1 => ith mail selected
      if(temp[i]==0)
      {
          temp[i]=1;
          Log.d("Bz","Temp:"+i+"==="+temp[i]);
          t.setBackgroundColor(Color.parseColor("#148eac"));// getResources().getColor(R.color.colorPrimaryDark)
      }
        else
      {
          temp[i]=0;
          Log.d("Bz","Temp:"+i+"==="+temp[i]);
          t.setBackgroundColor(Color.TRANSPARENT);
      }
    }

    @Override
    public void onClick(View view) {
        CheckBox t= (CheckBox) view;
        if(t.isChecked())    // all mails selected
        {
            for (int i=0;i<rows.length();i++)
            {
                temp[i]=1;
                Log.d("Bz","Temp:"+i+"==="+temp[i]);
            }

        }
        else
        {
            for (int i=0;i<rows.length();i++)   // all mails deselected
            {
                temp[i]=0;
                Log.d("Bz","Temp:"+i+"==="+temp[i]);
            }
        }

    }

    public class MyAsyncTask  extends AsyncTask<String,String,Void> {
        private String jsonResponse;





        @Override
        protected Void doInBackground(String... urls) {
            // ensuring download of json first then sending mail is executed
                try {
                    String result = DownloadUrl(urls[0]);
                    int start = result.indexOf("{", result.indexOf("{") + 1);
                    int end = result.lastIndexOf("}");
                    jsonResponse = result.substring(start, end);
                    JSONObject table = new JSONObject(jsonResponse);
                    rows = table.getJSONArray("rows");
                     Log.d("Bz","array length:"+rows.length());
                    temp = new int[rows.length()];
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray columns = row.getJSONArray("c");
                        name = columns.getJSONObject(0).optString("v");
                        Log.d("Bz", "name:" + name);
                        email = columns.getJSONObject(1).optString("v");
                        Log.d("Bz", "Email:" + email);
                        Log.d("Bz","count:"+count);
                        adapter.add(name + ":" + email);
                       // publishProgress(name, email);

                    }
                    Mail m=new Mail();
                    Log.d("Bz","entered done area");
                    for (int i =0;i<rows.length();i++)
                    {
                        if (temp[i]==1)
                        {


                            try {
                                flag=1;
                                JSONObject row=rows.getJSONObject(i);
                                JSONArray columns;
                                columns = row.getJSONArray("c");
                                name =columns.getJSONObject(0).optString("v");
                                email=columns.getJSONObject(1).optString("v");
                                Log.d("Bz","Retracted name:"+name+" And email:"+email);
                                if(m.send(name,email)) {
                                    Log.d("Bz","Email was sent successfully to"+name+":\n"+email);
                                    count++;
                                    Log.d("Bz","count:"+count);

                                   publishProgress(adapter.getItem(i));

                                } else {
                                    Log.d("Bz","Email was not sent.");
                                }
                                Thread.sleep(2000);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    }
                    if (flag==0)   // if no mails selected
                    {
                        Toast.makeText(getApplicationContext(),"No Email Selected",Toast.LENGTH_LONG).show();
                    }
                    return  null;

                } catch (IOException e) {
                    Log.d("Bz", "IoException:" + e);
                } catch (JSONException e) {
                    Log.d("Bz", "" + e);

                } catch (Exception e) {
                    Log.d("Bz", "Exception:" + e);

                } finally {
                    return null;
                }
            }




        @Override
        protected void onPreExecute() {
        adapter= (ArrayAdapter<String>) list.getAdapter();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

                pb2.setVisibility(View.GONE);

        }

       @Override
        protected void onProgressUpdate(String... values) {   // adding up values to array adapter

               adapter.remove(values[0]);

        }









        private String DownloadUrl(String s) throws IOException {
            InputStream inputStream=null;

            String contentAsString=null;
            try {
                URL url = new URL(s);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
               connection.setRequestMethod("GET");

                connection.setDoInput(true);
                connection.connect();
                int responsecode = connection.getResponseCode();
                if (responsecode == 200) {
                    inputStream = connection.getInputStream();

                    contentAsString = convertStreamToString(inputStream);

                }
                return contentAsString;
            }
            finally {
                if (inputStream!=null)
                {
                    inputStream.close();
                }
            }

        }
        private String convertStreamToString(InputStream inputStream)
        {
            BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb=new StringBuilder();
            String line=null;
            try {
                while((line=reader.readLine())!=null)
                {
                    sb.append(line+"\n");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

}
