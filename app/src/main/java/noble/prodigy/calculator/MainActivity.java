package noble.prodigy.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private StringBuilder inputStringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);
        inputStringBuilder = new StringBuilder();

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        int[] buttonIds = {
                R.id.btnClear, R.id.btnAllClear, R.id.btnBackspace, R.id.btnDivide,
                R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnMultiply,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btnSubtract,
                R.id.btn1, R.id.btn2, R.id.btn3, R.id.btnAdd,
                R.id.btn0, R.id.btnDecimal, R.id.btnEqual
        };

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(this::onButtonClick);
        }
    }

    private void onButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "C":
                clear();
                break;
            case "AC":
                allClear();
                break;
            case "⌫":
                backspace();
                break;
            case "=":
                calculateResult();
                break;
            default:
                appendInput(buttonText);
                break;
        }
    }

    private void clear() {
        inputStringBuilder.delete(0, inputStringBuilder.length());
        updateResultTextView();
    }

    private void allClear() {
        clear();
        resultTextView.setText("0");
    }

    private void backspace() {
        if (inputStringBuilder.length() > 0) {
            inputStringBuilder.deleteCharAt(inputStringBuilder.length() - 1);
            updateResultTextView();
        }
    }

    private void appendInput(String input) {
        inputStringBuilder.append(input);
        updateResultTextView();
    }

    private void updateResultTextView() {
        resultTextView.setText(inputStringBuilder.toString());
    }

    private void calculateResult() {
        try {
            String expression = inputStringBuilder.toString();
            double result = evaluateExpression(expression);
            DecimalFormat decimalFormat = new DecimalFormat("#.##########");
            String resultString = decimalFormat.format(result);
            resultTextView.setText(resultString);
            inputStringBuilder.setLength(0);
            inputStringBuilder.append(resultString);
        } catch (Exception e) {
            resultTextView.setText("Error");
        }
    }

    private double evaluateExpression(String expression) {
        // You can use a library or your own logic to evaluate the expression here
        // For simplicity, we will use a basic eval() function
        return eval(expression);
    }

    private double eval(final String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            // number = [0-9]+

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
