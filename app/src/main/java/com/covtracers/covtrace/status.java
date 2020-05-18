package com.covtracers.covtrace;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.covtracers.covtrace.R;

public class status extends AppCompatActivity {
    TextView hyperLink;
    Spanned Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        hyperLink = findViewById(R.id.hyperlink);
        Text = Html.fromHtml("Click on this link to visit my Website <br />" +
                "<a href='https://www.android-examples.com//'>Android-Examples.com</a>");
        hyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperLink.setText(Text);

    }
}
