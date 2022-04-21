package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.MathUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.StringUtils;
import java.text.DecimalFormat;
import com.jefferson.application.br.app.SimpleDialog;
import android.support.design.widget.Snackbar;

public class CalculatorActivity extends MyCompatActivity implements OnLongClickListener {
    public final static String ACTION_CREATE_CODE = "create_code_action";

    private boolean createCode;
    private String code = null;

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if (id == R.id.calculator_backspaceButton) {
            editText.getText().clear();
            
        } else {
            enter();
        }
        return true;
    }

    private ArrayMap<Character, Character> operatorMap = new ArrayMap<>();
    protected char[] operations = new char[] {'+','×','÷', '√', '-'};
    protected EditText editText;
    protected Button resultButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_layout);
        createCode = ACTION_CREATE_CODE.equals(getIntent().getAction());
        editText = findViewById(R.id.calculator_layoutEditText);
        resultButton = findViewById(R.id.calculator_result);
        resultButton.setOnLongClickListener(this);
        editText.setLongClickable(false);
        findViewById(R.id.calculator_backspaceButton).setOnLongClickListener(this);
        createOperatorMap();
       
        if (createCode) {
            showTipDialog();
        }
    }
   
    private void showTipDialog() {
        SimpleDialog dialog = new SimpleDialog(this);
        dialog.setTitle("Tip");
        dialog.setMessage("Enter your code and press and hold the = button to confirm it.");
        dialog.setPositiveButton(getString(android.R.string.ok), null);
        dialog.show();
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

    @Override
    protected void onApplyCustomTheme() {
        //do nothing...
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
        if (editText.getText().toString().isEmpty()) { 
            return;
        }
        String expression = StringUtils.replaceEach(editText.getText().toString(), operatorMap);
        String result = "";
        try {
            DecimalFormat format = new DecimalFormat("0.#");
            result = format.format(MathUtils.eval(expression));
        } catch (RuntimeException e) {
            Toast.makeText(this, "Invalid format!", Toast.LENGTH_SHORT).show();
        }
        if (!result.isEmpty()) {
            editText.setText(result);
        }
    }

    public void backspace(View v) {
        int lenght = editText.getText().length();
        if (lenght == 0) {
            return;
        }
        editText.getText().delete(lenght - 1, lenght);
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

    public void enter() {
        String input = editText.getText().toString();
        if (input.isEmpty()) return;
        
        if (createCode) {
            if (input.length() > 50) {
                Toast.makeText(this, "Too big! Maximum 50 characters", 0).show();
                return;
            } else if (input.length() < 3) {
                showSnack("Too short! Minimum 3 characters");
                return;
            }
           
            if (code != null) {
                if (code.equals(input)) {
                    MyPreferences.putCalculatorCode(input);
                    Toast.makeText(this, "Code confirmed!", 0).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showSnack("The code does not match!");
                }
            } else {
                code = input;
                editText.getText().clear();
                showSnack("Type your code again to confirm it.");
            }
        } else if (input.equals(MyPreferences.getCalculatorCode())) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return;
        } 
        return;
    }

    private void showSnack(String message) {
        Snackbar.make(editText, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        
        if (createCode && code != null) {
            code = null;
            showSnack("Last code cleared!");
            return;
        } 
        super.onBackPressed();
    }
}
