package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.MathUtils;
import com.jefferson.application.br.util.StringUtils;
import java.text.DecimalFormat;

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
    private ArrayMap<Character, Character> operatorMap = new ArrayMap<>();
    protected char[] operations = new char[] {'+','×','÷', '√', '-'};
    protected EditText editText;
    protected Button resultButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_layout);
        editText = findViewById(R.id.calculator_layoutEditText);
        resultButton = findViewById(R.id.calculator_result);
        resultButton.setOnLongClickListener(this);
        editText.setLongClickable(false);
        findViewById(R.id.calculator_backspaceButton).setOnLongClickListener(this);
        createOperatorMap();
    }

    private void createOperatorMap() {

        for (Character c : operations) {
            char value = '0';
            switch (c) {
                case '√':
                    value = '^';
                    break;
                case '×':
                    value = '*';
                    break;
                case '÷':
                    value = '/';
                    break;
            }
            if (value != '0') {
                operatorMap.put(c, value);
            }
        }
    }

    public void openParenthesis(View v) {
        editText.append("(");
    }
    
    public void closeParenthesis(View v) {
        editText.append(")");
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

    public void exponentiation(View v) {
        appendOperation("√");
    }

    public void result(View v) {
        String expression = StringUtils.replaceEach(editText.getText().toString(), operatorMap);
        String result = "";
        try {
            DecimalFormat format = new DecimalFormat("0.#");
            result = format.format(MathUtils.eval(expression));
        } catch (RuntimeException e) {
            Toast.makeText(this, "Invalid format!", Toast.LENGTH_SHORT).show();
        }
        if (!result.isEmpty()){
            editText.setText(result);
        }
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
        String input = editText.getText().toString();
        if (input.equals("4321")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        } 
        return false;
    }
}
