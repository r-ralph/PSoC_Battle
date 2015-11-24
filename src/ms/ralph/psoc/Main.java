package ms.ralph.psoc;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Random;

public class Main {

    private static SerialPort port1;
    private static SerialPort port2;

    public static void main(String[] args) {
        try {
            init();
            while (true) {
                start();
                Thread.sleep(1000);
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void init() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Please input player 1 port name: ");
            String player1 = br.readLine();
            System.out.print("Please input player 2 port name: ");
            String player2 = br.readLine();
            CommPortIdentifier portId1 = CommPortIdentifier.getPortIdentifier(player1);
            CommPortIdentifier portId2 = CommPortIdentifier.getPortIdentifier(player2);

            Main.port1 = (SerialPort) portId1.open("battle", 5000);
            Main.port1.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            Main.port2 = (SerialPort) portId2.open("battle", 5000);
            Main.port2.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            Main.port1.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            Main.port2.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            System.out.println("Connection successful!");
        }
    }

    private static void start() throws IOException {
        System.out.println("Start battle.");
        InputStream port1InputStream = port1.getInputStream();
        OutputStream port1OutputStream = port1.getOutputStream();
        InputStream port2InputStream = port2.getInputStream();
        OutputStream port2OutputStream = port2.getOutputStream();

        send(port1OutputStream, 'e');
        send(port2OutputStream, 'e');

        Random rand = new Random();
        int time = rand.nextInt(10) + 5;
        int i = 0;

        while (i < time--) {
            System.out.println("Time : " + time);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        send(port1OutputStream, 'a');
        send(port2OutputStream, 'a');

        boolean winPlayer1;
        while (true) {
            int read1 = port1InputStream.read();
            int read2 = port2InputStream.read();
            if (read1 == 'b') {
                winPlayer1 = true;
                break;
            } else if (read2 == 'b') {
                winPlayer1 = false;
                break;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (port1InputStream.read() != -1) {
            port1InputStream.read();
        }
        while (port2InputStream.read() != -1) {
            port2InputStream.read();
        }

        if (winPlayer1) {
            System.out.println("Player 1 win!");
            send(port1OutputStream, 'c');
            send(port2OutputStream, 'd');
        } else {
            System.out.println("Player 2 win!");
            send(port1OutputStream, 'd');
            send(port2OutputStream, 'c');
        }
    }

    private static void send(OutputStream os, char command) throws IOException {
        os.write(command);
        os.write('\r');
        //os.write((Character.toString(command) + "\r").getBytes());
        os.flush();
    }
}
