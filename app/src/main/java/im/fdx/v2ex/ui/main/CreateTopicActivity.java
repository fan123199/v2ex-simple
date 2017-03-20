package im.fdx.v2ex.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.elvishew.xlog.XLog;
import com.google.gson.reflect.TypeToken;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.fdx.v2ex.R;
import im.fdx.v2ex.model.NodeModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.JsonManager;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static im.fdx.v2ex.network.HttpHelper.baseHeaders;

public class CreateTopicActivity extends AppCompatActivity {

    private static final String TAG = CreateTopicActivity.class.getSimpleName();

    private List<NodeModel> nodeModels = new ArrayList<>();
    private ArrayAdapter<NodeModel> adapter;
    private String mNodename;
    private String mTitle;
    private String mContent;
    private String once;
    private EditText etTitle;
    private EditText etContent;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        etTitle = (EditText) findViewById(R.id.et_title);
        etContent = (EditText) findViewById(R.id.et_content);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 1,2 not working, 3 is good
//        toolbar.setNavigationIcon(R.drawable.ic_twitter);
//        getSupportActionBar().setIcon(R.drawable.ic_twitter);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_twitter);


        SearchableSpinner spinner = (SearchableSpinner) findViewById(R.id.search_spinner_node);
        spinner.setTitle(getString(R.string.choose_node));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nodeModels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        HttpHelper.OK_CLIENT.newCall(new Request.Builder().url(JsonManager.URL_ALL_NODE).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Type type = new TypeToken<ArrayList<NodeModel>>() {
                }.getType();
                final ArrayList<NodeModel> nodes = JsonManager.myGson.fromJson(response.body().string(), type);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addAll(nodes);
//                        adapter.sort(new Comparator<NodeModel>() {
//                            @Override
//                            public int compare(NodeModel o1, NodeModel o2) {
//                                if (o1.getName().compareTo(o2.getName()) > 0) {
//                                    return 1;
//                                } else if (o1.getName().compareTo(o2.getName()) < 0) {
//                                    return -1;
//                                } else {
//                                    return 0;
//                                }
//                            }
//                        });


                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNodename = ((NodeModel) parent.getItemAtPosition(position)).getName();
                XLog.tag("Create").d(mNodename);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        item = menu.add(0, Menu.FIRST, Menu.NONE, "send")
                .setIcon(R.drawable.ic_send_white_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == Menu.FIRST) {

            mTitle = etTitle.getText().toString();
            mContent = etContent.getText().toString();

            if (TextUtils.isEmpty(mTitle) || TextUtils.isEmpty(mContent)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintUI.t(CreateTopicActivity.this, "标题和内容不能为空");
                    }
                });
            } else if (mTitle.length() > 120) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintUI.t(CreateTopicActivity.this, "主题内容不能超过 20000 个字符");
                    }
                });
            } else if (mContent.length() > 20000) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintUI.t(CreateTopicActivity.this, "字数超过限制");
                    }
                });
            } else {
                if (TextUtils.isEmpty(mNodename)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HintUI.t(CreateTopicActivity.this, getString(R.string.choose_node));
                        }
                    });
                } else {
                    postNew();
                }
            }
        }

        return true;
    }


    private void postNew() {

        HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                .headers(baseHeaders)
                .url("https://www.v2ex.com/new")
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() == 200) {
//                    <input type="hidden" name="once" value="79218" />

                    Pattern p = Pattern.compile("(?<=<input type=\"hidden\" name=\"once\" value=\")(\\d+)");
                    final Matcher matcher = p.matcher(response.body().string());
                    if (matcher.find()) {
                        once = matcher.group();
                        XLog.tag(TAG).d("once" + once);
                    } else {
                        handleError();
                        return;
                    }


                    RequestBody requestBody = new FormBody.Builder()
                            .add("title", mTitle)
                            .add("content", mContent)
                            .add("node_name", mNodename) //done
                            .add("once", once) //done
                            .build();


                    HttpHelper.OK_CLIENT.newCall(new Request.Builder()
                            .headers(baseHeaders)
                            .url("https://www.v2ex.com/new")
                            .post(requestBody)
                            .build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.code() == 302) {

//                                /t/348815#reply0
                                String location = response.header("Location");
                                Pattern p = Pattern.compile("(?<=/t/)(\\d+)");
                                Matcher matcher1 = p.matcher(location);
                                String topic = "";
                                if (matcher1.find()) {
                                    topic = matcher1.group();
                                    XLog.tag(TAG).d(topic);
                                    Intent intent = new Intent(CreateTopicActivity.this, DetailsActivity.class);
                                    intent.putExtra(Keys.KEY_TOPIC_ID, Long.parseLong(topic));
                                    startActivity(intent);
                                }
                                finish();

                            }
                        }
                    });
                }


            }
        });
    }

    private void handleError() {

    }

    @Deprecated
    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (etContent.toString().isEmpty() && etTitle.toString().isEmpty()) {
                item.setIcon(R.drawable.ic_send_white_24dp);
            } else {
                item.setIcon(R.drawable.ic_send_black_24dp);
            }
        }
    }
}
