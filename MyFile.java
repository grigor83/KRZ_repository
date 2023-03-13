import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;

import javax.swing.JOptionPane;

public class MyFile implements Serializable {
	File userDir;
	String fileName, userName;
	List<File> parts;    // list of directories which contains all parts of the same file; each folder in list contains one file
	
	public MyFile(File userDir, String fileName, List<File> parts) {
		this.userDir=userDir;
		userName=userDir.getName();
		this.fileName=fileName;
		this.parts=parts;
	}
	
	public void signFiles() {
		for(File dir: parts)
			try {
				Repository.CA_BODY.signFileScript(userDir, userName, dir, dir.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void verifySignatures(File userPrivateKey, File downloadFile) {
		try (BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(downloadFile))) {
			for(File dir: parts) {
				System.out.println(dir.getName());
				File signatureFile=new File(dir+File.separator+dir.getName()+".signature");
				File sourceFile=new File(dir+File.separator+dir.getName());
				if (!Repository.CA_BODY.verifySignatureScript(userPrivateKey, signatureFile, sourceFile)) {
					JOptionPane.showMessageDialog(null, "You dont have permission to download this file!");
					bos.close();
					downloadFile.delete();
					createMessage(userDir, sourceFile);
					return;					
				}
				
				BufferedInputStream bis =new BufferedInputStream(new FileInputStream(dir+File.separator+dir.getName()));
				bos.write(bis.readAllBytes());
				bis.close();				
			}
			JOptionPane.showMessageDialog(null, "Your file has been downloaded!");
			Desktop.getDesktop().open(downloadFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean verifySignatureWhileUploading(File userPrivateKey, File uploadFile) {		
		File signatureFile=null;
		if(uploadFile.getName().contains(".signature")) {
			signatureFile=uploadFile;
			uploadFile=new File(signatureFile.getParentFile()+File.separator+signatureFile.getName().replace(".signature", ""));
		}
		else
			signatureFile=new File(uploadFile+".signature");
		if(!signatureFile.exists())
			return true;
		
		try {
			if (!Repository.CA_BODY.verifySignatureScript(userPrivateKey, signatureFile, uploadFile)) {
				JOptionPane.showMessageDialog(null, "You dont have permission to access this file!");
				createMessage(userPrivateKey, signatureFile);
				return false;			
			}
			else
				return true;
		} catch (HeadlessException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void createMessage(File userDir, File file) throws IOException {
		File dir=file.getParentFile().getParentFile();
		File messages=new File(dir+File.separator+"messages.txt");
		if(!messages.exists()) {
			messages.createNewFile();
		}
		
		List<String> lines=Files.readAllLines(messages.toPath());
		lines.add("User: "+userDir.getName()+" has try to access your file "+file.getName());
		Files.write(messages.toPath(),lines);
	}
	
	public String toString() {
		return fileName;
	}

}