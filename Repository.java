import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Repository {
	
	public final static File ROOT_DIR= new File("C:/repository");
	public final static File USERS_DIR= new File("C:/repository/USERS");
	public static CA CA_BODY;
	
	public static void main(String[] args) throws Exception {
		createDir(ROOT_DIR);
		createDir(USERS_DIR);
		CA_BODY=new CA();
		CA_BODY.createRootCertificate();
		
		Frame gui=new Frame();
		
	}
	
	public static void createDir(File temp) {		
		if(!temp.exists()) 
			temp.mkdir();
	}
	
	public static void deleteDir(File temp) {
		for(File f: temp.listFiles()) {
			if(f.isFile())
				f.delete();
			else
				deleteDir(f);
		}
			
		temp.delete();
	}
	
	public static void createFile(File temp, boolean write) {		
		if(!temp.exists()) {
			try {
				temp.createNewFile();
				if(write) {
					FileWriter fw= new FileWriter(temp);
					fw.write("01");
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void copyFile(String originalFileName, File newFile) {
		try(FileInputStream fis = new FileInputStream(originalFileName);
				FileOutputStream fos = new FileOutputStream(newFile);) {
				
				int bajt=fis.read();
				while (bajt!=-1) {
					fos.write(bajt);
					bajt=fis.read();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
