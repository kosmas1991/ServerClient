package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
	private JTextField enterField;
	private JTextArea displayArea;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String chatServer;
	private Socket client;

	public Client(String host) {
		super("Da client");
		chatServer = host;
		enterField = new JTextField();
		enterField.setEditable(false);
		enterField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendData(event.getActionCommand());
				enterField.setText("");
			}
		});
		add(enterField, BorderLayout.NORTH);
		displayArea = new JTextArea();
		add(new JScrollPane(displayArea), BorderLayout.CENTER);
		setSize(500, 300);
		setVisible(true);
	}

	private void sendData(String message) {
		try {
			output.writeObject("CLIENT>>> " + message);
			output.flush();
			displayMessage("\nCLIENT>>> " + message);
		}catch(IOException ioException) {
			displayArea.append("\nError writing Object");
		}
	}

	public void runClient() {
		try {
			connectToServer();
			getStreams();
			processConnection();
		} catch (EOFException eofException) {
			displayMessage("\nClient Terminated connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	private void closeConnection() {
		displayMessage("\nClosing connection");
		setTextFieldEditable(false);
		try {
			output.close();
			input.close();
			client.close();
		}catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private void processConnection() throws IOException {
		setTextFieldEditable(true);
		do {
			try {
				message = (String) input.readObject();
				displayMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException) {
				displayMessage("\nUnknown type Object received");
			}
		}while(!message.equals("SERVER>>> TERMINATE"));
		
	}

	private void setTextFieldEditable(final boolean editable) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				enterField.setEditable(editable);
			}
		});
	}

	private void getStreams() throws IOException {
		output = new ObjectOutputStream(client.getOutputStream());
		output.flush();
		input = new ObjectInputStream(client.getInputStream());
		displayMessage("\nGot IO streams\n");
	}

	private void connectToServer() throws IOException {
		displayMessage("Attempting connection\n");
		client = new Socket(InetAddress.getByName(chatServer), 12345);
		displayMessage("Connected to " + client.getInetAddress().getHostName());
	}

	private void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayArea.append(messageToDisplay);
			}
		});
	}
}