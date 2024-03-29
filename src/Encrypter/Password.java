package Encrypter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Password
{
	private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    
//used to generate salt algorithm
public static String getSaltVar(int length)
{
    StringBuilder returnValue = new StringBuilder(length);
    for (int i = 0; i < length; i++) { returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()))); }
    return new String(returnValue);
}
    
    //used in "generateSaltPw.method" to encode user password and salt algo together
    public static byte[] hash(char[] userPassword, byte[] salt)
    {
        PBEKeySpec spec = new PBEKeySpec(userPassword, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(userPassword, Character.MIN_VALUE);
        try
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        { throw new AssertionError("Error while hashing a password: " + e.getMessage(), e); }
        finally { spec.clearPassword(); }
    }
    
    public static String generateMd5Pw(String userPassword) 
    { 
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(userPassword.getBytes()); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { hashtext = "0" + hashtext; } 
            return hashtext; 
        }  
        catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); } 
    }
    
    public static String generateSaltPw(String userPassword, String salt)
    {
        String returnValue = null;
        String md5Password = generateMd5Pw(userPassword);
        byte[] saltPassword = hash(md5Password.toCharArray(), salt.getBytes());
 
        returnValue = Base64.getEncoder().encodeToString(saltPassword);
 
        return returnValue;
    }
    
	//To verify, provide normal user password, generated string from "generateSaltPw.method" and Salt algorithm
	public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt)
	{
	    String newSecurePassword = generateSaltPw(providedPassword, salt);
	    if(newSecurePassword.contentEquals(securedPassword)) { return true; }
	    else { return false; }
	}
    
	public static void main (String args[])
	{
		Scanner input = new Scanner(System.in);
		System.out.print("Enter a password: ");
		try
		{
			String password = input.next();
			String saltVar = getSaltVar(100);
			String generatedMd5Pw = generateMd5Pw(password);
			String generatedSaltPw = generateSaltPw(password, saltVar);
			boolean verifiedUserPassword = verifyUserPassword(password, generatedSaltPw, saltVar);
			String verifiedUserPasswordToString = String.valueOf(verifiedUserPassword);
			System.out.println("Your input: " + password);
			System.out.println("Your SALT: " + saltVar);
			System.out.println("Your input in MD5: " + generatedMd5Pw); 
			System.out.println("Your input in MD5 + SALT: " + generatedSaltPw);
			System.out.println("Does the password and encrypted password match?");
			System.out.println("Answer: \'" + verifiedUserPasswordToString + "\'");
			//test
			//6cqDAiJCnOZ6zB3PrSIDgAMkQLYM8HUSlqGOOwuP6za8P3WNnQemfD4ZLbbMxlLqcYHjEPOAU3X5kSa45BnDfL9vfZRk8zUaeN3n
			//098f6bcd4621d373cade4e832627b4f6
			//48NqI0x6BMrWGCP43kbSLuBFlp4p6YwXJU6mQzEtvnU=
		}
		catch(Exception e) { }
	}
}