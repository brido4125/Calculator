import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/*ArrayList를 이용해서 각 수식의 연산자 및 피연산자들을 adding하고
 * 각각의 index마다 피연산자/연사자 검사 및 우선순위 비교를 반복문을 통해 구현했다.
 * 또한 메인 클래스를 JFrame을 상속받게하여 활용하였으며,
 * GreedLayout을 활용해 버튼구성의 효율성을 더하였다.
 */


//JFrame을 상속받아 좀 더 편리하게 Frame method를 활용
//To using keyboard input, implements KeyListener.
public class MainCalculator extends JFrame implements KeyListener {

    public static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.ROOT);
    private static final Color CUSTOM_GREEN = new Color(0x30B264);
    private static final Color CUSTOM_RED = new Color(0xF33F12);
    private static final Color CUSTOM_BLACK = new Color(0x1B1818);
    private static final List<String> OPERATORS = Arrays.asList("+", "-", "×", "÷");

    //제곱과 루트 계산에서 소숫점 이하 6자리까지만 표기한다
    static {
        FORMATTER.setMinimumFractionDigits(1);
        FORMATTER.setMaximumFractionDigits(6);
    }

    private final JTextField inputSpace;
    //Create Arraylist(equation) than contain operator and operand.
    private final List<String> equation = new ArrayList<>();
    private final MyActionListener actionListener;
    private String prevOperation;
    private String num = "";

    //MainCalculator가 JFrame을 상속받아 마치 초기화 된 Frame처럼 constructor에서 사용
    public MainCalculator() {
        setLayout(new BorderLayout(10, 10));
        setFocusable(true);
        requestFocus();

        inputSpace = new JTextField();
        inputSpace.setEditable(false);
        inputSpace.setHorizontalAlignment(JTextField.RIGHT);
        inputSpace.setFont(new Font("Arial", Font.BOLD, 50));
        inputSpace.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        String[] buttonValues = {"C", "÷", "×", "=", "D", "^", "√", ".", "7", "8", "9", "+", "4", "5", "6", "-", "1", "2", "3", "0"};
        JButton[] buttons = new JButton[buttonValues.length];

        actionListener = new MyActionListener();
        for (int i = 0; i < buttonValues.length; i++) {
            buttons[i] = new JButton(buttonValues[i]);
            buttons[i].setFont(new Font("Arial", Font.BOLD, 10));
            if (buttonValues[i].equals("C")) {
                buttons[i].setForeground(CUSTOM_GREEN);
            } else if (buttonValues[i].equals("D")) buttons[i].setForeground(Color.BLUE);
                //if btn is containing number, sorting by if-sentence
            else if ((i >= 8 && i <= 10) || (i >= 12 && i <= 14) || (i >= 16 && i <= 18))
                buttons[i].setForeground(CUSTOM_BLACK);
            else buttons[i].setForeground(CUSTOM_RED);
            buttons[i].addActionListener(actionListener);
            buttonPanel.add(buttons[i]);
        }

        add(inputSpace, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        pack();
        setTitle("계산기");
        setVisible(true);
        setSize(300, 370);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(this);
    }

    //To make beatuful GUI, we using LookAndFeelInfo.
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
        new MainCalculator();

    }

    //List contain all operators and numbers.
    private void fullTextParsing(String inputText) {
        //When fullTextParsing are called, List is reset by this method.
        equation.clear();

        for (int i = 0; i < inputText.length(); i++) {
            char ch = inputText.charAt(i);

            if (ch == '-' || ch == '+' || ch == '×' || ch == '÷') {
                equation.add(num);
                num = "";
                equation.add(ch + "");
            } else {
                //If 12 is adding in this List, String 1 is added and then String 2 is attached behind the String 2.
                num = String.format("%s%s", num, ch);
            }
        }
        equation.add(num);
        //Remove the empty string.
        equation.remove("");
    }

    public double calculate(String inputText) {
        fullTextParsing(inputText);

        double prev = 0;
        double current;
        String mode = "";

        for (int i = 0; i < equation.size(); i++) {
            String s = equation.get(i);

            switch (s) {
                case "+":
                    mode = "add";
                    break;
                case "-":
                    mode = "sub";
                    break;
                case "×":
                    mode = "mul";
                    break;
                case "÷":
                    mode = "div";
                    break;
                default:
                    // 곱셈 나눗셈을 먼저 계산한다
                    //Getting order of priority in the four fundamental arithmetic operations.
                    if ((mode.equals("mul") || mode.equals("div"))) {
                        Double one = Double.parseDouble(equation.get(i - 2));
                        Double two = Double.parseDouble(equation.get(i));
                        //Using trinomial operator to calculate divide and multiple.
                        double result = mode.equals("mul") ? one * two : one / two;
                        /* What is this algorithm?
                         * For example, there are 2 + 3 * 5 equation,
                         * index[0] = 2, index[1] = +, index[2] = 3, index[3] = *, index[4] = 5
                         * index [i] equals 5,so index [i-1] is *, and index [i-2] is 3
                         * So adding the result to [i+1].
                         * And then, remove the indexes that are calculated.
                         * In this situation, these are 3,*,5.
                         * */
                        equation.add(i + 1, Double.toString(result));
                        //우선순위가 적용된 부분은 arrayList에서 지운다
                        for (int j = 0; j < 3; j++) {
                            equation.remove(i - 2);
                        }
                        i -= 2;    // 결과값이 생긴 인덱스로 이동(ArrayList에서 숫자2개 연산자1개가 사라지고 숫자하나가 들어왓으니 총 인덱스를 -2해줌)
                    }
                    break;
            }
        }
        for (String s : equation) {
            if (s.equals("+")) {
                mode = "add";
            } else if (s.equals("-")) {
                mode = "sub";
            }
            //문자열이 연산자가 아닌 숫자일 경우
            else {
                //ArrayList에 있던 string을 double로 변환
                current = Double.parseDouble(s);
                if (mode.equals("add")) {
                    prev += current;
                } else if (mode.equals("sub")) {
                    prev -= current;
                } else {
                    prev = current;
                }
            }
            //사칙연산에서 소수점 6자리에서 반올림해서 너무 긴 값을 출력하지 않도록한다.
            prev = Math.round(prev * 100000) / 100000.0;
        }

        return prev;
    }
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        System.out.println(c);
        switch (c) {
            case '+':
                actionListener.actionPerformed(new ActionEvent("+", 1, "+"));
                break;
            case '-':
                actionListener.actionPerformed(new ActionEvent("-", 2, "-"));
                break;
            case '*':
                actionListener.actionPerformed(new ActionEvent("×", 3, "×"));
                break;
            case '/':
                actionListener.actionPerformed(new ActionEvent("÷", 4, "÷"));
                break;
            case '^':
                actionListener.actionPerformed(new ActionEvent("^", 5, "^"));
                break;
            case 'r':
                actionListener.actionPerformed(new ActionEvent("√", 6, "√"));
                break;
            case '=':
                actionListener.actionPerformed(new ActionEvent("=", 6, "="));
                break;
            default:
                actionListener.actionPerformed(new ActionEvent(String.valueOf(c), 7, String.valueOf(c)));
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //Clicked oeperation
            String operation = e.getActionCommand();
            //String from inputSapce
            String original = inputSpace.getText();
            if (original.isEmpty()) {
                // 숫자가 아니거나 -가 아니면 아무 것도 안 함
                if (!isNumber(operation) && !"-".equals(operation)) {
                    prevOperation = operation;
                    return;
                }
                inputSpace.setText(operation);
                prevOperation = operation;
                return;
            }
            // 이 밑으로는 inputSpace가 무조건 비어있지 않아 검사할 필요가 없음

            switch (operation) {
                case "C":
                    inputSpace.setText("");
                    break;
                //등호가 연산자일 경우
                case "=":
                    inputSpace.setText(Double.toString(calculate(original)));
                    num = "";
                    break;
                case "D":
                    StringBuilder strB = new StringBuilder(original);
                    strB.deleteCharAt(original.length() - 1);
                    inputSpace.setText(strB.toString());
                    break;
                //제곱과 루트 계산에서 숫자가 아니라 연산자가 나올 경우의 예외처리
                //연산자가 나왔다고 가정하고 제일 뒤의 스트링부터 하나씩 없애서 숫자가 나오면 수식에 알맞은 계산 진행.
                case "^":
                    if (!isNumber(prevOperation)) original = removeLastOperator(original);
                    inputSpace.setText(String.valueOf(FORMATTER.format(Math.pow(Double.parseDouble(original), 2))));
                    break;
                case "√":
                    if (!isNumber(prevOperation)) original = removeLastOperator(original);
                    inputSpace.setText(String.valueOf(FORMATTER.format(Math.sqrt(Double.parseDouble(original)))));
                    break;
                default: // 연산이거나 숫자일 경우 뒤에 추가
                    // 이전 값이 연산자인 경우 연산자를 대체함
                    if (!isNumber(operation) && OPERATORS.contains(prevOperation)) {
                        original = removeLastOperator(original);
                        inputSpace.setText(original + operation);
                        break;
                    }
                    // 숫자이거나 이전 값이 연산자가 아닌 경우 뒤에 추가
                    inputSpace.setText(original + operation);
                    break;
            }
            //마지막으로 누른 버튼 기억하는 변수 초기화
            prevOperation = operation;
        }

        private String removeLastOperator(String original) {
            return original.substring(0, original.length() - 1);
        }

        private boolean isNumber(String actionCommand) {
            try {
                // 숫자로 파싱 가능하면 true, 그렇지 않으면 false 반환
                Double.parseDouble(actionCommand);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
