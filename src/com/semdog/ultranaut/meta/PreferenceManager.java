package com.semdog.ultranaut.meta;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This method is responsible for loading and reading
 * ULTRANAUT preference files.
 * 
 * At the current time, the only preference that matters
 * is the fullscreen one (i.e., antialiasing is mandatory
 * and there is no audio. But hey, proof of concept.)
 * 
 * @author Sam
 *
 */

public class PreferenceManager {

	public static UltranautPref currentPref;
	
	public static void init() {
		try {
			FileInputStream fileInputStream = new FileInputStream("prefs.up");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			currentPref = (UltranautPref) objectInputStream.readObject();
			objectInputStream.close();
			fileInputStream.close();
			
			System.out.println("PreferenceManager: Located the Settings File!");
		} catch(IOException ioe) {
			System.err.println("PreferenceManager: There was an IOException!");
		
			currentPref = new UltranautPref((byte)100, (byte)100, (byte)100, false, true);
			System.out.println("PreferenceManager: Using default settings and writing a new settings file...");			
			writeFile();
		} catch(ClassNotFoundException cnfe) {
			System.err.println("ClassNotFoundException! That's REALLY bad");
		}
	}

	private static void writeFile() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("prefs.up");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(currentPref);
			objectOutputStream.close();
			fileOutputStream.close();
			System.out.println("PreferenceManager: Wrote Successfully!");
		} catch(IOException ioe) {
			System.err.println("PreferenceManager: There was an IOException! Woops!");
		}
	}
	
	public static void setValues(boolean fs, boolean aa) {
		currentPref.setFullScreen(fs);
		currentPref.setAntialiasing(aa);
		writeFile();
	}
	
}
