
package com.rath.osuplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MinimHandler {
  
  public MinimHandler() {}
  
  public String sketchPath(String fileName) {
    File f = new File(fileName);
    return f.getAbsolutePath();
  }
  
  public InputStream createInput(String fileName) {
    FileInputStream result = null;
    try {
      result = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
    return result;
  }
}
