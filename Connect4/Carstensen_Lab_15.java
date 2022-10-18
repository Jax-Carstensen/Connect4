import java.awt.EventQueue;
import javax.swing.JFrame;

class Carstensen_Lab_15 extends JFrame{
    private int screenWidth = 900;
    private int screenHeight = 950;
    
    public static Carstensen_Lab_15 ex;
    public Carstensen_Lab_15(){
        initUI();
    }
    private void initUI(){
        add(new Board(screenWidth, screenHeight, this));
        setSize(screenWidth, screenHeight);
        setTitle("Connect 4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    public static void main(String[] args){
        EventQueue.invokeLater(() -> {
            ex = new Carstensen_Lab_15();
            ex.setVisible(true);
        });
    }
    private static final long serialVersionUID = 1L;
}