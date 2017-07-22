package me.a3zcs.booklisting.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText searchWord;
    Button search;
    RecyclerView recyclerView;
    BookAdapter adapter;
    List<Book> bookList = new ArrayList<>();
    TextView noResult;
    LinearLayoutManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchWord = (EditText) findViewById(R.id.search_editText);
        search = (Button) findViewById(R.id.button);
        recyclerView = (RecyclerView) findViewById(R.id.book_list);
        noResult = (TextView) findViewById(R.id.no_result);
        manager = new LinearLayoutManager(this);

        if (savedInstanceState != null && savedInstanceState.containsKey("Book") && savedInstanceState.containsKey("position")) {
            bookList = savedInstanceState.getParcelableArrayList("Book");


            Parcelable state = savedInstanceState.getParcelable("position");
            recyclerView.setLayoutManager(manager);
            adapter = new BookAdapter(this, bookList);
            recyclerView.setAdapter(adapter);
            handelList(bookList);
            manager.onRestoreInstanceState(state);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new BookAdapter(this, bookList);
            recyclerView.setAdapter(adapter);
        }


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable(MainActivity.this)) {
                    if (!TextUtils.isEmpty(searchWord.getText().toString())) {
                        String searchingWord = searchWord.getText().toString().replace(" ", "+");
                        new fetchBookTask().execute(searchingWord);
                    } else {
                        searchWord.setError(getString(R.string.error));
                    }
                } else {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.check_network), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
        public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                return true;
            }
            return false;
        }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("Book", (ArrayList<? extends Parcelable>) bookList);
        outState.putParcelable("position", manager.onSaveInstanceState());
    }

    public void handelList(List<Book> bookList){
        this.bookList = bookList;
        adapter.books = bookList;
        adapter.notifyDataSetChanged();
        if (!bookList.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            noResult.setVisibility(View.GONE);
        }else {
            noResult.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private class fetchBookTask extends AsyncTask<String, Void, List<Book>> {
        private String ENDPOINT = "https://www.googleapis.com/books/v1/volumes?q=";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Book> doInBackground(String... strings) {
            List<Book> bookList = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(ENDPOINT+strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }
                Log.i("result", builder.toString());
                JSONObject topLevel = new JSONObject(builder.toString());

                JSONArray arrayObject = topLevel.getJSONArray("items");
                //weather = String.valueOf(object.getString("title"));
                List<String> authors;
                for (int i = 0; i < arrayObject.length(); i++) {
                    JSONObject object = arrayObject.getJSONObject(i).getJSONObject("volumeInfo");
                    authors = new ArrayList<>();
                    try {
                        JSONArray authorsList = object.getJSONArray("authors");
                        if (authors != null)
                            for (int j = 0; j < authorsList.length(); j++) {
                                authors.add(authorsList.getString(j));
                            }
                    } catch (JSONException e) {
                        authors.add(MainActivity.this.getString(R.string.na));
                    }


                    bookList.add(new Book(object.getString("title"), authors));
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                //handel item nullity
                bookList.clear();
            } finally {
                if (connection == null)
                    connection.disconnect();
                return bookList;
            }

        }

        @Override
        protected void onPostExecute(List<Book> bookList) {
            handelList(bookList);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
