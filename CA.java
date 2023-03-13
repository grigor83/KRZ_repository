import java.io.File;
import java.io.IOException;

public class CA {
	public final File  certsDir= new File(Repository.ROOT_DIR+File.separator +"certs"), 
			           newcertsDir=new File(Repository.ROOT_DIR+File.separator +"newcerts"), 
			           requestsDir=new File(Repository.ROOT_DIR+File.separator +"requests"), 
			           privateDir=new File(Repository.ROOT_DIR+File.separator +"private"), 
			           crlDir=new File(Repository.ROOT_DIR+File.separator +"crl"), 
			           indexFile=new File(Repository.ROOT_DIR+File.separator +"index.txt"), 
			           serialFile=new File(Repository.ROOT_DIR+File.separator +"serial"), 
			           crlnumberFile=new File(Repository.ROOT_DIR+File.separator +"crlnumber"), 
			           opensslConfigFile=new File(Repository.ROOT_DIR+File.separator +"openssl.cnf"), 
			           rootCertificateFile=new File(Repository.ROOT_DIR+File.separator +"ca-cert.pem"),
			           finishFile=new File(Repository.ROOT_DIR+File.separator +"finish.txt"),
			           errorFile=new File(Repository.ROOT_DIR+File.separator +"error.txt");
	
	public final String CA_PASSWORD="sigurnost", CA_SCRIPT="script/scriptCA.sh", REQUEST_SCRIPT="script/requestScript.sh", SIGN_SCRIPT="script/signScript.sh", 
						REVOKE_SCRIPT="script/revokeScript.sh", VALIDATE_SCRIPT="script/validateScript.sh", GENERATE_CRL_SCRIPT="script/generateCRLscript.sh",
						SIGN_FILE_SCRIPT="script/signFileScript.sh", VERIFY_SIGNATURE_SCRIPT="script/verifySignature.sh";
	
	public CA() {
		Repository.createDir(certsDir);
		Repository.createDir(newcertsDir);
		Repository.createDir(requestsDir);
		Repository.createDir(privateDir);
		Repository.createDir(crlDir);
		
		Repository.createFile(indexFile, false);
		Repository.createFile(serialFile, true);
		Repository.createFile(crlnumberFile, true);
		Repository.copyFile("openssl.cnf",opensslConfigFile);
	}
	
	public void createRootCertificate () throws IOException {
		if(rootCertificateFile.exists())
			return;
		
		Repository.createFile(rootCertificateFile, false);
		Runtime runtime = Runtime.getRuntime();		
		// private CA key will be created simultaneously while executing command openssl req -new -x509...
		runtime.exec("cmd.exe /c start "+CA_SCRIPT+" "+CA_PASSWORD);
		waitScript();
	}
	
	public File createRequest(String userName, String userPassword) throws IOException {
		File temp=new File(requestsDir+File.separator+ userName+".req");
		Runtime runtime = Runtime.getRuntime();	
		runtime.exec("cmd.exe /c start "+REQUEST_SCRIPT+" "+userName+" "+userPassword);
		waitScript();
		
		if(temp.exists() && temp.length()==0)
			temp.delete();
		return temp;
	}
	
	public File signRequest(String userName) throws IOException {	
		File temp=new File(certsDir+File.separator+ userName+".cert");
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+SIGN_SCRIPT+" "+userName);
		waitScript();

		if(temp.exists() && temp.length()==0)
			temp.delete();
		return temp;
	}
	
	public void revokeCert(String userName) throws IOException {		
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+REVOKE_SCRIPT+" "+userName+" "+CA_PASSWORD);
		waitScript();
	}
	
	public File validateCert(File userCertificate) throws IOException {		
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+VALIDATE_SCRIPT+" "+userCertificate.toString());
		waitScript();
		//skripta ce kreirati error.txt samo u slucaju da je validacija neuspjesna
		if(errorFile.exists()) {
			errorFile.delete();
			return null;
		}
		else 
			return userCertificate;
	}
	
	public void generateCRLList() throws IOException {
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+GENERATE_CRL_SCRIPT+" "+CA_PASSWORD);
		waitScript();
	}
	
	public void signFileScript(File userDir,String userName, File dir, String fileName) throws IOException {
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+SIGN_FILE_SCRIPT+" "+userDir+" "+userName+" "+dir+" "+fileName);
		waitScript();
	}
	
	public boolean verifySignatureScript(File userPrivateKey, File signatureFile, File sourceFile) throws IOException {
		Runtime rt= Runtime.getRuntime();
		rt.exec("cmd.exe /c start "+VERIFY_SIGNATURE_SCRIPT+" "+userPrivateKey+" "+signatureFile+" "+sourceFile);
		waitScript();
		if(errorFile.exists()) {
			errorFile.delete();
			return false;
		}
		else 
			return true;
	}
	
	private void waitScript() {
		while(!finishFile.exists()) {
			
		}
		
		finishFile.delete();
	}
}
