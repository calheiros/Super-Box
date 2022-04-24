package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.util.MathUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.StringUtils;
import java.text.DecimalFormat;
import android.animation.Animator;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;

public class CalculatorActivity extends MyCompatActivity implements OnLongClickListener {

    public final static String ACTION_CREATE_CODE = "create_code_action";

    private boolean createCode;
    private String code = null;
    private TextView hintTextView;
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
    protected char[] operations = new char[] {'+','×','÷', '√', '-', ','};
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
            hintTextView = findViewById(R.id.calculator_hintTextView);
            hintTextView.setText("Enter your code and press and hold the = button to confirm it.");
            //showTipDialog();
        }
    }

    private void showHintDialog() {
        SimpleDialog dialog = new SimpleDialog(this);
        dialog.setTitle("Hint");
        dialog.setMessage("Enter your code and press and hold the = button to confirm it.");
        dialog.setPositiveButton(getString(android.R.string.ok), null);
        dialog.show();
    }

    private void createOperatorMap() {
        if (createCode) {

        }

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
                case ',':
                    value = '.';
            }
            if (value != '0') {
                operatorMap.put(c, value);
            }
        }
    }

    private boolean canAppendDot(String text, int position) {
        if (text.isEmpty()) return true;
        // get start of number
        for (int i = position; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == ',') {
                return false;
            }
            if (!Character.isDigit(c)) {
                break;
            }
        }

        //get end of number

        for (int i = position; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ',') {
                return false;
            }
            if(!Character.isDigit(c)){
                break;
            }
        }
         return true;
    }

    @Override
    protected void onApplyCustomTheme() {
        //do nothing...
    }

    public void onKeyPressed(View v) {
        String key = null;

        switch (v.getId()) {
            case R.id.calculator_one:
                key = "1";
                break;
            case R.id.calculator_two:
                key = "2";
                break;
            case R.id.calculator_three:
                key = "3";
                break;
            case R.id.calculator_four:
                key = "4";
                break;
            case R.id.calculator_five:
                key = "5";
                break;
            case R.id.calculator_six:
                key = "6";
                break;
            case R.id.calculator_seven:
                key = "7";
                break;
            case R.id.calculator_eight:
                key = "8";
                break;
            case R.id.calculator_nine:
                key = "9";
                break;
            case R.id.calculator_zero:
                key = "0";
                break;
            case R.id.calculator_dot:
                boolean canAppend = canAppendDot(editText.getText().toString(), editText.getText().length() - 1);
                if (canAppend){
                    editText.append(",");
                }
                return;
            case R.id.calculator_plus:
                appendOperation("+");
                return;
            case R.id.calculator_division:
                appendOperation("÷");
                return;
            case R.id.calculator_subtration:
                appendOperation("-");
                return;
            case R.id.calculator_multiplication:
                appendOperation("×");
                return;
            case R.id.calculator_percentage:
                appendOperation("√");
                return;
            case R.id.calculator_open_parenthesis:
                key = "(";
                break;
            case R.id.calculator_close_parenthesis:
                key = ")";
                break;

        }
        editText.append(key);
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

    public void enter() {
        String input = editText.getText().toString();
        if (input.isEmpty()) return;

        if (createCode) {
            if (input.length() > 50) {
                Toast.makeText(this, "Too big! Maximum 50 characters", 0).show();
                return;
            } else if (input.length() < 3) {
                showHint("Too short! Minimum 3 characters");
                return;
            }

            if (code != null) {
                if (code.equals(input)) {
                    MyPreferences.putCalculatorCode(input);
                    Toast.makeText(this, "Code confirmed!", 0).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showHint("The code does not match!");
                }
            } else {
                code = input;
                editText.getText().clear();
                showHint("Type your code again to confirm it.");
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

    private void showHint(final String message) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animator) {

                }

                @Override
                public void onAnimationEnd(Animation aninator) {
                    Animation fadeIn = AnimationUtils.loadAnimation(CalculatorActivity.this, R.anim.fade_in);
                    fadeIn.setDuration(200);
                    hintTextView.setText(message);
                    hintTextView.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animator) {
                }
            }
        );
        hintTextView.startAnimation(fadeOut);
        //Snackbar.make(editText, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (createCode && code != null) {
            code = null;
            showHint("Last code cleared!");
            return;
        } 
        super.onBackPressed();
    }
}
