package ASCIIUtils;

import java.util.Scanner;

public class App {

    static ASCIIText txt = new ASCIIText(ASCIIFonts.MINIMAL);

    public static void main(String[] args) throws InterruptedException {

        showTextInDelay("Calculadora\nprofesional\nde edad", 100);

        Scanner in = new Scanner(System.in);


        while (true) {
            System.out.print("\u001bc");
            System.out.println(txt.getOf("Calculadora\nprofesional\nde edad"));
            System.out.println();
            System.out.print("Inserte su edad: ");

            String age = in.nextLine();
            int ageN;
            try {
                ageN = Integer.decode(age);
                hideTextInDelay("Calculadora\nprofesional\nde edad", 20);
                if (ageN <= 0) System.out.println("Aún no existes, cheers");
                else if (ageN > 120) System.out.println("Lo siento, pero no hablo con muertos");
                else {
                    System.out.println("Tu edad es: " + age);
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Número erróneo");
            }

        }



    }

    static void showTextInDelay(String text, int delay) throws InterruptedException {

        int charIndex = 1;
        while (charIndex <= text.length()) {
            System.out.print("\u001bc");
            System.out.println(txt.getOf(text.substring(0, charIndex)));
            charIndex++;
            Thread.sleep(delay);
        }

    }

    static void hideTextInDelay(String text, int delay) throws InterruptedException {

        int charIndex = text.length();
        while (charIndex >= 0) {
            System.out.print("\u001bc");
            if (charIndex == 0) return;
            System.out.println(txt.getOf(text.substring(0, charIndex)));
            charIndex--;
            Thread.sleep(delay);
        }

    }

    static void showLoading(String text, int delay) throws InterruptedException {

        int charIndex = 1;
        while (charIndex <= text.length()) {

            System.out.print("\u001bc");
            System.out.println(txt.getOf(text.substring(0, charIndex)));
            charIndex++;
            Thread.sleep(delay);

        }

    }

}
