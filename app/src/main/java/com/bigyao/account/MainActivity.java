package com.bigyao.account;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ListView accountList;
    private ClassifyAdapter adapter;
    ArrayList<Map<String, Object>> list = new ArrayList<>();

    private EditText searchEdit;
    private ImageView resetButton;
    private TextView searchButton;
    private String keyword = null;

    private TextView balanceView;   // 结算

    private DBOpenHandler dbOpenHandler;
    private SQLiteDatabase db;

    String dataPrevious = null;
    ArrayList<Integer> idList = new ArrayList<>();
    ArrayList<String> titleList = new ArrayList<>();
    ArrayList<String> dateList = new ArrayList<>();
    ArrayList<String> detailList = new ArrayList<>();
    ArrayList<String> incomeList = new ArrayList<>();

    final static int MAX_PROGRESS = 100; // 总进度
    private int progressStatus = 0; // 记录进度对话框的完成百分比
    private ProgressDialog pd;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) { // 定义一个负责更新的进度的Handler
            if (msg.what == 0x123){ // 表明消息是由该程序发送的
                progressStatus = msg.arg1;
                pd.setProgress(progressStatus);
            }
            if (progressStatus >= MAX_PROGRESS) { // 如果任务已经完成
                pd.dismiss(); // 关闭对话框
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbOpenHandler = new DBOpenHandler(this, "account", null, 1);
        db = dbOpenHandler.getReadableDatabase();
        db = dbOpenHandler.getWritableDatabase();

        init();
    }

    @Override
    protected void onStart(){
        super.onStart();
        setListView();
    }

    /**
     * 初始化搜索框和列表
     */
    private void init(){

        searchEdit = (EditText)findViewById(R.id.search_edit);
        resetButton = (ImageView)findViewById(R.id.reset_button);
        searchButton = (TextView)findViewById(R.id.search_button);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)
                    resetButton.setVisibility(View.GONE);
                else
                    resetButton.setVisibility(View.VISIBLE);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword = searchEdit.getText().toString();
                Log.i("search", "keyword: " + keyword);
                setListView();
                adapter.notifyDataSetChanged();
                searchEdit.setText("");
                // 收起软键盘
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        balanceView = (TextView)findViewById(R.id.balance);

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        int version = prefs.getInt("version", 0);
//        // 仅初始化一次
//        progressStatus = 0; // 将进度条的完成进度重设为0
//        pd = new ProgressDialog(MainActivity.this);
//        pd.setMax(MAX_PROGRESS);
//        pd.setTitle("仓库数据初始化");     // 设置对话框的标题
//        pd.setCancelable(false);        // 设置对话框不能用“取消”按钮关闭
//        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        pd.setIndeterminate(false);     // 设置对话框的进度条是否显示进度
//        pd.show();
//
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("initial", true);
//        editor.commit();

        accountList = (ListView)findViewById(R.id.accountList);
        accountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, ((TextView)view.findViewById(R.id.id)).getText(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
                intent.putExtra("id", ((TextView)view.findViewById(R.id.id)).getText());
                intent.putExtra("title", ((TextView)view.findViewById(R.id.title)).getText());
                intent.putExtra("date", ((TextView)view.findViewById(R.id.date)).getText());
                intent.putExtra("detail", ((TextView)view.findViewById(R.id.detail)).getText().toString().substring(3));
                String incomeStr = ((TextView)view.findViewById(R.id.income)).getText().toString();
                intent.putExtra("income", incomeStr.substring(3, incomeStr.length() - 1));
                startActivity(intent);
            }
        });
        accountList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String idStr = ((TextView)view.findViewById(R.id.id)).getText().toString();
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete:
                                Toast.makeText(MainActivity.this, "删除成功~",Toast.LENGTH_SHORT).show();
                                db.delete("account", "id = ?", new String[]{idStr});
                                setListView();
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });
    }

    private void setListView(){
        idList.clear();
        titleList.clear();
        dateList.clear();
        detailList.clear();
        incomeList.clear();

        Cursor cursor;
        if(keyword == null)
            cursor = db.rawQuery("select * from account order by date", null);
        else {
            String match = "%" + keyword + "%";
            cursor = db.rawQuery("select * from account where title like ? or date like ? or detail like ? order by date", new String[]{match, match, match});
        }
        int idId = cursor.getColumnIndex("id");
        int titleId = cursor.getColumnIndex("title");
        int dateId = cursor.getColumnIndex("date");
        int detailId = cursor.getColumnIndex("detail");
        int incomeId = cursor.getColumnIndex("income");

        int totalIncome = 0;    // 总收支

        if(cursor.moveToFirst()){
            dataPrevious = cursor.getString(dateId);
            idList.add(ClassifyAdapter.LABEL);
            titleList.add(null);
            dateList.add(dataPrevious);
            detailList.add(null);
            incomeList.add(null);

            dateList.add(dataPrevious);
            idList.add(cursor.getInt(idId));
            titleList.add(cursor.getString(titleId));
            detailList.add(cursor.getString(detailId));
            int income = cursor.getInt(incomeId);
            incomeList.add(String.format("%d.%02d", income / 100, Math.abs(income % 100)));
            totalIncome += income;
        }

        while(cursor.moveToNext()){
            int id = cursor.getInt(idId);
            String title = cursor.getString(titleId);
            String date = cursor.getString(dateId);
            String detail = cursor.getString(detailId);
            int income = cursor.getInt(incomeId);

            if(!date.equals(dataPrevious)){
                dataPrevious = date;
                idList.add(ClassifyAdapter.LABEL);
                titleList.add(null);
                dateList.add(dataPrevious);
                detailList.add(null);
                incomeList.add(null);
            }
            idList.add(id);
            dateList.add(date);
            titleList.add(title);
            detailList.add(detail);
            incomeList.add(String.format("%d.%02d", income / 100, Math.abs(income % 100)));
            totalIncome += income;
        }

        list.clear();
        int size = dateList.size();
        for(int i = 0; i < size; i++){
            Map<String, Object> map = new HashMap<>();
            map.put("id", idList.get(i));
            map.put("title", titleList.get(i));
            map.put("date", dateList.get(i));
            map.put("detail", "详情：" + detailList.get(i));
            map.put("income", "收支：" + incomeList.get(i) + "元");
            list.add(map);
        }

        adapter = new ClassifyAdapter(this, list, R.layout.income_item_list,
                new String[]{"id", "title", "date", "detail", "income"},
                new int[]{R.id.id, R.id.title, R.id.date, R.id.detail, R.id.income});
        accountList.setAdapter(adapter);

        balanceView.setText(String.format("总收支：%d.%02d元", totalIncome / 100, Math.abs(totalIncome % 100)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.insert:
                Intent intent = new Intent(this, IncomeActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("title", "");
                intent.putExtra("date", "");
                intent.putExtra("detail", "");
                intent.putExtra("income", "");
                startActivity(intent);
                break;
        }
        return true;
    }
}
