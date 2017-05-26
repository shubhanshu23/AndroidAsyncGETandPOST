package challenge.ventura.com.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String, String>> formheroku;
    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    String jsonStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_main);

        formheroku = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        new GetData().execute();

        Button submit=(Button)findViewById(R.id.button);

        submit.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, PostForm.class);
                myIntent.putExtra("key", jsonStr); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });



    }

    private class GetData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Http sh = new Http();
            // Making a request to url and getting response
            String url = "https://randomform.herokuapp.com/";
            jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONObject obj=jsonObj.getJSONObject("data");
                    System.out.println(obj);
                    JSONArray contacts = obj.getJSONArray("form_fields");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String component = c.getString("component");
                        String description = c.getString("description");
                        String editable = c.getString("editable");
                        String label = c.getString("label");
                        //String options = c.getString("options");
                        String required = c.getString("required");

                        // tmp hash map for single contact
                        HashMap<String, String> d = new HashMap<>();

                        // adding each child node to HashMap key => value
                        d.put("component",component);
                        d.put("description", description);
                        d.put("editable", editable);
                        d.put("label", label);
                        d.put("options", "");
                        d.put("required",required);

                        // adding contact to contact list
                        formheroku.add(d);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, formheroku,
                    R.layout.list_item, new String[]{"component","description", "editable","label","options","required"},
                    new int[]{R.id.component,R.id.description, R.id.editable,R.id.label,R.id.options,R.id.required});
            lv.setAdapter(adapter);
        }
    }
}