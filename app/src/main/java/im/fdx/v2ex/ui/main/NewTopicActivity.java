package im.fdx.v2ex.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import im.fdx.v2ex.ui.node.NodeModel;
import im.fdx.v2ex.network.HttpHelper;
import im.fdx.v2ex.network.NetManager;
import im.fdx.v2ex.ui.details.DetailsActivity;
import im.fdx.v2ex.utils.HintUI;
import im.fdx.v2ex.utils.Keys;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NewTopicActivity extends AppCompatActivity {

    private static final String TAG = NewTopicActivity.class.getSimpleName();

    private List<NodeModel> nodeModels = new ArrayList<>();
    private ArrayAdapter<NodeModel> adapter;
    private String mNodename;
    private String mTitle;
    private String mContent;
    private String once;
    private EditText etTitle;
    private EditText etContent;
    private SearchableSpinner spinner;

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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        spinner = (SearchableSpinner) findViewById(R.id.search_spinner_node);
        spinner.setTitle(getString(R.string.choose_node));
        spinner.setPositiveButton(getString(R.string.close));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nodeModels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNodename = ((NodeModel) parent.getItemAtPosition(position)).getName();
                XLog.tag(TAG).d(mNodename);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder().url(NetManager.URL_ALL_NODE).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError(NewTopicActivity.this);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Type type = new TypeToken<ArrayList<NodeModel>>() {
                }.getType();
                final ArrayList<NodeModel> nodes = NetManager.myGson.fromJson(response.body().string(), type);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addAll(nodes);
                        adapter.notifyDataSetChanged();
                        setNode(getIntent());
                    }
                });
            }
        });

        parseIntent(getIntent());

    }

    private void parseIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                etContent.setText(sharedText);
            }
        }
    }

    private void setNode(Intent intent) {
        if (intent.getStringExtra(Keys.KEY_NODE_NAME) != null) {
            mNodename = intent.getStringExtra(Keys.KEY_NODE_NAME);
            XLog.d(mNodename + "|" + spinner.getCount() + "|" + adapter.getCount());
            String nodeTitle;
            for (int i = 0; i < adapter.getCount(); i++) {
                if (mNodename.equals(adapter.getItem(i).getName())) {

                    XLog.d("yes, " + i);
                    nodeTitle = adapter.getItem(i).getTitle();
                    XLog.d(nodeTitle);

                    //有个bug，设了hint之后，setSelection就失效了。
                    spinner.setSelection(i);
                    XLog.d(spinner.getSelectedItemPosition());
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, Menu.FIRST, Menu.NONE, "send")
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
                HintUI.t(this, "标题和内容不能为空");
            } else if (mTitle.length() > 120) {
                HintUI.t(this, "标题字数超过限制");
            } else if (mContent.length() > 20000) {
                HintUI.t(this, "主题内容不能超过 20000 个字符");
            } else if (TextUtils.isEmpty(mNodename)) {
                HintUI.t(this, getString(R.string.choose_node));
            } else {
                postNew();
            }
        }
        return true;
    }


    private void postNew() {

        HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                .headers(HttpHelper.INSTANCE.getBaseHeaders())
                .url("https://www.v2ex.com/new")
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                NetManager.dealError(NewTopicActivity.this);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() != 200) {
                    NetManager.dealError(NewTopicActivity.this, response.code());
                    return;
                }
//              <input type="hidden" name="once" value="79218" />
                Pattern p = Pattern.compile("(?<=<input type=\"hidden\" name=\"once\" value=\")(\\d+)");
                final Matcher matcher = p.matcher(response.body().string());
                if (!matcher.find()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HintUI.s(etTitle, "无法发布主题，请退出后重试");
                        }
                    });
                    return;
                }

                once = matcher.group();
                XLog.tag(TAG).d("once" + once);

                RequestBody requestBody = new FormBody.Builder()
                        .add("title", mTitle)
                        .add("content", mContent)
                        .add("node_name", mNodename) //done
                        .add("once", once) //done
                        .build();

                HttpHelper.INSTANCE.getOK_CLIENT().newCall(new Request.Builder()
                        .headers(HttpHelper.INSTANCE.getBaseHeaders())
                        .url("https://www.v2ex.com/new")
                        .post(requestBody)
                        .build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        NetManager.dealError(NewTopicActivity.this);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.code() == 302) {

//                                /t/348815#reply0
                            String location = response.header("Location");
                            Pattern p = Pattern.compile("(?<=/t/)(\\d+)");
                            Matcher matcher = p.matcher(location);
                            String topic;
                            if (matcher.find()) {
                                topic = matcher.group();
                                XLog.tag(TAG).d(topic);
                                Intent intent = new Intent(NewTopicActivity.this, DetailsActivity.class);
                                intent.putExtra(Keys.KEY_TOPIC_ID, topic);
                                startActivity(intent);
                            }
                            finish();
                        }
                    }
                });
            }
        });
    }
}
