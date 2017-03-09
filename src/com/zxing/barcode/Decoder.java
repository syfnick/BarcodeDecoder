package com.zxing.barcode;

import java.awt.image.BufferedImage;  
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.google.zxing.BinaryBitmap;  
import com.google.zxing.DecodeHintType;  
import com.google.zxing.LuminanceSource;  
import com.google.zxing.MultiFormatReader;  
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;  

public class Decoder {

	private static Result[] decode(BinaryBitmap bitmap) {
		Hashtable<DecodeHintType, Boolean> hints = new Hashtable<DecodeHintType, Boolean>();  
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);  
  
        Result[] results = null;
        Reader reader = new MultiFormatReader();
  
        try {  
        	MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader); 
        	results = multiReader.decodeMultiple(bitmap, hints); 
        } catch (NotFoundException e) {
            e.printStackTrace();
        }  

        return results;
	}

	public static Result[] concatResults(Result[] hybrid_results, Result[] gh_results) {
	   int hybrid_results_len = hybrid_results.length;
	   int gh_results_len = gh_results.length;

	   Result[] concatedResults = new Result[hybrid_results_len + gh_results_len];
	   System.arraycopy(hybrid_results, 0, concatedResults, 0, hybrid_results_len);
	   System.arraycopy(gh_results, 0, concatedResults, hybrid_results_len, gh_results_len);
	   
	   return concatedResults;
	}

	public static Result[] uniqueResults(Result[] concatedResults) {
		Set<String> datas = new HashSet<String>();
        List<Result> resultList = new ArrayList<Result>();

        for (Result result: concatedResults) {
        	if (datas.add(result.toString())) {
        		resultList.add(result);
        	}
        }

        Result[] unqiueResults = resultList.toArray(new Result[resultList.size()]);
        
        return unqiueResults;
	}

	public static void main(String[] args) {

		File file = new File("path/to/file");  
		  
        BufferedImage bufferedImage = null;  
  
        try {  
        	
            bufferedImage = ImageIO.read(file);  
            
        } catch (IOException e) {  
        	
            e.printStackTrace();  
            
        }  

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);  
     
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result[] hybrid_results = decode(bitmap);
		
		bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source)); 
		Result[] gh_results = decode(bitmap);

		Result[] results = null;
		if (hybrid_results != null && gh_results != null) {
			results = concatResults(hybrid_results, gh_results);
		} else if (hybrid_results == null && gh_results != null) {
			results = gh_results;
		} else if (hybrid_results != null && gh_results == null) {
			results = hybrid_results;
		} else {
			System.out.println("No code detected.");
		}
		
		results = uniqueResults(results);
  
		if (results != null) {
			for(Result element:results) {
				System.out.println(element.toString());
		    }
		}  

	}

}
