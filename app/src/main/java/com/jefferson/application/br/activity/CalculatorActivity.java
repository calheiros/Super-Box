package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.jefferson.application.br.R;
import android.view.View.OnLongClickListener;
import android.text.Editable;
import android.widget.Toast;

public class CalculatorActivity extends AppCompatActivity implements OnLongClickListener  {

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.calculator_backspaceButton) {
            editText.getText().clear();
            return true;
        } else {
            return enter();
        }
    }

    protected char[] operations = new char[] {'+','×','÷', '%', '-'};
    protected EditText editText;
    protected Button resultButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_layout);
        editText = findViewById(R.id.calculator_layoutEditText);
        resultButton = findViewById(R.id.calculator_result);
        resultButton.setOnLongClickListener(this);
        findViewById(R.id.calculator_backspaceButton).setOnLongClickListener(this);
    }

    public void one(View v) {
        editText.append("1");
    }

    public void two(View v) {
        editText.append("2");
    }

    public void three(View v) {
        editText.append("3");
    }
    public void four(View v) {
        editText.append("4");
    }

    public void five(View v) {
        editText.append("5");
    }

    public void six(View v) {
        editText.append("6");
    }

    public void eight(View v) {
        editText.append("8");
    }

    public void seven(View v) {
        editText.append("7");
    }

    public void nine(View v) {
        editText.append("9");
    }

    public void zero(View v) {
        editText.append("0");
    }

    public void plus(View v) {
        appendOperation("+");
    }

    public void subtraction(View v) {
        appendOperation("-");
    }

    public void multiplication(View v) {
        appendOperation("×");
    }

    public void division(View v) {
        appendOperation("÷");
    }

    public void percentage(View v) {
        appendOperation("%");
    }

    public void result(View v) {

    }

    public void backspace(View v) {
        int lenght = editText.getText().length();
        if (lenght == 0) {
            return;
        }
        editText.setText(editText.getText().subSequence(0, lenght - 1));
    }

    private void appendOperation(String operation) {
        String text = editText.getText().toString();
        if (text.length() == 0) {
            return ;
        }
        char lastChar = text.charAt(text.length() - 1);
        for (char op : operations) {
            if (lastChar == op) {
                text = text.toString().substring(0, text.length() - 1);
                break;
            }
        }
        editText.setText(text + operation);
    }

    public void dot(View v) {
        if (!editText.getText().toString().contains(".")) {
            editText.append(".");
        }
    }

    public boolean enter() {
        if (editText.getText().toString().equals("4321")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }
}
