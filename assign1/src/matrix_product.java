import java.util.Scanner;

class Main {
    private static void OnMult(int m_ar, int m_br) {
        double temp;
        int i, j, k;

        var pha = new double[m_ar * m_ar];
        var phb = new double[m_ar * m_ar];
        var phc = new double[m_ar * m_ar];

        for (i = 0; i < m_ar; i++)
            for (j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = (double) 1.0;

        for (i = 0; i < m_br; i++)
            for (j = 0; j < m_br; j++)
                phb[i * m_br + j] = (double) (i + 1);

        for (int ii = 0; ii < 10; ii++) {

            long Time1 = System.nanoTime();
            for (i = 0; i < m_ar; i++) {
                for (j = 0; j < m_br; j++) {
                    temp = 0;
                    for (k = 0; k < m_ar; k++) {
                        temp += pha[i * m_ar + k] * phb[k * m_br + j];
                    }
                    phc[i * m_ar + j] = temp;
                }
            }
            long Time2 = System.nanoTime();

            System.out.println(String.format("%3.3f", (double) (Time2 - Time1) / 1_000_000_000));
        }
    }

    private static void OnMultLine(int m_ar, int m_br) {
        double temp;
        int i, j, k;

        var pha = new double[m_ar * m_ar];
        var phb = new double[m_ar * m_ar];
        var phc = new double[m_ar * m_ar];

        for (i = 0; i < m_ar; i++)
            for (j = 0; j < m_ar; j++)
                pha[i * m_ar + j] = (double) 1.0;

        for (i = 0; i < m_br; i++)
            for (j = 0; j < m_br; j++)
                phb[i * m_br + j] = (double) (i + 1);

        for (int ii = 0; ii < 10; ii++) {

            long Time1 = System.nanoTime();
            for(i = 0; i < m_ar; i++) {
                for(k = 0; k < m_ar; k++) {
                    for(j = 0; j < m_br; j++) {
                        phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                    }
                }
            }
            long Time2 = System.nanoTime();

            System.out.println(String.format("%3.3f", (double) (Time2 - Time1) / 1_000_000_000));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select size of the matrix: ");
        int m_ar = scanner.nextInt();
        int m_br = m_ar;

        int choice;
        do {
            System.out.println("Select an option:");
            System.out.println("1. Normal Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("0. Leave");

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    OnMult(m_ar,m_br);
                    break;
                case 2:
                    OnMultLine(m_ar,m_br);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        } while (choice != 0);
        scanner.close();
    }
}
