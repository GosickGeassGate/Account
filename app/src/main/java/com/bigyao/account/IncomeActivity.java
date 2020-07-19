package com.bigyao.account;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IncomeActivity extends AppCompatActivity {
    private String id;
    private EditText titleEdit;
    private EditText dateEdit;
    private EditText detailEdit;
    private EditText incomeEdit;

    private Button saveButton;
    private Button cancelButton;

    private DBOpenHandler dbOpenHandler;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_income);

        titleEdit = findViewById(R.id.titleEdit);
        dateEdit = findViewById(R.id.dateEdit);
        detailEdit = findViewById(R.id.detailEdit);
        incomeEdit = findViewById(R.id.incomeEdit);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        titleEdit.setText(intent.getStringExtra("title"));
        dateEdit.setText(intent.getStringExtra("date"));
        detailEdit.setText(intent.getStringExtra("detail"));
        incomeEdit.setText(intent.getStringExtra("income"));

        dbOpenHandler = new DBOpenHandler(this, "account", null, 1);
        db = dbOpenHandler.getReadableDatabase();
        db = dbOpenHandler.getWritableDatabase();

        saveButton = (Button)findViewById(R.id.save);
        cancelButton = (Button)findViewById(R.id.cancel);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int income;
                try{
                    income = (int)(Float.valueOf(incomeEdit.getText().toString()) * 100);
                    Toast.makeText(IncomeActivity.this, "收支：" + income, Toast.LENGTH_SHORT).show();
                }
                catch(Exception e){
                    Toast.makeText(IncomeActivity.this, "请输入收支数额", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String title = titleEdit.getText().toString();
                final String date = dateEdit.getText().toString();
                final String detail = detailEdit.getText().toString();

                if(id.equals("")) { // 增加记录
                    final Cursor cursor = db.rawQuery("select * from account where title = ? and date = ? and detail = ?", new String[]{title, date, detail});
                    if (cursor.moveToFirst()) {
                        if(cursor.getInt(cursor.getColumnIndex("income")) != income) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IncomeActivity.this);
                            AlertDialog alertDialog = dialogBuilder.setTitle("注意：")
                                    .setMessage("已存在记录——\n标题：" + cursor.getString(cursor.getColumnIndex("title")) +
                                            "\n标签：" + cursor.getString(cursor.getColumnIndex("date")) +
                                            "\n详情：" + cursor.getString(cursor.getColumnIndex("detail")) +
                                            "\n收支：" + cursor.getInt(cursor.getColumnIndex("income")) + "件" +
                                            "\n是否替换该记录？")
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ContentValues cv = new ContentValues();
                                            cv.put("income", income);
                                            db.update("account", cv, "id=?", new String[]{cursor.getString(cursor.getColumnIndex("id"))});
                                            finish();
                                        }
                                    }).create(); // 创建AlertDialog对象
                            alertDialog.show(); // 显示对话框
                        }
                        else
                            finish();
                    }
                    else{
                        ContentValues cv = new ContentValues();
                        cv.put("title", title != null ? title : "");
                        cv.put("date", date != null ? date : "");
                        cv.put("detail", detail != null ? detail : "");
                        cv.put("income", income);
                        db.insert("account", null, cv);
                        finish();
                    }
                }
                else {
                    ContentValues cv = new ContentValues();
                    cv.put("title", title != null ? title : "");
                    cv.put("date", date != null ? date : "");
                    cv.put("detail", detail != null ? detail : "");
                    cv.put("income", income);
                    db.update("account", cv, "id=?", new String[]{id});
                    finish();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

