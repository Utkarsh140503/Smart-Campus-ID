package com.example.vtop;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StartupActivity extends AppCompatActivity {

    LinearLayout layout1, layout2, layout3, layout4;
    TextView dotTextBox;
    private ListView listView;
    private String[] itemList = {"Spotlight",
            "LIST OF APPROVED STUDENTS WITH COURSE TYPE FOR THE REFAT - WINTER SEMESTER 2022-23(SENIORS)",
            "FAT SCHEDULE - SUMMER TERM II 2022-23",
            "FAT SCHEDULE - FALL INTER SEMESTER 202-23 (LAW PROGRAMME ONLY)",
            "FALL INTER SEM 2022-23- RECAT 1 APPROVED LIST",
            "WINTER SEMESTER 2022-23 STUDENT COURSE END FEEDBACK LINK IS LIVE(FRESHERS/SENIORS)"};


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        dotTextBox = findViewById(R.id.dotTextBox);
        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        layout4 = findViewById(R.id.layout4);

        dotTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartupActivity.this, SaveDataInFB.class);
                startActivity(i);
            }
        });

        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, StudentLoginActivity.class);
                startActivity(intent);
            }
        });

        layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, EmployeeLoginActivity.class);
                startActivity(intent);
            }
        });

        layout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, ParentLoginActivity.class);
                startActivity(intent);
            }
        });

        layout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartupActivity.this, VendorLoginActivity.class);
                startActivity(i);
            }
        });

        listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedItem = itemList[position];
                if (selectedItem.equals("LIST OF APPROVED STUDENTS WITH COURSE TYPE FOR THE REFAT - WINTER SEMESTER 2022-23(SENIORS)")) {
                    String url = "https://drive.google.com/file/d/1e0_VgjpHj40pTKZulsLW__i4Ef_IeE1j/view?usp=sharing";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else if (selectedItem.equals("FAT SCHEDULE - SUMMER TERM II 2022-23")) {
                    String url = "https://drive.google.com/file/d/1Izx2wHRDEjc0XJTKwXiQHBF36eVmzBNb/view?usp=sharing";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("FAT SCHEDULE - FALL INTER SEMESTER 202-23 (LAW PROGRAMME ONLY)")) {
                    String url = "https://drive.google.com/file/d/1fAXLlxcQeEYYtZt3Eh0QH7CTj-THe7-a/view?usp=sharing";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("FALL INTER SEM 2022-23- RECAT 1 APPROVED LIST")) {
                    String url = "https://drive.google.com/file/d/18sh_Dp3xVBXtAaJe_nNbTnCRvzCK3WGr/view?usp=sharing";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("WINTER SEMESTER 2022-23 STUDENT COURSE END FEEDBACK LINK IS LIVE(FRESHERS/SENIORS)")) {
                    String url = "https://vtopregcc.vit.ac.in/endfeedback/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else {
                    Toast.makeText(StartupActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}