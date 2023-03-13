import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Frame extends JFrame {
	
	private JButton logInButton, registerButton, pictureButton;
	static ImageIcon DEFAULT_IMAGE;
	public static final String defaultImage = "logo.jpg";
	File userDir;
	String userName, userPassword, userNamefromCert, userPasswordfromCert;
	int i=0;
	
	public Frame() {
		super("KRZ Repository");
		
		loadFirstFrame();
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
	}
	
	private void loadFirstFrame() {
		logInButton=new JButton("LOG IN");
		logInButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		logInButton.setBackground(Color.green);
		logInButton.setFont(new Font("SansSerif", Font.BOLD, 20));
		registerButton=new JButton("REGISTER");
		registerButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		registerButton.setBackground(Color.green);
		registerButton.setFont(new Font("SansSerif", Font.BOLD, 20));
		DEFAULT_IMAGE=new ImageIcon(defaultImage);
		pictureButton=new JButton(DEFAULT_IMAGE);
		pictureButton.setBackground(Color.black);
		
		add(BorderLayout.NORTH, logInButton);
		add(BorderLayout.SOUTH, registerButton);
		add(BorderLayout.CENTER, pictureButton);
		
		logInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Prvo treba unijeti svoj sertifikat
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setCurrentDirectory(Repository.USERS_DIR);
				chooser.setDialogTitle("Choose your certificate");
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			    	File userCertificate=chooser.getSelectedFile();
			    	try {
						userCertificate = Repository.CA_BODY.validateCert(userCertificate);
						if(userCertificate==null) {
							JOptionPane.showMessageDialog(null, "Your certificate is not valid!");
							return;
						}
					    enterUsernamePassword(userCertificate,false);
					    
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			    }  			    
			}
		});
		
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				register();
			}
		});
	}
	
	private void enterUsernamePassword (File userCertificate, boolean register) {
		while(true) {
			userName=JOptionPane.showInputDialog("Enter your username:");
			userPassword=JOptionPane.showInputDialog("Enter your password (min is 4 chars):");
			
			if(register) {
				File temp1=new File(Repository.CA_BODY.requestsDir+File.separator+ userName+".req");
				File temp2=new File(Repository.USERS_DIR+File.separator+ userName);
				if(temp1.exists() || temp2.exists()) {
					JOptionPane.showMessageDialog(null, "Username is already taken!");
					continue;
				}
			}
			if((userName==null || userName.length()>0) && 
					(userPassword==null || userPassword.length()>3))
				break;
		}	
		if(userName==null || userPassword==null)
			return;

		// In case of login, check if enter credentials corresponds credentials inside certificate
		if(!register) {
			if (checkCredentials(userCertificate)) {
				i=0;
				createSecondFrame();
			}
			else {
				JOptionPane.showMessageDialog(null, "Your credentials are not correct!");
				i++;
				if(i==4) {
					eraseUserAccount();
					JOptionPane.showMessageDialog(null, "Your account has been erased!");
					i=0;
					return;
				}
				
				if(i<3)
					enterUsernamePassword(userCertificate, register);
				else {
					try {
						Repository.CA_BODY.revokeCert(userNamefromCert);
						JOptionPane.showMessageDialog(null, "Your certificate has been suspended!");
						String[] options = {"Enter credentials one more time", "Create new account"}; 
						int result = JOptionPane.showOptionDialog(null, "Select option", "KRZ_repository", JOptionPane.YES_NO_OPTION,
					               JOptionPane.QUESTION_MESSAGE,
					               null,     //no custom icon
					               options,  //button titles
					               options[0]); //default button
						
						if(result == JOptionPane.YES_OPTION) {
							// try one more time enter credentials
							enterUsernamePassword(userCertificate, register);
							// if are credentials correct, take back user certificate
							takebackCert();
							i=0;
				        } else if (result == JOptionPane.NO_OPTION) {
				        	//user choose to create new account, but before we must delete the old one
				        	eraseUserAccount();
				        	JOptionPane.showMessageDialog(null, "Your account has been erased!");
				        	i=0;
				        	register();
				        } else {
				        	eraseUserAccount();
				        	i=0;
				         	}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private boolean checkCredentials(File userCertificate) {
		try {
			List<String> lines;
			lines = Files.readAllLines(Paths.get(userCertificate.toString()));
			String s= lines.stream()
					.filter(line -> line.contains("Subject:"))
					.distinct().findAny().get();
					
			userNamefromCert=s.split("CN=")[1].split("/")[0];
			userPasswordfromCert=s.split("emailAddress=")[1];
			System.out.println(userNamefromCert+" "+userPasswordfromCert);
			
			if(userName.equals(userNamefromCert) && userPassword.equals(userPasswordfromCert))
				return true;
			else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void register() {
		enterUsernamePassword(null, true);
		
		if(userName!=null && userPassword!=null) {
			File userDir=new File (Repository.USERS_DIR + File.separator + userName);
			Repository.createDir(userDir);
			try {
				File request = Repository.CA_BODY.createRequest(userName, userPassword);
				if(!request.exists()) {
					JOptionPane.showMessageDialog(null, "Your request has been failed!");
					for(File f: userDir.listFiles())
						f.delete();
					userDir.delete();
					return;
				}
				else
					JOptionPane.showMessageDialog(null, "Your request has been submited!");
				
				File certificate = Repository.CA_BODY.signRequest(userName);
				if(!certificate.exists()) {
					JOptionPane.showMessageDialog(null, "Your request has been declined!");
					return;
				}
				else {
					JOptionPane.showMessageDialog(null, "Your certificate has been signed!");
					Repository.copyFile(certificate.getCanonicalPath(), new File(userDir + File.separator + "certificate.pem"));
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private void eraseUserAccount() {
		File userDir=new File (Repository.USERS_DIR + File.separator + userNamefromCert);
		Repository.deleteDir(userDir);
		File userCertFile=new File(Repository.CA_BODY.certsDir+File.separator+userNamefromCert+".cert");
		userCertFile.delete();
		userCertFile=new File(Repository.CA_BODY.requestsDir+File.separator+userNamefromCert+".req");
		userCertFile.delete();

		try {
			// delete entry from index.txt
			List<String> newContent=new ArrayList<>();
			List<String> lines=Files.readAllLines(Repository.CA_BODY.indexFile.toPath());
			for(String line:lines) {
				String s=line.split("CN=")[1].split("/")[0];
				if (userNamefromCert.equals(s))
					continue;
				newContent.add(line);
			}
			Files.write(Repository.CA_BODY.indexFile.toPath(), newContent);
			Repository.CA_BODY.generateCRLList();
			JOptionPane.showMessageDialog(null, "Your certificate has been deleted!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		userName=userNamefromCert=userPassword=null;
		i=0;
	}
	
	public void takebackCert() throws IOException {
		List<String> newContent=new ArrayList<>();
		List<String> lines=Files.readAllLines(Repository.CA_BODY.indexFile.toPath());
		for(String line:lines) {
			String s=line.split("CN=")[1].split("/")[0];
			if (userNamefromCert.equals(s)) {
				String[] parts=line.split("[\t]");
				String newLine="V\t"+parts[1]+"\t\t"+parts[3]+"\t"+parts[4]+"\t"+parts[5];
				newContent.add(newLine);
			}
			else
				newContent.add(line);
		}
		//String newContentString = String.join("\n", newContent);
		//Files.write(Repository.CA_BODY.indexFile.toPath(), newContentString.getBytes(Charset.forName("UTF-8")));
		Files.write(Repository.CA_BODY.indexFile.toPath(), newContent);
		Repository.CA_BODY.generateCRLList();
	}
	
	private void createSecondFrame() {
		Frame2 frame2=new Frame2(this);		
	}
	
	
}
