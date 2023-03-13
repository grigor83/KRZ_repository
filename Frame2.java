import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Frame2 extends JFrame implements ActionListener, ListSelectionListener{
	Frame frame;
	JButton uploadButton;
	File userDir, filesDir, filesbin, messagesFile;
	JList<MyFile> list;
	List<MyFile> files;
	JScrollPane scroll;
	
	public Frame2(Frame frame) {
		super(frame.userNamefromCert);
		this.frame=frame;
		userDir=new File (Repository.USERS_DIR + File.separator + frame.userNamefromCert);
		filesDir=new File(userDir+File.separator+"files");
		filesDir.mkdir();
		filesbin = new File(filesDir+File.separator+"files.bin");
		loadObjects();
		loadMessages();
		uploadButton=new JButton("UPLOAD FILE");
		uploadButton.addActionListener(this);
		uploadButton.setBorder(BorderFactory.createLineBorder(Color.black,2));
		uploadButton.setBackground(Color.green);
		add(BorderLayout.SOUTH, uploadButton);
		scroll=new JScrollPane();
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	
		scroll.setViewportView(loadList());
		add(BorderLayout.CENTER,scroll);
		setSize(500,400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private JList<MyFile> loadList(){
		DefaultListModel<MyFile> lm=new DefaultListModel<>();
		if(files!=null) {
			for(MyFile mf: files)
				lm.addElement(mf);
			list= new JList<MyFile>(lm);
			list.addListSelectionListener(this);
			return list;
		}
		else {
			lm.addElement(new MyFile(userDir, "\tYour folder is empty!", null));
			list= new JList<MyFile>(lm);
			list.addListSelectionListener(this);
			return list;
		}
//		DefaultListModel<String> lm=new DefaultListModel<>();
//		if(files!=null) {
//			for(MyFile mf: files)
//				lm.addElement(mf.fileName);
//			list= new JList<String>(lm);
//			list.addListSelectionListener(this);
//			return list;
//		}
//		else {
//			lm.addElement("nemate fajlova");
//			list= new JList<String>(lm);
//			list.addListSelectionListener(this);
//			return list;
//		}
	}	
	
	private void loadObjects() {
		if(!filesbin.exists()) {
			try {
				filesbin.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}	
		
		ObjectInputStream ois=null;
		try {
			ois=new ObjectInputStream(new FileInputStream(filesbin));
			files=(List<MyFile>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
	private void loadMessages() {
		messagesFile=new File(filesDir+File.separator+"messages.txt");
		if(!messagesFile.exists())
			return;
		
		try {
			List<String> messages=Files.readAllLines(messagesFile.toPath());
			JOptionPane.showMessageDialog(null, "WARNING!\n"+messages);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setCurrentDirectory(new File("C:\\"));
		chooser.setDialogTitle("Choose file to upload");
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
	    	File uploadFile=chooser.getSelectedFile();
	    	String fileName=uploadFile.getName().replaceAll("\\s", "");
	    	// ovdje ubaci provjeru potpisa prilikom upload-a fajla
	    	File userPrivateKey=new File(userDir+File.separator+userDir.getName());
	    	if (!MyFile.verifySignatureWhileUploading(userPrivateKey,uploadFile))
	    		return;
	    	
	    	List<File> parts=new ArrayList<>();
	    	Random rand=new Random();
	    	int n= rand.nextInt(7)+4;
	    	long size=uploadFile.length()/n;
	    	long bytesWrite=0;
	    	try (BufferedInputStream bis =new BufferedInputStream(new FileInputStream(uploadFile))) {
	    		for(int i=1;i<=n;i++) {
	    			File newDir=new File(filesDir+File.separator+i+fileName);
			    	newDir.mkdir();
			    	File newFile=new File(newDir+File.separator+i+fileName);
			    	newFile.createNewFile();
			    	BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(newFile));
			    	
	    			if(i==n) {
	    				byte[] buffer=bis.readNBytes((int) (uploadFile.length()-bytesWrite));
				    	bos.write(buffer);
	    			}
	    			else {
	    				byte[] buffer=bis.readNBytes((int) size);
				    	bos.write(buffer);
				    	bytesWrite+=size;
	    			}
			    	bos.close();
			    	parts.add(newDir);
		    	}	
			} catch (IOException e1) {
				e1.printStackTrace();
			}   	
	    	MyFile mf=new MyFile(userDir, fileName, parts);
	    	if(files==null)
	    		files=new ArrayList<>();
	    	files.add(mf);
	    	try (ObjectOutputStream oos= new ObjectOutputStream (new FileOutputStream(filesbin))){
	    		oos.writeObject(files);
	    	}
	    	catch(IOException e2) {
				e2.printStackTrace();
	    	}
	    	loadObjects();
	    	scroll.setViewportView(loadList());
	    	mf.signFiles();
	    }
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			download(list.getSelectedValue());
			System.out.println(list.getSelectedValue());			
          }
	}
	
	public void download(MyFile myFile) {
		File userPrivateKey=new File(userDir+File.separator+userDir.getName());
		// destination where to download selected file
		File downloadFile=new File(userDir+File.separator+myFile.fileName);
		myFile.verifySignatures(userPrivateKey, downloadFile);		
	}

}
